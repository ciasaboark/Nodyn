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
import io.phobotic.nodyn.sync.Image;

/**
 * Created by Jonathan Nelson on 9/12/17.
 */

public class ShadowAsset {
    private int id;
    @Image
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

    public ShadowAsset setId(int id) {
        this.id = id;
        return this;
    }

    public String getImage() {
        return image;
    }

    public ShadowAsset setImage(String image) {
        this.image = image;
        return this;
    }

    public String getName() {
        return name;
    }

    public ShadowAsset setName(String name) {
        this.name = name;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public ShadowAsset setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public String getSerial() {
        return serial;
    }

    public ShadowAsset setSerial(String serial) {
        this.serial = serial;
        return this;
    }

    public String getModel() {
        return model;
    }

    public ShadowAsset setModel(String model) {
        this.model = model;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ShadowAsset setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public ShadowAsset setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public ShadowAsset setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public ShadowAsset setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public ShadowAsset setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    public String getEol() {
        return eol;
    }

    public ShadowAsset setEol(String eol) {
        this.eol = eol;
        return this;
    }

    public String getPurchaseCost() {
        return purchaseCost;
    }

    public ShadowAsset setPurchaseCost(String purchaseCost) {
        this.purchaseCost = purchaseCost;
        return this;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public ShadowAsset setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public ShadowAsset setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public ShadowAsset setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
        return this;
    }

    public String getLastCheckout() {
        return lastCheckout;
    }

    public ShadowAsset setLastCheckout(String lastCheckout) {
        this.lastCheckout = lastCheckout;
        return this;
    }

    public String getExpectedCheckin() {
        return expectedCheckin;
    }

    public ShadowAsset setExpectedCheckin(String expectedCheckin) {
        this.expectedCheckin = expectedCheckin;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public ShadowAsset setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public String getCompanyName() {
        return companyName;
    }

    public ShadowAsset setCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }
}
