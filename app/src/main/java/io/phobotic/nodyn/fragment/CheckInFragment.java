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
import android.widget.Button;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.fragment.listener.CheckInOutListener;
import io.phobotic.nodyn.view.AssetScanList;
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
    private User authorizingUser;
    private boolean authorizationRequired;
    private boolean verificationRequired;

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
        init();

        return rootView;
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
        Button endButton = (Button) rootView.findViewById(R.id.button_end);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 8/23/17 show verifications/warnings
                getActivity().onBackPressed();
//                listener.onCheckOutFinished(rootView, "check_in_root");
            }
        });
        scanner = (AssetScanList) rootView.findViewById(R.id.scan_list);
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        authorizationRequired = prefs.getBoolean("check_in_require_scan", false);
        verificationRequired = prefs.getBoolean("check_in_show_verify", false);

        //if authorization is not required then there is no need to be able to remove assets once
        //+ they are scanned
        scanner.setAssetsRemovable(authorizationRequired);

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
            //if at least one asset has been scanned, the checkout the items to the associate
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
                verifyAuthorization(user);
            }
        }
    }

    private void verifyAuthorization(final User user) {
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
        return true;
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
                scanner.clear();
            } else {
                //mark assets in the list as checked in.  this is to prevent the assets from being
                //+ checked in again if the list was not cleared
                for (Asset asset : scannedAssetsList) {
                    asset.setAssignedTo(null);
                }
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
            if (asset.getAssignedTo() != null && asset.getAssignedTo().length() > 0) {
                newList.add(asset);
            }
        }

        return newList;
    }

    private void showNotification(String err) {
        Snackbar snackbar = Snackbar.make(rootView, err, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void processAssetScan(Asset asset) {
        try {
            addAssetToScannedList(asset);

            //if authorization is not required then go ahead and check the asset back in
            if (!authorizationRequired) {
                checkinAssets(null, false);
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

    private void addAssetToScannedList(Asset asset) throws AssetAlreadyScannedException,
            AssetNotCheckedOutException {
        //if the asset is already in the list we can not add it again
        if (scanner.getScannedAssets().contains(asset)) {
            throw new AssetAlreadyScannedException("Asset " + asset.getTag() + " already scanned");
        }


        //if the asset is already checked out then it can not be scanned
        if (asset.getAssignedTo() == null || asset.getAssignedTo().equals("")) {
            throw new AssetNotCheckedOutException("Asset " + asset.getTag() + " is not checked out and can not be checked in");
        } else {
            scanner.addAsset(asset);
        }
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
