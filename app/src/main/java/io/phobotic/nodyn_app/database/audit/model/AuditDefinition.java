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

import org.apache.poi.util.IntList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import io.phobotic.nodyn_app.database.converter.IntegerListTypeConverter;

/**
 * Created by Jonathan Nelson on 1/23/18.
 */

@Entity(tableName = "audit_definition")
@TypeConverters({IntegerListTypeConverter.class})
public class AuditDefinition implements Serializable {
    public static final String META_ALL_ASSETS = "ALL";
    public static final String META_ASSIGNED_ASSETS = "ASSIGNED";

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private long createTimestamp = -1;
    private long updateTimestamp = -1;
    private long lastAuditTimestamp = -1;
    private List<Integer> requiredModelIDs = new ArrayList<>();
    private boolean auditAllModels = false;
    private List<Integer> requiredStatusIDs = new ArrayList<>();
    private boolean auditAllStatuses = false;
    private String metaStatus;
    private String details;
    private boolean isBlindAudit = false;
    private String schedule;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public void setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
    }

    public long getLastAuditTimestamp() {
        return lastAuditTimestamp;
    }

    public void setLastAuditTimestamp(long lastAuditTimestamp) {
        this.lastAuditTimestamp = lastAuditTimestamp;
    }

    public List<Integer> getRequiredModelIDs() {
        return requiredModelIDs;
    }

    public void setRequiredModelIDs(List<Integer> requiredModelIDs) {
        this.requiredModelIDs = requiredModelIDs;
    }

    public boolean isAuditAllModels() {
        return auditAllModels;
    }

    public void setAuditAllModels(boolean auditAllModels) {
        this.auditAllModels = auditAllModels;
    }

    public List<Integer> getRequiredStatusIDs() {
        return requiredStatusIDs;
    }

    public void setRequiredStatusIDs(List<Integer> requiredStatusIDs) {
        this.requiredStatusIDs = requiredStatusIDs;
    }

    public boolean isAuditAllStatuses() {
        return auditAllStatuses;
    }

    public void setAuditAllStatuses(boolean auditAllStatuses) {
        this.auditAllStatuses = auditAllStatuses;
    }

    public String getMetaStatus() {
        return metaStatus;
    }

    public void setMetaStatus(String metaStatus) {
        this.metaStatus = metaStatus;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean isBlindAudit() {
        return isBlindAudit;
    }

    public void setBlindAudit(boolean blindAudit) {
        isBlindAudit = blindAudit;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }
}
