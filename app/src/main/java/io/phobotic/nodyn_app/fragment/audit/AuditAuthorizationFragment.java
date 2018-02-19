/*
 * Copyright (c) 2018 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.fragment.audit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.GroupNotFoundException;
import io.phobotic.nodyn_app.database.model.Group;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.view.BadgeScanView;
import io.phobotic.nodyn_app.view.UserCardView;

/**
 * Created by Jonathan Nelson on 1/17/18.
 */

public class AuditAuthorizationFragment extends Fragment {

    private static final String ALLOWED_GROUPS = "allowed_groups";
    private static final String TAG = AuditAuthorizationFragment.class.getSimpleName();
    private static final String ALLOW_ALL_USERS = "allow_all_users";

    private List<Integer> allowedGroups = new ArrayList<>();

    private AuditStatusListener listener;
    private View rootView;
    private FloatingActionButton fabGo;
    private TextView groups;
    private TextView authenticatedMessage;
    private View cardBox;
    private UserCardView card;
    private User authorizedUser;
    private FloatingActionButton deauthorizeButton;
    private Database db;
    private BadgeScanView badgeScanner;
    private View inputBox;
    private boolean allowAllUsers;

    public static AuditAuthorizationFragment newInstance(ArrayList<Integer> allowedGroups,
                                                         boolean allowAllUsers) {
        AuditAuthorizationFragment fragment = new AuditAuthorizationFragment();
        Bundle args = new Bundle();
        if (allowedGroups == null) {
            allowedGroups = new ArrayList<>();
        }

        args.putIntegerArrayList(ALLOWED_GROUPS, allowedGroups);
        args.putBoolean(ALLOW_ALL_USERS, allowAllUsers);
        fragment.setArguments(args);
        return fragment;
    }

    public AuditAuthorizationFragment() {
        // Required empty public constructor
    }

    public AuditAuthorizationFragment setListener(AuditStatusListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Audit Authorization");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = Database.getInstance(getContext());
        if (getArguments() != null) {
            allowAllUsers = getArguments().getBoolean(ALLOW_ALL_USERS);
            allowedGroups = getArguments().getIntegerArrayList(ALLOWED_GROUPS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_audit_authorization, container, false);
        init();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        badgeScanner.requestFocus();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    private void init() {
        cardBox = rootView.findViewById(R.id.card_box);
        cardBox.setVisibility(View.GONE);
        card = (UserCardView) rootView.findViewById(R.id.card);

        inputBox = rootView.findViewById(R.id.input_box);
        inputBox.setVisibility(View.VISIBLE);
        badgeScanner = (BadgeScanView) rootView.findViewById(R.id.badge_scanner);
        authenticatedMessage = (TextView) rootView.findViewById(R.id.authenticated_message);

        deauthorizeButton = (FloatingActionButton) rootView.findViewById(R.id.unauthorize_button);
        deauthorizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authorizedUser = null;
                Animation bottomDown = AnimationUtils.loadAnimation(getContext(),
                        R.anim.bottom_down);
                final Animation bottomUp = AnimationUtils.loadAnimation(getContext(),
                        R.anim.bottom_up);
                fabGo.hide();
                bottomDown.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        cardBox.setVisibility(View.GONE);
                        inputBox.setVisibility(View.VISIBLE);
                        inputBox.startAnimation(bottomUp);
                        bottomUp.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                badgeScanner.requestFocus();
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                cardBox.startAnimation(bottomDown);


            }
        });

        initFabs();
        initGroupList();
        initInput();
    }

    private void initFabs() {
        fabGo = (FloatingActionButton) rootView.findViewById(R.id.fab_go);

        fabGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAuditAuthorized(authorizedUser);
                }
            }
        });

        fabGo.hide();
    }

    private void initGroupList() {
        groups = (TextView) rootView.findViewById(R.id.groups);

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        String prefix = "";
        for (Integer groupID : allowedGroups) {
            try {
                Group g = db.findGroupByID(groupID);
                sb.append(prefix + g.getName());
                prefix = ", ";
            } catch (GroupNotFoundException e) {
                Log.d(TAG, "Unable to find group with ID '" + groupID + "', skipping");
            }
        }
        sb.append("]");
        groups.setText(sb.toString());
    }

    private void initInput() {
        badgeScanner.setOnUserScannedListener(new BadgeScanView.OnUserScannedListener() {
            @Override
            public void onUserScanned(User user) {
                processUserScan(user);
            }

            @Override
            public void onUserScanError(String message) {
                //let the BadgeScanView handle the errors
            }
        });
    }

    private void processUserScan(final User user) {
        badgeScanner.reset();

        //if the user is a member of the allowed groups then proceede
        int[] groups = user.getGroupsIDs();
        if (groups == null) {
            groups = new int[]{};
        }

        boolean userAuthenticated = allowAllUsers;
        for (int groupID : groups) {
            List<Integer> unionGroups = new ArrayList<>();
            unionGroups.addAll(allowedGroups);
            List<Integer> userGroups = new ArrayList<>();
            for (int i : groups) {
                userGroups.add(i);
            }

            unionGroups.retainAll(userGroups);
            if (unionGroups.size() > 0) {
                userAuthenticated = true;
                break;
            }
        }

        if (!userAuthenticated) {
            String error = getString(R.string.audit_not_uthorized_message, user.getName());
            showError(error);
        } else {
            Resources res = getResources();
            String message = res.getString(R.string.audit_authorized_message, user.getName());
            authenticatedMessage.setText(message);
            Animation bottomDown = AnimationUtils.loadAnimation(getContext(),
                    R.anim.bottom_down);
            inputBox.startAnimation(bottomDown);
            bottomDown.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    inputBox.setVisibility(View.GONE);

                    Animation bottomUp = AnimationUtils.loadAnimation(getContext(),
                            R.anim.bottom_up);
                    bottomUp.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            fabGo.setEnabled(true);
                            fabGo.show();
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    authorizedUser = user;

                    card.setUser(user);
                    card.setVisibility(View.VISIBLE);
                    cardBox.startAnimation(bottomUp);
                    cardBox.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });


        }
    }

    private void showError(String errMsg) {
        AlertDialog a = new AlertDialog.Builder(getContext())
                .setTitle("Errror")
                .setMessage(errMsg)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                })
                .create();
        a.show();
    }
}

