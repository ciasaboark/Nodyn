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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 1/14/18.
 */

public class Audit implements Serializable {
    private int id = -1;
    private long begin = -1;
    private long end = -1;
    private int userID = -1;
    private boolean completed = false;
    private boolean extracted = false;
    private int definedAuditID = -1;
    private List<Integer> modelIDs;
    private List<Integer> statusIDs;
    private List<AuditDetailRecord> detailRecords;
    private boolean isBlindAudit;

    public static Audit newEmptyAudit(long begin, User user, AuditDefinition auditDefinition) {
        Audit audit = new Audit(begin, user, auditDefinition);
        return audit;
    }

    public Audit(int id, long begin, long end, int userID, List<Integer> modelIDs,
                 List<Integer> statusIDs, boolean completed, boolean extracted, int definedAuditID,
                 boolean isBlindAudit) {
        this.id = id;
        this.begin = begin;
        this.end = end;
        this.userID = userID;
        this.modelIDs = modelIDs;
        this.statusIDs = statusIDs;
        this.completed = completed;
        this.extracted = extracted;
        this.definedAuditID = definedAuditID;
        this.detailRecords = new ArrayList<>();
        this.isBlindAudit = isBlindAudit;
    }

    private Audit(long begin, User user, AuditDefinition auditDefinition) {
        this.begin = begin;
        if (user != null) {
            this.userID = user.getId();
        }
        this.modelIDs = auditDefinition.getRequiredModelIDs();
        this.statusIDs = auditDefinition.getRequiredStatusIDs();
        this.definedAuditID = auditDefinition.getId();
    }

    public List<AuditDetailRecord> getDetailRecords() {
        return detailRecords;
    }

    public Audit setDetailRecords(List<AuditDetailRecord> detailRecords) {
        this.detailRecords = detailRecords;
        return this;
    }

    public void addDetailRecord(AuditDetailRecord record) {
        this.detailRecords.add(record);
    }

    public List<Integer> getModelIDs() {
        return modelIDs;
    }

    public Audit setModelIDs(List<Integer> modelIDs) {
        this.modelIDs = modelIDs;
        return this;
    }

    public int getDefinedAuditID() {
        return definedAuditID;
    }

    public Audit setDefinedAuditID(int definedAuditID) {
        this.definedAuditID = definedAuditID;
        return this;
    }

    public List<Integer> getStatusIDs() {
        return statusIDs;
    }

    public Audit setStatusIDs(List<Integer> statusIDs) {
        this.statusIDs = statusIDs;
        return this;
    }

    public int getId() {
        return id;
    }

    public Audit setId(int id) {
        this.id = id;
        return this;
    }

    public long getBegin() {
        return begin;
    }

    public Audit setBegin(long begin) {
        this.begin = begin;
        return this;
    }

    public long getEnd() {
        return end;
    }

    public Audit setEnd(long end) {
        this.end = end;
        return this;
    }

    public int getUserID() {
        return userID;
    }

    public Audit setUserID(int userID) {
        this.userID = userID;
        return this;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Audit setCompleted(boolean completed) {
        this.completed = completed;
        return this;
    }

    public boolean isExtracted() {
        return extracted;
    }

    public Audit setExtracted(boolean extracted) {
        this.extracted = extracted;
        return this;
    }

    public boolean isBlindAudit() {
        return isBlindAudit;
    }

    public Audit setBlindAudit(boolean blindAudit) {
        isBlindAudit = blindAudit;
        return this;
    }
}
