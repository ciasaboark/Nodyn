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

package io.phobotic.nodyn_app.database.model;

/**
 * Created by Jonathan Nelson on 9/14/17.
 */

public class Manufacturer {
    private int id = -1;
    private String name;
    private String createdAt;
    private String supportEmail;
    private String supportPhone;
    private String supportURL;
    private String URL;

    public String getCreatedAt() {
        return createdAt;
    }

    public Manufacturer setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public Manufacturer setSupportEmail(String supportEmail) {
        this.supportEmail = supportEmail;
        return this;
    }

    public String getSupportPhone() {
        return supportPhone;
    }

    public Manufacturer setSupportPhone(String supportPhone) {
        this.supportPhone = supportPhone;
        return this;
    }

    public String getSupportURL() {
        return supportURL;
    }

    public Manufacturer setSupportURL(String supportURL) {
        this.supportURL = supportURL;
        return this;
    }

    public String getURL() {
        return URL;
    }

    public Manufacturer setURL(String URL) {
        this.URL = URL;
        return this;
    }

    @Override
    public int hashCode() {
        return (String.valueOf(id) + name).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Manufacturer)) {
            return false;
        } else {
            return this.getId() == ((Manufacturer) obj).getId() &&
                    this.getName().equals(((Manufacturer) obj).getName());
        }
    }

    public int getId() {
        return id;
    }

    public Manufacturer setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Manufacturer setName(String name) {
        this.name = name;
        return this;
    }

    public static class Columns {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String CREATED_AT = "createdAt";
        public static final String SUPPORT_EMAIL = "supportEmail";
        public static final String SUPPORT_PHONE = "supportPhone";
        public static final String SUPPORT_URL = "supportURL";
        public static final String URL = "url";
    }
}
