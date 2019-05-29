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
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.animation.ProgressBarAnimation;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.fragment.listener.CheckInOutListener;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.view.AssetScannerView;

import static io.phobotic.nodyn_app.helper.MediaHelper.playSoundEffect;

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
    private static final String ARGS_VERIFIED = "verified";
    private static final int MAX_PROGRESS = 1000;
    private CheckInOutListener listener;
    private View rootView;
    private AssetScannerView scanner;
    private TextSwitcher title;
    private TextSwitcher message;
    private User authorizingUser;
    private SharedPreferences prefs;
    private FloatingActionButton checkinButton;
    private View footer;
    private View warning;
    private CountDownTimer timer;
    private ProgressBar warningProgress;
    private TextView warningMessage;
    private boolean checkInsVerified;

    public static CheckInFragment newInstance(User authorizingUser, boolean checkInsVerified) {
        CheckInFragment fragment = new CheckInFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_AUTHORIZATION, authorizingUser);
        args.putBoolean(ARGS_VERIFIED, checkInsVerified);
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
            checkInsVerified = getArguments().getBoolean(ARGS_VERIFIED, false);
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
        if (timer != null) {
            timer.cancel();
        }
    }

    private void init() {
        footer = rootView.findViewById(R.id.footer);
        warning = rootView.findViewById(R.id.warning);
        warning = rootView.findViewById(R.id.countdown_warning);
        warningProgress = rootView.findViewById(R.id.warning_progress);
        warningProgress.setMax(MAX_PROGRESS);
        warningProgress.setProgress(MAX_PROGRESS);
        warningMessage = rootView.findViewById(R.id.warning_text_1);
        initTextSwitchers();
        initScanner();

        resetCountdown();
    }

    public void setListener(@Nullable CheckInOutListener listener) {
        this.listener = listener;
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

    private void fadeInText(TextSwitcher view, String newText) {
        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        view.setOutAnimation(out);
        view.setInAnimation(in);
        view.setText(newText);
    }

    private void initScanner() {
        scanner = rootView.findViewById(R.id.scan_list);
        scanner.setListener(new AssetScannerView.OnAssetScannedListener() {
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

                    scanner.requestFocus();
                }
            }

            @Override
            public void onScanError(String message) {
                showNotification(message);
            }
        });

        //the check-in fragment will immediately check in assets as they are scanned
        scanner.setAssetsRemovable(false);
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
            final long showWarningAt = Math.max(50000l, (long) (timeout * .5));

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

                        String warningText = getString(R.string.check_in_warning_message_1);
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

    private void processAssetScan(Asset asset) {
        //the de-authorization timer should only be active when the check-in list is empty
        resetCountdown();

        //we need to be careful to only allow assets to be checked-in if that asset model is allowed
        //+ to be checked out
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
                checkinAsset(asset);
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
                playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
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
                playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
                d.show();
            }
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
            if (allowedModelIDs.contains(String.valueOf(asset.getModelID()))) {
                modelValid = true;
            }
        }

        return modelValid;
    }

    private void showNotification(String err) {
        Snackbar snackbar = Snackbar.make(rootView, err, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void checkinAsset(Asset asset) {
        Database db = Database.getInstance(getContext());
        try {
            asset.setAssignedToID(-1);
            List<Asset> l = new ArrayList<>();
            l.add(asset);
            db.checkinAssets(l, authorizingUser, System.currentTimeMillis(), authorizingUser, checkInsVerified);
            String username = authorizingUser == null ? "" : authorizingUser.getName() + " ";

            fadeInText(title, getResources().getString(R.string.check_in_title_continue_scan_assets));
            fadeInText(message, getResources().getString(R.string.check_in_message_check_in_immediate));

            playSoundEffect(getContext(), R.raw.n_audioman__blip);

        } catch (Exception e) {
            e.printStackTrace();
            showNotification("Caught exception " + e.getClass().getSimpleName() +
                    " checking out assets");
        }
    }

    private void stopCountdown() {
        if (timer != null) {
            timer.cancel();
        }

        AnimationHelper.collapse(warning);
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
            }
        }
    }

    private void showFooter() {
        AnimationHelper.expand(footer);
    }

    private void hideFooter() {
        AnimationHelper.collapse(footer);
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
