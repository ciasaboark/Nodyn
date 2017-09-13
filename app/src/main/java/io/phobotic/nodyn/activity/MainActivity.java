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


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import io.fabric.sdk.android.Fabric;
import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.fragment.ActionsListFragment;
import io.phobotic.nodyn.fragment.AssetListFragment;
import io.phobotic.nodyn.fragment.BackendErrorFragment;
import io.phobotic.nodyn.fragment.CheckInOutChooserFragment;
import io.phobotic.nodyn.fragment.UserListFragment;
import io.phobotic.nodyn.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn.schedule.SyncScheduler;
import io.phobotic.nodyn.service.SyncService;
import io.phobotic.nodyn.sync.SyncManager;
import io.phobotic.nodyn.sync.adapter.SyncAdapter;
import io.phobotic.nodyn.sync.adapter.dummy.DummyAdapter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnListFragmentInteractionListener {
    public static final String BROADCAST_SCANNER_CONNECTED = "scanner connected";
    public static final String BROADCAST_SCANNER_DISCONNECTED = "scanner disconnected";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String MAIN_FRAGMENT = "mainFragment";
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_users, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_assets, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notification, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_sync_snipeit_3, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_check_in, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_check_out, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_email, false);

        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final View v = findViewById(R.id.drawer_layout);

        FrameLayout frame = (FrameLayout) findViewById(R.id.frame);

        Fragment newFragment = null;

        if (savedInstanceState == null) {
            //use the default fragment if we did not have a previous one (i.e. app first start)
            newFragment = getDefaultMainFragment();
        } else {
            //if we already had a fragment loaded then use that
            currentFragment = getSupportFragmentManager().getFragment(savedInstanceState, MAIN_FRAGMENT);
            newFragment = currentFragment;

            //unless it is the sync adapter error fragment
            if (newFragment instanceof BackendErrorFragment) {
                newFragment = getDefaultMainFragment();
            }
        }

        //override the new fragment if we need to show the sync adapter error
        if (shouldShowAdapterError()) {
            newFragment = BackendErrorFragment.newInstance();
        }

        updateMainFragment(newFragment);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "configuration changed");
        int keyboard = getResources().getConfiguration().keyboard;
        Log.d(TAG, "keyboard int value now: " + keyboard);

        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Intent i = new Intent(BROADCAST_SCANNER_CONNECTED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
            Toast.makeText(this, "hardware keyboard available (val " + keyboard + ")", Toast.LENGTH_SHORT).show();
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            Intent i = new Intent(BROADCAST_SCANNER_DISCONNECTED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
            Toast.makeText(this, "hardware keyboard disconnected (val " + keyboard + ")", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        SyncScheduler scheduler = new SyncScheduler(this);
        scheduler.scheduleSyncIfNeeded();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Fragment curFragment = getSupportFragmentManager().findFragmentById(R.id.frame);
        //Save the fragment's instance
        if (curFragment == null) {
            Log.e(TAG, "Main activity current fragment is null.  Unable to store current fragment");
        } else {
            getSupportFragmentManager().putFragment(outState, MAIN_FRAGMENT, curFragment);
        }
    }

    private Fragment getDefaultMainFragment() {
        return CheckInOutChooserFragment.newInstance();
    }

    /**
     * If no sync backend has been selected then an error should be displayed
     *
     * @return true if the chosen {@link SyncAdapter} is an instance of the default
     * {@link DummyAdapter}
     */
    private boolean shouldShowAdapterError() {
        boolean showAdapterError = false;

        SyncAdapter syncAdapter = SyncManager.getPrefferedSyncAdapter(this);
        if (syncAdapter instanceof DummyAdapter) {
            showAdapterError = true;
        }

        return showAdapterError;
    }

    private void updateMainFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (currentFragment == null) {
            ft.add(R.id.frame, fragment).commit();
        } else {
            ft.replace(R.id.frame, fragment).commit();
        }

        currentFragment = fragment;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sync) {
            Log.d(TAG, "Sync button clicked, starting fetchFullModel process in background");
            Intent i = new Intent(MainActivity.this, SyncService.class);
            startService(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //disable drawer icons for assets and users until a sync backend is selected
        setDrawerIconsState();

        //if the user is returning to the main activity after setting up the sync backend
        //+ then show the default main fragment
        if (currentFragment instanceof BackendErrorFragment && !shouldShowAdapterError()) {
            Fragment defaultFragment = getDefaultMainFragment();
            updateMainFragment(defaultFragment);
        } else {
            showSyncAdapterErrorIfNeeded();
        }
    }

    /**
     * Adjust the enabled/disabled state of the navigation drawer items for assets and users
     */
    private void setDrawerIconsState() {
        NavigationView nav = (NavigationView) findViewById(R.id.nav_view);
        boolean itemEnabled = getMenuIconsState();
        Menu menu = nav.getMenu();
        MenuItem assetsItem = menu.findItem(R.id.nav_assets);
        MenuItem usersItem = menu.findItem(R.id.nav_users);

        setMenuOptionState(assetsItem, itemEnabled);
        setMenuOptionState(usersItem, itemEnabled);
    }

    private void showSyncAdapterErrorIfNeeded() {
        if (shouldShowAdapterError()) {
            BackendErrorFragment fragment = BackendErrorFragment.newInstance();
            updateMainFragment(fragment);
        }
    }

    /**
     * Get the desired enabled/disabled state of the navigation items for assets and users
     *
     * @return false if the navigation items should be disabled, true if they should be enabled
     */
    private boolean getMenuIconsState() {
        return !shouldShowAdapterError();
    }

    private void setMenuOptionState(@NotNull MenuItem item, boolean enabled) {
        item.setEnabled(enabled);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        boolean highlightItem = false;
        Fragment newFragment = null;

        if (id == R.id.nav_check_in_out) {
            newFragment = CheckInOutChooserFragment.newInstance();
            highlightItem = true;
        } else if (id == R.id.nav_assets) {
            newFragment = AssetListFragment.newInstance(1);
            highlightItem = true;
        } else if (id == R.id.nav_users) {
            newFragment = UserListFragment.newInstance(1);
            highlightItem = true;
        } else if (id == R.id.nav_history) {
            newFragment = ActionsListFragment.newInstance(1);
            highlightItem = true;
        } else if (id == R.id.nav_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            highlightItem = false;
        } else if (id == R.id.nav_about) {
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
            highlightItem = false;
        } else if (id == R.id.nav_send) {
            // TODO: 7/15/17
        }

        if (newFragment != null) {
            updateMainFragment(newFragment);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return highlightItem;
    }

    @Override
    public void onListFragmentInteraction(User user, @Nullable Pair<View, String>... sharedElements) {
        Intent i = new Intent(this, UserDetailsActivity.class);
        i.putExtra(UserDetailsActivity.BUNDLE_USER, user);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                sharedElements);

        startActivity(i, options.toBundle());
    }

    @Override
    public void onListFragmentInteraction(Asset asset, @Nullable Pair<View, String>... sharedElements) {
        //make sharedElements null safe
        int size = 0;
        for (Pair p : sharedElements) {
            if (p != null) {
                size++;
            }
        }
        Pair<View, String>[] safePairs = new Pair[size];
        int index = 0;
        for (Pair p : sharedElements) {
            if (p != null) {
                safePairs[index] = p;
                index++;
            }
        }

        Intent i = new Intent(this, AssetDetailsActivity.class);
        i.putExtra(AssetDetailsActivity.BUNDLE_ASSET, asset);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this,
                safePairs);
        startActivity(i, options.toBundle());
    }
}
