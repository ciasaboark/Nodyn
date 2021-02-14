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

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import io.phobotic.nodyn_app.database.audit.model.Audit;
import io.phobotic.nodyn_app.database.audit.model.AuditHeader;
import io.phobotic.nodyn_app.database.audit.model.AuditDefinition;
import io.phobotic.nodyn_app.database.audit.model.AuditDetail;

/**
 * Created by Jonathan Nelson on 2020-02-18.
 */

@Database(entities = {AuditHeader.class, AuditDefinition.class, AuditDetail.class}, version = 1, exportSchema = false)
public abstract class AuditDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "audits_database";
    private static volatile AuditDatabase INSTANCE;

    public static AuditDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AuditDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AuditDatabase.class, DATABASE_NAME)
                            .allowMainThreadQueries()
                            .build();
                }
            }
        }

        return INSTANCE;
    }

    public abstract HeaderDao headerDao();
    public abstract DetailDao detailDao();
    public abstract DefinitionDao definitionDao();

    public void pruneExtractedAudits() {
        List<AuditHeader> extractedHeaders = headerDao().findExtracted();
        for (AuditHeader h: extractedHeaders) {
            detailDao().deleteAudit(h.getId());
            headerDao().delete(h.getId());
        }

        //also prune any headers that do not have any detail records
    }

    public List<Audit> getAllUnextracted() {
        List<Audit> audits = new ArrayList<>();

        List<AuditHeader> unextractedHeaders = headerDao().findUnExtracted();
        for (AuditHeader h: unextractedHeaders) {
            List<AuditDetail> details = detailDao().findByAudit(h.getId());
            Audit audit = new Audit(h);
            audit.setDetails(details);
            audits.add(audit);
        }

        return audits;
    }

}
