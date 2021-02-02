/*
 * Copyright (c) 2019 Jonathan Nelson <ciasaboark@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.phobotic.nodyn_app.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.journeyapps.barcodescanner.camera.CameraSettings;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.RoomDBWrapper;
import io.phobotic.nodyn_app.database.scan.ScanRecord;
import io.phobotic.nodyn_app.database.scan.ScanRecordDatabase;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class ScanInputView extends RelativeLayout {
    private static final String TAG = ScanInputView.class.getSimpleName();
    private static final long DURATION = 250;
    private static final String SCAN_TYPE = "ScanInputView";
    private static final String SCANNER_LAST_CAMERA = "scanner_last_camera";
    private static final String SCANNER_LAST_INPUT_METHOD = "scanner_last_input_method";
    private Context context;
    private OnTextInputListener listener;
    private View rootView;
    private TextView preview;
    private boolean isGhostMode;
    private InputMethod inputMethod;
    private BroadcastReceiver broadcastReceiver;


    private ScanRecordDatabase db;

    private View hardwareWrapper;
    private PulsatorLayout pulsator;
    private EditText hardwareEditText;

    private View oskWrapper;
    private ImageButton searchButton;
    private EditText oskEditText;


    private View spinnerWrapper;
    private Spinner spinner;

    private View cameraWrapper;
    private DecoratedBarcodeView scannerCanvas;
    private FloatingActionButton cameraButton;
    private FloatingActionButton torchButton;
    private boolean isTorchOn = false;


    public ScanInputView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public ScanInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public ScanInputView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public ScanInputView setListener(OnTextInputListener listener) {
        this.listener = listener;
        return this;
    }

    public void reset() {
        oskEditText.setText("");
        hardwareEditText.setText("");
        updatePreview(null);
        focusInput();
    }

    private void updatePreview(String str) {
        if (str == null || str.equals("")) {
            str = "";
        } else {
            str = ghostInputIfRequired(str);
        }

        preview.setText(str);
    }

    public void focusInput() {
        if (this.inputMethod != null) {
            switch (this.inputMethod) {
                case OSK:
                    oskEditText.requestFocus();
                    break;
                case HARDWARE:
                    hardwareEditText.requestFocus();
                    break;
            }
        }
    }

    private String ghostInputIfRequired(String input) {
        String str = input;

        if (isGhostMode) {
            char dot = '•';
            Random random = new Random(System.currentTimeMillis());
            int minSize = input.length();
            int size = random.nextInt(minSize * 3 - minSize) + minSize;
            str = StringUtils.repeat(dot, size);
        }

        return str;
    }

    private void enableGhostModeIfRequired() {
        //make sure the text entry area ghosts the text as well
        if (isGhostMode) {
            oskEditText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            oskEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            oskEditText.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_NORMAL);
            oskEditText.setTransformationMethod(null);
        }

        oskEditText.setSelection(oskEditText.getText().length());

        updatePreview(null);
    }

    private void hideAllInputs() {
        cameraWrapper.setVisibility(View.GONE);
        oskWrapper.setVisibility(View.GONE);
        hardwareWrapper.setVisibility(View.GONE);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        if (!isInEditMode()) {
            this.context = context;
            rootView = inflate(context, R.layout.view_scan_input, this);
            findViews();
            hideAllInputs();

            initButtons();
            initInput();
            initCamera();

            oskEditText.clearFocus();
            hardwareEditText.clearFocus();
            focusInput();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            isGhostMode = prefs.getBoolean(getResources().getString(R.string.pref_key_general_kiosk_echo),
                    Boolean.parseBoolean(getResources().getString(R.string.pref_default_general_kiosk_echo)));
            enableGhostModeIfRequired();

            updatePreview(null);

            db = RoomDBWrapper.getInstance(context).getScanRecordDatabase();

            initBestInputMode();

            //hold off on initializing the spinner until after we know which input mode will be used
            //+ this will allow us to select the correct default value
            initSpinner();
        }
    }

    private void initSpinner() {
        List<InputMethod> allowedInputModes = getAllowedInputModes();
        List<InputMethodItem> methodItems = new ArrayList<>();


        Collections.sort(allowedInputModes, new Comparator<InputMethod>() {
            @Override
            public int compare(InputMethod o1, InputMethod o2) {
                return ((Integer) o1.value).compareTo(o2.value);
            }
        });

        //keep track of which item in the list corrisponds with the input method we are using
        int selectedMethod = -1;

        for (int i = 0; i < allowedInputModes.size(); i++) {
            InputMethod m = allowedInputModes.get(i);
            InputMethodItem methodItem = null;
            if (m.equals(this.inputMethod)) {
                selectedMethod = i;
            }

            switch (m) {
                case HARDWARE:
                    methodItem = new InputMethodItem("Scanner", R.drawable.scanner, InputMethod.HARDWARE);
                    break;
                case OSK:
                    methodItem = new InputMethodItem("Keyboard", R.drawable.keyboard, InputMethod.OSK);
                    break;
                case CAMERA:
                    methodItem = new InputMethodItem("Camera", R.drawable.camera_front_variant, InputMethod.CAMERA);
                    break;
            }



            if (methodItem != null) {
                methodItems.add(methodItem);
            }

        }

        //if only a single input method is allowed there is no reason to show the spinner
        if (allowedInputModes.size() <= 1) {
            spinnerWrapper.setVisibility(View.GONE);
        } else {
            final InputMethodItem[] items = methodItems.toArray(new InputMethodItem[]{});

            ArrayAdapter<InputMethodItem> adapter = new ArrayAdapter<InputMethodItem>(getContext(),
                    android.R.layout.select_dialog_item,
                    android.R.id.text1,
                    items) {


                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    TextView tv = v.findViewById(android.R.id.text1);

                    tv.setText("");
                    Drawable d = getResources().getDrawable(items[position].iconRes);
                    d.setTint(getResources().getColor(R.color.white));
                    tv.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);

                    return v;
                }

                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    View v = super.getView(position, convertView, parent);
                    TextView tv = v.findViewById(android.R.id.text1);

                    tv.setText(items[position].label);
                    tv.setCompoundDrawablesWithIntrinsicBounds(items[position].iconRes, 0, 0, 0);
                    int dp8 = (int) (8 * getResources().getDisplayMetrics().density + 0.8f);
                    tv.setCompoundDrawablePadding(dp8);

                    return v;
                }
            };

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);


            if (this.inputMethod != null) {
                int index = -1;
                for (int i = 0; i < methodItems.size(); i++) {
                    InputMethodItem item = methodItems.get(i);
                    if (item.inputMethod == this.inputMethod) {
                        index = i;
                        break;
                    }
                }

                if (index != -1) spinner.setSelection(index);
            }

            spinner.setSelection(selectedMethod);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    InputMethodItem i = items[position];
                    Log.d(TAG, String.format("Selected input method %s", i.inputMethod));
                    if (i.inputMethod != ScanInputView.this.inputMethod) {
                        switchInputMethod(i.inputMethod);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    @NonNull
    private List<InputMethod> getAllowedInputModes() {
        List<InputMethod> allowedInputTypes = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean allowAllInputTypes = !prefs.getBoolean(
                getResources().getString(R.string.pref_key_general_kiosk_restrict_input_modes),
                Boolean.parseBoolean(getResources().getString(R.string.pref_default_general_kiosk_restrict_input_modes)));

        boolean allowHardwareInput = prefs.getBoolean(
                getResources().getString(R.string.pref_key_general_kiosk_input_mode_hardware),
                Boolean.parseBoolean(getResources().getString(R.string.pref_default_general_kiosk_input_mode_hardware)));
        boolean allowOSKInput = prefs.getBoolean(
                getResources().getString(R.string.pref_key_general_kiosk_input_mode_osk),
                Boolean.parseBoolean(getResources().getString(R.string.pref_default_general_kiosk_input_mode_osk)));
        boolean allowCameraInput = prefs.getBoolean(
                getResources().getString(R.string.pref_key_general_kiosk_input_mode_camera),
                Boolean.parseBoolean(getResources().getString(R.string.pref_default_general_kiosk_input_mode_camera)));

        if (allowAllInputTypes || allowHardwareInput) {
            allowedInputTypes.add(InputMethod.HARDWARE);
        }

        if (allowAllInputTypes || allowOSKInput) {
            allowedInputTypes.add(InputMethod.OSK);
        }

        if (allowAllInputTypes || allowCameraInput) {
            allowedInputTypes.add(InputMethod.CAMERA);
        }
        return allowedInputTypes;
    }

    public void initCamera() {
        CameraSettings settings = new CameraSettings();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        //use the last camera
        int camera = prefs.getInt(SCANNER_LAST_CAMERA, Camera.CameraInfo.CAMERA_FACING_FRONT);

        settings.setRequestedCameraId(camera);

        scannerCanvas.getBarcodeView().setCameraSettings(settings);
    }

    public void initBestInputMode() {
        List<InputMethod> allowedInputModes = getAllowedInputModes();

        Collections.sort(allowedInputModes, new Comparator<InputMethod>() {
            @Override
            public int compare(InputMethod o1, InputMethod o2) {
                return ((Integer) o1.value).compareTo(o2.value);
            }
        });

        if (allowedInputModes.size() == 0) {
            FirebaseCrashlytics.getInstance().log(Log.ERROR + " " + TAG + " Allowed input methods list size empty.  This should not have happened.  At least one input method should always be available");
        }

        //use the last used input method (if possible)
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int lastInput = prefs.getInt(SCANNER_LAST_INPUT_METHOD, -1);
        InputMethod lastInputMethod = null;
        if (lastInput != -1) {
            try {
                lastInputMethod = InputMethod.getInputMethodForValue(lastInput);
            } catch (IllegalArgumentException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        }

        InputMethod inputMethod;
        if (lastInputMethod != null && allowedInputModes.contains(lastInputMethod)) {
            inputMethod = lastInputMethod;
        } else {
            inputMethod = allowedInputModes.get(0);
        }

        switchInputMethod(inputMethod);
    }


    private void switchInputMethod(InputMethod inputMethod) {
        this.inputMethod = inputMethod;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putInt(SCANNER_LAST_INPUT_METHOD, inputMethod.value).apply();

        switch (inputMethod) {
            case OSK:
                switchToOSKInput();
                break;
            case HARDWARE:
                switchToHardwareInput();
                break;
            case CAMERA:
                switchToCameraInput();
                break;
        }
    }

    private void findViews() {
        preview = rootView.findViewById(R.id.preview);

        oskWrapper = rootView.findViewById(R.id.osk_wrapper);
        oskEditText = rootView.findViewById(R.id.osk_edit_text);
        searchButton = rootView.findViewById(R.id.search_button);

        hardwareWrapper = rootView.findViewById(R.id.hardware_wrapper);
        hardwareEditText = rootView.findViewById(R.id.hardware_edit_text);
        pulsator = rootView.findViewById(R.id.pulse);


        spinnerWrapper = rootView.findViewById(R.id.spinner_wrapper);
        spinner = rootView.findViewById(R.id.spinner);


        cameraWrapper = rootView.findViewById(R.id.camera_wrapper);
        scannerCanvas = rootView.findViewById(R.id.canvas);
        cameraButton = rootView.findViewById(R.id.camera_button);
        torchButton = rootView.findViewById(R.id.torch_button);
    }

    public void focus() {
        focusInput();
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            focusInput();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!isInEditMode()) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            context.registerReceiver(broadcastReceiver, filter);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void initInput() {
        hardwareEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    pulsator.start();
                } else {
                    pulsator.stop();
                }
            }
        });

        TextView.OnEditorActionListener al = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean evenHandled = false;

                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_FLAG_NO_ENTER_ACTION
                        || actionId == EditorInfo.IME_NULL) {

                    String str = v.getText().toString();
                    handleInputString(str);
                    updatePreview(str);
                    evenHandled = true;
                }

                return evenHandled;
            }
        };

        //The on screen keyboard will trigger an editor action once the search button is clicked.
        oskEditText.setOnEditorActionListener(al);
        hardwareEditText.setOnEditorActionListener(al);

        //input coming from the scanner (or hardware keyboard) will only be terminated by a newline
        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                updatePreview(str);
                if (str.length() == 1 && listener != null) {
                    listener.onTextInputBegin();
                }
                if (str.contains("\n")) {
                    handleInputString(str);
                }
            }
        };

        oskEditText.addTextChangedListener(tw);
        hardwareEditText.addTextChangedListener(tw);
    }

    private void initButtons() {
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = oskEditText.getText().toString();
                handleInputString(s);
            }
        });

        torchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraSettings settings = scannerCanvas.getBarcodeView().getCameraSettings();

                Drawable d;
                if (isTorchOn) {
                    scannerCanvas.setTorchOff();
                    d = getResources().getDrawable(R.drawable.flashlight);
                    isTorchOn = false;
                } else {
                    scannerCanvas.setTorchOn();
                    d = getResources().getDrawable(R.drawable.flashlight_off);
                    isTorchOn = true;
                }

                torchButton.setImageDrawable(d);
            }
        });

        // if the device does not have flashlight in its camera,
        // then remove the switch flashlight button...
        if (!hasFlash()) {
            torchButton.setVisibility(View.GONE);
        }

        cameraButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraSettings settings = scannerCanvas.getBarcodeView().getCameraSettings();


                if (scannerCanvas.getBarcodeView().isPreviewActive()) {
                    scannerCanvas.pause();
                }

                Drawable d;

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

                int camera;
                //swap the id of the camera to be used
                if (settings.getRequestedCameraId() == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    camera = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    d = getResources().getDrawable(R.drawable.camera_front_variant);
                } else {
                    camera = Camera.CameraInfo.CAMERA_FACING_BACK;
                    d = getResources().getDrawable(R.drawable.camera_rear_variant);
                }

                prefs.edit().putInt(SCANNER_LAST_CAMERA, camera).apply();

                settings.setRequestedCameraId(camera);

                scannerCanvas.getBarcodeView().setCameraSettings(settings);

                cameraButton.setImageDrawable(d);

                scannerCanvas.resume();
            }
        });
    }

    private boolean hasFlash() {
        return context.getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    /**
     * Rebuild the view so that the on screen keyboard is visible and is the primary input method
     */
    private void switchToOSKInput() {
        //fade out the pulsator and the preview canvas, then show the EditText
        AnimationHelper.fadeOut(getContext(), hardwareWrapper);
        AnimationHelper.fadeOut(getContext(), cameraWrapper);
        //no need to show the preview.  The EditText will already show what is being typed
        AnimationHelper.fadeOut(getContext(), preview);
        AnimationHelper.fadeIn(getContext(), oskWrapper);
        forceShowOSK();

        try {
            scannerCanvas.pauseAndWait();
        } catch (Exception e) {

        }

        oskEditText.requestFocus();
    }

    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }

        return null;
    }

