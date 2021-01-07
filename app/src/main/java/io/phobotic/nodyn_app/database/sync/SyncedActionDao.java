/*
 * Copyright (c) 2020 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.database.sync;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

/**
 * Created by Jonathan Nelson on 2020-02-13.
 */
@Dao
public interface SyncedActionDao {
    @Query("SELECT * from synced_action where syncUuid = :syncUuid")
    List<SyncedAction> getActions(String syncUuid);

    @Query("DELETE from synced_action where syncUuid = :syncUuid")
    void deleteSyncActions(String syncUuid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SyncedAction syncedAction);
}
