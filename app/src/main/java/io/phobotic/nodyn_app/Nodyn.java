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

package io.phobotic.nodyn_app;

import android.app.Application;
import android.util.Log;



import androidx.preference.PreferenceManager;

import io.phobotic.nodyn_app.database.RoomDBWrapper;
import io.phobotic.nodyn_app.schedule.SyncScheduler;

import com.google.firebase.BuildConfig;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

/**
 * Created by Jonathan Nelson on 10/19/17.
 */

public class Nodyn extends Application {
    public static final String TAG = Nodyn.class.getSimpleName();

    public Nodyn() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Nodyn starting");

        Log.d(TAG, "Starting Crashlytics");
        //start crashlytics only if not a debug build
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();

        //disable crashlytics on debug builds
        Log.d(TAG, "Crashlytics collection is " + (BuildConfig.DEBUG ? "disabled" : "enabled"));
        crashlytics.setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG);


        Log.d(TAG, "Forcing sync schedule reset");
        SyncScheduler scheduler = new SyncScheduler(this);
        scheduler.forceScheduleSync();
        scheduler.schedulePastDueAlertsWake();
        scheduler.scheduleStatisticsUpdate();

        Log.d(TAG, "Inserting defaults for preferences");
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_users, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_assets, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_sync_snipeit_3, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_check_in, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_check_out, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_email, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_audit, false);

        //initialize the Room databases
        RoomDBWrapper roomDBWrapper = RoomDBWrapper.getInstance(getApplicationContext());
        //System.out.println(scanRecords.size());
    }
}
