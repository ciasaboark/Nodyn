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

package io.phobotic.nodyn_app.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.helper.PastDueEmailHelper;
import io.phobotic.nodyn_app.reporting.CustomEvents;
import io.phobotic.nodyn_app.schedule.SyncScheduler;

/**
 * Created by Jonathan Nelson on 10/19/17.
 */

public class PastDueAlertService extends IntentService {
    private static final String TAG = PastDueAlertService.class.getSimpleName();
    private Database db;
    private SharedPreferences prefs;

    public PastDueAlertService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "Waking");
        db = Database.getInstance(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Past due notices should only be sent if email alerts and past due reminders have been enabled
        boolean sendEmailAlerts = prefs.getBoolean(getString(R.string.pref_key_email_enable),
                Boolean.parseBoolean(getString(R.string.pref_default_email_enable)));
        boolean sendPastDueAlerts = prefs.getBoolean(getString(R.string.pref_key_past_due_enabled),
                Boolean.parseBoolean(getString(R.string.pref_default_past_due_enabled)));

        if (sendEmailAlerts && sendPastDueAlerts) {
            checkForPastDueAssets();
        } else {
            //reschedule for the next wake time
            SyncScheduler scheduler = new SyncScheduler(this);
            scheduler.schedulePastDueAlertsWake();
        }

    }

    private void checkForPastDueAssets() {
        List<Asset> pastDueAssets = getPastDueAssets();

        if (pastDueAssets.size() > 0) {
            List<Asset> dueSoonAssets = getDueSoonAssets();
            Answers.getInstance().logCustom(new CustomEvent(CustomEvents.ASSET_PAST_DUE));

            //should we also send an alert to the current asset holder
            boolean includeCurrentHolder = prefs.getBoolean(
                    getString(R.string.pref_key_past_due_include_owner),
                    Boolean.parseBoolean(getString(R.string.pref_default_past_due_include_owner)));

            PastDueEmailHelper emailHelper = new PastDueEmailHelper(this);
            if (includeCurrentHolder) {
                for (Asset a : pastDueAssets) {
                    emailHelper.sendCurrentOwnerReminder(a);
                }
            }


            emailHelper.sendBulkReminder(pastDueAssets, dueSoonAssets);
        }

        SyncScheduler scheduler = new SyncScheduler(this);
        scheduler.schedulePastDueAlertsWake();
    }

    @NonNull
    private List<Asset> getPastDueAssets() {
        List<Asset> allAssets = db.getAssets();
        List<Asset> pastDueAssets = new ArrayList<>();

        long now = System.currentTimeMillis();
        for (Asset asset : allAssets) {
            if (asset.getAssignedToID() != -1) {
                long expectedCheckin = asset.getExpectedCheckin();
                if (expectedCheckin != -1) {
                    if (expectedCheckin < now) {
                        Date d = new Date(expectedCheckin);
                        DateFormat df = DateFormat.getDateTimeInstance();
                        String expectedDateString = df.format(d);
                        Log.d(TAG, "Asset " + asset.getTag() + " is past due.  Checkin expected " +
                                "by " + expectedDateString);
                        pastDueAssets.add(asset);
                    }
                }
            }
        }

        return pastDueAssets;
    }

    @NonNull
    private List<Asset> getDueSoonAssets() {
        List<Asset> allAssets = db.getAssets();
        List<Asset> dueSoonAssets = new ArrayList<>();

        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTimeInMillis(System.currentTimeMillis());
        tomorrow.add(Calendar.DATE, 1);

        for (Asset asset : allAssets) {
            if (asset.getAssignedToID() != -1) {
                if (asset.getExpectedCheckin() != -1) {
                    Calendar expectedCheckin = Calendar.getInstance();
                    expectedCheckin.setTimeInMillis(asset.getExpectedCheckin());
                    if (expectedCheckin.after(now) && expectedCheckin.before(tomorrow)) {
                        Date d = new Date(asset.getExpectedCheckin());
                        DateFormat df = DateFormat.getDateTimeInstance();
                        String expectedDateString = df.format(d);
                        Log.d(TAG, "Asset " + asset.getTag() + " is due within the next 24 hours.  Checkin expected " +
                                "by " + expectedDateString);
                        dueSoonAssets.add(asset);
                    }
                }
            }
        }

        return dueSoonAssets;
    }
}
