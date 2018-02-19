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

package io.phobotic.nodyn_app.database.model;

/**
 * Created by Jonathan Nelson on 7/19/17.
 */

public class MaintenanceRecord {
    private String type;
    private long createdAt;
    private int assetID;
    private long startTime;
    private long completeTime;
    private String notes;
    private String title;
    private String supplier;
    private int userID;

    public String getType() {
        return type;
    }

    public MaintenanceRecord setType(String type) {
        this.type = type;
        return this;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public MaintenanceRecord setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public int getAssetID() {
        return assetID;
    }

    public MaintenanceRecord setAssetID(int assetID) {
        this.assetID = assetID;
        return this;
    }

    public long getStartTime() {
        return startTime;
    }

    public MaintenanceRecord setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public long getCompleteTime() {
        return completeTime;
    }

    public MaintenanceRecord setCompleteTime(long completeTime) {
        this.completeTime = completeTime;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public MaintenanceRecord setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MaintenanceRecord setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getSupplier() {
        return supplier;
    }

    public MaintenanceRecord setSupplier(String supplier) {
        this.supplier = supplier;
        return this;
    }

    public int getUserID() {
        return userID;
    }

    public MaintenanceRecord setUserID(int userID) {
        this.userID = userID;
        return this;
    }
}
