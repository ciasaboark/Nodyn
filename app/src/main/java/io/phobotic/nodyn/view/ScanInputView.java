/*
 * Copyright (c) 2017 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.usb.UsbManager;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.Random;

import io.phobotic.nodyn.R;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;

public class ScanInputView extends LinearLayout {
    private static final String TAG = ScanInputView.class.getSimpleName();
    private static final long DURATION = 250;
    private Context context;
    private OnTextInputListener listener;
    private View rootView;
    private TextView preview;
    private EditText input;
    private ImageButton button;
    private boolean usingKeyboard = false;
    private BroadcastReceiver broadcastReceiver;
    private boolean ghostMode = false;
    private boolean forceScanInput = false;
    private PulsatorLayout pulsator;

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
        input.setText("");
        updatePreview();
    }

    private void updatePreview() {
        // TODO: 8/9/17 fancy animations and fading?
        String str = input.getText().toString();

        if (str == null || str.equals("")) {
            str = "Scan Input";
        } else {
            str = ghostInput(str);
        }

        preview.setText(str);
    }

    private String ghostInput(String input) {
        String str = input;

        if (ghostMode) {
            char dot = '•';
            Random random = new Random(System.currentTimeMillis());
            int minSize = input.length();
            int size = random.nextInt(minSize * 3 - minSize) + minSize;
            str = StringUtils.repeat(dot, size);
        }

        return str;
    }

    public ScanInputView setGhostMode(boolean ghostMode) {
        this.ghostMode = ghostMode;

        //make sure the text entry area ghosts the text as well
        if (ghostMode) {
            input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            input.setInputType(InputType.TYPE_TEXT_VARIATION_NORMAL);
            input.setTransformationMethod(null);
        }

        input.setSelection(input.getText().length());

        updatePreview();
        return this;
    }

    public ScanInputView setForceScanInput(boolean forceScanInput) {
        this.forceScanInput = forceScanInput;
        if (forceScanInput) {
            button.setVisibility(View.GONE);
        } else {
            button.setVisibility(View.VISIBLE);
        }
        return this;
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        if (!isInEditMode()) {
            this.context = context;
            rootView = inflate(context, R.layout.view_scan_input, this);

            preview = (TextView) rootView.findViewById(R.id.preview);
            button = (ImageButton) rootView.findViewById(R.id.image);
            input = (EditText) rootView.findViewById(R.id.edit_text);

            pulsator = (PulsatorLayout) findViewById(R.id.pulse);
            pulsator.start();


            initButton();
            initInput();
            initReceiver();

            input.clearFocus();
            input.requestFocus();

            updatePreview();

//            //default to the hidden input if a hardware keyboard is present
//            if (isHardwareKeyboardAvailable()) {
//                useScanner(false);
//            } else {
//                useKeyboard(false);
//            }

            useScanner(false);
        }
    }

    private void initReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "received broadcast intent");
                Toast.makeText(getContext(), "Input received broadcast intent", Toast.LENGTH_SHORT).show();

                //if scan mode has been forced then we need to skip showing the OSK when a keyboard
                //+ or scanner is detatched
                if (!forceScanInput) {
                    if (isHardwareKeyboardAvailable()) {
                        Toast.makeText(context, "hardware keyboard available", Toast.LENGTH_SHORT).show();
                        useScanner(true);
                    } else {
                        Toast.makeText(context, "hardware keyboard not available", Toast.LENGTH_SHORT).show();
                        useKeyboard(true);
                    }
                }
            }
        };
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
        context.unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            input.requestFocus();
        }
    }

    private void initInput() {
        //The on screen keyboard will trigger an editor action once the search button is clicked.
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean evenHandled = false;

                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_FLAG_NO_ENTER_ACTION
                        || actionId == EditorInfo.IME_NULL) {

                    String str = v.getText().toString();
                    handleInputString(str);
                    updatePreview();
                    evenHandled = true;
                }

                return evenHandled;
            }
        });

        //input coming from the scanner (or hardware keyboard) will only be terminated by a newline
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                updatePreview();
                String str = s.toString();
                if (str.contains("\n")) {
                    handleInputString(str);
                }
            }
        });
    }

    private void initButton() {
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //if we were using the OSK then collapse the input
                if (usingKeyboard) {
                    useScanner(true);
                } else {
                    useKeyboard(true);
                }

                //swap the focus
                input.clearFocus();
                button.requestFocus();
                input.requestFocus();

            }
        });
    }

    private boolean isHardwareKeyboardAvailable() {
        boolean hardwareKeyboardAvailable = false;
        Configuration config = getResources().getConfiguration();

        if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            hardwareKeyboardAvailable = true;
        } else if (config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            hardwareKeyboardAvailable = false;
        }

        return hardwareKeyboardAvailable;
    }

    private void useKeyboard(boolean transitionDrawable) {
        if (usingKeyboard) {
            return;
        }

        Animation fadeOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                pulsator.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        pulsator.startAnimation(fadeOut);

        int curWidth = input.getLayoutParams().width;
        int finalWidth = 300;
        ObjectAnimator oa = ObjectAnimator.ofInt(input, "width", curWidth, finalWidth);
        oa.setDuration(DURATION);
        oa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int width = (int) animation.getAnimatedValue();
                input.getLayoutParams().width = width;
            }
        });
        oa.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                forceShowOSK();
                usingKeyboard = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        oa.start();

        final ObjectAnimator previewAnimator = new ObjectAnimator().ofFloat(preview, "alpha", 1f, 0f);
        previewAnimator.setDuration(DURATION);
        previewAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                preview.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        previewAnimator.start();

        if (transitionDrawable) {
            Drawable d = button.getDrawable().getCurrent();
            if (d instanceof TransitionDrawable) {
                ((TransitionDrawable) d).setCrossFadeEnabled(true);
                ((TransitionDrawable) d).startTransition((int) DURATION);
            }
        }
    }

    private void useScanner(boolean transitionDrawable) {
        if (!usingKeyboard) {
            return;
        }

        Animation fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                pulsator.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        pulsator.startAnimation(fadeIn);

        int curWidth = input.getLayoutParams().width;
        int finalWidth = 0;
        ObjectAnimator oa = ObjectAnimator.ofInt(input, "width", curWidth, finalWidth);
        oa.setDuration(DURATION);
        oa.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int width = (int) animation.getAnimatedValue();
                input.getLayoutParams().width = width;
            }
        });
        oa.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                forceHideOSK();
                usingKeyboard = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        oa.start();

        final ObjectAnimator previewAnimator = new ObjectAnimator().ofFloat(preview, "alpha", 0f, 1f);
        previewAnimator.setDuration(DURATION);
        previewAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                preview.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        previewAnimator.start();

        if (transitionDrawable) {
            Drawable d = button.getDrawable().getCurrent();
            if (d instanceof TransitionDrawable) {
                ((TransitionDrawable) d).setCrossFadeEnabled(true);
                ((TransitionDrawable) d).reverseTransition(250);
            }
        }
    }

    private void handleInputString(String str) {
        Log.d(TAG, "handling input string: '" + input + "'");

        if (input != null) {
            input.setText("");

            str = str.replaceAll("\n", "");

            if (str.length() > 0) {
                if (listener != null) {
                    listener.onTextInput(str);
                }

                //manually set the preview text again so that it sticks around
                str = ghostInput(str);
                preview.setText(str);

            }
        }
    }

    private void forceShowOSK() {
        Log.d(TAG, "showing OSK");
        input.requestFocus();

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

    public interface OnTextInputListener {
        void onTextInput(String text);
    }


}
