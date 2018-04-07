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

package io.phobotic.nodyn_app.database.scan;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Jonathan Nelson on 3/21/18.
 */
@Entity
public class ScanRecord {
    @PrimaryKey
    private int id;

    @ColumnInfo(name = "type")
    private String type;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name = "data")
    private String data;

    @ColumnInfo(name = "accepted")
    private boolean accepted;

    @ColumnInfo(name = "consumer")
    private String consumer;

    @ColumnInfo(name = "notes")
    private String notes;

    public ScanRecord() {
    }

    public ScanRecord(int id, String type, long timestamp, String data, boolean accepted, String consumer, String notes) {
        this.id = id;
        this.type = type;
        this.timestamp = timestamp;
        this.data = data;
        this.accepted = accepted;
        this.consumer = consumer;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public ScanRecord setId(int id) {
        this.id = id;
        return this;
    }

    public String getType() {
        return type;
    }

    public ScanRecord setType(String type) {
        this.type = type;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ScanRecord setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getData() {
        return data;
    }

    public ScanRecord setData(String data) {
        this.data = data;
        return this;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public ScanRecord setAccepted(boolean accepted) {
        this.accepted = accepted;
        return this;
    }

    public String getConsumer() {
        return consumer;
    }

    public ScanRecord setConsumer(String consumer) {
        this.consumer = consumer;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public ScanRecord setNotes(String notes) {
        this.notes = notes;
        return this;
    }
}
