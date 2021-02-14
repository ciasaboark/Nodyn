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

package io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow;

import com.google.gson.annotations.SerializedName;

import io.phobotic.nodyn_app.database.model.MaintenanceRecord;
import io.phobotic.nodyn_app.helper.URLHelper;
import io.phobotic.nodyn_app.sync.HtmlEncoded;

import static io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow.TimeHelper.toTimestamp;

/**
 * Created by Jonathan Nelson on 10/28/17.
 */

public class Snipeit4MaintenanceRecord {
    @SerializedName("asset_maintenance_type")
    private String type;

    @SerializedName(value = "asset", alternate = {"asset_name"})
    private Marker asset;

    @SerializedName("created_at")
    private DateSnippet createdAt;

    @SerializedName("completion_date")
    private DateSnippet completedAt;

    @SerializedName("start_date")
    private DateSnippet startedAt;

    private String cost;

    private int id;

    @HtmlEncoded
    private String notes;

    private Marker supplier;
    private String title;

    @SerializedName("user_id")
    private Marker user;

    public MaintenanceRecord toMaintenanceRecord() {
        MaintenanceRecord record = new MaintenanceRecord()
                .setAssetID(asset.id)
                .setCompleteTime(completedAt == null ? -1 : toTimestamp(completedAt.getDatetimme(), true))
                .setCreatedAt(createdAt == null ? -1 : toTimestamp(createdAt.getDatetimme(), true))
                .setStartTime(startedAt == null ? -1 : toTimestamp(startedAt.getDatetimme(), true))
                .setNotes(URLHelper.decode(notes))
                .setSupplier(supplier == null ? null : URLHelper.decode(supplier.name))
                .setTitle(URLHelper.decode(title))
                .setType(URLHelper.decode(type))
                .setUserID(user == null ? -1 : user.id);

        return record;
    }

    private class Marker {
        @SerializedName("id")
        public int id;
        @SerializedName("name")
        public String name;
    }

}
