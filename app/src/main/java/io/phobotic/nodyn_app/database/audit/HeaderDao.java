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

package io.phobotic.nodyn_app.database.audit;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import io.phobotic.nodyn_app.database.audit.model.AuditHeader;

/**
 * Created by Jonathan Nelson on 2020-02-18.
 */

@Dao
public abstract class HeaderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(AuditHeader auditHeader);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract List<Long> insert(List<AuditHeader> list);

    @Query("SELECT * FROM audit_header WHERE id = :id")
    public abstract AuditHeader find(int id);

    @Query("SELECT * FROM audit_header")
    public abstract List<AuditHeader> findAll();

    @Query("SELECT * FROM audit_header WHERE extracted = 1")
    public abstract List<AuditHeader> findExtracted();

    @Query("SELECT * FROM audit_header WHERE extracted = 0")
    public abstract List<AuditHeader> findUnExtracted();

    @Query("DELETE FROM audit_header where id = :id")
    public abstract void delete(int id);

    @Query("DELETE FROM audit_header")
    public abstract void clear();

    public void replace(List<AuditHeader> list) {
        clear();
        insert(list);
    }
}