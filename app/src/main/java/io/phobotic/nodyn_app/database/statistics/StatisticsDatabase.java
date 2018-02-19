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

package io.phobotic.nodyn_app.database.statistics;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import io.phobotic.nodyn_app.database.statistics.helper.ThirtyDayHelper;
import io.phobotic.nodyn_app.database.statistics.model.DayActivity;

/**
 * Created by Jonathan Nelson on 12/6/17.
 */

public class StatisticsDatabase {
    public static final String BROADCAST_ASSET_CHECKOUT = "asset_checkout";
    public static final String BROADCASE_ASSET_CHECKIN = "asset_checkin";
    private static final String TAG = StatisticsDatabase.class.getSimpleName();
    private static StatisticsDatabase instance;
    private final Context context;
    private final SQLiteDatabase db;
    private final ThirtyDayHelper thirtyDayHelper;


    public static StatisticsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new StatisticsDatabase(context);
        }

        return instance;
    }

    private StatisticsDatabase(Context context) {
        this.context = context;
        StatisticsDatabaseOpenHelper helper = new StatisticsDatabaseOpenHelper(context);
        this.db = helper.getWritableDatabase();
        thirtyDayHelper = new ThirtyDayHelper(db);
    }

    public List<DayActivity> getThirtyDayActivity() {
        return thirtyDayHelper.findAll();
    }

    public void replaceThirtyDayActivity(List<DayActivity> dayActivities) {
        thirtyDayHelper.replace(dayActivities);
    }

}