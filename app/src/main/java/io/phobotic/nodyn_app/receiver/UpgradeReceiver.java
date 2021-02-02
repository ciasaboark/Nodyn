/*
 * Copyright (c) 2018 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import io.phobotic.nodyn_app.reporting.CustomEvents;
import io.phobotic.nodyn_app.schedule.SyncScheduler;

/**
 * Created by Jonathan Nelson on 7/9/17.
 */

public class UpgradeReceiver extends BroadcastReceiver {
    public static final String TAG = UpgradeReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Upgrade receiver started, scheduling sync service to wake");

        SyncScheduler scheduler = new SyncScheduler(context);
        scheduler.forceScheduleSync();

        FirebaseAnalytics.getInstance(context).logEvent(CustomEvents.UPGRADE_RECEIVER_FIRED, null);
    }
}
