/*
 * Copyright (c) 2017 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn.sync.adapter.snipeit4.shadow;

import com.google.gson.annotations.SerializedName;

import io.phobotic.nodyn.database.model.Manufacturer;

/**
 * Created by Jonathan Nelson on 9/14/17.
 */

public class Snipeit4Manufacturer {
    private int id;
    private String name;
    @SerializedName("created_at")
    private DateSnippet createdAt;
    @SerializedName("support_email")
    private String supportEmail;
    @SerializedName("support_phone")
    private String supportPhone;
    @SerializedName("support_url")
    private String supportURL;
    @SerializedName("url")
    private String URL;


    public Manufacturer toManufacturer() {
        Manufacturer manufacturer = new Manufacturer()
                .setId(id)
                .setName(name)
                .setCreatedAt(createdAt == null ? null : createdAt.getDatetimme())
                .setSupportEmail(supportEmail)
                .setSupportPhone(supportPhone)
                .setSupportURL(supportURL)
                .setURL(URL);

        return manufacturer;
    }
}
