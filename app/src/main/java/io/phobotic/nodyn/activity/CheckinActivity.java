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

package io.phobotic.nodyn.activity;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.fragment.CheckInFragment;
import io.phobotic.nodyn.fragment.CheckOutAuthorizationFragment;
import io.phobotic.nodyn.fragment.CheckOutFragment;
import io.phobotic.nodyn.fragment.listener.CheckInOutListener;

public class CheckinActivity extends AppCompatActivity implements CheckInOutListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        setContentView(R.layout.activity_checkin);

        if (savedInstanceState == null) {
            Fragment newFragment = CheckInFragment.newInstance(null);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.frame, newFragment).commit();
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected boolean isValidFragment(String fragmentName) {
        return CheckOutFragment.class.getName().equals(fragmentName)
                || CheckOutAuthorizationFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onCheckOutAuthorized(User authorizedUser) {

    }

    @Override
    public void onCheckOutFinished(View transitonView, String transitionName) {

    }

    @Override
    public void onCheckInFinished(View transitonView, String transitionName) {

    }
}