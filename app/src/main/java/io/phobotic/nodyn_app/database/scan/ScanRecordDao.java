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

package io.phobotic.nodyn_app.database.scan;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.phobotic.nodyn_app.R;

/**
 * Created by Jonathan Nelson on 4/2/18.
 */
@Dao
public abstract class ScanRecordDao {
    private Context context;

    @Query("SELECT * FROM scanlog")
    public abstract List<ScanRecord> getAll();

    @Query("SELECT * FROM scanlog LIMIT 1")
    public abstract ScanRecord getOne();

    @Query("SELECT * FROM scanlog WHERE timestamp BETWEEN :begin AND :end")
    public abstract List<ScanRecord> loadAllByIds(long begin, long end);

    /**
     * Insert the given ScanRecords into the database.  Insertion will only be performed
     * if the user has enabled the scan log entries in the application preferences
     *
     * @param records
     */
    public void upsertAll(ScanRecord... records) {
        boolean insertRecords = false;
        if (context != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            insertRecords = prefs.getBoolean(context.getString(R.string.pref_key_beta_scanlog_enable),
                    Boolean.parseBoolean(context.getString(R.string.pref_default_beta_scanlog_enable)));
        }

        if (insertRecords) {
            pruneOldRecords();
            insertAll(records);
        }
    }

    @Query("DELETE FROM scanlog where id NOT IN (SELECT id from scanlog ORDER BY id DESC LIMIT 1000)")
    public abstract void pruneOldRecords();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(ScanRecord... records);

    @Delete
    public abstract void delete(ScanRecord record);

    public void setContext(Context context) {
        this.context = context;
    }
}
