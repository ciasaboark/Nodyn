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

package io.phobotic.nodyn.fragment;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.exception.CategoryNotFoundException;
import io.phobotic.nodyn.database.exception.ManufacturerNotFoundException;
import io.phobotic.nodyn.database.exception.ModelNotFoundException;
import io.phobotic.nodyn.database.exception.StatusNotFoundException;
import io.phobotic.nodyn.database.exception.UserNotFoundException;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.Category;
import io.phobotic.nodyn.database.model.Manufacturer;
import io.phobotic.nodyn.database.model.Model;
import io.phobotic.nodyn.database.model.Status;
import io.phobotic.nodyn.database.model.User;

/**
 * Created by Jonathan Nelson on 9/17/17.
 */

public class SimplifiedAsset extends Asset {
    private String modelName = "";
    private String statusName = "";
    private String assignedToName = "";
    private String locationName = "";
    private String categoryName = "";
    private String manufacturerName = "";
    private String companyName = "";

    public SimplifiedAsset(@NotNull Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset can not be null");
        }

        super.setAssignedToID(asset.getAssignedToID());
        super.setCategoryID(asset.getCategoryID());
        super.setCompanyID(asset.getCompanyID());
        super.setCreatedAt(asset.getCreatedAt());
        super.setEol(asset.getEol());
        super.setExpectedCheckin(asset.getExpectedCheckin());
        super.setId(asset.getId());
        super.setImage(asset.getImage());
        super.setLastCheckout(asset.getLastCheckout());
        super.setLocationID(asset.getLocationID());
        super.setManufacturerID(asset.getManufacturerID());
        super.setModelID(asset.getModelID());
        super.setName(asset.getName());
        super.setNotes(asset.getNotes());
        super.setOrderNumber(asset.getOrderNumber());
        super.setPurchaseCost(asset.getPurchaseCost());
        super.setPurchaseDate(asset.getPurchaseDate());
        super.setSerial(asset.getSerial());
        super.setStatusID(asset.getStatusID());
        super.setTag(asset.getTag());
    }

    public String getModelName() {
        return modelName;
    }

    public SimplifiedAsset setModelName(String modelName) {
        if (modelName == null) modelName = "";

        this.modelName = modelName;
        return this;
    }

    public String getStatusName() {
        return statusName;
    }

    public SimplifiedAsset setStatusName(String statusName) {
        if (statusName == null) statusName = "";

        this.statusName = statusName;
        return this;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public SimplifiedAsset setAssignedToName(String assignedToName) {
        if (assignedToName == null) assignedToName = "";

        this.assignedToName = assignedToName;
        return this;
    }

    public String getLocationName() {
        return locationName;
    }

    public SimplifiedAsset setLocationName(String locationName) {
        if (locationName == null) locationName = "";

        this.locationName = locationName;
        return this;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public SimplifiedAsset setCategoryName(String categoryName) {
        if (categoryName == null) categoryName = "";

        this.categoryName = categoryName;
        return this;
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public SimplifiedAsset setManufacturerName(String manufacturerName) {
        if (manufacturerName == null) manufacturerName = "";

        this.manufacturerName = manufacturerName;
        return this;
    }

    public String getCompanyName() {
        return companyName;
    }

    public SimplifiedAsset setCompanyName(String companyName) {
        if (companyName == null) companyName = "";

        this.companyName = companyName;
        return this;
    }

    public static class Builder {
        private Map<Integer, String> modelMap = new HashMap<>();
        private Map<Integer, String> statusMap = new HashMap<>();
        private Map<Integer, String> assignedToMap = new HashMap<>();
        private Map<Integer, String> locationMap = new HashMap<>();
        private Map<Integer, String> categoryMap = new HashMap<>();
        private Map<Integer, String> manufacturerMap = new HashMap<>();
        private Map<Integer, String> companyMap = new HashMap<>();

        public SimplifiedAsset fromAsset(@NotNull Context context, @NotNull Asset asset) {
            SimplifiedAsset simplifiedAsset = new SimplifiedAsset(asset);
            Database db = Database.getInstance(context);

            setModelName(db, simplifiedAsset);
            setStatusName(context, db, simplifiedAsset);
            setAssignedToName(db, simplifiedAsset);
            setLocationName(db, simplifiedAsset);
            setCategoryName(db, simplifiedAsset);
            setManufacturerName(db, simplifiedAsset);
            setCompanyName(db, simplifiedAsset);

            return simplifiedAsset;
        }

        private void setModelName(Database db, SimplifiedAsset asset) {
            int modelID = asset.getModelID();
            if (modelID != -1) {
                String modelName = modelMap.get(modelID);
                if (modelName == null) {
                    try {
                        Model m = db.findModelByID(modelID);
                        modelName = m.getName();
                        modelMap.put(modelID, modelName);
                    } catch (ModelNotFoundException e) {
                    }
                }
                asset.setModelName(modelName);
            }
        }

        private void setStatusName(Context context, Database db, SimplifiedAsset asset) {
            int statusID = asset.getStatusID();
            //if the asset is assigned then use the virtual assigned status
            if (asset.getAssignedToID() != -1) {
                String status = context.getString(R.string.asset_status_assigned);
                asset.setStatusName(status);
            } else if (statusID != -1) {
                String statusName = statusMap.get(statusID);
                if (statusName == null) {
                    try {
                        Status s = db.findStatusByID(statusID);
                        statusName = s.getName();
                        statusMap.put(statusID, statusName);
                    } catch (StatusNotFoundException e) {
                    }
                }
                asset.setStatusName(statusName);
            }
        }

        private void setAssignedToName(Database db, SimplifiedAsset asset) {
            int assignedToID = asset.getAssignedToID();
            if (assignedToID != -1) {
                String userName = assignedToMap.get(assignedToID);
                if (userName == null) {
                    try {
                        User u = db.findUserByID(assignedToID);
                        userName = u.getName();
                        assignedToMap.put(assignedToID, userName);
                    } catch (UserNotFoundException e) {
                    }
                }
                asset.setAssignedToName(userName);
            }
        }

        private void setLocationName(Database db, SimplifiedAsset asset) {
            int locationID = asset.getLocationID();
            if (locationID != -1) {
                String locationName = locationMap.get(locationID);
                if (locationName == null) {
                    // TODO: 9/17/17 implement this part once locations are added
//                    try {
//                        User u = db.(locationID);
//                        locationName = u.getName();
//                        assignedToMap.put(locationID, locationName);
//                    } catch (UserNotFoundException e) {
//                    }
                }
                asset.setLocationName(locationName);
            }
        }

        private void setCategoryName(Database db, SimplifiedAsset asset) {
            int categoryID = asset.getCategoryID();
            if (categoryID != -1) {
                String categoryName = categoryMap.get(categoryID);
                if (categoryName == null) {
                    try {
                        Category c = db.findCategoryByID(categoryID);
                        categoryName = c.getName();
                        categoryMap.put(categoryID, categoryName);
                    } catch (CategoryNotFoundException e) {
                    }
                }
                asset.setCategoryName(categoryName);
            }
        }

        private void setManufacturerName(Database db, SimplifiedAsset asset) {
            int manufacturerID = asset.getManufacturerID();
            if (manufacturerID != -1) {
                String manufacturerName = manufacturerMap.get(manufacturerID);
                if (manufacturerName == null) {
                    try {
                        Manufacturer m = db.findManufacturerByID(manufacturerID);
                        manufacturerName = m.getName();
                        manufacturerMap.put(manufacturerID, manufacturerName);
                    } catch (ManufacturerNotFoundException e) {
                    }
                }
                asset.setManufacturerName(manufacturerName);
            }
        }

        private void setCompanyName(Database db, SimplifiedAsset asset) {
            int companyID = asset.getCompanyID();
            if (companyID != -1) {
                String companyName = companyMap.get(companyID);
                if (companyName == null) {
                    // TODO: 9/17/17 implement this onces Companies are added
//                    try {
//                        Manufacturer m = db.(companyID);
//                        companyName = m.getName();
//                        companyMap.put(companyID, companyName);
//                    } catch (ManufacturerNotFoundException e) {
//                    }
                }
                asset.setCompanyName(companyName);
            }
        }


    }
}
