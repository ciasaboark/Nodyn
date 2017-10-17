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

import com.google.gson.annotations.SerializedName;

import io.phobotic.nodyn.sync.HtmlEncoded;

/**
 * Created by Jonathan Nelson on 9/13/17.
 */

public class Snipeit3Category {
    private int id;
    @HtmlEncoded
    private String name;
    @SerializedName("category_type")
    private String categoryType;
    private int count;
    private String acceptance;
    private String eula;

    public int getId() {
        return id;
    }

    public Snipeit3Category setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Snipeit3Category setName(String name) {
        this.name = name;
        return this;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public Snipeit3Category setCategoryType(String categoryType) {
        this.categoryType = categoryType;
        return this;
    }

    public int getCount() {
        return count;
    }

    public Snipeit3Category setCount(int count) {
        this.count = count;
        return this;
    }

    public String getAcceptance() {
        return acceptance;
    }

    public Snipeit3Category setAcceptance(String acceptance) {
        this.acceptance = acceptance;
        return this;
    }

    public String getEula() {
        return eula;
    }

    public Snipeit3Category setEula(String eula) {
        this.eula = eula;
        return this;
    }
}
