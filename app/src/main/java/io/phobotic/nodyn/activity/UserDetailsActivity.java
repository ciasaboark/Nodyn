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
import android.widget.FrameLayout;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.fragment.UserDetailsFragment;

public class UserDetailsActivity extends AppCompatActivity {

    public static final String BUNDLE_USER = "user";
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        ActionBar actionBar = getSupportActionBar();
//        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        user = (User) getIntent().getSerializableExtra(BUNDLE_USER);

        FrameLayout frame = (FrameLayout) findViewById(R.id.frame);

        if (savedInstanceState == null) {
            Fragment newFragment = UserDetailsFragment.newInstance(user);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.frame, newFragment).commit();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
