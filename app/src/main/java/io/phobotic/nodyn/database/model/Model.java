package io.phobotic.nodyn.database.model;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public class Model {
    private String id;
    private String manufacturer;
    private String name;
    private String image;
    private String modelnumber;
    private int numassets;
    private String depreciation;
    private String category;
    private String eol;
    private String note;
    private String fieldset;
//    private String actions;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getModelnumber() {
        return modelnumber;
    }

    public void setModelnumber(String modelnumber) {
        this.modelnumber = modelnumber;
    }

    public int getNumassets() {
        return numassets;
    }

    public void setNumassets(int numassets) {
        this.numassets = numassets;
    }

    public String getDepreciation() {
        return depreciation;
    }

    public void setDepreciation(String depreciation) {
        this.depreciation = depreciation;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEol() {
        return eol;
    }

    public void setEol(String eol) {
        this.eol = eol;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFieldset() {
        return fieldset;
    }

    public void setFieldset(String fieldset) {
        this.fieldset = fieldset;
    }
}
