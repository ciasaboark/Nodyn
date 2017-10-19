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

package io.phobotic.nodyn.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import io.phobotic.nodyn.reporting.CustomEvents;
import io.phobotic.nodyn.schedule.SyncScheduler;

/**
 * Created by Jonathan Nelson on 7/9/17.
 */

public class BootReceiver extends BroadcastReceiver {
    public static final String TAG = BootReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Boot receiver started, scheduling sync service to wake");

        SyncScheduler scheduler = new SyncScheduler(context);
        scheduler.forceScheduleSync();

        Answers.getInstance().logCustom(new CustomEvent(CustomEvents.BOOT_RECEIVER_FIRED));
    }
}
