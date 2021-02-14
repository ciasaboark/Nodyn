/*
 * Copyright (c) 2019 Jonathan Nelson <ciasaboark@gmail.com>
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
 * Created by Jonathan Nelson on 7/7/17.
 */


public class Asset implements Serializable {
    private int id = -1;
    private String tag;
    private String image;
    private String name;
    private String serial;
    private int modelID = -1;
    private int statusID = -1;
    private int assignedToID = -1;
    private int locationID = -1;
    private int categoryID = -1;
    private int manufacturerID = -1;
    private String eol;
    private String purchaseCost;
    private String purchaseDate;
    private String notes;
    private String orderNumber;
    private long lastCheckout = -1;
    private long expectedCheckin = -1;
    private long createdAt = -1;
    private int companyID = -1;


    public int getId() {
        return id;
    }

    public Asset setId(int id) {
        this.id = id;
        return this;
    }

    public String getImage() {
        return image;
    }

    public Asset setImage(String image) {
        this.image = image;
        return this;
    }

    public String getName() {
        return name;
    }

    public Asset setName(String name) {
        this.name = name;
        return this;
    }

    public String getSerial() {
        return serial;
    }

    public Asset setSerial(String serial) {
        this.serial = serial;
        return this;
    }

    public int getModelID() {
        return modelID;
    }

    public Asset setModelID(int modelID) {
        this.modelID = modelID;
        return this;
    }

    public int getStatusID() {
        return statusID;
    }

    public Asset setStatusID(int statusID) {
        this.statusID = statusID;
        return this;
    }

    public int getAssignedToID() {
        return assignedToID;
    }

    public Asset setAssignedToID(int assignedToID) {
        this.assignedToID = assignedToID;
        return this;
    }

    public int getLocationID() {
        return locationID;
    }

    public Asset setLocationID(int locationID) {
        this.locationID = locationID;
        return this;
    }

    public int getCategoryID() {
        return categoryID;
    }

    public Asset setCategoryID(int categoryID) {
        this.categoryID = categoryID;
        return this;
    }

    public int getManufacturerID() {
        return manufacturerID;
    }

    public Asset setManufacturerID(int manufacturerID) {
        this.manufacturerID = manufacturerID;
        return this;
    }

    public String getEol() {
        return eol;
    }

    public Asset setEol(String eol) {
        this.eol = eol;
        return this;
    }

    public String getPurchaseCost() {
        return purchaseCost;
    }

    public Asset setPurchaseCost(String purchaseCost) {
        this.purchaseCost = purchaseCost;
        return this;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public Asset setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public Asset setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Asset setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
        return this;
    }

    public long getLastCheckout() {
        return lastCheckout;
    }

    public Asset setLastCheckout(long lastCheckout) {
        this.lastCheckout = lastCheckout;
        return this;
    }

    public long getExpectedCheckin() {
        return expectedCheckin;
    }

    public Asset setExpectedCheckin(long expectedCheckin) {
        this.expectedCheckin = expectedCheckin;
        return this;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public Asset setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public int getCompanyID() {
        return companyID;
    }

    public Asset setCompanyID(int companyID) {
        this.companyID = companyID;
        return this;
    }

    /**
     * Asset equality is based off the assigned tag only
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Asset)) {
            return false;
        } else {
            return this.hashCode() == obj.hashCode();
        }
    }

    public String getTag() {
        return tag;
    }

    public Asset setTag(String tag) {
        this.tag = tag;
        return this;
    }

    @Override
    public String toString() {
        return tag;
    }

    public class Columns {
        public static final String ID = "id";
        public static final String IMAGE = "image";
        public static final String NAME = "name";
        public static final String TAG = "tag";
        public static final String SERIAL = "serial";
        public static final String MODEL_ID = "model";
        public static final String STATUS_ID = "status";
        public static final String ASSIGNED_TO_ID = "assigned_to";
        public static final String LOCATION_ID = "location";
        public static final String CATEGORY_ID = "category";
        public static final String MANUFACTURER_ID = "manufacturer";
        public static final String EOL = "eol";
        public static final String PURCHASE_COST = "purchase_cost";
        public static final String PURCHASE_DATE = "purchase_date";
        public static final String NOTES = "notes";
        public static final String ORDER_NUMBER = "order_number";
        public static final String LAST_CHECKOUT = "last_checkout";
        public static final String EXPECTED_CHECKIN = "expected_checkin";
        public static final String CREATED_AT = "created_at";
        public static final String COMPANY_ID = "company_name";
    }

    @Override
    public int hashCode() {
        return String.format("%d%s", id, tag).hashCode();
    }
}
