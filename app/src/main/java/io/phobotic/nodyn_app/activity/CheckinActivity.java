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

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.fragment.CheckInFragment;
import io.phobotic.nodyn_app.fragment.CheckOutFragment;
import io.phobotic.nodyn_app.fragment.UserAuthorizationFragment;
import io.phobotic.nodyn_app.fragment.listener.CheckInOutListener;
import io.phobotic.nodyn_app.view.VerifyCheckinView;

public class CheckinActivity extends AppCompatActivity implements CheckInOutListener, UserAuthorizationFragment.OnUserAuthorizedListener {
    private static final String TAG = CheckinActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);
        setupActionBar();

        if (savedInstanceState == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            boolean authorizationRequired = prefs.getBoolean(
                    getString(R.string.pref_key_check_in_require_scan), Boolean.parseBoolean(
                            getString(R.string.pref_default_check_in_require_scan)));

            Fragment newFragment;
            if (authorizationRequired) {
                Set<String> groupSet = prefs.getStringSet(getResources()
                        .getString(R.string.pref_key_check_in_authenticating_groups), new HashSet<String>());
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
                        UserAuthorizationFragment.Role.CHECK_IN, groupIDList, false);
                ((UserAuthorizationFragment) newFragment).setListener(this);
            } else {
                //if user authentication is not required then go ahead and load the check-in fragment
                newFragment = CheckInFragment.newInstance(null, false);
                ((CheckInFragment) newFragment).setListener(this);
            }

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.frame, newFragment).commit();
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
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
        //if the authorized use is required to verify asset returns we need to show that dialog box now
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean verificationRequired = prefs.getBoolean(getString(R.string.pref_key_check_in_show_verify),
                Boolean.parseBoolean(getString(R.string.pref_default_check_in_show_verify)));
        if (verificationRequired) {
            verifyAssetCheckin(authorizedUser);
        } else {
            loadCheckInFragment(authorizedUser, false);
        }
    }

    private void verifyAssetCheckin(final User user) {
        View markdownView = new VerifyCheckinView(this, null);
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Checkin Guidelines")
                .setView(markdownView)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing to do here
                    }
                })
                .setPositiveButton("I Agree", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadCheckInFragment(user, true);
                    }
                })
                .create();
        d.show();
    }

    public void loadCheckInFragment(@Nullable User authorizedUser, boolean verified) {
        CheckInFragment fragment = CheckInFragment.newInstance(authorizedUser, verified);
        fragment.setListener(this);

        loadFragment(fragment);
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment curFragment = fm.findFragmentById(R.id.frame);

        FragmentTransaction ft = fm.beginTransaction();

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

    }

    @Override
    public void onCheckInFinished(View transitonView, String transitionName) {

    }
}
