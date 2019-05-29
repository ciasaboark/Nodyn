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

package io.phobotic.nodyn_app.database.audit;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.audit.helper.AuditDetailHelper;
import io.phobotic.nodyn_app.database.audit.helper.AuditHelper;
import io.phobotic.nodyn_app.database.audit.helper.DefinedAuditHelper;
import io.phobotic.nodyn_app.database.audit.model.Audit;
import io.phobotic.nodyn_app.database.audit.model.AuditDefinition;
import io.phobotic.nodyn_app.database.audit.model.AuditDetailRecord;
import io.phobotic.nodyn_app.database.exception.AuditDefinitionNotFoundException;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 1/14/18.
 */

public class AuditDatabase {
    private static final String TAG = AuditDatabase.class.getSimpleName();
    private static AuditDatabase instance;
    private final Context context;
    private final SQLiteDatabase db;
    private final AuditDetailHelper detailsHelper;
    private final AuditHelper auditHelper;
    private final DefinedAuditHelper definedAuditHelper;


    public static AuditDatabase getInstance(Context context) {
        if (instance == null) {
            instance = new AuditDatabase(context);
        }

        return instance;
    }

    private AuditDatabase(Context context) {
        this.context = context;
        AuditDatabaseOpenHelper helper = new AuditDatabaseOpenHelper(context);
        this.db = helper.getWritableDatabase();
        this.auditHelper = new AuditHelper(db);
        this.detailsHelper = new AuditDetailHelper(db);
        this.definedAuditHelper = new DefinedAuditHelper(db);
    }

    public Audit getAudit(int auditID) {
        Audit audit = auditHelper.findByID(auditID);
        if (audit != null) {
            List<AuditDetailRecord> detailRecords = detailsHelper.findByAuditID(auditID);
            audit.setDetailRecords(detailRecords);
        }

        return audit;
    }

    /**
     * Create a new {@link Audit} from this audit definition.  This method will include any
     * models or statuses that were not present at the time the audit definition was created
     * (provided {@link AuditDefinition#auditAllModels} or
     * {@link AuditDefinition#auditAllStatuses} is set
     *
     * @param user
     * @param auditDefinition
     * @return
     */
    public Audit createAuditFromDefinition(User user, AuditDefinition auditDefinition) {
        Audit audit = Audit.newEmptyAudit(System.currentTimeMillis(), user, auditDefinition);
        long rowID = auditHelper.insert(audit);

        Database db = Database.getInstance(context);

        List<Model> allModels = db.getModels();
        List<Integer> modelIDs = new ArrayList<>();
        if (!auditDefinition.isAuditAllModels()) {
            modelIDs.addAll(auditDefinition.getRequiredModelIDs());
        } else {
            for (Model m : allModels) {
                modelIDs.add(m.getId());
            }
        }

        List<Status> allStatuses = db.getStatuses();
        List<Integer> statusIDs = new ArrayList<>();
        if (!auditDefinition.isAuditAllStatuses()) {
            statusIDs.addAll(auditDefinition.getRequiredStatusIDs());
        } else {
            for (Status s : allStatuses) {
                statusIDs.add(s.getId());
            }
        }

        audit.setId((int) rowID)
                .setModelIDs(modelIDs)
                .setStatusIDs(statusIDs)
                .setDefinedAuditID(auditDefinition.getId());

        return audit;
    }

    public List<Audit> getAudits() {
        List<Audit> audits = auditHelper.findAll();
        for (Audit audit : audits) {
            int auditID = audit.getId();
            List<AuditDetailRecord> detailRecords = detailsHelper.findByAuditID(auditID);
            audit.setDetailRecords(detailRecords);
        }

        return audits;
    }

    public List<AuditDetailRecord> getDetailRecords() {
        return detailsHelper.findAll();
    }

    public void storeAudit(@NotNull Audit audit) {
        auditHelper.insert(audit);
        detailsHelper.removeAuditDetails(audit.getId());
        if (audit.getDetailRecords() != null) {
            for (AuditDetailRecord detailRecord : audit.getDetailRecords()) {
                detailsHelper.insert(detailRecord);
            }
        }

        if (audit.isCompleted() && audit.getDefinedAuditID() != -1) {
            updateDefinitionLastCompleted(audit);
        }
    }

    private void updateDefinitionLastCompleted(Audit audit) {
        AuditDefinition definition = definedAuditHelper.findByID(audit.getDefinedAuditID());
        if (definition != null) {
            definition.setLastAuditTimestamp(audit.getEnd());
            definedAuditHelper.insert(definition);
        }
    }

    public void pruneExtractedRecords() {
        List<Audit> extractedHeaders = auditHelper.findExtracted();
        for (Audit audit : extractedHeaders) {
            auditHelper.remove(audit.getId());
            detailsHelper.removeAuditDetails(audit.getId());
        }
    }

    public List<AuditDefinition> getDefinedAudits() {
        List<AuditDefinition> auditDefinitions = definedAuditHelper.findAll();
        return auditDefinitions;
    }

    public void deleteDefinedAudit(int definedAuditID) {
        definedAuditHelper.remove(definedAuditID);
    }

    public void storeDefinedAudit(AuditDefinition auditDefinition) {
        definedAuditHelper.insert(auditDefinition);
    }

    public void updateDefinedAuditUpdateTimestamp(int id) {
        AuditDefinition auditDefinition = definedAuditHelper.findByID(id);
        if (auditDefinition != null) {
            auditDefinition.setUpdateTimestamp(System.currentTimeMillis());
            definedAuditHelper.insert(auditDefinition);
        }
    }

    public void updateDefinedAuditLastAuditTimestamp(int id) {
        AuditDefinition auditDefinition = definedAuditHelper.findByID(id);
        if (auditDefinition != null) {
            auditDefinition.setLastAuditTimestamp(System.currentTimeMillis());
            definedAuditHelper.insert(auditDefinition);
        }
    }

    public AuditDefinition findAuditDefinitionByID(int definedAuditID) throws AuditDefinitionNotFoundException {
        AuditDefinition definition = definedAuditHelper.findByID(definedAuditID);
        if (definition == null) {
            throw new AuditDefinitionNotFoundException();
        }

        return definition;
    }

}