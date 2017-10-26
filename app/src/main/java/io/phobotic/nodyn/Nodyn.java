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

package io.phobotic.nodyn;

import android.app.Application;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.phobotic.nodyn.schedule.SyncScheduler;

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
        Crashlytics crashlytics = new Crashlytics.Builder().disabled(BuildConfig.DEBUG).build();
        try {
            String id = Installation.id(this);
            crashlytics.setUserIdentifier(id);
        } catch (RuntimeException e) {

        }
        Fabric.with(this, crashlytics);

        Log.d(TAG, "Forcing sync schedule reset");
        SyncScheduler scheduler = new SyncScheduler(this);
        scheduler.forceScheduleSync();
        scheduler.schedulePastDueAlertsWake();

        Log.d(TAG, "Inserting defaults for missing preferences");
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_users, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_assets, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_sync_snipeit_3, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_check_in, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_check_out, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_email, false);
    }
}
