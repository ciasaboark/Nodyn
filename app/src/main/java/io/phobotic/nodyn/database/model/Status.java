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
 * Created by Jonathan Nelson on 9/10/17.
 */

public class Status {
    private int id;
    private String name;
    private String type;
    private String color;

    public Status() {
    }

    public Status(String name, String type, String color) {
        this.name = name;
        this.type = type;
        this.color = color;
    }

    public Status(int id, String name, String type, String color) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.color = color;
    }

    public int getId() {
        return id;
    }

    public Status setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Status setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public Status setType(String type) {
        this.type = type;
        return this;
    }

    public String getColor() {
        return color;
    }

    public Status setColor(String color) {
        this.color = color;
        return this;
    }

    @Override
    public String toString() {
        return name + " - " + type;
    }

    public class Columns {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String TYPE = "type";
        public static final String COLOR = "color";
    }
}
