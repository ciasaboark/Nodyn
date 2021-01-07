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

package io.phobotic.nodyn_app.database.statistics.summary.models;

import java.util.List;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import io.phobotic.nodyn_app.database.statistics.ShortActionConverter;
import io.phobotic.nodyn_app.database.statistics.SummedAction;

/**
 * Created by Jonathan Nelson on 2019-05-11.
 */
@Entity(tableName = "model_statistics")
public class ModelStatistics {
    @PrimaryKey()
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "most_used_asset")
    private int mostUsedAssetID;

    @ColumnInfo(name = "least_used_asset")
    private int leasttUsedAssetID;

    @ColumnInfo(name = "last_updated")
    private long lastUpdated;

    @ColumnInfo(name = "action_history")
    @TypeConverters(ShortActionConverter.class)
    private List<SummedAction> actionHistory;

    public ModelStatistics() {
    }

    @Ignore
    public ModelStatistics(int id, int mostUsedAssetID, int leasttUsedAssetID, long lastUpdated, List<SummedAction> actionHistory) {
        this.id = id;
        this.mostUsedAssetID = mostUsedAssetID;
        this.leasttUsedAssetID = leasttUsedAssetID;
        this.lastUpdated = lastUpdated;
        this.actionHistory = actionHistory;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<SummedAction> getActionHistory() {
        return actionHistory;
    }

    public void setActionHistory(List<SummedAction> actionHistory) {
        this.actionHistory = actionHistory;
    }

    public int getMostUsedAssetID() {
        return mostUsedAssetID;
    }

    public void setMostUsedAssetID(int mostUsedAssetID) {
        this.mostUsedAssetID = mostUsedAssetID;
    }

    public int getLeasttUsedAssetID() {
        return leasttUsedAssetID;
    }

    public void setLeasttUsedAssetID(int leasttUsedAssetID) {
        this.leasttUsedAssetID = leasttUsedAssetID;
    }
}
