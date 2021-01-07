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

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

/**
 * Created by Jonathan Nelson on 2020-02-13.
 */

@Entity(tableName = "sync_attempt")
public class SyncAttempt {
    public static final int SYNC_TYPE_FULL = 0;
    public static final int SYNC_TYPE_QUICK = 1;

    @PrimaryKey
    @NotNull
    private String uuid;

    private long startTime;
    private long endTime;
    private String exceptionClass;
    private String exceptionMessage;
    private String backend;
    private String releaseName;
    private int releaseCode;
    //no notice = 0, email alert = 1
    private int noticeType;
    private boolean noticeSent;
    //full sync = 0, quick sync = 1
    private int syncType;
    private boolean fullModelFetched;
    private boolean allActionItemsSynced;

    public SyncAttempt(String uuid, long startTime, long endTime, int syncType, boolean fullModelFetched,
                       boolean allActionItemsSynced,
                       String exceptionClass, String exceptionMessage, String backend,
                       String releaseName, int releaseCode, int noticeType, boolean noticeSent) {
        this.uuid = uuid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.syncType = syncType;
        this.fullModelFetched = fullModelFetched;
        this.allActionItemsSynced = allActionItemsSynced;
        this.exceptionClass = exceptionClass;
        this.exceptionMessage = exceptionMessage;
        this.backend = backend;
        this.releaseName = releaseName;
        this.releaseCode = releaseCode;
        this.noticeType = noticeType;
        this.noticeSent = noticeSent;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isFullModelFetched() {
        return fullModelFetched;
    }

    public void setFullModelFetched(boolean fullModelFetched) {
        this.fullModelFetched = fullModelFetched;
    }

    public boolean isAllActionItemsSynced() {
        return allActionItemsSynced;
    }

    public void setAllActionItemsSynced(boolean allActionItemsSynced) {
        this.allActionItemsSynced = allActionItemsSynced;
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public String getBackend() {
        return backend;
    }

    public void setBackend(String backend) {
        this.backend = backend;
    }

    public String getReleaseName() {
        return releaseName;
    }

    public void setReleaseName(String releaseName) {
        this.releaseName = releaseName;
    }

    public int getReleaseCode() {
        return releaseCode;
    }

    public void setReleaseCode(int releaseCode) {
        this.releaseCode = releaseCode;
    }

    public int getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(int noticeType) {
        this.noticeType = noticeType;
    }

    public boolean isNoticeSent() {
        return noticeSent;
    }

    public void setNoticeSent(boolean noticeSent) {
        this.noticeSent = noticeSent;
    }

    public int getSyncType() {
        return syncType;
    }

    public void setSyncType(int syncType) {
        this.syncType = syncType;
    }


}