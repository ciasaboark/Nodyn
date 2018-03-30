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

/**
 * Created by Jonathan Nelson on 1/23/18.
 */

public class AuditDefinition implements Serializable {
    public static final String META_ALL_ASSETS = "ALL";
    public static final String META_ASSIGNED_ASSETS = "ASSIGNED";
    private int id = -1;
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

//    public AuditDefinition(String name, long createTimestamp, List<Integer> requiredModelIDs,
//                           boolean auditAllModels, List<Integer> requiredStatusIDs,
//                           boolean auditAllStatuses, String metaStatus,String details,
//                           boolean isBlindAudit) {
//        this(-1, name, createTimestamp, -1, -1, requiredModelIDs, auditAllModels,
//                requiredStatusIDs, auditAllStatuses, metaStatus, details, isBlindAudit);
//    }
//
//    public AuditDefinition(int id, String name, long createTimestamp, long updateTimestamp,
//                           long lastAuditTimestamp, List<Integer> requiredModelIDs, boolean auditAllModels,
//                           List<Integer> requiredStatusIDs, boolean auditAllStatuses, String metaStatus,
//                           String details, boolean isBlindAudit) {
//        this.id = id;
//        this.name = name;
//        this.createTimestamp = createTimestamp;
//        this.updateTimestamp = updateTimestamp;
//        this.lastAuditTimestamp = lastAuditTimestamp;
//        this.requiredModelIDs = requiredModelIDs;
//        this.auditAllModels = auditAllModels;
//        this.requiredStatusIDs = requiredStatusIDs;
//        this.auditAllStatuses = auditAllStatuses;
//        this.metaStatus = metaStatus;
//        this.details = details;
//        this.isBlindAudit = isBlindAudit;
//    }

    public boolean isBlindAudit() {
        return isBlindAudit;
    }

    public AuditDefinition setBlindAudit(boolean blindAudit) {
        isBlindAudit = blindAudit;
        return this;
    }

    public int getId() {
        return id;
    }

    public AuditDefinition setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public AuditDefinition setName(String name) {
        this.name = name;
        return this;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    public AuditDefinition setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
        return this;
    }

    public long getUpdateTimestamp() {
        return updateTimestamp;
    }

    public AuditDefinition setUpdateTimestamp(long updateTimestamp) {
        this.updateTimestamp = updateTimestamp;
        return this;
    }

    public long getLastAuditTimestamp() {
        return lastAuditTimestamp;
    }

    public AuditDefinition setLastAuditTimestamp(long lastAuditTimestamp) {
        this.lastAuditTimestamp = lastAuditTimestamp;
        return this;
    }

    public List<Integer> getRequiredModelIDs() {
        return requiredModelIDs;
    }

    public AuditDefinition setRequiredModelIDs(List<Integer> requiredModelIDs) {
        this.requiredModelIDs = requiredModelIDs;
        return this;
    }

    public List<Integer> getRequiredStatusIDs() {
        return requiredStatusIDs;
    }

    public AuditDefinition setRequiredStatusIDs(List<Integer> requiredStatusIDs) {
        this.requiredStatusIDs = requiredStatusIDs;
        return this;
    }

    public String getDetails() {
        return details;
    }

    public AuditDefinition setDetails(String details) {
        this.details = details;
        return this;
    }

    public boolean isAuditAllModels() {
        return auditAllModels;
    }

    public AuditDefinition setAuditAllModels(boolean auditAllModels) {
        this.auditAllModels = auditAllModels;
        return this;
    }

    public boolean isAuditAllStatuses() {
        return auditAllStatuses;
    }

    public AuditDefinition setAuditAllStatuses(boolean auditAllStatuses) {
        this.auditAllStatuses = auditAllStatuses;
        return this;
    }

    public String getMetaStatus() {
        return metaStatus;
    }

    public AuditDefinition setMetaStatus(String metaStatus) {
        this.metaStatus = metaStatus;
        return this;
    }

    public String getSchedule() {
        return schedule;
    }

    public AuditDefinition setSchedule(String schedule) {
        this.schedule = schedule;
        return this;
    }
}
