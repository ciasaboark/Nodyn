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
import androidx.room.Transaction;

/**
 * Created by Jonathan Nelson on 2020-02-13.
 */
@Dao
public abstract class SyncAttemptDao {
    @Query("DELETE FROM sync_attempt WHERE startTime <= :cutoff AND (noticeType = 0 OR noticeSent = 1)")
    public abstract void prune(long cutoff);

    @Query("DELETE FROM sync_attempt")
    public abstract void deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertAll(List<SyncAttempt> syncAttempts);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(SyncAttempt syncAttempt);

    @Transaction
    @Query("SELECT * from sync_attempt")
    public abstract List<SyncAttempt> findAll();

    @Transaction
    @Query("SELECT * from sync_attempt WHERE noticeType <> 0 and noticeSent = 0 AND ((fullModelFetched = 0 AND syncType = 0) OR allActionItemsSynced = 0)")
    public abstract List<SyncAttempt> findUnsetFailures();

    @Query("SELECT * from sync_attempt WHERE fullModelFetched = 1 AND backend = :adapterName ORDER BY endTime DESC LIMIT 1")
    public abstract SyncAttempt findLastSuccessfulSync(String adapterName);

    @Query("SELECT * from sync_attempt WHERE fullModelFetched = 1 ORDER BY endTime")
    public abstract List<SyncAttempt> findLastSyncs();
}