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

package io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow;

import com.google.gson.annotations.SerializedName;

import io.phobotic.nodyn_app.database.model.Company;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.helper.URLHelper;

import static io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow.TimeHelper.toTimestamp;

/**
 * Created by Jonathan Nelson on 11/12/20.
 */
public class Snipeit4Company {
    private int id;
    private String name;
    private String image;

    @SerializedName("assets_count")
    private int numassets;

    @SerializedName("users_count")
    private int numUsers;

    @SerializedName("created_at")
    public DateSnippet createdAt;


    public Company toCompany(boolean isTimeUTC) {
        Company company = new Company()
                .setId(id)
                .setName(URLHelper.decode(name))
                .setImage(URLHelper.decode(image))
                .setUserCount(numUsers)
                .setAssetCount(numassets)
                .setCreatedAt(createdAt == null ? -1 : toTimestamp(
                        createdAt.getDatetimme(), isTimeUTC));

        return company;
    }
}
