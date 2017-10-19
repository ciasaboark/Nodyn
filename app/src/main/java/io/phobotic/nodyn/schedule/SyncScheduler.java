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

package io.phobotic.nodyn.schedule;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.phobotic.nodyn.reporting.CustomEvents;
import io.phobotic.nodyn.service.PastDueAlertService;
import io.phobotic.nodyn.service.SyncService;

/**
 * Created by Jonathan Nelson on 7/9/17.
 */

public class SyncScheduler {
    private static final String TAG = SyncScheduler.class.getSimpleName();
    private static final int SYNC_REQUEST_CODE = 1;
    private static final int PAST_DUE_REQUEST_CODE = 1;
    private final Context context;


    public SyncScheduler(@NotNull Context ctx) {
        if (ctx == null) {
            throw new IllegalArgumentException("Context can not be null");
        }

        this.context = ctx;
    }

    public void scheduleSyncIfNeeded() {
        if (isAlarmScheduled()) {
            Log.d(TAG, "Skipping scheduling sync alarm, one is already set");
        } else {
            Log.d(TAG, "No repeating sync alarm set.  Setting one now");
            scheduleSync(getNewPendingIntent());
        }
    }

    private boolean isAlarmScheduled() {
        //pi will be null if it already exists
        PendingIntent pi = getExistingPendingIntent();
        return pi == null;
    }

    private void scheduleSync(@NotNull PendingIntent pi) {

        long now = System.currentTimeMillis();
        long wakeAt = now + (5 * 1000 * 60);    //wake 5 minutes from now

        DateFormat df = new SimpleDateFormat();
        Date d = new Date();
        d.setTime(wakeAt);
        Log.d(TAG, "Scheduling next sync at " + df.format(d));

        schedulePendingIntent(pi, wakeAt);

        Answers.getInstance().logCustom(new CustomEvent(CustomEvents.SYNC_SCHEDULED));
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
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wakeAt, pi);
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

        long wakeAT = cal.getTimeInMillis();
        long now = System.currentTimeMillis();

        //if we are already past the 6AM wake time then push it ahead to tomorrow
        if (wakeAT <= now) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            wakeAT = cal.getTimeInMillis();
        }

        DateFormat df = new SimpleDateFormat();
        Date d = new Date(cal.getTimeInMillis());
        String dateString = df.format(d);
        Log.d(TAG, "Scheduling past due asset alert check at " + dateString);

        schedulePendingIntent(pi, wakeAT);
    }
}
