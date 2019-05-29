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

package io.phobotic.nodyn_app.database;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;
import io.phobotic.nodyn_app.database.scan.ScanRecordDatabase;

/**
 * Created by Jonathan Nelson on 4/7/18.
 */
public class RoomDBWrapper {
    private static RoomDBWrapper instance;
    private final ScanRecordDatabase scanRecordDatabase;

    public static RoomDBWrapper getInstance(Context context) {
        if (instance == null) {
            instance = new RoomDBWrapper(context);
        }
        return instance;
    }

    private RoomDBWrapper(final Context context) {
        RoomDatabase.Callback rdc = new RoomDatabase.Callback() {
            @Override
            public void onOpen(@NonNull SupportSQLiteDatabase db) {
                scanRecordDatabase.scanRecordDao().setContext(context);
            }
        };

        scanRecordDatabase = Room.databaseBuilder(context,
                ScanRecordDatabase.class, "scan_records.dbfoo")
                .addCallback(rdc)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        //in order for the callback to fire we need to perform at least one query.  It's
        //+ important to handle this before any insertions are done, otherwise we won't
        //+ have access to the user's preferences to determine whether an insertion should
        //+ happen or not
        scanRecordDatabase.scanRecordDao().getOne();
    }

    public ScanRecordDatabase getScanRecordDatabase() {
        return scanRecordDatabase;
    }
}
