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

package io.phobotic.nodyn_app.database.statistics.assets;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Action;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.statistics.UsageRecord;

/**
 * Created by Jonathan Nelson on 2019-05-11.
 */
public class AssetStatisticsBuilder {
    private final String TAG = AssetStatisticsBuilder.class.getSimpleName();
    private final SharedPreferences prefs;
    private final Database db;
    private final Context context;
    private final Asset asset;
    private final List<Action> actionList;
    private final Map<Integer, Integer> userCheckouts = new HashMap<>();

    public AssetStatisticsBuilder(@NotNull Context context, @NotNull Asset asset) {
        this.context = context;
        this.asset = asset;
        this.actionList = new ArrayList<>();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        db = Database.getInstance(context);
    }

    public void recordAction(@NotNull Action action) {
        actionList.add(action);
        if (action.getDirection() == Action.Direction.CHECKOUT ||
                action.getDirection() == Action.Direction.CHECKIN) {
            Integer count = userCheckouts.get(action.getUserID());
            if (count == null) {
                count = 0;
            }

            count++;
            userCheckouts.put(action.getUserID(), count);
        }
    }

    public AssetStatistics build(long minTimestamp) {
        AssetStatistics assetStatistics = new AssetStatistics();
        assetStatistics.setLastUpdated(System.currentTimeMillis());
        assetStatistics.setId(asset.getId());
        assetStatistics.setTag(asset.getTag());
        assetStatistics.setUsageRecords(getUsageRecords(minTimestamp));
        assetStatistics.setFavoringUser(getFavoringUser());

        return assetStatistics;
    }

    private List<UsageRecord> getUsageRecords(long minTimestamp) {
        Collections.sort(actionList, new Comparator<Action>() {
            @Override
            public int compare(Action o1, Action o2) {
                return Long.compare(o1.getTimestamp(), o2.getTimestamp());
            }
        });

        List<UsageRecord> usageRecords = new ArrayList<>();
        UsageRecord record = null;

        for (Action action : actionList) {
            //special case.  the action history only goes far back enough to see the checkin record
            //+ but not the initial checkout record
            if (action.getDirection() == Action.Direction.CHECKIN && record == null) {
                record = new UsageRecord();
                record.setCheckoutTimestamp(minTimestamp);
                record.setCheckinTimestamp(action.getTimestamp());
                usageRecords.add(record);
            } else {
                if (record == null) record = new UsageRecord();

                switch (action.getDirection()) {
                    case CHECKOUT:
                        record.setCheckoutTimestamp(action.getTimestamp());
                        record.setAssignedUser(action.getUserID());
                        break;
                    case CHECKIN:
                        record.setCheckinTimestamp(action.getTimestamp());
                        usageRecords.add(record);
                        record = new UsageRecord();

                }
            }
        }

        return usageRecords;
    }

    private int getFavoringUser() {
        int favoringUserID = -1;
        int favoringUserCount = Integer.MIN_VALUE;
        for (Map.Entry<Integer, Integer> entry : userCheckouts.entrySet()) {
            int userID = entry.getKey();
            int count = entry.getValue();

            if (count > favoringUserCount) {
                favoringUserID = userID;
                favoringUserCount = count;
            }
        }

        return favoringUserID;
    }
}
