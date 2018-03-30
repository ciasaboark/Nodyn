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

/**
 * Created by Jonathan Nelson on 3/21/18.
 */

public class ScanLog {
    private String scanType;
    private long timestamp;
    private String scannedData;
    private boolean scanAccepted;
    private Class consumer;
    private String notes;

    public ScanLog(String scanType, long timestamp, String scannedData, boolean scanAccepted, Class consumer, String notes) {
        this.scanType = scanType;
        this.timestamp = timestamp;
        this.scannedData = scannedData;
        this.scanAccepted = scanAccepted;
        this.consumer = consumer;
        this.notes = notes;
    }

    public String getScanType() {
        return scanType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getScannedData() {
        return scannedData;
    }

    public boolean isScanAccepted() {
        return scanAccepted;
    }

    public Class getConsumer() {
        return consumer;
    }

    public String getNotes() {
        return notes;
    }
}
