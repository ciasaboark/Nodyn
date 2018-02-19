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

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * Created by Jonathan Nelson on 1/14/18.
 */

public class AuditDetailRecord implements Serializable {
    private Integer id;
    private int auditID;
    private int assetID;
    private long timestamp;
    private Status status;
    private String notes;
    private boolean extracted;

    //used solely to maintain expanded/collapsed state in recyclerview
    private boolean isExpanded;

    public AuditDetailRecord(@Nullable Integer id, int auditID, int assetID, long timestamp, Status status, String notes, boolean extracted) {
        this.id = id;
        this.auditID = auditID;
        this.assetID = assetID;
        this.timestamp = timestamp;
        this.status = status;
        this.notes = notes;
        this.extracted = extracted;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public AuditDetailRecord setExpanded(boolean expanded) {
        isExpanded = expanded;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public AuditDetailRecord setId(Integer id) {
        this.id = id;
        return this;
    }

    public int getAuditID() {
        return auditID;
    }

    public AuditDetailRecord setAuditID(int auditID) {
        this.auditID = auditID;
        return this;
    }

    public int getAssetID() {
        return assetID;
    }

    public AuditDetailRecord setAssetID(int assetID) {
        this.assetID = assetID;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public AuditDetailRecord setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public AuditDetailRecord setStatus(Status status) {
        this.status = status;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public AuditDetailRecord setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public boolean isExtracted() {
        return extracted;
    }

    public AuditDetailRecord setExtracted(boolean extracted) {
        this.extracted = extracted;
        return this;
    }

    public enum Status {
        UNDAMAGED,
        DAMAGED,
        OTHER,
        NOT_AUDITED,
        UNEXPECTED
    }
}
