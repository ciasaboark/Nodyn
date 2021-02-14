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

package io.phobotic.nodyn_app.database.statistics.summary.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.sync.Action;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.statistics.SummedAction;

/**
 * Created by Jonathan Nelson on 2019-05-11.
 */
public class ModelStatisticsBuilder {
    private final String TAG = ModelStatisticsBuilder.class.getSimpleName();
    private final SharedPreferences prefs;
    private final Database db;
    private final Context context;
    private final Model model;
    private final Map<Asset, Integer> assetUsageMap;
    private final List<Action> actionList;

    public ModelStatisticsBuilder(@NotNull Context context, @NotNull Model model) {
        this.context = context;
        this.model = model;
        this.assetUsageMap = new HashMap<>();
        this.actionList = new ArrayList<>();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        db = Database.getInstance(context);
    }

    public void recordAction(@NotNull Action action) {
        actionList.add(action);

        if (action.getDirection() == Action.Direction.CHECKOUT) {
            try {
                Asset a = db.findAssetByID(action.getAssetID());
                Integer i = assetUsageMap.get(a);
                if (i == null) i = 0;

                i++;

                assetUsageMap.put(a, i);
            } catch (AssetNotFoundException e) {
                Log.d(TAG, String.format("Skipping action record for asset with ID %d. " +
                        "Could not find matching asset in the database", action.getAssetID()));
            }

        }
    }

    public ModelStatistics build() {
        ModelStatistics modelStatistics = new ModelStatistics();
        modelStatistics.setLastUpdated(System.currentTimeMillis());
        modelStatistics.setId(model.getId());

        //it's possible some of the assets were not used in the time frame we are looking at.
        //+ Go ahead and build a list of all assets that could be checked out

        boolean allowAllStatuses = prefs.getBoolean(context.getString(R.string.pref_key_asset_status_allow_all),
                Boolean.parseBoolean(context.getString(R.string.pref_default_asset_status_allow_all)));
        Set<String> allowedStatuses = prefs.getStringSet(context.getString(
                R.string.pref_key_asset_status_allowed_statuses), new HashSet<String>());

        List<Asset> allAssets = db.findAssetsByModelID(model.getId());
        List<Asset> filteredList = new ArrayList<>();
        if (allowAllStatuses) {
            filteredList = allAssets;
        } else {
            filteredList = new ArrayList<>();
            for (Asset asset : allAssets) {
                if (allowedStatuses.contains(String.valueOf(asset.getStatusID()))) {
                    filteredList.add(asset);
                }
            }
        }

        //zero out the usage for any asset not already in the usage map
        for (Asset asset : filteredList) {
            Integer usage = assetUsageMap.get(asset);
            if (usage == null) {
                assetUsageMap.put(asset, 0);
            }
        }

        int leastUsedAssetID = -1;
        int leastUsedCount = Integer.MAX_VALUE;

        int mostUsedAssetID = -1;
        int mostUsedCount = Integer.MIN_VALUE;

        for (Map.Entry<Asset, Integer> entry : assetUsageMap.entrySet()) {
            Asset a = entry.getKey();
            Integer count = entry.getValue();

            if (count < leastUsedCount) {
                leastUsedAssetID = a.getId();
                leastUsedCount = count;
            }

            if (count > mostUsedCount) {
                mostUsedAssetID = a.getId();
                mostUsedCount = count;
            }
        }

        modelStatistics.setLeasttUsedAssetID(leastUsedAssetID);
        modelStatistics.setMostUsedAssetID(mostUsedAssetID);
        modelStatistics.setActionHistory(getActionSummary());

        return modelStatistics;
    }

    private List<SummedAction> getActionSummary() {

        Calendar calendar = Calendar.getInstance();
        Map<Long, SummedAction> summedActionMap = new HashMap<>();

        for (Action action : actionList) {
            calendar.setTimeInMillis(action.getTimestamp());
            shiftToBeginningOfDay(calendar);
            long timestamp = calendar.getTimeInMillis();

            SummedAction summedAction = summedActionMap.get(timestamp);
            if (summedAction == null) summedAction = new SummedAction(timestamp, 0, 0, 0);

            switch (action.getDirection()) {
                case CHECKOUT:
                    summedAction.setTotalCheckouts(summedAction.getTotalCheckouts() + 1);
                    break;
                case CHECKIN:
                    summedAction.setTotalCheckins(summedAction.getTotalCheckins() + 1);
                    break;
            }

            summedActionMap.put(timestamp, summedAction);
        }

        List<SummedAction> list = new ArrayList<>();
        for (Map.Entry<Long, SummedAction> entry : summedActionMap.entrySet()) {
            list.add(entry.getValue());
        }

        return list;
    }

    private void shiftToBeginningOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
    }
}
