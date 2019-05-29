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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.database.scan.ScanRecord;
import io.phobotic.nodyn_app.database.scan.ScanRecordDatabase;
import io.phobotic.nodyn_app.fragment.listener.CheckInOutListener;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.helper.AssetHelper;
import io.phobotic.nodyn_app.helper.MediaHelper;
import io.phobotic.nodyn_app.reporting.CustomEvents;
import io.phobotic.nodyn_app.view.AssetScannerView;
import io.phobotic.nodyn_app.view.BadgeScanView;
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
    private TextSwitcher title;
    private FloatingActionButton checkoutButton;
    private TextSwitcher message;
    private View footer;
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
        CustomEvent ce = new CustomEvent(CustomEvents.CHECKOUT_SESSION_COMPLETE)
                .putCustomAttribute(CustomEvents.CHECKOUT_COUNTS_FOR_SESSION, usersCheckedOut);
        Answers.getInstance().logCustom(ce);
        if (timer != null) {
            timer.cancel();
        }
    }

    private void init() {
        footer = rootView.findViewById(R.id.footer);
        mainBox = rootView.findViewById(R.id.checkout);
        warning = rootView.findViewById(R.id.warning);
        warningProgress = rootView.findViewById(R.id.warning_progress);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        errorTextColor = typedValue.data;

        initTextSwitchers();
        initScanner();
        initCheckoutFab();
        initCountDownTimer();
    }

    private void initTextSwitchers() {
        title = rootView.findViewById(R.id.title);
        title.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(getContext());
                t.setTextAppearance(getContext(), android.R.style.TextAppearance_Material_Title);
                t.setTextColor(getResources().getColor(R.color.section_title));
                return t;
            }
        });
        title.setCurrentText(getResources().getString(R.string.check_out_title_scan_an_asset));

        message = rootView.findViewById(R.id.message);
        message.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(getContext());
                t.setTextAppearance(getContext(), android.R.style.TextAppearance_Material);
                return t;
            }
        });
        message.setCurrentText(getResources().getString(R.string.blank));
    }

    public void setListener(@Nullable CheckInOutListener listener) {
        this.listener = listener;
    }

    private void initScanner() {
        scanner = rootView.findViewById(R.id.scan_list);
        scanner.setAssetsRemovable(true);
        scanner.setListener(new AssetScannerView.OnAssetScannedListener() {
            @Override
            public void onAssetScanned(Asset asset) {
                processAssetScan(asset);
            }

            @Override
            public void onAssetScanListChanged(@NotNull List<Asset> assets) {
                if (assets.isEmpty()) {
                    if (footer.getVisibility() != View.GONE) {
                        hideFooter();
                    }
                    resetCountdown();
                }
            }

            @Override
            public void onScanError(String message) {
                showNotification(message);
            }
        });
    }

    private void initCheckoutFab() {
        checkoutButton = rootView.findViewById(R.id.checkout_button);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                final AlertDialog d = new AlertDialog.Builder(getContext())
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
        });
        checkoutButton.hide();
    }

    private void showFooter() {
        AnimationHelper.expand(footer);
        showCheckoutButton();
    }

    private void hideFooter() {
        hideCheckoutButton();
        AnimationHelper.collapse(footer);
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
            AlertDialog d = new AlertDialog.Builder(getContext())
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
            AlertDialog d = new AlertDialog.Builder(getContext())
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
                AlertDialog d = new AlertDialog.Builder(getContext())
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
                AlertDialog d = new AlertDialog.Builder(getContext())
                        .setTitle("Asset Not Available")
                        .setMessage("Asset '" + asset.getTag() + "' has already been scanned")
                        .setPositiveButton(getResources().getString(android.R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //nothing to do here
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
            Crashlytics.logException(e);
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
            AlertDialog d = new AlertDialog.Builder(getContext())
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
            checkoutAssetsToUser(user, false);
            scanner.reset();
        } else {
            final VerifyCheckOutView verifyCheckOutView = new VerifyCheckOutView(user,
                    getContext(), null);


            boolean multiple = scanner.getScannedAssets().size() > 1;
            final AlertDialog d = new AlertDialog.Builder(getContext())
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
                            checkoutAssetsToUser(user, true);
                            scanner.reset();
                        }
                    })
                    .create();
            d.show();
        }
    }

    private void tryAddAssetToScannedList(Asset asset) throws AssetAlreadyCheckedOutException,
            AssetAlreadyScannedException {
        //if the asset is already in the list we can not add it again
        List<Asset> scannedAssets = scanner.getScannedAssets();
        if (scannedAssets.contains(asset)) {
            throw new AssetAlreadyScannedException("Asset " + asset.getTag() + " already scanned");
        }


        //if the asset is already checked out then it can not be scanned
        if (asset.getAssignedToID() == -1) {
            scanner.addAsset(asset);

            //if this was the first asset scanned then transition the instructions
            if (scanner.getScannedAssets().size() == 1) {
                fadeInText(title, getResources().getString(R.string.check_out_title_scan_more_assets));
                fadeInText(message, getResources().getString(R.string.check_out_message_scan_user));
                showFooter();
            }
        } else {
            throw new AssetAlreadyCheckedOutException("Asset " + asset.getTag() +
                    " is already checked out to user ID '" + asset.getAssignedToID() + "'");
        }
    }

    private void showCheckoutButton() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(checkoutButton, "scaleX", 0, 1);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(checkoutButton, "scaleY", 0, 1);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(scaleX, scaleY);
        animSetXY.setInterpolator(new BounceInterpolator());
        animSetXY.setDuration(500);
        checkoutButton.setVisibility(View.VISIBLE);
        animSetXY.start();
    }

    private void hideCheckoutButton() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(checkoutButton, "scaleX", 1, 0);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(checkoutButton, "scaleY", 1, 0);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(scaleX, scaleY);
        animSetXY.setInterpolator(new BounceInterpolator());
        animSetXY.setDuration(500);
        animSetXY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                checkoutButton.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSetXY.start();
    }

    private void checkoutAssetsToUser(User user, boolean isVerified) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            // TODO: 8/20/17 pull hours from settings
            cal.add(Calendar.HOUR_OF_DAY, 24);
            long expectedCheckin = cal.getTimeInMillis();
            List<Asset> scannedAssetsList = scanner.getScannedAssets();
            db.checkoutAssetsToUser(user, scannedAssetsList, expectedCheckin, authorizingUser,
                    isVerified);
            showNotification("Checked out " + scannedAssetsList.size() + " assets to " + user.getName());

            fadeInText(title, getResources().getString(R.string.check_out_title_scan_an_asset));
            fadeInText(message, getResources().getString(R.string.blank));
            hideFooter();
            playSoundEffect(getContext(), R.raw.original_sound__confirmation_downward);

            scanner.requestFocus();

            CustomEvent ce = new CustomEvent(CustomEvents.USER_CHECKOUT)
                    .putCustomAttribute(CustomEvents.USER_CHECKOUT_COUNT, scannedAssetsList.size());
            usersCheckedOut++;
            Answers.getInstance().logCustom(ce);

            scanner.reset();
        } catch (Exception e) {
            showNotification("Caught exception " + e.getClass().getSimpleName() + " checking out assets");
        }
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
