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

package io.phobotic.nodyn_app.database.model;

import java.io.Serializable;

/**
 * Created by Jonathan Nelson on 11/11/20.
 */
public class Company implements Serializable {
    private int id;
    private String name;
    private String image;
    private int userCount = 0;
    private int assetCount = 0;
    private long createdAt;

    public String getName() {
        return name;
    }

    public Company setName(String name) {
        this.name = name;
        return this;
    }

    public String getImage() {
        return image;
    }

    public Company setImage(String image) {
        this.image = image;
        return this;
    }

    public int getUserCount() {
        return userCount;
    }

    public Company setUserCount(int userCount) {
        this.userCount = userCount;
        return this;
    }

    public int getAssetCount() {
        return assetCount;
    }

    public Company setAssetCount(int assetCount) {
        this.assetCount = assetCount;
        return this;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Company setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public int getId() {
        return id;
    }

    public Company setId(int id) {
        this.id = id;
        return this;
    }

    public class Columns {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String USER_COUNT = "user_count";
        public static final String ASSET_COUNT = "asset_count";
        public static final String IMAGE = "image";
        public static final String CREATED_AT = "created";
    }
}
