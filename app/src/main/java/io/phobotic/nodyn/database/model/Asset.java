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

import com.google.gson.annotations.SerializedName;

import io.phobotic.nodyn.sync.HtmlEncoded;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */


public class Asset {
    private int id;
    @HtmlEncoded
    private String image;
    @HtmlEncoded
    private String name;

    @HtmlEncoded
    @SerializedName("asset_tag")
    private String tag;

    private String serial;
    @HtmlEncoded
    private String model;
    @SerializedName("status_label")
    private String status;

    @HtmlEncoded
    @SerializedName("assigned_to")
    private String assignedTo;

    private String location;

    public String getCategory() {
        return category;
    }

    public Asset setCategory(String category) {
        this.category = category;
        return this;
    }

    @HtmlEncoded
    private String category;

    @HtmlEncoded
    private String manufacturer;

    private String eol;
    @SerializedName("purchase_cost")
    private String purchaseCost;
    @SerializedName("purchase_date")
    private String purchaseDate;
    private String notes;
    @SerializedName("order_number")
    private String orderNumber;
    @SerializedName("last_checkout")
    private String lastCheckout;
    @SerializedName("expected_checkin")
    private String expectedCheckin;
    @SerializedName("created_at")
    private String createdAt;
    private String companyName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getEol() {
        return eol;
    }

    public void setEol(String eol) {
        this.eol = eol;
    }

    public String getPurchaseCost() {
        return purchaseCost;
    }

    public void setPurchaseCost(String purchaseCost) {
        this.purchaseCost = purchaseCost;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getLastCheckout() {
        return lastCheckout;
    }

    public void setLastCheckout(String lastCheckout) {
        this.lastCheckout = lastCheckout;
    }

    public String getExpectedCheckin() {
        return expectedCheckin;
    }

    public void setExpectedCheckin(String expectedCheckin) {
        this.expectedCheckin = expectedCheckin;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public class Columns {
        public static final String ID = "id";
        public static final String IMAGE = "";
        public static final String NAME = "";
        public static final String TAG = "";
        public static final String SERIAL = "";
        public static final String MODEL = "";
        public static final String STATUS = "";
        public static final String ASSIGNED_TO = "";
        public static final String LOCATION = "";
        public static final String CATEGORY = "";
        public static final String MANUFACTURER = "";
        public static final String EOL = "";
        public static final String PURCHASE_COST = "";
        public static final String PURCHASE_DATE = "";
        public static final String NOTES = "";
        public static final String ORDER_NUMBER = "";
        public static final String LAST_CHECKOUT = "";
        public static final String EXPECTED_CHECKIN = "";
        public static final String CREATED_AT = "";
        public static final String COMPANY_NAME = "";
    }
}
