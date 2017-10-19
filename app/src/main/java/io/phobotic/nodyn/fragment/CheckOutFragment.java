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

package io.phobotic.nodyn.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.exception.ModelNotFoundException;
import io.phobotic.nodyn.database.exception.StatusNotFoundException;
import io.phobotic.nodyn.database.exception.UserNotFoundException;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.Status;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.fragment.listener.CheckInOutListener;
import io.phobotic.nodyn.view.AssetScanList;
import io.phobotic.nodyn.view.BadgeScanView;
import io.phobotic.nodyn.view.VerifyCheckOutView;
import us.feras.mdv.MarkdownView;

public class CheckOutFragment extends Fragment {
    private static final String ARG_AUTHORIZATION = "arg_authorization";
    Database db;
    private CheckInOutListener listener;
    private View rootView;
    private AssetScanList scanner;
    private User authorizingUser;
    private TextSwitcher title;
    private FloatingActionButton checkoutButton;
    private TextSwitcher message;
    private View footer;
    private View mainBox;

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
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = Database.getInstance(getContext());

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
    }

//    private void showFooter() {
//        int initialHeight = mainBox.getMeasuredHeight();
//        int finalHeight = initialHeight - footer.getMeasuredHeight();
//        ValueAnimator animator = ValueAnimator.ofInt(initialHeight, finalHeight);
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                int height = (int) animation.getAnimatedValue();
//                ViewGroup.LayoutParams layoutParams = mainBox.getLayoutParams();
//                layoutParams.height = height;
//                mainBox.setLayoutParams(layoutParams);
//            }
//        });
//
//        animator.setDuration(1000);
//        animator.start();
//    }

    public void setListener(@Nullable CheckInOutListener listener) {
        this.listener = listener;
    }

    private void init() {
        footer = rootView.findViewById(R.id.footer);
        mainBox = rootView.findViewById(R.id.checkout);
        initTextSwitchers();
        initScanner();
        initCheckoutFab();
    }

    private void showFooter() {
//        Animation bottomUp = AnimationUtils.loadAnimation(getContext(),
//                R.anim.bottom_up);
//        bottomUp.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//                footer.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                showCheckoutButton();
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//
//        footer.startAnimation(bottomUp);
        footer.setVisibility(View.VISIBLE);
        showCheckoutButton();
    }

    private void hideFooter() {
//        Animation bottomDown = AnimationUtils.loadAnimation(getContext(),
//                R.anim.bottom_down);
//
//        bottomDown.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//                hideCheckoutButton();
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                footer.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//
//        footer.startAnimation(bottomDown);

        hideCheckoutButton();
        footer.setVisibility(View.GONE);
    }

    private void initCheckoutFab() {
        checkoutButton = (FloatingActionButton) rootView.findViewById(R.id.checkout_button);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final BadgeScanView badgeScanView = new BadgeScanView(getContext(), null);

                final AlertDialog d = new AlertDialog.Builder(getContext())
                        .setTitle("Scan associate ID badge")
                        .setView(badgeScanView)
                        .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
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
                    }

                    @Override
                    public void onUserScanError(@NotNull String message) {
                        //let the badge scanner handle bad input
                    }
                });

                d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

                d.show();

            }
        });
        checkoutButton.hide();
    }

    private void initScanner() {
        scanner = (AssetScanList) rootView.findViewById(R.id.scan_list);
        scanner.setAssetsRemovable(true);
        scanner.setListener(new AssetScanList.OnAssetScannedListener() {
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
                }
            }

            @Override
            public void onScanError(String message) {
                showNotification(message);
            }
        });
    }

    private void initTextSwitchers() {
        title = (TextSwitcher) rootView.findViewById(R.id.title);
        title.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(getContext());
                t.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                t.setTextAppearance(getContext(), android.R.style.TextAppearance_Material_Large);
                return t;
            }
        });
        title.setCurrentText(getResources().getString(R.string.check_out_title_scan_an_asset));

        message = (TextSwitcher) rootView.findViewById(R.id.message);
        message.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(getContext());
                t.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                t.setTextAppearance(getContext(), android.R.style.TextAppearance_Material);
                return t;
            }
        });
        message.setCurrentText(getResources().getString(R.string.blank));
    }

    private void processUserScan(User user) {
        //if at least one asset has been scanned, the checkout the items to the associate
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
            final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.correct);
            mp.start();
        } else {
            confirmCheckouts(user);
        }
    }

    private void processAssetScan(Asset asset) {
        if (!modelCanBeCheckedOut(asset)) {
            String modelName = "";
            try {
                modelName = " '" + db.findModelByID(asset.getModelID()).getName() + "'";
            } catch (ModelNotFoundException e) {
            }

            View v = getLayoutInflater(null).inflate(R.layout.view_model_unavailable, null);

            AlertDialog d = new AlertDialog.Builder(getContext())
                    .setTitle("Model not available")
                    .setView(v)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing here
                        }
                    }).create();
            d.show();
        } else if (!isAssetStatusValid(asset)) {
            String statusString = "";
            try {
                Status status = db.findStatusByID(asset.getStatusID());
                statusString = " Current asset status is '" +
                        status.getName() + "'";
            } catch (StatusNotFoundException e) {
            }

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
            d.show();
        } else {
            try {
                tryAddAssetToScannedList(asset);
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
                        DateFormat df = new SimpleDateFormat();
                        lastCheckout = df.format(d);
                    }
                    sb.append(" on " + lastCheckout);
                } else {
                    sb.append(" at an unknown time");
                }

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
                final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.bassy_chirp);
                mp.start();
                d.show();
            } catch (AssetAlreadyScannedException e) {
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
                final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.bassy_chirp);
                mp.start();
                d.show();

            }
        }
    }

    private void showNotification(String err) {
        Snackbar snackbar = Snackbar.make(rootView, err, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void confirmCheckouts(final User user) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean requireVerification = prefs.getBoolean(
                getString(R.string.pref_key_check_out_verify), Boolean.parseBoolean(
                        getString(R.string.pref_default_check_out_verify)));
        if (!requireVerification) {
            checkoutAssetsToUser(user, false);
        } else {
            final VerifyCheckOutView verifyCheckOutView = new VerifyCheckOutView(getContext(), null);


            final AlertDialog d = new AlertDialog.Builder(getContext())
                    .setTitle("Confirm Checkout for " + user.getName())
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
                        }
                    })
                    .create();
            d.show();
        }
    }

    /**
     * Return true if all asset models are allowed to be checked out, or if this asset model is
     * one of the chosen models that can be checked out
     *
     * @param asset
     * @return
     */
    private boolean modelCanBeCheckedOut(Asset asset) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean modelValid = false;

        boolean allModelsValid = prefs.getBoolean(getString(
                R.string.pref_key_check_out_all_models), Boolean.parseBoolean(
                getString(R.string.pref_default_check_out_all_models)));
        if (allModelsValid) {
            modelValid = true;
        } else {
            Set<String> allowedModelIDs = prefs.getStringSet(
                    getString(R.string.pref_key_check_out_models), new HashSet<String>());
            if (allowedModelIDs.contains(String.valueOf(String.valueOf(asset.getModelID())))) {
                modelValid = true;
            }
        }

        return modelValid;
    }

    private boolean isAssetStatusValid(Asset asset) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean statusValid = false;

        boolean allStatusesAllowed = prefs.getBoolean(getString(
                R.string.pref_key_asset_status_allow_all), Boolean.parseBoolean(
                getString(R.string.pref_default_asset_status_allow_all)));
        if (allStatusesAllowed) {
            statusValid = true;
        } else {
            Set<String> allowedStatusIDs = getAllowedStatusIDs();
            if (allowedStatusIDs.contains(String.valueOf(String.valueOf(asset.getStatusID())))) {
                statusValid = true;
            }
        }

        return statusValid;
    }

    @NonNull
    private Set<String> getAllowedStatusIDs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs.getStringSet(getString(
                R.string.pref_key_asset_status_allowed_statuses), new HashSet<String>());
    }

    private Set<String> getAllowedStatusNames() {
        Set<String> allowedStatusIDs = getAllowedStatusIDs();
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
            scanner.reset();
            fadeInText(title, getResources().getString(R.string.check_out_title_scan_an_asset));
            fadeInText(message, getResources().getString(R.string.blank));
            hideFooter();

            final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.brd3);
            mp.start();
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
