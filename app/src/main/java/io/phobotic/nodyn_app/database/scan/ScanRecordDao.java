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

package io.phobotic.nodyn_app.database.scan;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by Jonathan Nelson on 4/2/18.
 */
@Dao
public abstract class ScanRecordDao {
    @Query("SELECT * FROM scanlog")
    public abstract List<ScanRecord> getAll();

    @Query("SELECT * FROM scanlog WHERE timestamp BETWEEN :begin AND :end")
    public abstract List<ScanRecord> loadAllByIds(long begin, long end);

    public void upsertAll(ScanRecord... records) {
        pruneOldRecords();
        insertAll(records);
    }

    @Query("DELETE FROM scanlog where id NOT IN (SELECT id from scanlog ORDER BY id DESC LIMIT 10)")
    public abstract void pruneOldRecords();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(ScanRecord... records);

    @Delete
    public abstract void delete(ScanRecord record);
}
