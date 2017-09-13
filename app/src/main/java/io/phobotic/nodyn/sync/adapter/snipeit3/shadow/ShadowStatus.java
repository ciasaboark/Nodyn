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

package io.phobotic.nodyn.sync.adapter.snipeit3.shadow;

import io.phobotic.nodyn.sync.HtmlEncoded;

/**
 * Created by Jonathan Nelson on 9/12/17.
 */

public class ShadowStatus {
    private int id;
    private String name;
    private String type;

    @HtmlEncoded
    private String color;

    public int getId() {
        return id;
    }

    public ShadowStatus setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ShadowStatus setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public ShadowStatus setType(String type) {
        this.type = type;
        return this;
    }

    public String getColor() {
        return color;
    }

    public ShadowStatus setColor(String color) {
        this.color = color;
        return this;
    }
}
