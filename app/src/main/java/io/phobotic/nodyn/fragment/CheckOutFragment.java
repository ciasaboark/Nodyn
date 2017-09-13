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
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.jetbrains.annotations.Nullable;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.phobotic.nodyn.ObservableMarkdownView;
import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.fragment.listener.CheckInOutListener;
import io.phobotic.nodyn.view.AssetScanList;
import io.phobotic.nodyn.view.VerifyCheckOutView;
import us.feras.mdv.MarkdownView;

public class CheckOutFragment extends Fragment {
    private static final String ARG_AUTHORIZATION = "arg_authorization";

    private CheckInOutListener listener;
    private View rootView;
    private AssetScanList scanner;
    private User authorizingUser;
    private TextSwitcher title;
    private TextSwitcher message;


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

    public void setListener(@Nullable CheckInOutListener listener) {
        this.listener = listener;
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
        rootView = inflater.inflate(R.layout.fragment_check_out, container, false);
        init();

        return rootView;
    }

    private void init() {
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
        title.setCurrentText(getResources().getString(R.string.check_out_title_scan_an_asset));

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

        scanner = (AssetScanList) rootView.findViewById(R.id.scan_list);
        scanner.setAssetsRemovable(true);
        scanner.setListener(new AssetScanList.OnInputScanned() {
            @Override
            public void onUserScanned(User user) {
                processUserScan(user);
            }

            @Override
            public void onAssetScanned(Asset asset) {
                processAssetScan(asset);
            }

            @Override
            public void onScanError(String message) {
                showNotification(message);
            }
        });


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
            AlertDialog d = new AlertDialog.Builder(getContext())
                    .setTitle("Model not available")
                    .setMessage("Checkouts of asset model '" + asset.getModel() + "' are not allowed.")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing here
                        }
                    }).create();
            d.show();
        } else if (assetStatusInvalid(asset)) {
            AlertDialog d = new AlertDialog.Builder(getContext())
                    .setTitle("Asset not available")
                    .setMessage("Asset '" + asset.getTag() + "' can not be checked out.  " +
                            "Asset status must be 'Ready to Deploy'.  Current asset status is '" +
                            asset.getStatus() + "'")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing here
                        }
                    }).create();
            d.show();
        } else {
            try {
                addAssetToScannedList(asset);
            } catch (AssetAlreadyCheckedOutException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("Asset '" + asset.getTag() + "' is not available.\n\n");
                sb.append("This asset was checked out by ");

                if (asset.getAssignedTo() != null && asset.getAssignedTo().length() > 0) {
                    sb.append("user " + asset.getAssignedTo());
                } else {
                    sb.append("an unknown user");
                }

                if (asset.getLastCheckout() != null && asset.getLastCheckout().length() > 0) {
                    sb.append(" on " + asset.getLastCheckout());
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
        boolean requireVerification = prefs.getBoolean("check_out_show_verify", false);
        if (!requireVerification) {
            checkoutAssetsToUser(user, false);
        } else {
            final VerifyCheckOutView verifyCheckOutView = new VerifyCheckOutView(getContext(), null);


            final AlertDialog d = new AlertDialog.Builder(getContext())
                    .setTitle("Confirm Checkout")
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
                            //do nothing here, this will be overridden after dialog is shown
                            //this is a workaround so that we can choose whether or not to close
                            //+ dialog when the button is clicked
                        }
                    })
                    .create();
            d.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    final Button button = d.getButton(DialogInterface.BUTTON_POSITIVE);
                    updateButtonText(button, verifyCheckOutView);
                    final ObservableMarkdownView markdown = verifyCheckOutView.getMarkdownView();
                    //watch for scrolling in the webview so we can see if the entire license was displayed

                    markdown.setOnScrollChangeListener(new ObservableMarkdownView.OnScrollChangeListener() {
                        @Override
                        public void onScrollChange(WebView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                            updateButtonText(button, verifyCheckOutView);
                        }
                    });


                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //if the user has not viewed the entire EULA then scroll down

                            int textTotalHeight = verifyCheckOutView.getHeight();
                            int pageHeight = markdown.getHeight();
                            int scrollY = markdown.getScrollY();
                            if (scrollY < textTotalHeight - pageHeight) {// not touch the bottom
                                int newY = scrollY + pageHeight;
                                ObjectAnimator anim = ObjectAnimator.ofInt(markdown, "scrollY", scrollY, newY);
                                anim.addListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        updateButtonText(button, verifyCheckOutView);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                });
                                anim.setDuration(400).start();
                            } else {// touch the bottom, dismiss the dialog
                                checkoutAssetsToUser(user, true);
                                d.dismiss();
                            }
                        }
                    });
                }
            });
            d.show();
        }
    }

    private boolean modelCanBeCheckedOut(Asset asset) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean modelValid = false;

        boolean allModelsValid = prefs.getBoolean("check_out_all_models", true);
        if (allModelsValid) {
            modelValid = true;
        } else {
            Set<String> allowedModels = prefs.getStringSet("check_out_models", new HashSet<String>());
            if (allowedModels.contains(asset.getModel())) {
                modelValid = true;
            }
        }

        return modelValid;
    }

    private boolean assetStatusInvalid(Asset asset) {
        // TODO: 9/8/17 asset status should be broken into enum value
        boolean statusInvalid = true;
        String status = asset.getStatus();
        if (status != null) status = status.toUpperCase();
        if ("READY TO DEPLOY".equals(status)) {
            statusInvalid = false;
        }

        return statusInvalid;
    }

    private void addAssetToScannedList(Asset asset) throws AssetAlreadyCheckedOutException,
            AssetAlreadyScannedException {
        //if the asset is already in the list we can not add it again
        if (scanner.getScannedAssets().contains(asset)) {
            throw new AssetAlreadyScannedException("Asset " + asset.getTag() + " already scanned");
        }


        //if the asset is already checked out then it can not be scanned
        if (asset.getAssignedTo() == null || asset.getAssignedTo().equals("")) {
            scanner.addAsset(asset);

            //if this was the first asset scanned then transition the instructions
            if (scanner.getScannedAssets().size() == 1) {
                fadeInText(title, getResources().getString(R.string.check_out_title_scan_more_assets));
                fadeInText(message, getResources().getString(R.string.check_out_message_scan_user));
            }
        } else {
            throw new AssetAlreadyCheckedOutException("Asset " + asset.getTag() +
                    " is already checked out to '" + asset.getAssignedTo() + "'");
        }
    }

    private void checkoutAssetsToUser(User user, boolean isVerified) {
        Database db = Database.getInstance(getContext());
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
            scanner.clear();
            fadeInText(title, getResources().getString(R.string.check_out_title_scan_an_asset));
            fadeInText(message, getResources().getString(R.string.blank));

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
