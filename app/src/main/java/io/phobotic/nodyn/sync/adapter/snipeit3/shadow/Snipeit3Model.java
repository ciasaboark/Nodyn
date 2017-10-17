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
import io.phobotic.nodyn.sync.Image;

/**
 * Created by Jonathan Nelson on 9/12/17.
 */

public class Snipeit3Model {
    private int id;

    @HtmlEncoded
    private String manufacturer;

    @HtmlEncoded
    private String name;

    @Image
    private String image;

    private String modelnumber;
    private int numassets;
    private String depreciation;

    @HtmlEncoded
    private String category;

    private String eol;
    private String note;
    private String fieldset;

    public int getId() {
        return id;
    }

    public Snipeit3Model setId(int id) {
        this.id = id;
        return this;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public Snipeit3Model setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    public String getName() {
        return name;
    }

    public Snipeit3Model setName(String name) {
        this.name = name;
        return this;
    }

    public String getImage() {
        return image;
    }

    public Snipeit3Model setImage(String image) {
        this.image = image;
        return this;
    }

    public String getModelnumber() {
        return modelnumber;
    }

    public Snipeit3Model setModelnumber(String modelnumber) {
        this.modelnumber = modelnumber;
        return this;
    }

    public int getNumassets() {
        return numassets;
    }

    public Snipeit3Model setNumassets(int numassets) {
        this.numassets = numassets;
        return this;
    }

    public String getDepreciation() {
        return depreciation;
    }

    public Snipeit3Model setDepreciation(String depreciation) {
        this.depreciation = depreciation;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public Snipeit3Model setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getEol() {
        return eol;
    }

    public Snipeit3Model setEol(String eol) {
        this.eol = eol;
        return this;
    }

    public String getNote() {
        return note;
    }

    public Snipeit3Model setNote(String note) {
        this.note = note;
        return this;
    }

    public String getFieldset() {
        return fieldset;
    }

    public Snipeit3Model setFieldset(String fieldset) {
        this.fieldset = fieldset;
        return this;
    }
}
