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

package io.phobotic.nodyn_app.database.statistics.day_activity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * Created by Jonathan Nelson on 2019-05-11.
 */
@Dao
public abstract class DayActivityDao {
    @Delete
    public abstract void delete(DayActivity dayActivity);

    @Query("SELECT * from day_statistics ORDER BY timestamp")
    public abstract List<DayActivity> getAll();

    @Query("SELECT * FROM day_statistics WHERE timestamp = :timestamp")
    public abstract DayActivity getActivityForDay(long timestamp);

    @Query("SELECT * FROM day_statistics WHERE timestamp >= :timestamp")
    public abstract List<DayActivity> getActivityWithCutoff(long timestamp);

    /**
     * Insert the given DayActivty records into the database.
     *
     * @param records
     */
    public void replace(List<DayActivity> records) {
        deleteAll();
        insertAll(records);
    }

    @Query("DELETE FROM day_statistics")
    public abstract void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(List<DayActivity> records);
}