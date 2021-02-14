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

package io.phobotic.nodyn_app.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.zxing.client.android.Intents;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.ToDoubleBiFunction;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.animation.ProgressBarAnimation;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.RoomDBWrapper;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.exception.CategoryNotFoundException;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.exception.StatusNotFoundException;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Category;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.database.scan.ScanRecord;
import io.phobotic.nodyn_app.database.scan.ScanRecordDatabase;
import io.phobotic.nodyn_app.database.sync.Action;
import io.phobotic.nodyn_app.fragment.listener.CheckInOutListener;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.helper.AssetHelper;
import io.phobotic.nodyn_app.helper.MediaHelper;
import io.phobotic.nodyn_app.reporting.CustomEvents;
import io.phobotic.nodyn_app.service.SyncService;
import io.phobotic.nodyn_app.sync.SyncManager;
import io.phobotic.nodyn_app.sync.adapter.SyncAdapter;
import io.phobotic.nodyn_app.view.AssetScannerView;
import io.phobotic.nodyn_app.view.BadgeScanView;
import io.phobotic.nodyn_app.view.ScannedAssetView;
import io.phobotic.nodyn_app.view.VerifyCheckOutView;
import us.feras.mdv.MarkdownView;

import static io.phobotic.nodyn_app.helper.MediaHelper.playSoundEffect;

public class CheckOutFragment extends Fragment {
    private static final String TAG = CheckOutFragment.class.getSimpleName();
    private static final String ARG_AUTHORIZATION = "arg_authorization";
    private static final int MAX_PROGRESS = 1000;
    private static final String SCAN_TYPE = "ASSET_CHECKOUT";
    Database db;
    private CheckInOutListener listener;
    private View rootView;
    private AssetScannerView scanner;
    private User authorizingUser;
    private ExtendedFloatingActionButton checkoutButton;
    private View mainBox;
    private boolean isSoundEffectsEnabled;
    private CountDownTimer timer;
    private View warning;
    private ProgressBar warningProgress;
    private TextView warningMessage;
    private int usersCheckedOut = 0;
    private ScanRecordDatabase scanLogDb;
    private Timer scannerTimer;
    @ColorInt
    private int errorTextColor;

    public static CheckOutFragment newInstance(User authorizingUser) {
        CheckOutFragment fragment = new CheckOutFragment();
        Bundle args = new Bundle();
        if (authorizingUser != null) {
            args.putSerializable(ARG_AUTHORIZATION, authorizingUser);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public CheckOutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Asset Check Out");
        }
        usersCheckedOut = 0;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = Database.getInstance(getContext());
        scanLogDb = RoomDBWrapper.getInstance(getContext()).getScanRecordDatabase();

        if (getArguments() != null) {
            authorizingUser = (User) getArguments().getSerializable(ARG_AUTHORIZATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_check_out, container, false);
        init();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (scanner != null) {
            scanner.focus();
        }

        //ugly hack.  Use a timer to move the input focus back to the scanner on a regular
        //+ basis.
        this.scannerTimer = new Timer();
        scannerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //must run on the UI thread
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (scanner != null) {
                            Log.d(TAG, "moving input back to scanner");
                            scanner.focus();
                        }
                    }
                });
            }
        }, 0, 1000);

    }



    @Override
    public void onStop() {
        super.onStop();
        if (this.scannerTimer != null) {
            this.scannerTimer.cancel();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        Bundle bundle = new Bundle();
        bundle.putInt(CustomEvents.CHECKOUT_COUNTS_FOR_SESSION, usersCheckedOut);
        FirebaseAnalytics.getInstance(getContext()).logEvent(CustomEvents.CHECKOUT_SESSION_COMPLETE, bundle);
        if (timer != null) {
            timer.cancel();
        }
    }

    private void init() {
        mainBox = rootView.findViewById(R.id.checkout);
        warning = rootView.findViewById(R.id.warning);
        warningProgress = rootView.findViewById(R.id.warning_progress);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        errorTextColor = typedValue.data;

        initScanner();
        initCheckoutFab();
        initCountDownTimer();
    }

    public void setListener(@Nullable CheckInOutListener listener) {
        this.listener = listener;
    }

    private void initScanner() {
        scanner = rootView.findViewById(R.id.scan_list);
        scanner.setAssetsRemovable(true);
        scanner.setCheckAssetAvailability(true); // TODO: 2/3/2021 this should be removed after live checkout has been tested
        scanner.setListener(new AssetScannerView.OnAssetScannedListener() {
            @Override
            public void onAssetScanned(Asset asset) {
                processAssetScan(asset);
            }

            @Override
            public void onAssetScanListChanged(@NotNull List<AssetScannerView.ScannedAsset> assets) {
                if (!isCheckoutAvailable(assets)) {
                    hideCheckoutButton();
                    resetCountdown();
                } else {
                    showCheckoutButton();
                    resetCountdown();
                }
            }

            @Override
            public void onScanError(String message) {
                showNotification(message);
            }
        });
    }

    /**
     * Returns true only if no assets currently have a check in progress and at least one asset
     * is either available or unknown
     * @param assets
     * @return
     */
    private boolean isCheckoutAvailable(List<AssetScannerView.ScannedAsset> assets) {
        boolean atLeastOneAssetAvailable = false;
        boolean isCheckInProgress = false;

        for (AssetScannerView.ScannedAsset a: assets) {
            if (a.getAvailability() == AssetScannerView.AVAILABILITY.AVAILABLE ||
                    a.getAvailability() == AssetScannerView.AVAILABILITY.UNKNOWN) {
                atLeastOneAssetAvailable = true;
            } else if (a.getAvailability() == AssetScannerView.AVAILABILITY.CHECKING) {
                isCheckInProgress = true;
            }
        }

        return atLeastOneAssetAvailable && !isCheckInProgress;
    }

    private void initCheckoutFab() {
        checkoutButton = rootView.findViewById(R.id.checkout_button);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmAssetsAvailableThenCheckout();
            }
        });
