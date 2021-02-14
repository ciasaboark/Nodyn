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

package io.phobotic.nodyn_app.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;


import com.google.firebase.analytics.FirebaseAnalytics;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.annotation.Nullable;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.reporting.CustomEvents;
import io.phobotic.nodyn_app.service.PastDueAlertService;
import io.phobotic.nodyn_app.service.StatisticsService;
import io.phobotic.nodyn_app.service.SyncService;
import io.phobotic.nodyn_app.sync.SyncManager;

/**
 * Created by Jonathan Nelson on 7/9/17.
 */

public class SyncScheduler {
    private static final String TAG = SyncScheduler.class.getSimpleName();
    private static final int SYNC_REQUEST_CODE = 1;
    private static final int PAST_DUE_REQUEST_CODE = 2;
    private static final int STATISTICS_REQUEST_CODE = 3;
    private final Context context;


    public SyncScheduler(@NotNull Context ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("Context can not be null");
        }

        this.context = ctx;
    }

    public void scheduleSyncIfNeeded() {
        if (!SyncManager.isFirstSyncComplete(context)) {
            Log.d(TAG, "Skipping scheduling sync alarm.  Backend has not completed first sync yet");
        } else {
            Log.d(TAG, "Scheduling alarm now");
            scheduleSync(getNewPendingIntent());
        }
    }

    private boolean isAlarmScheduled() {
        //pi will be null if it already exists
        PendingIntent pi = getExistingPendingIntent();
        return pi != null;
    }

    public static Date getNextWakeTimestamp(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int defaultPeriodInt = Integer.parseInt(context.getString(R.string.pref_default_sync_frequency));
        int wakePeriod = defaultPeriodInt;
        try {
            String wakePeriodString = prefs.getString(context.getString(R.string.pref_key_sync_frequency),
                    context.getString(R.string.pref_default_sync_frequency));
            wakePeriod = Integer.parseInt(wakePeriodString);
        } catch (NumberFormatException e) {
            //just use the default value for now
        }

        long now = System.currentTimeMillis();
        long wakeAt = now + (1000 * 60 * wakePeriod);

        Date d = new Date();
        d.setTime(wakeAt);

        return d;
    }

    private void scheduleSync(@NotNull PendingIntent pi) {
        DateFormat df = DateFormat.getDateTimeInstance();
        Date d = SyncScheduler.getNextWakeTimestamp(context);

        Log.d(TAG, "Scheduling next sync at " + df.format(d));

        schedulePendingIntent(pi, d.getTime());

        FirebaseAnalytics.getInstance(context).logEvent(CustomEvents.SYNC_SCHEDULED, null);
    }

    private
    @NotNull
    PendingIntent getNewPendingIntent() {
        PendingIntent pi = getPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT);
        return pi;
    }

    private
    @Nullable
    PendingIntent getExistingPendingIntent() {
        PendingIntent pi = getPendingIntent(PendingIntent.FLAG_NO_CREATE);
        return pi;
    }

    private void schedulePendingIntent(@NotNull PendingIntent pi, long wakeAt) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wakeAt, pi);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, wakeAt, pi);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, wakeAt, pi);
        }
    }

    private
    @Nullable
    PendingIntent getPendingIntent(int flags) {
        Intent intent = new Intent(context, SyncService.class);
        PendingIntent pi = PendingIntent.getService(context, SYNC_REQUEST_CODE,
                intent, flags);
        return pi;
    }

    public void forceScheduleSync() {
        Log.d(TAG, "Scheduling new sync alarm without checking for previously scheduled one");
        scheduleSync(getNewPendingIntent());
    }

    public void scheduleStatisticsUpdate() {
        Log.d(TAG, "Rescheduling statistics service wake");
        Intent intent = new Intent(context, StatisticsService.class);
        PendingIntent pi = PendingIntent.getService(context, STATISTICS_REQUEST_CODE,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        long now = System.currentTimeMillis();

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(now));
        cal.set(Calendar.HOUR_OF_DAY, cal.getActualMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getActualMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getActualMaximum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getActualMaximum(Calendar.MILLISECOND));
        Date d;
        DateFormat df = DateFormat.getDateTimeInstance();
        long wakeAt = cal.getTimeInMillis();


        while (wakeAt < now) {
            d = new Date(cal.getTimeInMillis());
            String dateString = df.format(d);
            Log.d(TAG, "Too late for statistics service wake time of " + dateString + ", pushing ahead by 30 minutes");
            cal.add(Calendar.DAY_OF_MONTH, 1);
            wakeAt = cal.getTimeInMillis();
        }


        d = new Date(cal.getTimeInMillis());
        String dateString = df.format(d);
        Log.d(TAG, "Scheduling statistics service wake at " + dateString);
        schedulePendingIntent(pi, wakeAt);
    }

    public void schedulePastDueAlertsWake() {
        Intent intent = new Intent(context, PastDueAlertService.class);
        PendingIntent pi = PendingIntent.getService(context, PAST_DUE_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //set wakeup for 6AM
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 6);
        cal.set(Calendar.MINUTE, cal.getActualMinimum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getActualMinimum(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cal.getActualMinimum(Calendar.MILLISECOND));

        long wakeAt = cal.getTimeInMillis();
        long now = System.currentTimeMillis();

        //if we are already past the 6AM wake time then push it ahead to tomorrow
        if (wakeAt <= now) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            wakeAt = cal.getTimeInMillis();
        }

        DateFormat df = DateFormat.getDateTimeInstance();
        Date d = new Date(cal.getTimeInMillis());
        String dateString = df.format(d);
        Log.d(TAG, "Scheduling past due asset alert check at " + dateString);

        schedulePendingIntent(pi, wakeAt);
    }
}
