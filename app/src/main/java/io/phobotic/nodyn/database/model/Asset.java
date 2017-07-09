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
    private String expected_checkin;
    @SerializedName("created_at")
    private String created_at;
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

    public String getExpected_checkin() {
        return expected_checkin;
    }

    public void setExpected_checkin(String expected_checkin) {
        this.expected_checkin = expected_checkin;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
