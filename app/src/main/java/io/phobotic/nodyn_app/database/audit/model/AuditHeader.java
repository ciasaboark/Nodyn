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

package io.phobotic.nodyn_app.database.audit.model;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.audit.AuditDatabase;
import io.phobotic.nodyn_app.database.converter.IntegerListTypeConverter;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 1/14/18.
 */

@Entity(tableName = "audit_header")
@TypeConverters({IntegerListTypeConverter.class})
public class AuditHeader implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private long begin = -1;
    private long end = -1;
    private int userID = -1;
    private boolean completed = false;
    private boolean extracted = false;
    private int definedAuditID = -1;
    private List<Integer> modelIDs;
    private List<Integer> statusIDs;
    private boolean isBlindAudit;

    public static AuditHeader newEmptyAudit(long begin, User user, AuditDefinition auditDefinition) {
        AuditHeader audit = new AuditHeader();
        audit.setBegin(begin);
        audit.setUserID(user == null ? -1 : user.getId());
        audit.setDefinedAuditID(auditDefinition.getId());
        return audit;
    }

    public static AuditHeader fromDefinition(Context context, User user, AuditDefinition definition) {
        AuditHeader header = AuditHeader.newEmptyAudit(System.currentTimeMillis(), user, definition);
        AuditDatabase auditDatabase = AuditDatabase.getInstance(context);
        long rowID = auditDatabase.headerDao().insert(header);

        Database db = Database.getInstance(context);

        List<Model> allModels = db.getModels();
        List<Integer> modelIDs = new ArrayList<>();
        if (!definition.isAuditAllModels()) {
            modelIDs.addAll(definition.getRequiredModelIDs());
        } else {
            for (Model m: allModels) {
                modelIDs.add(m.getId());
            }
        }

        List<Status> allStatuses = db.getStatuses();
        List<Integer> statusIDs = new ArrayList<>();
        if (!definition.isAuditAllStatuses()) {
            statusIDs.addAll(definition.getRequiredStatusIDs());
        } else {
            for (Status s: allStatuses) {
                statusIDs.add(s.getId());
            }
        }

        header.setId((int) rowID);
        header.setModelIDs(modelIDs);
        header.setStatusIDs(statusIDs);
        header.setDefinedAuditID(definition.getId());

        return header;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isExtracted() {
        return extracted;
    }

    public void setExtracted(boolean extracted) {
        this.extracted = extracted;
    }

    public int getDefinedAuditID() {
        return definedAuditID;
    }

    public void setDefinedAuditID(int definedAuditID) {
        this.definedAuditID = definedAuditID;
    }

    public List<Integer> getModelIDs() {
        return modelIDs;
    }

    public void setModelIDs(List<Integer> modelIDs) {
        this.modelIDs = modelIDs;
    }

    public List<Integer> getStatusIDs() {
        return statusIDs;
    }

    public void setStatusIDs(List<Integer> statusIDs) {
        this.statusIDs = statusIDs;
    }

    public boolean isBlindAudit() {
        return isBlindAudit;
    }

    public void setBlindAudit(boolean blindAudit) {
        isBlindAudit = blindAudit;
    }
}
