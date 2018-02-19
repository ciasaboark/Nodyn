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

package io.phobotic.nodyn_app.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.phobotic.nodyn_app.database.model.Action;
import io.phobotic.nodyn_app.database.statistics.StatisticsDatabase;
import io.phobotic.nodyn_app.database.statistics.model.DayActivity;
import io.phobotic.nodyn_app.schedule.SyncScheduler;
import io.phobotic.nodyn_app.sync.SyncManager;
import io.phobotic.nodyn_app.sync.adapter.SyncAdapter;
import io.phobotic.nodyn_app.sync.adapter.SyncException;
import io.phobotic.nodyn_app.sync.adapter.SyncNotSupportedException;

/**
 * Created by Jonathan Nelson on 12/6/17.
 */

public class StatisticsService extends IntentService {
    public static final String BROADCAST_BUILD_STATISTICS_START = "build_statistics_start";
    public static final String BROADCAST_BUILD_STATISTICS_FINISH = "build_statistics_finish";
    private static final String TAG = StatisticsService.class.getSimpleName();

    public StatisticsService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "waking");
        final SyncAdapter syncAdapter = SyncManager.getPrefferedSyncAdapter(this);
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);

        Intent i = new Intent(BROADCAST_BUILD_STATISTICS_START);
        broadcastManager.sendBroadcast(i);

        tryBuildThirtyDayStats(syncAdapter);

        i = new Intent(BROADCAST_BUILD_STATISTICS_FINISH);
        broadcastManager.sendBroadcast(i);
        SyncScheduler scheduler = new SyncScheduler(this);
        scheduler.scheduleStatisticsUpdate();
    }

    private void tryBuildThirtyDayStats(SyncAdapter syncAdapter) {
        try {
            Calendar calendar = Calendar.getInstance();

            Map<Long, DayActivity> dayActivityMap = new HashMap<>();

            calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
            calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            long cutoff = calendar.getTimeInMillis();

            Date d = new Date(cutoff);
            DateFormat df = DateFormat.getDateInstance();
            String dateString = df.format(d);
            Log.d(TAG, "Pulling action history from backend as far back as " + dateString);

            List<Action> actions = new ArrayList<Action>();
            int curPage = 0;
            boolean fetchNextPage = true;

            //continue fetching pages of action history until there are no more left, or until we
            //+ hit the cutoff.

            while (fetchNextPage) {
                try {
                    Log.d(TAG, "Fetching action history page " + curPage);
                    List<Action> pageActions = syncAdapter.getActivity(this, curPage);
                    if (pageActions == null || pageActions.isEmpty()) {
                        //we have reached the end of the data we can pull
                        Log.d(TAG, "reached end of action data before hitting cutoff");
                        fetchNextPage = false;
                    } else {
                        for (Action a : pageActions) {
                            if (a.getTimestamp() < cutoff) {
                                final String STATISTICS_RECORDS_FOR_THIRY_DAYS = "statistics records for 30 days";
                                final String PAGE_COUNT = "page_count";
                                final String RECORD_COUNT = "record_count";
                                CustomEvent ce = new CustomEvent(STATISTICS_RECORDS_FOR_THIRY_DAYS)
                                        .putCustomAttribute(PAGE_COUNT, curPage)
                                        .putCustomAttribute(RECORD_COUNT, actions.size());
                                Answers.getInstance().logCustom(ce);
                                fetchNextPage = false;
                                Log.d(TAG, String.format("reached first record older than cutoff " +
                                        "(%d record within cutoff)", actions.size()));
                                break;
                            } else {
                                actions.add(a);
                            }
                        }

                        curPage++;
                    }
                } catch (SyncNotSupportedException e) {
                    fetchNextPage = false;
                } catch (SyncException e) {
                    fetchNextPage = false;  // TODO: 2/17/18 more processing here?
                }
            }


            for (Action action : actions) {
                if (action.getTimestamp() >= cutoff && action.getTimestamp() <= System.currentTimeMillis()) {
                    calendar.setTimeInMillis(action.getTimestamp());
                    calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
                    calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
                    calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
                    calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));

                    long timestamp = calendar.getTimeInMillis();
                    DayActivity activity = dayActivityMap.get(timestamp);
                    if (activity == null) activity = new DayActivity(timestamp, 0, 0);

                    switch (action.getDirection()) {
                        case CHECKIN:
                            activity.setCheckinCount(activity.getCheckinCount() + 1);
                            break;
                        case CHECKOUT:
                            activity.setCheckoutCount(activity.getCheckoutCount() + 1);
                            break;
                    }

                    dayActivityMap.put(timestamp, activity);
                }
            }

            Log.d(TAG, "Found history for " + dayActivityMap.size() + " days");

            List<DayActivity> dayActivityList = new ArrayList<>();
            for (Map.Entry<Long, DayActivity> entry : dayActivityMap.entrySet()) {
                dayActivityList.add(entry.getValue());
            }

            StatisticsDatabase database = StatisticsDatabase.getInstance(this);
            database.replaceThirtyDayActivity(dayActivityList);
        } catch (Exception e) {
            Log.e(TAG, "Caught exception building 30 day history: " + e.getMessage());
            Crashlytics.logException(e);
        }
    }
}
