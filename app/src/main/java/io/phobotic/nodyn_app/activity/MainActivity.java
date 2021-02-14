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


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.helper.StringUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Manufacturer;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.database.sync.SyncAttempt;
import io.phobotic.nodyn_app.database.sync.SyncDatabase;
import io.phobotic.nodyn_app.fragment.ActionHistoryFragment;
import io.phobotic.nodyn_app.fragment.BackendErrorFragment;
import io.phobotic.nodyn_app.fragment.DashboardFragment;
import io.phobotic.nodyn_app.fragment.FirstSyncErrorFragment;
import io.phobotic.nodyn_app.fragment.asset.AssetListFragment;
import io.phobotic.nodyn_app.fragment.dash.LastSyncFragment;
import io.phobotic.nodyn_app.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn_app.fragment.user.UserListFragment;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.helper.SettingsHelper;
import io.phobotic.nodyn_app.schedule.SyncScheduler;
import io.phobotic.nodyn_app.service.StatisticsService;
import io.phobotic.nodyn_app.service.SyncService;
import io.phobotic.nodyn_app.sync.SyncManager;
import io.phobotic.nodyn_app.sync.adapter.SyncAdapter;
import io.phobotic.nodyn_app.sync.adapter.dummy.DummyAdapter;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnListFragmentInteractionListener, OnSetupCompleteListener {
    public static final String BROADCAST_SCANNER_CONNECTED = "scanner connected";
    public static final String BROADCAST_SCANNER_DISCONNECTED = "scanner disconnected";
    public static final String SYNC_NOW = "sync now";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String MAIN_FRAGMENT = "mainFragment";
    private Fragment currentFragment;
    private ImageButton syncIcon;
    private BroadcastReceiver br;
    private ProgressBar syncProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        this.syncProgress = findViewById(R.id.progress);
        this.syncProgress.setVisibility(View.GONE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Nodyn");

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FrameLayout frame = findViewById(R.id.frame);

        Fragment newFragment = null;

        if (savedInstanceState == null) {
            //use the default fragment if we did not have a previous one (i.e. app first start)
            newFragment = getDefaultMainFragment();
            updateMainFragment(newFragment);
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
        boolean hideSyncOverflowOption = false;
        if (shouldShowAdapterError()) {
            newFragment = BackendErrorFragment.newInstance();
            hideSyncOverflowOption = true;
            updateMainFragment(newFragment);
        } else if (shouldShowFirstSyncError()) {
            newFragment = FirstSyncErrorFragment.newInstance();
            ((FirstSyncErrorFragment) newFragment).setOnSetupCompleteListener(this);
            hideSyncOverflowOption = true;
            updateMainFragment(newFragment);
        }



        Intent i = getIntent();
        final boolean syncNow = i.getBooleanExtra(SYNC_NOW, false);
        if (syncNow) {
            Intent si = new Intent(this, SyncService.class);
            startService(si);
        }

        syncIcon = (ImageButton) toolbar.findViewById(R.id.sync_icon);
        syncIcon.setVisibility(View.INVISIBLE);
        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case SyncService.BROADCAST_SYNC_START:
                        syncIcon.setImageDrawable(getResources().getDrawable(R.drawable.sync));
                        AnimationHelper.fadeIn(MainActivity.this, syncIcon, new AnimationHelper.AnimateListener() {
                            @Override
                            public void onAnimationFinished() {
                                syncIcon.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate));
                            }
                        });
                        syncProgress.setIndeterminate(true);
                        AnimationHelper.fadeIn(MainActivity.this, syncProgress);
                        break;
                    case SyncService.BROADCAST_SYNC_FINISH:
                        AnimationHelper.fadeOut(MainActivity.this, syncIcon, new AnimationHelper.AnimateListener() {
                            @Override
                            public void onAnimationFinished() {
                                syncIcon.clearAnimation();
                            }
                        });
                        AnimationHelper.fadeOut(MainActivity.this, syncProgress);
                        break;
                    case SyncService.BROADCAST_SYNC_FAIL:
                        syncIcon.clearAnimation();
                        syncIcon.setVisibility(View.VISIBLE);
                        syncIcon.setImageDrawable(getResources().getDrawable(R.drawable.sync_alert));
                        AnimationHelper.fadeOut(MainActivity.this, syncProgress);
                        break;
                    case SyncService.BROADCAST_SYNC_UPDATE:
                        int progress = intent.getIntExtra(SyncService.BROADCAST_SYNC_PROGRESS_MAIN, -1);

                        if (progress > 0) {
                            syncProgress.setIndeterminate(false);
                            syncProgress.setProgress(progress);
                        }

                        break;
                }
            }
        };
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt("foo", 1);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private Fragment getDefaultMainFragment() {
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

    private boolean shouldShowFirstSyncError() {
        return !SyncManager.isFirstSyncComplete(this);
    }

    private void updateMainFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (currentFragment == null) {
            ft.add(R.id.frame, fragment, "main_fragment").commit();
        } else {
            ft.replace(R.id.frame, fragment, "main_fragment").commit();
        }

        currentFragment = fragment;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        //hide the sync now button from the overflow menu if the backend has not been configured yet
        if (currentFragment != null &&
                (currentFragment instanceof FirstSyncErrorFragment || currentFragment instanceof BackendErrorFragment)) {
            MenuItem item = menu.findItem(R.id.action_sync);
            item.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sync) {
            Log.d(TAG, "Sync button clicked, starting fetchFullModel process in background");
            Intent i = new Intent(this, SyncService.class);
            i.putExtra(SyncService.SYNC_TYPE_KEY, SyncService.SYNC_TYPE_FULL);
            startService(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(br);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //disable drawer icons for assets and users until a sync backend is selected
        setDrawerIconsState();

        //hide the audit assets option in the navigation menu if that option is disabled
        hideDrawerIcons();

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

        IntentFilter filter = new IntentFilter();
        filter.addAction(SyncService.BROADCAST_SYNC_START);
        filter.addAction((SyncService.BROADCAST_SYNC_FINISH));
        filter.addAction(SyncService.BROADCAST_SYNC_FAIL);
        filter.addAction(SyncService.BROADCAST_SYNC_UPDATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(br, filter);
    }

    /**
     * Adjust the enabled/disabled state of the navigation drawer items for assets and users
     */
    private void setDrawerIconsState() {
        NavigationView nav = findViewById(R.id.nav_view);
        boolean itemEnabled = getMenuIconsState();
        Menu menu = nav.getMenu();
        MenuItem dash = menu.findItem(R.id.nav_dash);
        MenuItem checkout = menu.findItem(R.id.nav_check_out);
        MenuItem checkin = menu.findItem(R.id.nav_check_in);
        MenuItem history = menu.findItem(R.id.nav_history);
        MenuItem assetsItem = menu.findItem(R.id.nav_assets);
        MenuItem usersItem = menu.findItem(R.id.nav_users);
        MenuItem audit = menu.findItem(R.id.nav_audit);

        setMenuOptionState(dash, itemEnabled);
        setMenuOptionState(checkout, itemEnabled);
        setMenuOptionState(checkin, itemEnabled);
        setMenuOptionState(history, itemEnabled);
        setMenuOptionState(assetsItem, itemEnabled);
        setMenuOptionState(usersItem, itemEnabled);
        setMenuOptionState(audit, itemEnabled);
    }

    /**
     * Hide the asset audit navigation menu item if audits have been disabled in settings
     */
    private void hideDrawerIcons() {
        NavigationView nav = findViewById(R.id.nav_view);
        Menu menu = nav.getMenu();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean checkInEnabled = prefs.getBoolean(getString(R.string.pref_key_checkin_allow),
                Boolean.parseBoolean(getString(R.string.pref_default_checkin_allow)));
        MenuItem checkInItem = menu.findItem(R.id.nav_check_in);
        checkInItem.setVisible(checkInEnabled);

        boolean checkOutEnabled = prefs.getBoolean(getString(R.string.pref_key_checkout_allow),
                Boolean.parseBoolean(getString(R.string.pref_default_checkout_allow)));
        MenuItem checkOutItem = menu.findItem(R.id.nav_check_out);
        checkOutItem.setVisible(checkOutEnabled);

        boolean assetsEnabled = prefs.getBoolean(getString(R.string.pref_key_asset_enable_browse),
                Boolean.parseBoolean(getString(R.string.pref_default_asset_enable_browse)));
        MenuItem assetsItem = menu.findItem(R.id.nav_assets);
        assetsItem.setVisible(assetsEnabled);

        boolean usersEnabled = prefs.getBoolean(getString(R.string.pref_key_users_enable_browse),
                Boolean.parseBoolean(getString(R.string.pref_default_users_enable_browse)));
        MenuItem usersItem = menu.findItem(R.id.nav_users);
        usersItem.setVisible(usersEnabled);

        boolean auditsEnabled = prefs.getBoolean(getString(R.string.pref_key_audit_enable_audits),
                Boolean.parseBoolean(getString(R.string.pref_default_audit_enable_audits)));
        MenuItem auditItem = menu.findItem(R.id.nav_audit);
        auditItem.setVisible(auditsEnabled);
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
        return !(shouldShowAdapterError() || shouldShowFirstSyncError());
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
//            newFragment = ActionsListFragment.newInstance(1, -1);
            newFragment = ActionHistoryFragment.newInstance();
            highlightItem = true;
        } else if (id == R.id.nav_audit) {
            Intent i = new Intent(this, AuditActivity.class);
            startActivity(i);
            highlightItem = false;
        } else if (id == R.id.nav_settings) {
            SettingsHelper.loadKioskSettings(this, null);
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
            sb.append("https://play.google.com/store/apps/details?id=io.phobotic.nodyn_app");
            shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out Nodyn");
            startActivity(Intent.createChooser(shareIntent, "Share Nodyn"));

        }

        if (newFragment != null) {
            updateMainFragment(newFragment);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
        for (Pair<View, String> p : sharedElements) {
            if (p != null) {
                size++;
            }
        }
        Pair<View, String>[] safePairs = new Pair[size];
        int index = 0;
        for (Pair<View, String> p : sharedElements) {
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

    @Override
    public void onSetupComplete() {
        setDrawerIconsState();

        Fragment newFragment = newFragment = getDefaultMainFragment();

        //override the new fragment if we need to show the sync adapter error
        if (shouldShowAdapterError()) {
            newFragment = BackendErrorFragment.newInstance();
        } else if (shouldShowFirstSyncError()) {
            newFragment = FirstSyncErrorFragment.newInstance();
            ((FirstSyncErrorFragment) newFragment).setOnSetupCompleteListener(this);
        }

        updateMainFragment(newFragment);

        //since the first sync is complete we can go ahead and trigger building the statistics
        //+ database.  We will do this after a delay so that the 30 day activity fragment
        //+ inside the dashboard can have a bit of time to initialize it's broadcast receiver
        final Context context = this;
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (context != null) {
                    Intent i = new Intent(context, StatisticsService.class);
                    startService(i);
                }
            }
        }, 2000);


    }
}
