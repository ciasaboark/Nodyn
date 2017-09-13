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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.fragment.listener.CheckInOutListener;
import io.phobotic.nodyn.transition.DetailsTransition;


public class CheckInOutChooserFragment extends Fragment implements CheckInOutListener {
    private CheckInOutListener listener;
    private View rootView;
    private View checkInRoot;
    private View checkOutRoot;
    private TextView checkInButton;
    private TextView checkOutButton;

    public CheckInOutChooserFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_check_in_out_chooser, container, false);
        init();

        return rootView;
    }

    private void init() {
        checkInButton = (TextView) rootView.findViewById(R.id.button_in);
        checkInRoot = rootView.findViewById(R.id.check_in_root);
        checkOutRoot = rootView.findViewById(R.id.check_out_root);
        checkInRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCheckIn();
            }
        });

        checkOutButton = (TextView) rootView.findViewById(R.id.button_out);
        checkOutRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCheckOut();
            }
        });
    }

    public void startCheckIn() {
        CheckInFragment fragment = CheckInFragment.newInstance(null);
        fragment.setListener(this);

        loadFragment(fragment);
    }

    public void startCheckOut() {
        //if asset check out requires authentication
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean requireScan = prefs.getBoolean(getResources()
                .getString(R.string.pref_key_check_out_require_scan), false);
        Fragment fragment = null;
        if (requireScan) {
            loadCheckOutAuthorizationFragment();
        } else {
            loadCheckOutFragment(null);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        Fragment curFragment = fm.findFragmentById(R.id.frame);
        fragment.setSharedElementEnterTransition(new DetailsTransition());
//        fragment.setEnterTransition(new Fade());
        fragment.setSharedElementReturnTransition(new DetailsTransition());

        FragmentTransaction ft = fm.beginTransaction();
        ft.addSharedElement(checkOutRoot, "check_out_root");
        ft.addSharedElement(checkOutButton, "check_out_button");

        ft.addSharedElement(checkInRoot, "check_in_root");
        ft.addSharedElement(checkInButton, "check_in_button");

        if (curFragment == null) {
            ft.add(R.id.frame, fragment);
        } else {
            ft.replace(R.id.frame, fragment);
            ft.addToBackStack(null);
        }

        ft.commit();
    }

    public void loadCheckOutAuthorizationFragment() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> groupSet = prefs.getStringSet(getResources()
                .getString(R.string.pref_key_check_out_scan_groups), new HashSet<String>());
        ArrayList<String> groupList = new ArrayList<>();
        groupList.addAll(groupSet);

        CheckOutAuthenticatorFragment fragment = CheckOutAuthenticatorFragment.newInstance(groupList);
        fragment.setListener(this);

        loadFragment(fragment);
    }

    public void loadCheckOutFragment(@Nullable User authorizedUser) {
        CheckOutFragment fragment = CheckOutFragment.newInstance(authorizedUser);
        fragment.setListener(this);

        loadFragment(fragment);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onCheckOutAuthorized(User authorizedUser) {
        loadCheckOutFragment(authorizedUser);
    }

    @Override
    public void onCheckOutFinished(View transitonView, String transitionName) {
        CheckInOutChooserFragment fragment = CheckInOutChooserFragment.newInstance();
        fragment.setListener(this);
        replaceFragment(fragment, transitonView, transitionName);
    }

    public static CheckInOutChooserFragment newInstance() {
        CheckInOutChooserFragment fragment = new CheckInOutChooserFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void setListener(@Nullable CheckInOutListener listener) {
        this.listener = listener;
    }

    private void replaceFragment(Fragment fragment, View transitionView, String transitionName) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        Fragment curFragment = fm.findFragmentById(R.id.frame);
        fragment.setSharedElementEnterTransition(new DetailsTransition());
        fragment.setEnterTransition(new DetailsTransition());
        fragment.setSharedElementReturnTransition(new DetailsTransition());

        FragmentTransaction ft = fm.beginTransaction();
        ft.addSharedElement(transitionView, transitionName);

        if (curFragment == null) {
            ft.add(R.id.frame, fragment);
        } else {
            ft.replace(R.id.frame, fragment);
            ft.addToBackStack(null);
        }

        ft.commit();
    }

    @Override
    public void onCheckInFinished(View transitonView, String transitionName) {
        CheckInOutChooserFragment fragment = CheckInOutChooserFragment.newInstance();
        fragment.setListener(this);
        replaceFragment(fragment, transitonView, transitionName);
    }
}
