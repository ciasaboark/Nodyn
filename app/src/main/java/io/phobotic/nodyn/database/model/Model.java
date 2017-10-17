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
 * Created by Jonathan Nelson on 7/7/17.
 */

public class Model {
    private int id = -1;
    private int manufacturerID = -1;
    private String name;
    private String image;
    private String modelNumber;
    private int numassets;
    private String depreciation;
    private int categoryID = -1;
    private String eol;
    private String note;
    private int fieldsetID;
    private String createdAt;

    public String getName() {
        return name;
    }

    public Model setName(String name) {
        this.name = name;
        return this;
    }

    public String getImage() {
        return image;
    }

    public Model setImage(String image) {
        this.image = image;
        return this;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public Model setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
        return this;
    }

    public int getNumassets() {
        return numassets;
    }

    public Model setNumassets(int numassets) {
        this.numassets = numassets;
        return this;
    }

    public String getDepreciation() {
        return depreciation;
    }

    public Model setDepreciation(String depreciation) {
        this.depreciation = depreciation;
        return this;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public Model setCategoryID(int categoryID) {
        this.categoryID = categoryID;
        return this;
    }

    public String getEol() {
        return eol;
    }

    public Model setEol(String eol) {
        this.eol = eol;
        return this;
    }

    public String getNote() {
        return note;
    }

    public Model setNote(String note) {
        this.note = note;
        return this;
    }

    public int getFieldsetID() {
        return fieldsetID;
    }

    public Model setFieldsetID(int fieldsetID) {
        this.fieldsetID = fieldsetID;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Model setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    @Override
    public int hashCode() {
        return name.hashCode() + manufacturerID;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Model)) {
            return false;
        } else {
            return this.getId() == ((Model) obj).getId() &&
                    this.manufacturerID == ((Model) obj).getManufacturerID();
        }
    }

    public int getManufacturerID() {
        return manufacturerID;
    }

    public int getId() {
        return id;
    }

    public Model setId(int id) {
        this.id = id;
        return this;
    }

    public Model setManufacturerID(int manufacturerID) {
        this.manufacturerID = manufacturerID;
        return this;
    }

    @Override
    public String toString() {
        return name;
    }



    public class Columns {
        public static final String ID = "id";
        public static final String MANUFACTURER_ID = "manufacturer";
        public static final String NAME = "name";
        public static final String IMAGE = "image";
        public static final String MODEL_NUMBER = "modelnum";
        public static final String NUM_ASSETS = "numassets";
        public static final String DEPRECIATION = "depreciation";
        public static final String CATEGORY_ID = "category";
        public static final String EOL = "eol";
        public static final String NOTES = "note";
        public static final String FIELDSET_ID = "fieldset";
        public static final String CREATED_AT = "createdAt";
    }
}