//        checkoutButton.setEnabled(false);
        checkoutButton.hide();
    }

    private void updateDurationText(TextView tv, int duration) {
        String text;
        if (duration == 0) {
            text = "indefinitely";
        } else {
            text = String.format("%d %s", duration, (duration == 1 ? "day" : "days"));
        }

        tv.setText(text);
    }

    public class RepeatListener implements View.OnTouchListener {

        private Handler handler = new Handler();

        private int initialInterval;
        private final int normalInterval;
        private final View.OnClickListener clickListener;
        private View touchedView;

        private Runnable handlerRunnable = new Runnable() {
            @Override
            public void run() {
                if(touchedView.isEnabled()) {
                    handler.postDelayed(this, normalInterval);
                    clickListener.onClick(touchedView);
                } else {
                    // if the view was disabled by the clickListener, remove the callback
                    handler.removeCallbacks(handlerRunnable);
                    touchedView.setPressed(false);
                    touchedView = null;
                }
            }
        };

        /**
         * @param initialInterval The interval after first click event
         * @param normalInterval The interval after second and subsequent click
         *       events
         * @param clickListener The OnClickListener, that will be called
         *       periodically
         */
        public RepeatListener(int initialInterval, int normalInterval,
                              View.OnClickListener clickListener) {
            if (clickListener == null)
                throw new IllegalArgumentException("null runnable");
            if (initialInterval < 0 || normalInterval < 0)
                throw new IllegalArgumentException("negative interval");

            this.initialInterval = initialInterval;
            this.normalInterval = normalInterval;
            this.clickListener = clickListener;
        }

        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    handler.removeCallbacks(handlerRunnable);
                    handler.postDelayed(handlerRunnable, initialInterval);
                    touchedView = view;
                    touchedView.setPressed(true);
                    clickListener.onClick(view);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    handler.removeCallbacks(handlerRunnable);
                    touchedView.setPressed(false);
                    touchedView = null;
                    return true;
            }

            return false;
        }

    }

    private void showCheckoutBadgeScanDialog(final @NonNull List<AssetScannerView.ScannedAsset> assetsToCheckout) {
        final View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_checkout_user_scan, null);

        //is the user allowed to change the duration of the checkout?
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        final boolean allowDurationOverride = prefs.getBoolean(getString(R.string.pref_key_check_out_allow_duration_override),
                Boolean.parseBoolean(getString(R.string.pref_default_check_out_allow_duration_override)));

        final TextView durationOverrideEditText = dialogView.findViewById(R.id.duration_value);
        //set the duration to the currently selected value
        final int minDuration = 0, maxDuration = 30;

        int c = Integer.parseInt(prefs.getString(getString(R.string.pref_key_check_out_duration),
                getString(R.string.pref_default_check_out_duration)));
        final Integer[] checkoutDuration = {c};

        updateDurationText(durationOverrideEditText, checkoutDuration[0]);

        if (allowDurationOverride) {
            //wire up the buttons;
            MaterialButton minusButton = dialogView.findViewById(R.id.button_less);
            minusButton.setOnTouchListener(new RepeatListener(500, 100, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkoutDuration[0] > minDuration) {
                        checkoutDuration[0]--;
                        updateDurationText(durationOverrideEditText, checkoutDuration[0]);
                    }
                }
            }));

            MaterialButton plusButton = dialogView.findViewById(R.id.button_more);
            plusButton.setOnTouchListener(new RepeatListener(500, 100, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checkoutDuration[0] < maxDuration) {
                        checkoutDuration[0]++;
                        updateDurationText(durationOverrideEditText, checkoutDuration[0]);
                    }
                }
            }));
        } else {
            dialogView.findViewById(R.id.number_picker_wrapper).setVisibility(View.GONE);
        }

        final BadgeScanView badgeScanView = dialogView.findViewById(R.id.badge_scanner);
        final TextView error = dialogView.findViewById(R.id.error);
        error.setTextColor(errorTextColor);
        String title;
        final String type;
        if (assetsToCheckout.size() > 1) {
            type = "assets";
        } else {
            type = "asset";
        }
        final AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                .setTitle(String.format("Checkout %s", type))
                .setView(dialogView)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing to do here, dialog will dismiss
                    }
                })
                .create();

        badgeScanView.setOnUserScannedListener(new BadgeScanView.OnUserScannedListener() {
            @Override
            public void onUserScanned(@NotNull User user) {
                d.dismiss();
                if (allowDurationOverride) {
                    processUserScan(user, checkoutDuration[0], assetsToCheckout);
                } else {
                    processUserScan(user, null, assetsToCheckout);
                }
                AnimationHelper.collapseAndFadeOut(getContext(), error);
            }

            @Override
            public void onUserScanError(@NotNull String message) {
                //let the badge scanner handle bad input

                badgeScanView.reset();
                String errorText = String.format("Unable to checkout %s to associate.  %s.\n" +
                        "Please contact your equipment manager for assistance.", type, message);
                error.setText(errorText);
                AnimationHelper.expandAndFadeIn(getContext(), error);
                MediaHelper.playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
            }

            @Override
            public void onInputBegin() {
                AnimationHelper.collapseAndFadeOut(getContext(), error);
            }
        });

        d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        pauseCountdown();

        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //resume the countdown
                resetCountdown();
            }
        });

        d.show();
    }


    private void initCountDownTimer() {
        warning.setVisibility(View.GONE);
        warningProgress.setMax(MAX_PROGRESS);
        warningProgress.setProgress(MAX_PROGRESS);
        warningMessage = rootView.findViewById(R.id.warning_text_1);

        resetCountdown();
    }

    private void processAssetScan(Asset asset) {
        resetCountdown();
        AssetHelper helper = new AssetHelper();
        if (!helper.modelCanBeCheckedOut(getContext(), asset.getModelID())) {
            String modelName = "";
            try {
                modelName = " '" + db.findModelByID(asset.getModelID()).getName() + "'";
            } catch (ModelNotFoundException e) {
            }

            View v = LayoutInflater.from(getContext()).inflate(R.layout.view_model_unavailable, null);
            recordBadScan(asset, "model not available");
            AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                    .setTitle("Model not available")
                    .setView(v)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing here
                        }
                    }).create();

            playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
            d.show();
        } else if (!helper.isAssetStatusValid(getContext(), asset)) {
            String statusString = "";
            try {
                Status status = db.findStatusByID(asset.getStatusID());
                statusString = " Current asset status is '" +
                        status.getName() + "'";
            } catch (StatusNotFoundException e) {
            }

            recordBadScan(asset, "asset not available");
            Set<String> allowedStatusIDs = getAllowedStatusNames();
            AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                    .setTitle("Asset not available")
                    .setMessage("Asset '" + asset.getTag() + "' can not be checked out.  " +
                            "Asset status must be one of " + allowedStatusIDs.toString() +
                            "." + statusString)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing here
                        }
                    }).create();

            playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
            d.show();
        } else {
            try {
                tryAddAssetToScannedList(asset);
                recordGoodScan(asset);
            } catch (AssetAlreadyScannedException e) {
                recordBadScan(asset, "asset has already been scanned to checkout list");
                AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                        .setTitle("Asset Not Available")
                        .setMessage("Asset '" + asset.getTag() + "' has already been scanned")
                        .setPositiveButton(getResources().getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //nothing to do here
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (scanner != null) {
                                                    scanner.focus();
                                                }
                                            }
                                        }, 100);
                                    }
                                }).create();
                playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
                d.show();

            }
        }
    }

    private void resetCountdown() {
        if (timer != null) {
            timer.cancel();
        }

        warningProgress.setProgress(MAX_PROGRESS);
        AnimationHelper.collapse(warning);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String timeLimitString = prefs.getString(
                getString(R.string.pref_key_check_out_timeout_limit),
                getString(R.string.pref_default_check_out_timeout_limit));
        int minutes = 0;
        try {
            minutes = Integer.parseInt(timeLimitString);
        } catch (NumberFormatException e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        if (minutes > 0 && authorizingUser != null) {
            final int timeout = minutes * 1000 * 60;

            //show the warning when we at 50% of the alloted time (with a minimum 50 second warning)
            final long showWarningAt = Math.max(50000, (long) (timeout * .5));

            timer = new CountDownTimer(timeout, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    //                Log.d(TAG, "tick");
                    if (millisUntilFinished <= showWarningAt) {
                        //animate in the warning if it is not already visible
                        AnimationHelper.expand(warning);

                        //update the progressbar
                        float percentPerMs = (float) MAX_PROGRESS / (float) timeout;
                        float progress = ((float) millisUntilFinished * percentPerMs);
                        int curProgress = warningProgress.getProgress();
                        ProgressBarAnimation anim = new ProgressBarAnimation(warningProgress, curProgress, progress);
                        anim.setDuration(1000);
                        warningProgress.startAnimation(anim);

                        String warningText = getString(R.string.check_out_warning_message_1);
                        int remainingSeconds = Math.round(millisUntilFinished / 1000);

                        int value;
                        String measurement;

                        if (remainingSeconds < 60) {
                            value = remainingSeconds;
                            if (value == 1) {
                                measurement = getString(R.string.second);
                            } else {
                                measurement = getString(R.string.seconds);
                            }
                        } else {
                            value = remainingSeconds / 60;

                            if (value == 1) {
                                measurement = getString(R.string.minute);
                            } else {
                                measurement = getString(R.string.minutes);
                            }
                        }

                        warningText = String.format(warningText, String.valueOf(value), measurement);
                        warningMessage.setText(warningText);
                    }
                }

                @Override
                public void onFinish() {
                    Activity a = getActivity();
                    if (a != null) {
                        a.finish();
                    }
                }
            };

            timer.start();
        }
    }

    /**
     * Process a successful scan of a user attempting to check out the scanned assets.
     * @param user
     * @param requestedDays
     */
    private void processUserScan(User user, @Nullable Integer requestedDays,
                                 @NonNull List<AssetScannerView.ScannedAsset> assetsToCheckout) {
        resetCountdown();

        //if at least one asset has been scanned, then checkout the items to the associate
        if (assetsToCheckout.isEmpty()) {
            AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                    .setTitle("No assets scanned")
                    .setMessage("Unable to checkout " + user.getName() + ", no assets have been scanned")
                    .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //nothing to do here
                        }
                    })
                    .create();
            d.show();
            playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
        } else {
            confirmCheckouts(user, requestedDays, assetsToCheckout);
        }
    }

    /**
     * Show a warning dialog if any of the scanned assets could not be verified as available
     * @param user
     */
    private void confirmAssetsAvailableThenCheckout() {
        resetCountdown();

        List<AssetScannerView.ScannedAsset> scannedAssets = scanner.getScannedAssets();
        List<Asset> availableAssets = new ArrayList<>();
        List<Asset> unverifiedAssets = new ArrayList<>();
        List<Asset> unavailableAssets = new ArrayList<>();

        final List<AssetScannerView.ScannedAsset> assetsToCheckout = new ArrayList<>();

        for (AssetScannerView.ScannedAsset sa: scannedAssets) {
            if (sa.getAvailability() == AssetScannerView.AVAILABILITY.AVAILABLE) {
                availableAssets.add(sa.getAsset());
                assetsToCheckout.add(sa);
            } else if (sa.getAvailability() == AssetScannerView.AVAILABILITY.NOT_AVAILABLE) {
                unavailableAssets.add(sa.getAsset());
            } else if (sa.getAvailability() == AssetScannerView.AVAILABILITY.UNKNOWN) {
                unverifiedAssets.add(sa.getAsset());
                assetsToCheckout.add(sa);
            }
        }

        //show a warning dialog if required, otherwise move on to the check out
        if (unverifiedAssets.size() > 0 || unavailableAssets.size() > 0) {

            String unavailableMessage = toStringList(unavailableAssets);
            String unverifiedMessage = toStringList(unverifiedAssets);

            StringBuilder sb = new StringBuilder();
            if (unavailableMessage != null) {
                sb.append(String.format("Some scanned assets were confirmed unavailable. These assets will not be included in the checked out::\n%s",
                        unavailableMessage));
            }

            if (unverifiedMessage != null) {
                if (unavailableMessage != null) sb.append("\n\n");
                sb.append(String.format("I could not verify availability of some assets. If you continue I will still attempt to check these out:\n%s", unverifiedMessage));
            }

            AlertDialog d = new MaterialAlertDialogBuilder(getContext())
                    .setTitle("Asset status warning")
                    .setMessage(sb.toString())
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetCountdown();
                            //just drop back to showing the existing list of scanned assets
                        }
                    })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetCountdown();
                            showCheckoutBadgeScanDialog(assetsToCheckout);
                        }
                    })
                    .create();
            d.show();
        } else {
            showCheckoutBadgeScanDialog(assetsToCheckout);
        }
    }

    private String toStringList(List<Asset> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        String message = null;
        StringBuilder sb = new StringBuilder();
        for (Asset a: list) {
            sb.append(String.format("  • %s\n", a.getTag()));
        }
        message = sb.toString();

        return message;
    }

    private void pauseCountdown() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private void recordBadScan(Asset asset, String reason) {
        recordScan(asset, reason, false);
    }

    private void recordGoodScan(Asset asset) {
        recordScan(asset, "", true);
    }

    private void recordScan(Asset asset, String reason, boolean isAccepted) {
        String prefix;
        if (authorizingUser == null) {
            prefix = "[no authorizing user] ";
        } else {
            prefix = String.format("[Autorization: %s] ", authorizingUser.toString());
        }

        scanLogDb.scanRecordDao().upsertAll(new ScanRecord(SCAN_TYPE, System.currentTimeMillis(),
                asset.getTag(), isAccepted, this.getClass().getSimpleName(), prefix + reason));
    }



    private void showNotification(String err) {
        Snackbar snackbar = Snackbar.make(rootView, err, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private Set<String> getAllowedStatusNames() {
        AssetHelper helper = new AssetHelper();
        Set<String> allowedStatusIDs = helper.getAllowedStatusIDs(getContext());
        Set<String> allowedStatusNames = new HashSet<>();

        Database db = Database.getInstance(getContext());
        for (String s : allowedStatusIDs) {
            try {
                Integer i = Integer.parseInt(s);
                Status status = db.findStatusByID(i);
                allowedStatusNames.add(status.getName());
            } catch (NumberFormatException | StatusNotFoundException e) {
                //just skip these for now
            }
        }

        return allowedStatusNames;
    }


    /**
     * Shows the EULA confirmation to the user (if enabled).  Control flows to the live checkout
     * process if the eula is disabled or if all eulas are accepted
     * @param user
     * @param requestedDays
     */
    private void confirmCheckouts(@NotNull final User user, @Nullable final Integer requestedDays,
                                  @NonNull List<AssetScannerView.ScannedAsset> assetsToCheckout) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String eulaType = prefs.getString(getString(R.string.pref_key_check_out_eula_type),
                getString(R.string.pref_default_check_out_eula_type));

        if ("none".equals(eulaType)) {
            //eulas have been disabled in the app settings.  Jump straight to the checkout process
            liveCheckoutAssetsToUser(user, false, requestedDays, assetsToCheckout);
        } else {
            showEulasThenCheckout(user, requestedDays, eulaType, assetsToCheckout);
        }
    }

    /**
     * Groups the distinct EULA texts by which assets and categories will be covered
     */
    class EulaGroups {
        private List<GroupedEula> groupedEulas = new ArrayList<>();

        public void add(String eula, int categoryID, String categoryName, Asset asset) {
            //do we already have a group with a matching eula?
            boolean isAddNeeded = true;

            for (GroupedEula g: groupedEulas) {
                if (g.getEula() != null && g.getEula().equals(eula)) {
                    g.add(categoryID, categoryName, asset);
                    isAddNeeded = false;
                    break;
                }
            }

            if (isAddNeeded) {
                GroupedEula groupedEula = new GroupedEula(eula, categoryID, categoryName, asset);
                groupedEulas.add(groupedEula);
            }
        }

        public void add(GroupedEula groupedEula) {
            groupedEulas.add(groupedEula);
        }

        public List<GroupedEula> getGroupedEulas() {
            return groupedEulas;
        }
    }

    class GroupedEula {
        Set<String> categoryNames = new HashSet<>();
        Set<Integer> categoryIds = new HashSet<>();
        Set<Asset> assets = new HashSet<>();
        String eula;

        public GroupedEula(String eula, int categoryID, String categoryName, Asset asset) {
            this.eula = eula;
            add(categoryID, categoryName, asset);
        }

        public void add(int categoryID, String categoryName, Asset asset) {
            categoryIds.add(categoryID);
            categoryNames.add(categoryName);
            assets.add(asset);
        }

        public Set<String> getCategoryNames() {
            return categoryNames;
        }

        public Set<Integer> getCategoryIds() {
            return categoryIds;
        }

        public Set<Asset> getAssets() {
            return assets;
        }

        public String getEula() {
            return eula;
        }

        public String getHeaderText() {
            StringBuilder sb = new StringBuilder();
            String catPlural = categoryNames.size() > 1 ? "categories " : "category ";
            sb.append(String.format("<p>Please read the following agreement carefully. This agreement covers the asset %s", catPlural));

            String prefix = "";
            for (String catName : categoryNames) {
                sb.append(String.format("%s<b><u>%s</u></b>", prefix, catName));
                prefix = ", ";
            }
            sb.append(".</p>");

            catPlural = categoryNames.size() > 1 ? "These categories include" : "This category includes";
            sb.append(String.format("%s the following asset tags: ", catPlural));
            prefix = "";
            for (Asset a : assets) {
                sb.append(String.format("%s•<b>%s</b>", prefix, a.getTag()));
                prefix = " ";
            }
            sb.append(".");

            return sb.toString();
        }
    }

    /**
     * Builds and returns a set of grouped eulas.
     * Category based eulas are grouped together if the eula text is identical
     * Will return only a single eula group if only the default eula should be displayed
     * @param eulaType
     * @return
     */
    private EulaGroups buildEulaGroups(String eulaType, @NonNull List<AssetScannerView.ScannedAsset> assetsToCheckout) {
        EulaGroups eulaGroups = new EulaGroups();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String defaultEula = prefs.getString(getString(R.string.pref_key_check_out_eula),
                getString(R.string.pref_default_check_out_eula));

        if ("default".equals(eulaType)) {
            //we only need to display the default eula.
            List<AssetScannerView.ScannedAsset> scannedAssets = assetsToCheckout;
            for (AssetScannerView.ScannedAsset sa: scannedAssets) {
                Asset a = sa.getAsset();
                if (defaultEula != null && defaultEula.length() > 0) {
                    eulaGroups.add(defaultEula, -1, "All Assets", a);
                }
            }
        } else {
            //try to build some header text showing what models this agreement covers
            List<Asset> defaultEulaAssets = new ArrayList<>();
            for (AssetScannerView.ScannedAsset scannedAsset: assetsToCheckout) {
                Asset a = scannedAsset.getAsset();
                int catID = a.getCategoryID();
                String categoryName = "unknown";
                String eulaText = null;
                try {
                    Category cat = db.findCategoryByID(catID);
                    categoryName = cat.getName();
                    eulaText = cat.getEulaText();
                } catch (CategoryNotFoundException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                if (eulaText == null) {
                    //for some reason the eula text was empty.  Add this asset to the list of assets
                    //+ that will use the default eula (if enabled)
                    defaultEulaAssets.add(a);
                } else {
                    eulaGroups.add(eulaText, catID, categoryName, a);
                }
            }

            //add an eula group to use the default eula for any assets that did not have a category
            //+ based eula
            for (Asset a: defaultEulaAssets) {
                if (defaultEula != null && defaultEula.length() > 0) {
                    eulaGroups.add(defaultEula, -1, "All other assets", a);
                }
            }
        }

        return eulaGroups;
    }

    /**
     * Shows whatever eulas a required, then proceeds with the live checkout process if all eulas
     * are agreed with
     * @param user
     * @param requestedDays
     * @param eulaType
     */
    private void showEulasThenCheckout(final User user, final Integer requestedDays, String eulaType,
                                       @NonNull final List<AssetScannerView.ScannedAsset> assetsToCheckout) {
        final EulaGroups eulaGroups = buildEulaGroups(eulaType, assetsToCheckout);

        if (eulaGroups.getGroupedEulas().isEmpty()) {
            Log.d(TAG, String.format("EULA text has been enabled (type '%s'), but no EULAs exist", eulaType));
            liveCheckoutAssetsToUser(user, false, requestedDays, assetsToCheckout);
        } else {

            final int[] curIndex = {0};

            final VerifyCheckOutView verifyCheckOutView = new VerifyCheckOutView(user,
                    getContext(), null,
                    eulaGroups.getGroupedEulas().get(0).getEula(),
                    eulaGroups.getGroupedEulas().get(0).getHeaderText());

            //change the positive button text to reflect wither another eula will be shown
            int nextButtonStrId;
            if (eulaGroups.getGroupedEulas().size() > 1) {
                nextButtonStrId = R.string.i_agree_next;
            } else {
                nextButtonStrId = R.string.i_agree;
            }

            boolean multiple = assetsToCheckout.size() > 1;
            final AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                    .setTitle(String.format("%s Checkout", (multiple ? "Assets" : "Asset")))
                    .setView(verifyCheckOutView)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //nothing to do here
                            if (scanner != null) {
                                scanner.focus();
                            }
                        }
                    })
                    .setPositiveButton(nextButtonStrId, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //will be overridden in the onShow below
                        }
                    })
                    .create();

            d.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    //force the dialog to take up a lot of space.  This is a pain, but the webview
                    //+ underlying the markdownview does not handle a wrap_content height
                    //+ and may render with 0dp height
                    d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);

                    //override the positive button onclick listener so we can prevent the dialog
                    //+ from closing if there are more eulas to display
                    d.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //show the next view in line
                            curIndex[0]++;
                            String nextEula = null;
                            String nextHeaderMsg = null;
                            if (curIndex[0] > eulaGroups.getGroupedEulas().size() - 1) {
                                //the 'i agree' button has been clicked on the last eula
                                liveCheckoutAssetsToUser(user, true, requestedDays, assetsToCheckout);
                                d.dismiss();
                            } else if (curIndex[0] == eulaGroups.getGroupedEulas().size() - 1) {
                                //the eula we are about to display will be the last, change the button text
                                d.getButton(DialogInterface.BUTTON_POSITIVE)
                                        .setText(R.string.i_agree);
                            }

                            if (curIndex[0] < eulaGroups.getGroupedEulas().size()) {
                                GroupedEula g = eulaGroups.getGroupedEulas().get(curIndex[0]);
                                nextHeaderMsg = g.getHeaderText();
                                nextEula = g.getEula();
                                verifyCheckOutView.setEulaText(nextEula);
                                verifyCheckOutView.setHeaderText(nextHeaderMsg);
                            }
                        }
                    });
                }
            });
            d.show();
        }
    }

    /**
     * Try to add the asset to the asset scanner view.  Will throw an AssetAlreadyScannedException
     * if the asset is already in the list
     * @param asset
     * @throws AssetAlreadyScannedException
     */
    private void tryAddAssetToScannedList(Asset asset) throws AssetAlreadyScannedException {
        //if the asset is already in the list we can not add it again
        List<AssetScannerView.ScannedAsset> scannedAssets = scanner.getScannedAssets();
        for (AssetScannerView.ScannedAsset scannedAsset: scannedAssets) {
            if (scannedAsset.getAsset().equals(asset)) {
                throw new AssetAlreadyScannedException("Asset " + asset.getTag() + " already scanned");
            }
        }

        //add the asset to the scan list (even if the local DB shows it is checked out).  The scan list
        //+ will double check the asset availability
        scanner.addAsset(asset);
    }

    private void showCheckoutButton() {
        checkoutButton.show();
    }

    private void hideCheckoutButton() {
        checkoutButton.hide();
    }

    /**
     * Immediately checkout each asset in the scanned asset list to the given user.  If the sync
     * adapter indicates the asset can not be checked out an error should be shown
     * @param user
     * @param isVerified
     */
    private void liveCheckoutAssetsToUser(final User user, final boolean isVerified,
                                          @Nullable final Integer requestedDays,
                                          @NotNull final List<AssetScannerView.ScannedAsset> assetsToCheckout) {
        final long expectedCheckin = getExpectedCheckin(requestedDays);

        //map the scanned assets (by tag) to the underlying view in the dialog list
        final Map<String, ScannedAssetView> viewMap = new HashMap();
        View v = getLayoutInflater().inflate(R.layout.view_live_checkout, null);

        //a textview to hold any error message that needs to be displayed
        final TextView messageTv = v.findViewById(R.id.message);
        messageTv.setText("Hold on.  Checking assets out now...");

        LinearLayout list = (LinearLayout) v.findViewById(R.id.list);

        for (AssetScannerView.ScannedAsset a: assetsToCheckout) {
            ScannedAssetView sav = new ScannedAssetView(getContext(), null, a);
            if (a.getAvailability() == AssetScannerView.AVAILABILITY.NOT_AVAILABLE) {
                //skip assets we confirmed were not available
            } else {
                viewMap.put(a.getAsset().getTag(), sav);
                sav.setAssetRemovable(false);
                sav.showProgress();
                list.addView(sav);
            }
        }

        final AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                .setTitle("Checking out assets")
                .setView(v)
                .setPositiveButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        d.setCancelable(false);


        //disable the close button until all assets have been processed
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                ((AlertDialog) d).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (scanner != null) {
                    scanner.focus();
                }
            }
        });

        d.show();

        Window window = d.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        final SyncAdapter syncAdapter = SyncManager.getPrefferedSyncAdapter(getContext());
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }

                //keep track of which checkouts went ok and which need to be tried again later
                final List<Asset> checkoutErrors = new ArrayList<>();
                final List<Asset> checkoutSuccess = new ArrayList<>();

                String notes = Action.generateNotesFromData(getContext(), Action.Direction.CHECKOUT,
                        (authorizingUser == null ? null : authorizingUser.getName()), isVerified);

                //check out the assets to the user and update the UI when we receive a response back
                for (AssetScannerView.ScannedAsset sa: assetsToCheckout) {
                    Asset a = sa.getAsset();
                    final ScannedAssetView sav = viewMap.get(sa.getAsset().getTag());
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //we may have already closed the dialog at this point
                                if (sav != null) {
                                    sav.showProgress();
                                    //ugly hack.  set the focus on the current view.  this _should_ trigger
                                    //+ the scrollview to scroll so the view is visible
                                    sav.getParent().requestChildFocus(sav, sav);
                                }
                            }
                        });

                        syncAdapter.checkoutAssetTo(getContext(), a.getId(), a.getTag(), user.getId(),
                                System.currentTimeMillis(), expectedCheckin, notes);

                        checkoutSuccess.add(sa.getAsset());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (sav != null) {
                                    sav.showCheck();
                                }
                            }
                        });

                    } catch (Exception e) {
                        checkoutErrors.add(sa.getAsset());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (sav != null) {
                                    sav.showError();
                                }
                            }
                        });
                        e.printStackTrace();
                    }
                }

                try {
                    //insert records into the database showing what was checked out successfully
                    db.checkoutAssetsToUser(user, checkoutSuccess, expectedCheckin, authorizingUser,
                            isVerified, true);

                    //stage checkout records for any assets that could not be checked out so these can
                    //+ be handled in the next sync
                    db.checkoutAssetsToUser(user, checkoutSuccess, expectedCheckin, authorizingUser,
                            isVerified, false);
                } catch (UserNotFoundException| AssetNotFoundException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //since the checkout process is now finished we can clear the list once the
                        //+ dialog is closed
                        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                scanner.reset();
                                StringBuilder sb = new StringBuilder(user.getName() + ":");
                                String and = "";
                                if (checkoutSuccess.size() > 0) {
                                    sb.append(String.format(" %d live ", checkoutSuccess.size()));
                                    and = "and ";
                                }
                                if (checkoutErrors.size() > 0) {
                                    sb.append(String.format("%s %d deferred ", and, checkoutErrors.size()));
                                }
                                sb.append("checkouts");
                                Snackbar snackbar = Snackbar.make(getView(), sb.toString(), 2000);
                                snackbar.show();
                            }
                        });

                        if (checkoutErrors.size() == 0) {
                            //the live checkout went ok for all assets.  Go ahead and close the dialog
                            playSoundEffect(getContext(), R.raw.original_sound__confirmation_downward);
                            d.dismiss();
                        } else {
                            //at least one asset could not be checked out live.  Warn the user that
                            //+ those assets will have a deffered checkout
                            playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
                            StringBuilder warningMessage = new StringBuilder(
                                    "Live checkout failed for some assets. I will attempt to " +
                                            "check these out during the next sync\n");
                            for (Asset a: checkoutErrors) {
                                warningMessage.append(String.format("• %s", a.getTag()));
                            }
                            AnimationHelper.fadeSwitchText(getContext(), messageTv,
                                    warningMessage.toString());

                            //cache these checkout requests in the database for the next sync
                            try {
                                db.checkoutAssetsToUser(user, checkoutErrors, expectedCheckin,
                                        authorizingUser, isVerified);
                            } catch (UserNotFoundException|AssetNotFoundException e) {
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }

                            //enable the close button on the dialog
                            ((AlertDialog) d).getButton(DialogInterface.BUTTON_POSITIVE)
                                    .setText(R.string.close);;
                            ((AlertDialog) d).getButton(DialogInterface.BUTTON_POSITIVE)
                                    .setEnabled(true);
                        }
                    }
                });


            }
        });
    }

    /**
     * Returns a long timestamp representing when the assets should be checked in. Timestamp
     * will come from the app settings
     * @param requestedDays
     * @return
     */
    private long getExpectedCheckin() {
        return getExpectedCheckin(null);
    }

    /**
     * Returns a long timestamp representing when the assets should be checked in. Timestamp
     * will come from the app settings, or from the requestedDays if provided
     * @param requestedDays
     * @return
     */
    private long getExpectedCheckin(@Nullable final Integer requestedDays) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int durationDays = Integer.parseInt(
                prefs.getString(getString(R.string.pref_key_check_out_duration),
                    getString(R.string.pref_default_check_out_duration)));

        if (requestedDays != null) {
            durationDays = requestedDays;
            FirebaseAnalytics.getInstance(getContext()).logEvent(
                    CustomEvents.CUSTOM_CHECKOUT_DURATION, null);
        }

        long expectedCheckinTimestamp;
        if (durationDays == 0) {
            //return a timestamp of -1 to indicate indefinate check out duration
            expectedCheckinTimestamp = -1;
        } else {
            int durationHours = 24 * durationDays;
            cal.add(Calendar.HOUR_OF_DAY, durationHours);
            expectedCheckinTimestamp =  cal.getTimeInMillis();
        }

        return expectedCheckinTimestamp;
    }

    private class AssetAlreadyCheckedOutException extends Exception {
        public AssetAlreadyCheckedOutException(String message) {
            super(message);
        }
    }

    private class AssetAlreadyScannedException extends Exception {
        public AssetAlreadyScannedException(String message) {
            super(message);
        }
    }

}
