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

import io.phobotic.nodyn.database.model.Asset;

import static io.phobotic.nodyn.sync.adapter.snipeit4.shadow.TimeHelper.toTimestamp;

/**
 * Created by Jonathan Nelson on 9/12/17.
 */

public class Snipeit4Asset {
    private static final String TAG = Snipeit4Asset.class.getSimpleName();
    public int id;
    public String image;
    public String name;

    @SerializedName("asset_tag")
    public String tag;

    public String serial;

    public Snippet model;

    @SerializedName("status_label")
    public Snippet status;

    @SerializedName("assigned_to")
    public Snippet assignedTo;
//    public String location;

    public Snippet category;
    public Snippet manufacturer;
//    public String eol;

    public String purchaseCost;
    public DateSnippet purchaseDate;
    public String notes;
    public String orderNumber;

    @SerializedName("last_checkout")
    public DateSnippet lastCheckout;

    @SerializedName("expected_checkin")
    public DateSnippet expectedCheckin;

    @SerializedName("created_at")
    public DateSnippet createdAt;
    public Snippet company;

    public Asset toAsset() {
        Asset asset = new Asset()
                .setId(id)
                .setImage(image)
                .setName(name)
                .setTag(tag)
                .setSerial(serial)
                .setModelID(model == null ? -1 : model.getId())
                .setStatusID(status == null ? -1 : status.getId())
                .setAssignedToID(assignedTo == null ? -1 : assignedTo.getId())
                .setCategoryID(category == null ? -1 : category.getId())
                .setManufacturerID(manufacturer == null ? -1 : manufacturer.getId())
                .setPurchaseCost(purchaseCost)
                .setPurchaseDate(purchaseDate == null ? null : purchaseDate.getDatetimme())
                .setNotes(notes)
                .setOrderNumber(orderNumber)
                .setLastCheckout(lastCheckout == null ? -1 : toTimestamp(lastCheckout.getDatetimme()))
                .setExpectedCheckin(expectedCheckin == null ? -1 : toTimestamp(expectedCheckin.getDatetimme()))
                .setCreatedAt(createdAt == null ? -1 : toTimestamp(createdAt.getDatetimme()))
                .setCompanyID(company == null ? -1 : company.getId());

        return asset;
    }


}
