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

import io.phobotic.nodyn.database.model.Model;

/**
 * Created by Jonathan Nelson on 9/12/17.
 */

public class Snipeit4Model {
    private int id;
    private Snippet manufacturer;
    private String name;
    private String image;
    private String modelnumber;

    @SerializedName("assets_count")
    private int numassets;
    private String depreciation;
    private Snippet category;
    private String eol;
    private String notes;
    private Snippet fieldset;


    public Model toModel() {
        Model model = new Model()
                .setId(id)
                .setName(name)
                .setImage(image)
                .setModelNumber(modelnumber)
                .setNumassets(numassets)
                .setDepreciation(depreciation)
                .setEol(eol)
                .setNote(notes)
                .setFieldsetID(fieldset == null ? -1 : fieldset.getId())
                .setManufacturerID(manufacturer == null ? -1 : manufacturer.getId())
                .setCategoryID(category == null ? -1 : category.getId());

        return model;
    }
}