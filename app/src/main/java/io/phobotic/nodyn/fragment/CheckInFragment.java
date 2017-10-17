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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.exception.ModelNotFoundException;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.fragment.listener.CheckInOutListener;
import io.phobotic.nodyn.view.AssetScanList;
import io.phobotic.nodyn.view.BadgeScanView;
import io.phobotic.nodyn.view.VerifyCheckinView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CheckInFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CheckInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CheckInFragment extends Fragment {
    private static final String TAG = CheckInFragment.class.getSimpleName();
    private static final String ARG_AUTHORIZATION = "authorization";
    private CheckInOutListener listener;
    private View rootView;
    private AssetScanList scanner;
    private TextSwitcher title;
    private TextSwitcher message;
    private User authorizingUser;
    private boolean authorizationRequired;
    private boolean verificationRequired;
    private SharedPreferences prefs;
    private FloatingActionButton checkinButton;
    private View footer;
    private View warning;

    public static CheckInFragment newInstance(User authorizingUser) {
        CheckInFragment fragment = new CheckInFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_AUTHORIZATION, authorizingUser);
        fragment.setArguments(args);
        return fragment;
    }

    public CheckInFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Asset Check In");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            authorizingUser = (User) getArguments().getSerializable(ARG_AUTHORIZATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_check_in, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        init();

        return rootView;
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public void setListener(@Nullable CheckInOutListener listener) {
        this.listener = listener;
    }

    private void init() {
        footer = rootView.findViewById(R.id.footer);
        warning = rootView.findViewById(R.id.warning);
        initTextSwitchers();
        initScanner();
        initCheckinFab();
    }

    private void fadeInText(TextSwitcher view, String newText) {
        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        view.setOutAnimation(out);
        view.setInAnimation(in);
        view.setText(newText);
    }

    private void initScanner() {
        scanner = (AssetScanList) rootView.findViewById(R.id.scan_list);
        scanner.setListener(new AssetScanList.OnAssetScannedListener() {
            @Override
            public void onAssetScanned(Asset asset) {
                processAssetScan(asset);
            }

            @Override
            public void onAssetScanListChanged(@NotNull List<Asset> assets) {
                if (assets.isEmpty() && checkinButton.getVisibility() != View.GONE) {
                    if (footer.getVisibility() != View.GONE) {
                        hideFooter();
                    }

                    if (warning.getVisibility() != View.GONE) {
                        hideWarning();
                    }
                }
            }

            @Override
            public void onScanError(String message) {
                showNotification(message);
            }
        });

        authorizationRequired = prefs.getBoolean(
                getString(R.string.pref_key_check_in_require_scan), Boolean.parseBoolean(
                        getString(R.string.pref_default_check_in_require_scan)));
        verificationRequired = prefs.getBoolean(getString(R.string.pref_key_check_in_show_verify),
                Boolean.parseBoolean(getString(R.string.pref_default_check_in_show_verify)));

        //if authorization is not required then there is no need to be able to remove assets once
        //+ they are scanned
        scanner.setAssetsRemovable(authorizationRequired);
    }

    private void initTextSwitchers() {
        title = (TextSwitcher) rootView.findViewById(R.id.title);
        title.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(getContext());
                t.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                t.setTextAppearance(getContext(), android.R.style.TextAppearance_Material_Large_Inverse);
                return t;
            }
        });
        title.setCurrentText(getResources().getString(R.string.check_in_title_scan_an_asset));

        message = (TextSwitcher) rootView.findViewById(R.id.message);
        message.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(getContext());
                t.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                t.setTextAppearance(getContext(), android.R.style.TextAppearance_Material_Inverse);
                return t;
            }
        });
        message.setCurrentText(getResources().getString(R.string.blank));
    }

    private void processUserScan(User user) {
        if (!authorizationRequired) {
            //show a warning dialog
            AlertDialog d = new AlertDialog.Builder(getContext())
                    .setTitle("User scanned")
                    .setMessage("User scan is not required to check in assets.  Assets will be checked in immediately once scanned")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //nothing to do here
                        }
                    })
                    .create();
            d.show();
        } else {
            if (scanner.getScannedAssets().isEmpty()) {
                AlertDialog d = new AlertDialog.Builder(getContext())
                        .setTitle("No assets scanned")
                        .setMessage("Unable to checkin assets.  No assets have been scanned")
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
                verifyAuthorizationAndCheckin(user);
            }
        }
    }

    private void verifyAuthorizationAndCheckin(final User user) {
        if (!isUserAuthenticated(user)) {
            AlertDialog d = new AlertDialog.Builder(getContext())
                    .setTitle("Not authenticated")
                    .setMessage("User " + user.getName() + " is not authenticated to check in assets.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //nothing to do here
                        }
                    })
                    .create();
            d.show();
        } else {
            if (!verificationRequired) {
                checkinAssets(user, false);
            } else {
                verifyAssetCheckin(user);
            }
        }
    }

    private void verifyAssetCheckin(final User user) {
        View markdownView = new VerifyCheckinView(getContext(), null);
        AlertDialog d = new AlertDialog.Builder(getContext())
                .setTitle("Confirm Checkin")
                .setView(markdownView)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing to do here
                    }
                })
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkinAssets(user, true);
                    }
                })
                .create();
        d.show();
    }

    private boolean isUserAuthenticated(User user) {
        int[] groups = user.getGroupsIDs();
        if (groups == null) {
            groups = new int[]{};
        }

        List<String> userGroups = new ArrayList<>();
        for (int i : groups) {
            userGroups.add(String.valueOf(i));
        }

        boolean userAuthenticated = false;
        Set<String> allowedGroupSet = prefs.getStringSet(getString(
                R.string.pref_key_check_in_authenticating_groups), new HashSet<String>());
        List<String> allowedGroups = new ArrayList<>();
        allowedGroups.addAll(allowedGroupSet);

        allowedGroups.retainAll(userGroups);
        if (allowedGroups.size() > 0) {
            userAuthenticated = true;
        }

        return userAuthenticated;
    }

    private void checkinAssets(User user, boolean isVerified) {
        Database db = Database.getInstance(getContext());
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(System.currentTimeMillis());
            // TODO: 8/20/17 pull hours from settings
            cal.add(Calendar.HOUR_OF_DAY, 24);
            long expectedCheckin = cal.getTimeInMillis();
            List<Asset> scannedAssetsList = scanner.getScannedAssets();
            scannedAssetsList = removeAlreadyCheckedInAssets(scannedAssetsList);
            db.checkinAssets(scannedAssetsList, user, System.currentTimeMillis(), user, isVerified);
            String username = user == null ? "" : user.getName() + " ";
            showNotification(username + " checked in " + scannedAssetsList.size() + " assets");

            //if authorization was not required then leave scanned items in the list
            if (authorizationRequired) {
                scanner.reset();
            } else {
                //mark assets in the list as checked in.  this is to prevent the assets from being
                //+ checked in again if the list was not cleared
                for (Asset asset : scannedAssetsList) {
                    asset.setAssignedToID(-1);
                }

                fadeInText(title, getResources().getString(R.string.check_in_title_scan_an_asset));
                fadeInText(message, getResources().getString(R.string.blank));
            }

            //hide the header and footer to reset the view
            if (footer.getVisibility() != View.GONE) {
                hideFooter();
            }

            if (warning.getVisibility() != View.GONE) {
                hideWarning();
            }

            final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.brd3);
            mp.start();


        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Caught exception " + e.getClass().getSimpleName() +
                    " checking out assets");
        }
    }

    private List<Asset> removeAlreadyCheckedInAssets(List<Asset> assets) {
        List<Asset> newList = new ArrayList<>();

        for (Asset asset : assets) {
            if (asset.getAssignedToID() != -1) {
                newList.add(asset);
            }
        }

        return newList;
    }

    private void showNotification(String err) {
        Snackbar snackbar = Snackbar.make(rootView, err, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void initCheckinFab() {
        checkinButton = (FloatingActionButton) rootView.findViewById(R.id.checkout_button);
        checkinButton.setOnClickListener(new View.OnClickListener() {
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

                d.show();
            }
        });
        checkinButton.hide();
    }

    private void showCheckInButton() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(checkinButton, "scaleX", 0, 1);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(checkinButton, "scaleY", 0, 1);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(scaleX, scaleY);
        animSetXY.setInterpolator(new BounceInterpolator());
        animSetXY.setDuration(500);
        checkinButton.setVisibility(View.VISIBLE);
        animSetXY.start();
    }

    private void hideCheckInButton() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(checkinButton, "scaleX", 1, 0);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(checkinButton, "scaleY", 1, 0);
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
                checkinButton.setVisibility(View.GONE);
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

    private void processAssetScan(Asset asset) {
        //if the model could not be checked out then we also should not be able to check it in
        if (!modelCanBeCheckedOut(asset)) {
            String modelName = "";
            try {
                Database db = Database.getInstance(getContext());
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
        } else {
            try {
                tryAddAssetToScannedList(asset);
                fadeInText(title, getResources().getString(R.string.check_in_title_continue_scan_assets));

                //if authorization is not required then go ahead and check the asset back in
                if (!authorizationRequired) {
                    checkinAssets(null, false);
                    fadeInText(message, getResources().getString(R.string.check_in_message_check_in_immediate));
                } else {
                    fadeInText(message, getResources().getString(R.string.check_in_message_scan_badge_to_complete));
                }


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

            } catch (AssetNotCheckedOutException e) {
                AlertDialog d = new AlertDialog.Builder(getContext())
                        .setTitle("Asset Not Checked Out")
                        .setMessage("Asset '" + asset.getTag() + "' is not checked out and can not " +
                                "be checked back in")
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

    private void tryAddAssetToScannedList(Asset asset) throws AssetAlreadyScannedException,
            AssetNotCheckedOutException {
        //if the asset is already in the list we can not add it again
        if (scanner.getScannedAssets().contains(asset)) {
            throw new AssetAlreadyScannedException("Asset " + asset.getTag() + " already scanned");
        }


        //if the asset is not checked out it can't be checked back in
        if (asset.getAssignedToID() == -1) {
            throw new AssetNotCheckedOutException("Asset " + asset.getTag() + " is not checked out and can not be checked in");
        } else {
            scanner.addAsset(asset);

            //if this was the first asset scanned then transition the instructions
            if (scanner.getScannedAssets().size() == 1) {
                fadeInText(title, getResources().getString(R.string.check_in_title_continue_scan_assets));
                fadeInText(message, getResources().getString(R.string.check_in_message_scan_badge_to_complete));

                showFooter();

                if (authorizationRequired) {
                    showWarning();
                }

            }
        }
    }

    private void showWarning() {
        warning.setVisibility(View.VISIBLE);
    }

    private void hideWarning() {
        warning.setVisibility(View.GONE);
    }

    private void showFooter() {
        footer.setVisibility(View.VISIBLE);
        //only show the checkin FAB if authorization is required
        if (authorizationRequired) {
            showCheckInButton();
        }
    }

    private void hideFooter() {
        if (checkinButton.getVisibility() == View.VISIBLE) {
            hideCheckInButton();
        }
        footer.setVisibility(View.GONE);
    }

    private class AssetAlreadyScannedException extends Exception {
        public AssetAlreadyScannedException(String message) {
            super(message);
        }
    }

    private class AssetNotCheckedOutException extends Exception {
        public AssetNotCheckedOutException(String message) {
            super(message);
        }
    }
}
