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

package io.phobotic.nodyn_app.database.sync;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Created by Jonathan Nelson on 8/20/17.
 */

@Entity(tableName = "synced_action")
public class SyncedAction {
    @PrimaryKey
    private int id;

    //A reference back to the sync attempt
    private String syncUuid;

    private String direction;
    private int assetID;
    private int userID;
    private String authorization = null;
    private long timestamp;
    private long expectedCheckin = -1;
    private boolean verified = false;
    private String notes = null;

    private boolean isSyncSuccess;
    private boolean willRetrySync;
    private String exceptionType = null;
    private String exceptionMessage = null;
    private String message = null;

    public static SyncedAction fromAction(String syncUuid, Action action) {
        if (syncUuid == null || syncUuid.length() == 0) {
            throw new IllegalArgumentException("Sync UUID can not be null or empty string");
        }
        if (action == null) {
            throw new IllegalArgumentException("Action can not be null");
        }

        SyncedAction syncedAction = new SyncedAction();
        syncedAction.syncUuid = syncUuid;
        syncedAction.direction = action.getDirection().toString();
        syncedAction.assetID = action.getAssetID();
        syncedAction.userID = action.getUserID();
        syncedAction.authorization = action.getAuthorization();
        syncedAction.timestamp = action.getTimestamp();
        syncedAction.expectedCheckin = action.getExpectedCheckin();
        syncedAction.verified = action.isVerified();
        syncedAction.notes = action.getNotes();

        return syncedAction;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSyncUuid() {
        return syncUuid;
    }

    public void setSyncUuid(String syncUuid) {
        this.syncUuid = syncUuid;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getAssetID() {
        return assetID;
    }

    public void setAssetID(int assetID) {
        this.assetID = assetID;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getExpectedCheckin() {
        return expectedCheckin;
    }

    public void setExpectedCheckin(long expectedCheckin) {
        this.expectedCheckin = expectedCheckin;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isSyncSuccess() {
        return isSyncSuccess;
    }

    public void setSyncSuccess(boolean syncSuccess) {
        isSyncSuccess = syncSuccess;
    }

    public boolean isWillRetrySync() {
        return willRetrySync;
    }

    public void setWillRetrySync(boolean willRetrySync) {
        this.willRetrySync = willRetrySync;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
