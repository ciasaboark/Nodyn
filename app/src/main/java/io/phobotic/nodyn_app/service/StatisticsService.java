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
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.model.Action;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.statistics.assets.AssetStatistics;
import io.phobotic.nodyn_app.database.statistics.assets.AssetStatisticsBuilder;
import io.phobotic.nodyn_app.database.statistics.assets.AssetStatisticsDatabase;
import io.phobotic.nodyn_app.database.statistics.day_activity.DayActivity;
import io.phobotic.nodyn_app.database.statistics.day_activity.DayActivityDatabase;
import io.phobotic.nodyn_app.database.statistics.models.ModelStatistics;
import io.phobotic.nodyn_app.database.statistics.models.ModelStatisticsBuilder;
import io.phobotic.nodyn_app.database.statistics.models.ModelStatisticsDatabase;
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

        boolean statisticsEnabled = prefs.getBoolean(getString(R.string.pref_key_stats_enable),
                Boolean.parseBoolean(getString(R.string.pref_default_stats_enable)));

        if (!statisticsEnabled) {
            Log.d(TAG, "Statistics module has been disabled in settings.  Skipping building " +
                    "thirty day statistics");
        } else {
            Intent i = new Intent(BROADCAST_BUILD_STATISTICS_START);
            broadcastManager.sendBroadcast(i);

            tryBuildThirtyDayStats(syncAdapter);

            i = new Intent(BROADCAST_BUILD_STATISTICS_FINISH);
            broadcastManager.sendBroadcast(i);
        }

        SyncScheduler scheduler = new SyncScheduler(this);
        scheduler.scheduleStatisticsUpdate();
    }

    private void tryBuildThirtyDayStats(SyncAdapter syncAdapter) {
        try {
            Calendar calendar = Calendar.getInstance();

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
            try {
                actions = syncAdapter.getThirtyDayActivity(this);
            } catch (SyncNotSupportedException e) {
                Log.e(TAG, String.format("Sync adapter %s does not support fetching action " +
                        "history", syncAdapter.getAdapterName()));
            } catch (SyncException e) {
                Crashlytics.logException(e);
                e.printStackTrace();
                Log.e(TAG, String.format("Caught SyncException fetching thirty day activty " +
                        "using adapter %s: %s", syncAdapter.getAdapterName(), e.getMessage()));
            }

            if (actions == null) actions = new ArrayList<>();
            Log.d(TAG, String.format("Found %d actions for the last 30 days", actions.size()));

            List<ModelStatistics> modelStatistics = getModelStatistics(actions);
            ModelStatisticsDatabase modelDb = ModelStatisticsDatabase.getInstance(this);
            modelDb.assetStatisticsDao().replace(modelStatistics);

            List<AssetStatistics> assetStatistics = getAssetStatistics(actions, cutoff);
            AssetStatisticsDatabase assetDb = AssetStatisticsDatabase.getInstance(this);
            assetDb.assetStatisticsDao().replace(assetStatistics);

            List<DayActivity> dayActivityList = getDayActivities(actions);
            DayActivityDatabase dayActivityDb = DayActivityDatabase.getInstance(this);
            dayActivityDb.dayActivityDao().replace(dayActivityList);

        } catch (Exception e) {
            Log.e(TAG, "Caught exception building 30 day statistics: " + e.getMessage());
            Crashlytics.logException(e);
        }
    }

    private List<ModelStatistics> getModelStatistics(@NotNull List<Action> actions) {
        Map<Integer, ModelStatisticsBuilder> modelStatisticsMap = new HashMap<>();
        Database db = Database.getInstance(this);
        for (Action action : actions) {
            int assetID = action.getAssetID();
            try {
                Asset asset = db.findAssetByID(action.getAssetID());
                Model m = db.findModelByID(asset.getModelID());

                ModelStatisticsBuilder builder = modelStatisticsMap.get(m.getId());
                if (builder == null) builder = new ModelStatisticsBuilder(this, m);
                modelStatisticsMap.put(m.getId(), builder);

                builder.recordAction(action);
            } catch (AssetNotFoundException e) {
                Log.d(TAG, String.format("Unable to find asset with ID %d while processing 30 " +
                        "day action history.  This action record will be skipped", action.getAssetID()));
            } catch (ModelNotFoundException e) {
                Log.d(TAG, String.format("Unable to find model ID for asset ID %d while processing 30 " +
                        "day action history.  This action record will be skipped", action.getAssetID()));
            }
        }

        List<ModelStatistics> modelStatisticsList = new ArrayList<>();
        for (Map.Entry<Integer, ModelStatisticsBuilder> entry : modelStatisticsMap.entrySet()) {
            ModelStatisticsBuilder builder = entry.getValue();
            modelStatisticsList.add(builder.build());
        }

        return modelStatisticsList;
    }

    private List<AssetStatistics> getAssetStatistics(List<Action> actions, long minTimestamp) {
        Map<Integer, AssetStatisticsBuilder> assetStatisticsMap = new HashMap<>();
        Database db = Database.getInstance(this);
        for (Action action : actions) {
            int assetID = action.getAssetID();
            try {
                Asset asset = db.findAssetByID(action.getAssetID());

                AssetStatisticsBuilder builder = assetStatisticsMap.get(asset.getId());
                if (builder == null) builder = new AssetStatisticsBuilder(this, asset);
                assetStatisticsMap.put(asset.getId(), builder);

                builder.recordAction(action);
            } catch (AssetNotFoundException e) {
                Log.d(TAG, String.format("Unable to find asset with ID %d while processing 30 " +
                        "day action history.  This action record will be skipped", action.getAssetID()));
            }
        }

        List<AssetStatistics> assetStatistics = new ArrayList<>();
        for (Map.Entry<Integer, AssetStatisticsBuilder> entry : assetStatisticsMap.entrySet()) {
            AssetStatisticsBuilder builder = entry.getValue();
            assetStatistics.add(builder.build(minTimestamp));
        }

        return assetStatistics;
    }

    private List<DayActivity> getDayActivities(@NotNull List<Action> actions) {
        Map<Long, DayActivity> dayActivityMap = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        normalize(calendar);


        //insert placeholders for every day within the previous 30 days.  This will simplify building
        //+ the chart later on
        DateFormat df = DateFormat.getDateTimeInstance();
        int daysToSubtract = 0;
        for (int i = 0; i < 31; i++) {
            calendar.add(Calendar.DATE, daysToSubtract);
            normalize(calendar);
            long timestamp = calendar.getTimeInMillis();
            Log.d(TAG, String.format("Adding placeholder record for %s", df.format(new Date(timestamp))));
            dayActivityMap.put(timestamp, new DayActivity(timestamp, 0, 0, 0));
            daysToSubtract = -1;
        }

        for (Action action : actions) {
            calendar.setTimeInMillis(action.getTimestamp());
            normalize(calendar);
            long timestamp = calendar.getTimeInMillis();
            DayActivity activity = dayActivityMap.get(timestamp);
            if (activity == null) {
                Log.w(TAG, String.format("Expected to find placeholder DayActivty for timestamp " +
                        "%d (%s).  Found null.  Creating placeholder", timestamp, df.format(new Date(timestamp))));
                activity = new DayActivity(timestamp, 0, 0, 0);
            }

            switch (action.getDirection()) {
                case CHECKIN:
                    activity.setTotalCheckins(activity.getTotalCheckins() + 1);
                    break;
                case CHECKOUT:
                    activity.setTotalCheckouts(activity.getTotalCheckouts() + 1);
                    break;
            }

            dayActivityMap.put(timestamp, activity);
        }

        List<DayActivity> dayActivityList = new ArrayList<>();
        for (Map.Entry<Long, DayActivity> entry : dayActivityMap.entrySet()) {
            dayActivityList.add(entry.getValue());
        }
        return dayActivityList;
    }

    private void normalize(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
    }
}
