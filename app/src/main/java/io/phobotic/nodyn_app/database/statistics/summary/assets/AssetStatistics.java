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

package io.phobotic.nodyn_app.database.statistics.summary.assets;

import java.util.List;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import io.phobotic.nodyn_app.database.statistics.ShortActionConverter;
import io.phobotic.nodyn_app.database.statistics.UsageRecord;

/**
 * Created by Jonathan Nelson on 2019-05-11.
 */
@Entity(tableName = "asset_statistics")
public class AssetStatistics {
    @PrimaryKey()
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "tag")
    private String tag;

    @ColumnInfo(name = "percent_in_use")
    private float percentInUse;

    @ColumnInfo(name = "favoring_user")
    private int favoringUser;

    @ColumnInfo(name = "last_updated")
    private long lastUpdated;

    @ColumnInfo(name = "usage_records")
    @TypeConverters(ShortActionConverter.class)
    private List<UsageRecord> usageRecords;

    public AssetStatistics() {
    }

    @Ignore
    public AssetStatistics(int id, String tag, float percentInUse, int favoringUser,
                           long lastUpdated, List<UsageRecord> usageRecords) {
        this.id = id;
        this.tag = tag;
        this.percentInUse = percentInUse;
        this.favoringUser = favoringUser;
        this.lastUpdated = lastUpdated;
        this.usageRecords = usageRecords;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public float getPercentInUse() {
        return percentInUse;
    }

    public void setPercentInUse(float percentInUse) {
        this.percentInUse = percentInUse;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<UsageRecord> getUsageRecords() {
        return usageRecords;
    }

    public void setUsageRecords(List<UsageRecord> usageRecords) {
        this.usageRecords = usageRecords;
    }

    public int getFavoringUser() {
        return favoringUser;
    }

    public void setFavoringUser(int favoringUser) {
        this.favoringUser = favoringUser;
    }
}
