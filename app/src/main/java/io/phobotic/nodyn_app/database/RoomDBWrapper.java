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

package io.phobotic.nodyn_app.database;

import android.arch.persistence.room.Room;
import android.content.Context;

import io.phobotic.nodyn_app.database.scan.ScanRecordDatabase;

/**
 * Created by Jonathan Nelson on 4/7/18.
 */
public class RoomDBWrapper {
    private static RoomDBWrapper instance;
    private final ScanRecordDatabase scanRecordDatabase;

    public static RoomDBWrapper getInstance(Context context) {
        instance = new RoomDBWrapper(context);
        return instance;
    }

    private RoomDBWrapper(Context context) {
        scanRecordDatabase = Room.databaseBuilder(context,
                ScanRecordDatabase.class, "scan_records")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    }

    public ScanRecordDatabase getScanRecordDatabase() {
        return scanRecordDatabase;
    }
}
