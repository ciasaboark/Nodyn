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

package io.phobotic.nodyn_app.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.fragment.CheckOutFragment;
import io.phobotic.nodyn_app.fragment.UserAuthorizationFragment;
import io.phobotic.nodyn_app.fragment.listener.CheckInOutListener;
import io.phobotic.nodyn_app.service.SyncService;

public class CheckoutActivity extends AppCompatActivity implements CheckInOutListener, UserAuthorizationFragment.OnUserAuthorizedListener {
    private static final String TAG = CheckoutActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);
        setupActionBar();

        if (savedInstanceState == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean authorizationRequired = prefs.getBoolean(
                    getString(R.string.pref_key_check_out_require_authorization), Boolean.parseBoolean(
                            getString(R.string.pref_default_check_out_require_authorization)));

            Fragment newFragment;
            if (authorizationRequired) {
                Set<String> groupSet = prefs.getStringSet(getResources()
                        .getString(R.string.pref_key_check_out_authorization_groups), new HashSet<String>());
                ArrayList<Integer> groupIDList = new ArrayList<>();
                for (String s : groupSet) {
                    try {
                        int i = Integer.parseInt(s);
                        groupIDList.add(i);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Unable to parse selected group ID '" + s + "' as integer value, skipping");
                    }
                }
                newFragment = UserAuthorizationFragment.newInstance(
                        UserAuthorizationFragment.Role.CHECK_OUT, groupIDList, false)
                        .setListener(this);

            } else {
                newFragment = CheckOutFragment.newInstance(null);
                ((CheckOutFragment) newFragment).setListener(this);
            }

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.frame, newFragment).commit();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //go ahead a schedule a quick sync so any checkout records can be pushed out as soon as possible
        Intent i = new Intent(this, SyncService.class);
        i.putExtra(SyncService.SYNC_TYPE_KEY, SyncService.SYNC_TYPE_FULL);
        startService(i);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setTitle(R.string.title_activity_check_out);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected boolean isValidFragment(String fragmentName) {
        return CheckOutFragment.class.getName().equals(fragmentName)
                || UserAuthorizationFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onUserAuthorized(User authorizedUser) {
        loadCheckOutFragment(authorizedUser);
    }

    public void loadCheckOutFragment(@Nullable User authorizedUser) {
        CheckOutFragment fragment = CheckOutFragment.newInstance(authorizedUser);
        fragment.setListener(this);

        loadFragment(fragment);
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment curFragment = fm.findFragmentById(R.id.frame);

        FragmentTransaction ft = fm.beginTransaction();

//        ft.setCustomAnimations(android.R.anim.fade_out,
//                android.R.anim.fade_out);

        if (curFragment == null) {
            ft.add(R.id.frame, fragment);
        } else {
            ft.replace(R.id.frame, fragment);
            ft.addToBackStack(null);
        }

        ft.commit();
    }

    @Override
    public void onCheckOutFinished(View transitonView, String transitionName) {
        onBackPressed();
    }

    @Override
    public void onCheckInFinished(View transitonView, String transitionName) {

    }
}
