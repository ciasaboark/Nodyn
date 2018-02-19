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

package io.phobotic.nodyn_app.database.statistics.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn_app.database.helper.TableHelper;
import io.phobotic.nodyn_app.database.statistics.StatisticsDatabaseOpenHelper;
import io.phobotic.nodyn_app.database.statistics.model.DayActivity;

/**
 * Created by Jonathan Nelson on 12/6/17.
 */

public class ThirtyDayHelper extends TableHelper<DayActivity> {
    private static final String TAG = ThirtyDayHelper.class.getSimpleName();

    public ThirtyDayHelper(@NotNull SQLiteDatabase db) {
        super(db);
    }

    @Override
    public void replace(List<DayActivity> dayActivities) {
        Log.d(TAG, "Replacing all day activity statistics with activity list of size " + dayActivities.size());

        //use '1' as the where clause so we can get a count of the rows deleted
        int count = db.delete(StatisticsDatabaseOpenHelper.TABLE_THIRTY_DAY, "1", null);
        Log.d(TAG, "Deleted " + count + " rows from assets table");

        for (DayActivity dayActivity : dayActivities) {
            insert(dayActivity);
        }

        Log.d(TAG, "Finished adding daily statistics to table");
    }

    @Override
    public long insert(DayActivity activity) {
        ContentValues cv = new ContentValues();
        cv.put(StatisticsDatabaseOpenHelper.Columns.TIMESTAMP, activity.getTimestamp());
        cv.put(StatisticsDatabaseOpenHelper.Columns.CHECKOUT_COUNT, activity.getCheckoutCount());
        cv.put(StatisticsDatabaseOpenHelper.Columns.CHECKIN_COUNT, activity.getCheckinCount());

        long rowID = db.insertWithOnConflict(StatisticsDatabaseOpenHelper.TABLE_THIRTY_DAY, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        return rowID;
    }

    @Override
    public DayActivity findByID(@NotNull int id) {
        return null;
    }

    @Override
    public DayActivity findByName(@NotNull String name) {
        return null;
    }

    @NotNull
    @Override
    public List<DayActivity> findAll() {
        List<DayActivity> dayActivities = new ArrayList<>();

        Cursor cursor = null;

        try {
            cursor = db.query(StatisticsDatabaseOpenHelper.TABLE_THIRTY_DAY, null, null, null,
                    null, null, StatisticsDatabaseOpenHelper.Columns.TIMESTAMP, null);
            while (cursor.moveToNext()) {
                DayActivity dayActivity = getDayActivityFromCursor(cursor);
                dayActivities.add(dayActivity);

            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for assets: " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return dayActivities;
    }

    private DayActivity getDayActivityFromCursor(@NotNull Cursor cursor) {
        long timestamp = cursor.getLong(cursor.getColumnIndex(StatisticsDatabaseOpenHelper.Columns.TIMESTAMP));
        int checkoutCount = cursor.getInt(cursor.getColumnIndex(StatisticsDatabaseOpenHelper.Columns.CHECKOUT_COUNT));
        int checkinCount = cursor.getInt(cursor.getColumnIndex(StatisticsDatabaseOpenHelper.Columns.CHECKIN_COUNT));

        DayActivity dayActivity = new DayActivity(timestamp, checkoutCount, checkinCount);

        return dayActivity;
    }
}

