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
import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.GroupNotFoundException;
import io.phobotic.nodyn_app.database.model.Group;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.helper.ColorHelper;
import io.phobotic.nodyn_app.helper.MediaHelper;
import io.phobotic.nodyn_app.view.BadgeScanView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserAuthorizationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserAuthorizationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserAuthorizationFragment extends Fragment {
    private static final String TAG = UserAuthorizationFragment.class.getSimpleName();
    private static final String ARG_ALLOWED_GROUPS = "allowed_groups";
    private static final String ARG_ROLE = "role";
    private static final String ARG_ALLOW_ALL_USERS = "allow_all_users";

    private List<Integer> allowedGroups = new ArrayList<>();

    private OnUserAuthorizedListener listener;
    private View rootView;
    private ExtendedFloatingActionButton nextButton;
    private TextView groups;
    private User authorizedUser;
    private Button deauthorizeButton;
    private Database db;
    private BadgeScanView badgeScanner;
    private View inputBox;
    private Role role;
    private TextView error;
    private TextView message;
    private ViewSwitcher.ViewFactory warningFactory;
    private ViewSwitcher.ViewFactory normalFactory;
    @ColorInt
    private int normalTextColor;
    @ColorInt
    private int errorTextColor;
    private TextView title;
    private boolean allowAllUsers;

    public static UserAuthorizationFragment newInstance(Role role, ArrayList<Integer> allowedGroups,
                                                        boolean allowAllUsers) {

        UserAuthorizationFragment fragment = new UserAuthorizationFragment();
        Bundle args = new Bundle();
        if (allowedGroups == null) {
            allowedGroups = new ArrayList<>();
        }

        if (role == null) {
            throw new IllegalArgumentException("Role can not be null");
        }

        args.putSerializable(ARG_ROLE, role);
        args.putIntegerArrayList(ARG_ALLOWED_GROUPS, allowedGroups);
        args.putBoolean(ARG_ALLOW_ALL_USERS, allowAllUsers);
        fragment.setArguments(args);
        return fragment;
    }

    public UserAuthorizationFragment() {
        // Required empty public constructor
    }

    public UserAuthorizationFragment setListener(OnUserAuthorizedListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Authorization Required");

            if (role != null) {
                switch (role) {
                    case CHECK_IN:
                        actionBar.setTitle("Check In Authorization");
                        break;
                    case CHECK_OUT:
                        actionBar.setTitle("Check Out Authorization");
                        break;
                    case AUDIT:
                        actionBar.setTitle("Asset Audit Authorization");
                        break;

                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = Database.getInstance(getContext());
        if (getArguments() != null) {
            allowedGroups = getArguments().getIntegerArrayList(ARG_ALLOWED_GROUPS);
            role = (Role) getArguments().getSerializable(ARG_ROLE);
            allowAllUsers = getArguments().getBoolean(ARG_ALLOW_ALL_USERS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_user_authorization, container, false);

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
        title = rootView.findViewById(R.id.title);


        error = rootView.findViewById(R.id.error);
        error.setVisibility(View.GONE);

        inputBox = rootView.findViewById(R.id.input_box);
        inputBox.setVisibility(View.VISIBLE);
        badgeScanner = rootView.findViewById(R.id.badge_scanner);

        message = rootView.findViewById(R.id.message);
        message.setVisibility(View.VISIBLE);

        normalTextColor = message.getCurrentTextColor();

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        errorTextColor = typedValue.data;


        initTextViews();
        initButtons();
        initGroupList();
        initInput();
    }

    private void initTextViews() {
        String title = null;
        switch (role) {
            case CHECK_OUT:
                title = getString(R.string.user_authorization_title_check_outs);
                break;
            case CHECK_IN:
                title = getString(R.string.user_authorization_title_check_ins);
                break;
            case AUDIT:
                title = getString(R.string.user_authorization_title_audit);
                break;
        }

        this.title.setText(title);
    }


    private void initButtons() {
        nextButton = rootView.findViewById(R.id.fab_go);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onUserAuthorized(authorizedUser);
                }
            }
        });

        nextButton.setEnabled(false);


        deauthorizeButton = rootView.findViewById(R.id.unauthorize_button);
        deauthorizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reset the fragment view state to show a blank card without the bottom area
                authorizedUser = null;
                nextButton.setEnabled(false);
                AnimationHelper.scaleOut(nextButton);

                deauthorizeButton.setEnabled(false);
                AnimationHelper.scaleOut(deauthorizeButton);

                AnimationHelper.expandAndFadeIn(getContext(), message);
                AnimationHelper.collapse(error);


                //handle the badge scanner reset last so that input focus returns to the scanner
                badgeScanner.reset();
            }
        });
    }

    private void initGroupList() {
        groups = rootView.findViewById(R.id.groups);

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
            public void onUserScanError(String msg) {
                badgeScanner.reset();
                showError(msg);
            }

            @Override
            public void onInputBegin() {
                hideError();
            }
        });
    }

    private void processUserScan(final User user) {
        if (authorizedUser != null) {
            badgeScanner.reset();
        }

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

        Resources res = getResources();

        if (!userAuthenticated) {
            String err = res.getString(R.string.user_authorization_error_authorize_failed, user.getName(), role.noun);
            showError(err);
        } else {
            authorizedUser = user;
            showAuthorizedUser(user);
        }
    }

    private void showError(String errMsg) {
        if (error != null) {
            error.setTextColor(errorTextColor);
            error.setText(errMsg);

            AnimationHelper.expandAndFadeIn(getContext(), error);
            AnimationHelper.expandAndFadeIn(getContext(), message);

            MediaHelper.playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
        }
    }

    private void hideError() {
        if (error != null) {
            AnimationHelper.collapseAndFadeOut(getContext(), error);
        }
    }

    private void showAuthorizedUser(User user) {
        final Resources res = getResources();
        //hide the message saying 'scan badge now' and any previous error
        AnimationHelper.collapse(message);
        AnimationHelper.collapse(error);

        MediaHelper.playSoundEffect(getContext(), R.raw.n_audioman__blip);
        String message = null;
        switch (role) {
            case CHECK_OUT:
                message = res.getString(R.string.user_authorization_message_check_out, user.getName());
                break;
            case CHECK_IN:
                message = res.getString(R.string.user_authorization_message_check_in, user.getName());
                break;
            case AUDIT:
                message = res.getString(R.string.user_authorization_message_audit, user.getName());
                break;
        }

        nextButton.setEnabled(true);
        AnimationHelper.scaleIn(nextButton);

        deauthorizeButton.setEnabled(true);
        AnimationHelper.scaleIn(deauthorizeButton);

        badgeScanner.disableInput();
    }

    public enum Role {
        CHECK_IN("check in", "check-in"),
        CHECK_OUT("check out", "check-out"),
        AUDIT("audit", "audit");

        private final String verb;
        private final String noun;

        Role(String verb, String noun) {
            this.verb = verb;
            this.noun = noun;
        }
    }

    public interface OnUserAuthorizedListener {
        void onUserAuthorized(User authorizedUser);
    }
}
