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

package io.phobotic.nodyn.database.model;

/**
 * Created by Jonathan Nelson on 7/8/17.
 */

public class Group {
    private int id;
    private String name;
    private int users;
    private String createdAt;

    public int getId() {
        return id;
    }

    public Group setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Group setName(String name) {
        this.name = name;
        return this;
    }

    public int getUsers() {
        return users;
    }

    public Group setUsers(int users) {
        this.users = users;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Group setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public String toString() {
        return name;
    }

    public class Columns {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String USERS = "users";
        public static final String CREATED_AT = "created";
    }
}