//    private boolean isHardwareKeyboardAvailable() {
//        boolean hardwareKeyboardAvailable = false;
//        Configuration config = getResources().getConfiguration();
//
//        if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
//            hardwareKeyboardAvailable = true;
//        } else if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
//            hardwareKeyboardAvailable = false;
//        }
//
//        return hardwareKeyboardAvailable;
//    }

    private void switchToCameraInput() {
        forceHideOSK();
        AnimationHelper.fadeOut(getContext(), hardwareWrapper);
        AnimationHelper.fadeOut(getContext(), oskWrapper);
        //show the preview so we get some visual feedback when a barcode is scanned.
        AnimationHelper.fadeIn(getContext(), preview);
        AnimationHelper.fadeIn(getContext(), cameraWrapper);

        // TODO: 4/3/19 is there a speed penalty for attempting to decode all barcode formats?
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.values());
        scannerCanvas.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats, null, null, false));
        scannerCanvas.setStatusText("Ready to scan");
        final BeepManager beepManager = new BeepManager(getActivity());
        final long[] lastScan = {-1};
        final long delay = 3000;
        scannerCanvas.decodeContinuous(new BarcodeCallback() {

            @Override
            public void barcodeResult(BarcodeResult result) {
                //only process the scanned data if we have not read a barcode for a few seconds
                if (lastScan[0] == -1 || System.currentTimeMillis() - lastScan[0] >= delay) {
                    lastScan[0] = System.currentTimeMillis();
                    String data = result.getText();
                    Log.d(TAG, String.format("Read barcode data: <%s>", data));
                    if (listener != null) {
                        listener.onTextInputFinished(data);
                    }
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
                //nothing to do here
            }
        });
        scannerCanvas.resume();
    }

    /**
     * Rebuild the UI so that the on screen keyboard and camera input are hidden.  Input should
     * come directly from a hardware input device (keyboard, barcode scanner, etc...)
     *
     * @param transitionDrawable
     */
    private void switchToHardwareInput() {
        forceHideOSK();
        AnimationHelper.fadeOut(getContext(), oskWrapper);
        AnimationHelper.fadeOut(getContext(), cameraWrapper);
        try {
            scannerCanvas.pauseAndWait();
        } catch (Exception e) {

        }

        AnimationHelper.fadeIn(getContext(), hardwareWrapper);
        pulsator.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hardwareEditText.requestFocus();
            }
        }, 1000);
    }

    private void handleInputString(String str) {
        Log.d(TAG, "handling input string: '" + str + "'");

        //blank out the inputs
        oskEditText.setText("");
        hardwareEditText.setText("");

        str = str.replaceAll("\n", "");

        if (str.length() > 0) {
            String gstMde = String.format("ghost_mode: %b", isGhostMode);
            String forseScan = String.format("input_method: %s", inputMethod);

            db.scanRecordDao().upsertAll(new ScanRecord(SCAN_TYPE, System.currentTimeMillis(), str,
                    true, this.getClass().getSimpleName(),
                    String.format("[%s, %s]", gstMde, forseScan)));

            if (listener != null) {
                listener.onTextInputFinished(str);
            }

            //manually set the preview text again so that it sticks around
            str = ghostInputIfRequired(str);
            preview.setText(str);
        }
    }


    public enum InputMethod {
        OSK(2),
        HARDWARE(1),
        CAMERA(0);

        public int value;

        public static ScanInputView.InputMethod getInputMethodForValue(int value) {
            for (ScanInputView.InputMethod m : values()) {
                if (m.value == value) {
                    return m;
                }
            }

            throw new IllegalArgumentException(String.format("Unknown input method %d", value));
        }

        InputMethod(int value) {
            this.value = value;
        }
    }

    public interface OnTextInputListener {
        void onTextInputFinished(String text);

        void onTextInputBegin();
    }

    private void forceShowOSK() {
        Log.d(TAG, "showing OSK");
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
//        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInputFromInputMethod(getWindowToken(), 0);
//        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
    }

    private void forceHideOSK() {
        Log.d(TAG, "hiding OSK");
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    public class InputMethodItem {
        public final String label;
        public final int iconRes;
        public final ScanInputView.InputMethod inputMethod;

        public InputMethodItem(String label, @DrawableRes int iconRes, ScanInputView.InputMethod inputMethod) {
            this.label = label;
            this.iconRes = iconRes;
            this.inputMethod = inputMethod;
        }


    }


}
