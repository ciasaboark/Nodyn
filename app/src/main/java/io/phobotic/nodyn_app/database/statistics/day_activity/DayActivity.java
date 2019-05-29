/*
 * Copyright (c) 2019 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.database.statistics.day_activity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Created by Jonathan Nelson on 12/6/17.
 */
@Entity(tableName = "day_statistics")
public class DayActivity {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "timestamp")
    private long timestamp;

    @ColumnInfo(name = "total_checkouts")
    private int totalCheckouts;

    @ColumnInfo(name = "total_checkins")
    private int totalCheckins;

    @ColumnInfo(name = "total_audits")
    private int totalAudits;

    @Ignore
    public DayActivity() {
    }

    public DayActivity(long timestamp, int totalCheckouts, int totalCheckins, int totalAudits) {
        this.timestamp = timestamp;
        this.totalCheckouts = totalCheckouts;
        this.totalCheckins = totalCheckins;
        this.totalAudits = totalAudits;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public DayActivity setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public int getTotalCheckouts() {
        return totalCheckouts;
    }

    public DayActivity setTotalCheckouts(int totalCheckouts) {
        this.totalCheckouts = totalCheckouts;
        return this;
    }

    public int getTotalCheckins() {
        return totalCheckins;
    }

    public DayActivity setTotalCheckins(int totalCheckins) {
        this.totalCheckins = totalCheckins;
        return this;
    }

    public int getTotalAudits() {
        return totalAudits;
    }

    public void setTotalAudits(int totalAudits) {
        this.totalAudits = totalAudits;
    }
}
