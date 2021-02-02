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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.animation.ProgressBarAnimation;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.RoomDBWrapper;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.exception.StatusNotFoundException;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
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
            scanner.requestFocus();
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
        scanner.setCheckAssetAvailability(true);
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

    private void showCheckoutBadgeScanDialog() {
        final View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_checkout_user_scan, null);

//                final BadgeScanView badgeScanView = new BadgeScanView(getContext(), null, true, null);
        final BadgeScanView badgeScanView = dialogView.findViewById(R.id.badge_scanner);
        final TextView error = dialogView.findViewById(R.id.error);
        error.setTextColor(errorTextColor);
        String title;
        final String type;
        if (scanner.getScannedAssets().size() > 1) {
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
                processUserScan(user);
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
            } catch (AssetAlreadyCheckedOutException e) {
                User user = null;
                try {
                    user = db.findUserByID(asset.getAssignedToID());
                } catch (UserNotFoundException e2) {
                }

                StringBuilder sb = new StringBuilder();
                sb.append("Asset '" + asset.getTag() + "' is not available.\n\n");
                sb.append("This asset was checked out by ");

                if (user != null) {
                    sb.append("user " + user.getName());
                } else {
                    sb.append("an unknown user");
                }

                if (asset.getLastCheckout() != -1) {
                    String lastCheckout = null;
                    if (asset.getLastCheckout() != -1) {
                        Date d = new Date(asset.getLastCheckout());
                        DateFormat df = DateFormat.getDateTimeInstance();
                        lastCheckout = df.format(d);
                    }
                    sb.append(" on " + lastCheckout);
                } else {
                    sb.append(" at an unknown time");
                }

                recordBadScan(asset, sb.toString());
                AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                        .setTitle("Asset Not Available")
                        .setMessage(sb.toString())
                        .setPositiveButton(getResources().getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //nothing to do here
                                    }
                                }).create();
                playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
                d.show();
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
                                                scanner.requestFocus();
                                            }
                                        }, 1000);
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

    private void processUserScan(User user) {
        resetCountdown();

        //if at least one asset has been scanned, then checkout the items to the associate
        if (scanner.getScannedAssets().isEmpty()) {
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
            confirmCheckouts(user);
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

        for (AssetScannerView.ScannedAsset sa: scannedAssets) {
            if (sa.getAvailability() == AssetScannerView.AVAILABILITY.AVAILABLE) {
                availableAssets.add(sa.getAsset());
            } else if (sa.getAvailability() == AssetScannerView.AVAILABILITY.NOT_AVAILABLE) {
                unavailableAssets.add(sa.getAsset());
            } else if (sa.getAvailability() == AssetScannerView.AVAILABILITY.UNKNOWN) {
                unverifiedAssets.add(sa.getAsset());
            }
        }

        //show a warning dialog if required, otherwise move on to the check out
        if (unverifiedAssets.size() > 0 || unavailableAssets.size() > 0) {

            String unavailableMessage = toStringList(unavailableAssets);
            String unverifiedMessage = toStringList(unverifiedAssets);

            StringBuilder sb = new StringBuilder();
            if (unavailableMessage != null) {
                sb.append(String.format("Some scanned assets are not available. " +
                        "These assets should be placed back in the equipment closet: %s",
                        unavailableMessage));
            }

            if (unverifiedMessage != null) {
                sb.append(String.format("I could not verify availability of some assets. " +
                        "I will attempt to check these out later %s", unverifiedMessage));
            }

            AlertDialog d = new MaterialAlertDialogBuilder(getContext())
                    .setTitle("Some assets unavailable")
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
                            showCheckoutBadgeScanDialog();
                        }
                    })
                    .create();
            d.show();
        } else {
            showCheckoutBadgeScanDialog();
        }
    }

    private String toStringList(List<Asset> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        String message = null;
        StringBuilder sb = new StringBuilder();
        for (Asset a: list) {
            try {
                Model m = db.findModelByID(a.getModelID());
                sb.append(String.format("[%s - %s]", m.getName(), a.getTag()));
            } catch (ModelNotFoundException e) {

            }
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

    private void confirmCheckouts(@NotNull final User user) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean requireVerification = prefs.getBoolean(
                getString(R.string.pref_key_check_out_verify), Boolean.parseBoolean(
                        getString(R.string.pref_default_check_out_verify)));
        if (!requireVerification) {
            liveCheckoutAssetsToUser(user, false);

        } else {
            final VerifyCheckOutView verifyCheckOutView = new VerifyCheckOutView(user,
                    getContext(), null);


            boolean multiple = scanner.getScannedAssets().size() > 1;
            final AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                    .setTitle(String.format("Confirm %s Checkout", (multiple ? "Assets" : "Asset")))
                    .setView(verifyCheckOutView)
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //nothing to do here
                        }
                    })
                    .setPositiveButton(R.string.i_agree, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            liveCheckoutAssetsToUser(user, true);
                        }
                    })
                    .create();
            d.show();
        }
    }

    private void tryAddAssetToScannedList(Asset asset) throws AssetAlreadyCheckedOutException,
            AssetAlreadyScannedException {
        //if the asset is already in the list we can not add it again
        List<AssetScannerView.ScannedAsset> scannedAssets = scanner.getScannedAssets();
        for (AssetScannerView.ScannedAsset scannedAsset: scannedAssets) {
            if (scannedAsset.getAsset().equals(asset)) {
                throw new AssetAlreadyScannedException("Asset " + asset.getTag() + " already scanned");
            }
        }

        //if the asset is already checked out then it can not be scanned
        if (asset.getAssignedToID() == -1) {
            scanner.addAsset(asset);


        } else {
            throw new AssetAlreadyCheckedOutException("Asset " + asset.getTag() +
                    " is already checked out to user ID '" + asset.getAssignedToID() + "'");
        }
    }

    private void showCheckoutButton() {
        checkoutButton.show();
//        checkoutButton.setEnabled(true);
//        AnimationHelper.scaleIn(checkoutButton);
    }

    private void hideCheckoutButton() {
        checkoutButton.hide();
//        checkoutButton.setEnabled(false);
//        AnimationHelper.scaleOut(checkoutButton);
    }

    /**
     * Immediately checkout each asset in the scanned asset list to the given user.  If the sync
     * adapter indicates the asset can not be checked out an error should be shown
     * @param user
     * @param isVerified
     */
    private void liveCheckoutAssetsToUser(final User user, boolean isVerified) {
        final long expectedCheckin = getExpectedCheckin();
        final Map<AssetScannerView.ScannedAsset, ScannedAssetView> viewMap = new HashMap();
        View v = getLayoutInflater().inflate(R.layout.view_live_checkout, null);
        final TextView messageTv = v.findViewById(R.id.message);
        LinearLayout list = (LinearLayout) v.findViewById(R.id.list);

        for (AssetScannerView.ScannedAsset a: scanner.getScannedAssets()) {
            ScannedAssetView sav = new ScannedAssetView(getContext(), null, a);

            viewMap.put(a, sav);
            sav.setAssetRemovable(false);
            sav.showProgress();
            list.addView(sav);
        }

        final AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                .setTitle("Checking out assets")
                .setView(v)
                .setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        d.show();
        Window window = d.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //disable the close button until all assets have been processed
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                ((AlertDialog) d).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
            }
        });


        final SyncAdapter syncAdapter = SyncManager.getPrefferedSyncAdapter(getContext());
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                boolean allSuccess = true;

                //check out the assets to the user and update the UI when we receive a response back
                for (AssetScannerView.ScannedAsset sa: scanner.getScannedAssets()) {
                    Asset a = sa.getAsset();
                    final ScannedAssetView sav = viewMap.get(sa);
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sav.showProgress();
                                //ugly hack.  set the focus on the current view.  this _should_ trigger
                                //+ the scrollview to scroll so the view is visible
                                sav.getParent().requestChildFocus(sav, sav);
                            }
                        });

                        syncAdapter.checkoutAssetTo(getContext(), a.getId(), a.getTag(), user.getId(),
                                System.currentTimeMillis(), expectedCheckin, "Nodyn live checkout");
                        //insert an action into the local database so we can still track this for statistics
                        Action action = new Action(sa.getAsset(), user, System.currentTimeMillis(),
                                expectedCheckin, Action.Direction.CHECKOUT, true);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sav.showCheck();
                            }
                        });

                        db.insertAction(action);
                    } catch (Exception e) {
                        allSuccess = false;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sav.showError();
                            }
                        });
                        e.printStackTrace();
                    }
                }

                final boolean finalAllSuccess = allSuccess;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalAllSuccess) {
                            messageTv.setText("All assets checked out successfully!");
                            playSoundEffect(getContext(), R.raw.original_sound__confirmation_downward);
                        } else {
                            playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
                            messageTv.setText("Some assets could not be checked out.  These should be returned to the equipment closet");
                        }


                        //enable the close button on the dialog
                        ((AlertDialog) d).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
                        scanner.reset();
                    }
                });


            }
        });
    }

    /**
     * Inserts a checkout activity record into the local database.  The actual checkout process on
     * the backend system may occur some time in the future
     * @param user
     * @param isVerified
     */
    private void deferredCheckoutAssetsToUser(User user, boolean isVerified) {
        try {
            long expectedCheckin = getExpectedCheckin();
            List<AssetScannerView.ScannedAsset> scannedAssets = scanner.getScannedAssets();

            // TODO: 2020-02-20 check the list of scanned assets for any that are unavailable or unknown and show the appropriate warning
            List<Asset> assetList = new ArrayList<>();
            for (AssetScannerView.ScannedAsset sa: scannedAssets) {
                if (sa.getAvailability() == AssetScannerView.AVAILABILITY.UNKNOWN ||
                        sa.getAvailability() == AssetScannerView.AVAILABILITY.AVAILABLE) {
                    assetList.add(sa.getAsset());
                }
            }

            db.checkoutAssetsToUser(user, assetList, expectedCheckin, authorizingUser,
                    isVerified);
            showNotification("Checked out " + assetList.size() + " assets to " + user.getName());

            //go ahead and try to push out these records now instead of waiting until the next sync
            Intent i = new Intent(getContext(), SyncService.class);
            i.putExtra(SyncService.SYNC_TYPE_KEY, SyncService.SYNC_TYPE_QUICK);
            getContext().startService(i);

            hideCheckoutButton();
            playSoundEffect(getContext(), R.raw.original_sound__confirmation_downward);

            scanner.requestFocus();

            Bundle bundle = new Bundle();
            bundle.putInt(CustomEvents.USER_CHECKOUT_COUNT, assetList.size());
            usersCheckedOut++;
            FirebaseAnalytics.getInstance(getContext()).logEvent(CustomEvents.USER_CHECKOUT, bundle);
        } catch (Exception e) {
            showNotification("Caught exception " + e.getClass().getSimpleName() + " checking out assets");
        } finally {
            scanner.reset();
        }
    }

    private long getExpectedCheckin() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int durationDays = Integer.parseInt(
                prefs.getString(getString(R.string.pref_key_check_out_duration),
                    getString(R.string.pref_default_check_out_duration)));

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

    private void updateButtonText(Button button, VerifyCheckOutView verifyCheckOutView) {
        MarkdownView markdown = verifyCheckOutView.getMarkdownView();
        int textTotalHeight = verifyCheckOutView.getHeight();
        int pageHeight = markdown.getHeight();
        int scrollY = markdown.getScrollY();
        if (scrollY < textTotalHeight - pageHeight) {
            button.setText(R.string.more);
        } else {
            button.setText(R.string.i_agree);
        }
        button.invalidate();
    }

    private void fadeInText(TextSwitcher view, String newText) {
        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        view.setOutAnimation(out);
        view.setInAnimation(in);
        view.setText(newText);
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
