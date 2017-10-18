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


import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
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
import android.support.v7.app.AlertDialog;
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
import io.phobotic.nodyn.BuildConfig;
import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.fragment.ActionsListFragment;
import io.phobotic.nodyn.fragment.AssetListFragment;
import io.phobotic.nodyn.fragment.BackendErrorFragment;
import io.phobotic.nodyn.fragment.DashboardFragment;
import io.phobotic.nodyn.fragment.UserListFragment;
import io.phobotic.nodyn.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn.schedule.SyncScheduler;
import io.phobotic.nodyn.service.SyncService;
import io.phobotic.nodyn.sync.SyncManager;
import io.phobotic.nodyn.sync.adapter.SyncAdapter;
import io.phobotic.nodyn.sync.adapter.dummy.DummyAdapter;
import io.phobotic.nodyn.view.KioskPasswordView;

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
        PreferenceManager.setDefaultValues(this, R.xml.pref_sync_snipeit_3, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_check_in, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_check_out, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_email, false);

        super.onCreate(savedInstanceState);

        //start crashlytics only if not a debug build
        Crashlytics crashlytics = new Crashlytics.Builder().disabled(BuildConfig.DEBUG).build();
        Fabric.with(this, crashlytics);

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
        Log.e(TAG, "configuration changed");
        int keyboard = newConfig.keyboard;
        int keyboardHidden = newConfig.keyboardHidden;
        int hardKeyboardHidden = newConfig.hardKeyboardHidden;
        String message = "keyboard [: " + keyboard + "], keyboardHidden [" + keyboardHidden +
                "] hardKeyboardHidden [" + hardKeyboardHidden + "]";
        Log.e(TAG, message);

        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Intent i = new Intent(BROADCAST_SCANNER_CONNECTED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
            Toast.makeText(this, "Barcode scanner attached", Toast.LENGTH_SHORT).show();
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            Intent i = new Intent(BROADCAST_SCANNER_DISCONNECTED);
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
            Toast.makeText(this, "Barcode scanner disconnected", Toast.LENGTH_SHORT).show();
        }

        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        //// TODO: 10/4/17 replace with dash fragment once created
        return DashboardFragment.newInstance();
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

        SyncScheduler scheduler = new SyncScheduler(this);
        scheduler.scheduleSyncIfNeeded();
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

        if (id == R.id.nav_dash) {
            newFragment = DashboardFragment.newInstance();
            highlightItem = true;
        } else if (id == R.id.nav_check_in) {
            Intent i = new Intent(this, CheckinActivity.class);
            startActivity(i);
            highlightItem = false;
        } else if (id == R.id.nav_check_out) {
            Intent i = new Intent(this, CheckoutActivity.class);
            startActivity(i);
            highlightItem = false;
        } else if (id == R.id.nav_assets) {
            newFragment = AssetListFragment.newInstance(1);
            highlightItem = true;
        } else if (id == R.id.nav_users) {
            newFragment = UserListFragment.newInstance(1);
            highlightItem = true;
        } else if (id == R.id.nav_history) {
            newFragment = ActionsListFragment.newInstance(1, -1);
            highlightItem = true;
        } else if (id == R.id.nav_settings) {
            loadKioskSettings();
            //setttings starts in a separate fragment, so don't highlight this option
            highlightItem = false;
        } else if (id == R.id.nav_about) {
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
            highlightItem = false;
        } else if (id == R.id.nav_share) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            StringBuilder sb = new StringBuilder();
            sb.append("Check out Nodyn\n\n");
            sb.append("https://play.google.com/store/apps/details?id=io.phobotic.nodyn");
            shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out Nodyn");
            Uri imageUri = Uri.parse("android.resource://io.phobotic.nodyn/drawable/" + R.drawable.app_icon_64);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/png");

            startActivity(Intent.createChooser(shareIntent, "Foobar"));


//            sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
//
//            startActivity(sendIntent);
        }

        if (newFragment != null) {
            updateMainFragment(newFragment);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return highlightItem;
    }

    private void loadKioskSettings() {
        boolean kioskMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                getString(R.string.pref_key_general_kiosk), Boolean.parseBoolean(
                        getString(R.string.pref_default_general_kiosk)));

        if (kioskMode) {
            final KioskPasswordView kioskView = new KioskPasswordView(this);

            AlertDialog d = new AlertDialog.Builder(this)
                    .setTitle("Enter kiosk password")
                    .setView(kioskView)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String passwordInput = kioskView.getPassword();
                            if (passwordInput == null) passwordInput = "";

                            String storedPassword = PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                                    .getString(getString(R.string.pref_key_general_kiosk_password), null);

                            if (passwordInput.equals(storedPassword)) {
                                loadSettingsActivity();
                            } else {
                                showKioskPasswordMismatchDialog();
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO: 10/1/17 disable kiosk mode
                        }
                    })
                    .create();
            d.show();

//            final Button positiveButton = d.getButton(DialogInterface.BUTTON_POSITIVE);
//            positiveButton.setEnabled(false);
//
//            kioskView.setOnPasswordChangedListener(new KioskPasswordView.OnPasswordChangedListener() {
//                @Override
//                public void onPasswordChanged(String newPassword) {
//                    if (newPassword != null && newPassword.length() > 0) {
//                        positiveButton.setEnabled(true);
//                    }
//                }
//            });

        } else {
            loadSettingsActivity();
        }

    }

    private void loadSettingsActivity() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }

    private void showKioskPasswordMismatchDialog() {
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("Password Mismatch")
                .setMessage("Sorry, the password you entered is not correct")
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing to do here
                    }
                })
                .create();
        d.show();
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
