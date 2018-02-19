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
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jonathan Nelson on 12/6/17.
 */

public class StatisticsDatabaseOpenHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "statistics";
    public static final String TABLE_THIRTY_DAY = "thirty_day";

    private static final int VERSION = 1;

    public StatisticsDatabaseOpenHelper(Context ctx) {
        super(ctx, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_THIRTY_DAY);
        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        createThirtyDayTable(db);
    }

    private void createThirtyDayTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_THIRTY_DAY + " ( " +
                Columns.TIMESTAMP + " integer primary key not null, " +
                Columns.CHECKIN_COUNT + " integer, " +
                Columns.CHECKOUT_COUNT + " integer " +
                ")");
    }

    public class Columns {
        public static final String TIMESTAMP = "timestamp";
        public static final String CHECKIN_COUNT = "checkin_count";
        public static final String CHECKOUT_COUNT = "checkout_count";
    }
}