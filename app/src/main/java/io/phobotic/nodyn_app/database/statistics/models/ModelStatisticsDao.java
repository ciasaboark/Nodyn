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

package io.phobotic.nodyn_app.database.statistics.models;

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
public abstract class ModelStatisticsDao {
    @Delete
    public abstract void delete(ModelStatistics statistics);

    @Query("SELECT * from model_statistics")
    public abstract List<ModelStatistics> getAll();

    @Query("SELECT * FROM model_statistics WHERE id = :id")
    public abstract ModelStatistics getStatisticsForAsset(int id);

    /**
     * Insert the given ScanRecords into the database.  Insertion will only be performed
     * if the user has enabled the scan log entries in the application preferences
     *
     * @param records
     */
    public void replace(List<ModelStatistics> records) {
        deleteAll();
        insertAll(records);
    }

    @Query("DELETE FROM model_statistics")
    public abstract void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(List<ModelStatistics> records);
}
