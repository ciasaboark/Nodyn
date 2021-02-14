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

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import io.phobotic.nodyn_app.database.converter.StatusTypeListConverter;

/**
 * Created by Jonathan Nelson on 1/14/18.
 */

@Entity(tableName = "audit_detail")
@TypeConverters({StatusTypeListConverter.class})
public class AuditDetail implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private Integer id;
    private int auditID;
    private int assetID;
    private long timestamp;
    private Status status;
    private String notes;
    private boolean extracted;

    //used solely to maintain expanded/collapsed state in recyclerview
    private boolean isExpanded;

    public AuditDetail(@Nullable Integer id, int auditID, int assetID, long timestamp, Status status, String notes, boolean extracted) {
        this.id = id;
        this.auditID = auditID;
        this.assetID = assetID;
        this.timestamp = timestamp;
        this.status = status;
        this.notes = notes;
        this.extracted = extracted;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getAuditID() {
        return auditID;
    }

    public void setAuditID(int auditID) {
        this.auditID = auditID;
    }

    public int getAssetID() {
        return assetID;
    }

    public void setAssetID(int assetID) {
        this.assetID = assetID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isExtracted() {
        return extracted;
    }

    public void setExtracted(boolean extracted) {
        this.extracted = extracted;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public enum Status {
        UNDAMAGED,
        DAMAGED,
        OTHER,
        NOT_AUDITED,
        UNEXPECTED
    }
}
