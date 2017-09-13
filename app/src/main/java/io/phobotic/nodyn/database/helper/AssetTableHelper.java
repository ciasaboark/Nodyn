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

package io.phobotic.nodyn.database.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn.database.DatabaseOpenHelper;
import io.phobotic.nodyn.database.model.Asset;


/**
 * Created by Jonathan Nelson on 7/8/17.
 */

public class AssetTableHelper extends TableHelper<Asset> {
    public static final String[] DB_PROJECTION = new String[]{
            Asset.Columns.ID,
            Asset.Columns.IMAGE,
            Asset.Columns.NAME,
            Asset.Columns.TAG,
            Asset.Columns.SERIAL,
            Asset.Columns.MODEL_ID,
            Asset.Columns.STATUS_ID,
            Asset.Columns.ASSIGNED_TO_ID,
            Asset.Columns.LOCATION_ID,
            Asset.Columns.CATEGORY_ID,
            Asset.Columns.MANUFACTURER_ID,
            Asset.Columns.EOL,
            Asset.Columns.PURCHASE_COST,
            Asset.Columns.PURCHASE_DATE,
            Asset.Columns.NOTES,
            Asset.Columns.ORDER_NUMBER,
            Asset.Columns.LAST_CHECKOUT,
            Asset.Columns.EXPECTED_CHECKIN,
            Asset.Columns.CREATED_AT,
            Asset.Columns.COMPANY_NAME
    };
    private static final String TAG = AssetTableHelper.class.getSimpleName();


    public AssetTableHelper(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public void replace(List<Asset> assets) {
        Log.d(TAG, "Replacing all assets with asset list of size " + assets.size());

        //use '1' as the where clause so we can get a count of the rows deleted
        int count = db.delete(DatabaseOpenHelper.TABLE_ASSETS, "1", null);
        Log.d(TAG, "Deleted " + count + " rows from assets table");

        for (Asset asset: assets) {
            insert(asset);
        }

        Log.d(TAG, "Finished adding assets to table");
    }

    @Override
    public void insert(Asset asset) {
        ContentValues cv = new ContentValues();
        cv.put(Asset.Columns.ID, asset.getId());
        cv.put(Asset.Columns.IMAGE, asset.getImage());
        cv.put(Asset.Columns.NAME, asset.getName());
        cv.put(Asset.Columns.TAG, asset.getTag());
        cv.put(Asset.Columns.SERIAL, asset.getSerial());
        cv.put(Asset.Columns.MODEL_ID, asset.getModel());
        cv.put(Asset.Columns.STATUS_ID, asset.getStatus());
        cv.put(Asset.Columns.ASSIGNED_TO_ID, asset.getAssignedTo());
        cv.put(Asset.Columns.LOCATION_ID, asset.getLocation());
        cv.put(Asset.Columns.CATEGORY_ID, asset.getCategory());
        cv.put(Asset.Columns.MANUFACTURER_ID, asset.getManufacturer());
        cv.put(Asset.Columns.EOL, asset.getEol());
        cv.put(Asset.Columns.PURCHASE_COST, asset.getPurchaseCost());
        cv.put(Asset.Columns.PURCHASE_DATE, asset.getPurchaseDate());
        cv.put(Asset.Columns.NOTES, asset.getNotes());
        cv.put(Asset.Columns.ORDER_NUMBER, asset.getOrderNumber());
        cv.put(Asset.Columns.LAST_CHECKOUT, asset.getLastCheckout());
        cv.put(Asset.Columns.EXPECTED_CHECKIN, asset.getExpectedCheckin());
        cv.put(Asset.Columns.CREATED_AT, asset.getCreatedAt());
        cv.put(Asset.Columns.COMPANY_NAME, asset.getCompanyName());

        long rowID = db.insertWithOnConflict(DatabaseOpenHelper.TABLE_ASSETS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "inserted asset with tag '" + asset.getTag() + "' as row " + rowID);
    }

    @Override
    public Asset findByID(int id) {
        String[] args = {String.valueOf(id)};
        String selection = Asset.Columns.ID + " = ?";
        Cursor cursor;
        Asset asset = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_ASSETS, DB_PROJECTION, selection, args,
                    null, null, Asset.Columns.ID, null);
            while (cursor.moveToNext()) {
                int cid = cursor.getInt(cursor.getColumnIndex(Asset.Columns.ID));
                String image = cursor.getString(cursor.getColumnIndex(Asset.Columns.IMAGE));
                String name = cursor.getString(cursor.getColumnIndex(Asset.Columns.NAME));
                String tag = cursor.getString(cursor.getColumnIndex(Asset.Columns.TAG));
                String serial = cursor.getString(cursor.getColumnIndex(Asset.Columns.SERIAL));
                String model = cursor.getString(cursor.getColumnIndex(Asset.Columns.MODEL_ID));
                String status = cursor.getString(cursor.getColumnIndex(Asset.Columns.STATUS_ID));
                String assignedTo = cursor.getString(cursor.getColumnIndex(Asset.Columns.ASSIGNED_TO_ID));
                String location = cursor.getString(cursor.getColumnIndex(Asset.Columns.LOCATION_ID));
                String category = cursor.getString(cursor.getColumnIndex(Asset.Columns.CATEGORY_ID));
                String manucaturer = cursor.getString(cursor.getColumnIndex(Asset.Columns.MANUFACTURER_ID));
                String eol = cursor.getString(cursor.getColumnIndex(Asset.Columns.EOL));
                String purchaseCost = cursor.getString(cursor.getColumnIndex(Asset.Columns.PURCHASE_COST));
                String purchaseDate = cursor.getString(cursor.getColumnIndex(Asset.Columns.PURCHASE_DATE));
                String notes = cursor.getString(cursor.getColumnIndex(Asset.Columns.NOTES));
                String orderNumber = cursor.getString(cursor.getColumnIndex(Asset.Columns.ORDER_NUMBER));
                String lastCheckout = cursor.getString(cursor.getColumnIndex(Asset.Columns.LAST_CHECKOUT));
                String expectedCheckin = cursor.getString(cursor.getColumnIndex(Asset.Columns.EXPECTED_CHECKIN));
                String createdAt = cursor.getString(cursor.getColumnIndex(Asset.Columns.CREATED_AT));
                String companyName = cursor.getString(cursor.getColumnIndex(Asset.Columns.COMPANY_NAME));

                asset = new Asset()
                        .setId(cid)
                        .setImage(image)
                        .setName(name)
                        .setTag(tag)
                        .setSerial(serial)
                        .setModel(model)
                        .setStatus(status)
                        .setAssignedTo(assignedTo)
                        .setLocation(location)
                        .setCategory(category)
                        .setManufacturer(manucaturer)
                        .setEol(eol)
                        .setPurchaseCost(purchaseCost)
                        .setPurchaseDate(purchaseDate)
                        .setNotes(notes)
                        .setOrderNumber(orderNumber)
                        .setLastCheckout(lastCheckout)
                        .setExpectedCheckin(expectedCheckin)
                        .setCreatedAt(createdAt)
                        .setCompanyName(companyName);

            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for asset with ID " + id + ": " +
                    e.getMessage());
            e.printStackTrace();
        }

        return asset;
    }

    @Override
    public Asset findByName(String name) {
        String[] args = {name};
        String selection = Asset.Columns.NAME + " = ?";
        Cursor cursor;
        Asset asset = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_ASSETS, DB_PROJECTION, selection, args,
                    null, null, Asset.Columns.ID, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(Asset.Columns.ID));
                String image = cursor.getString(cursor.getColumnIndex(Asset.Columns.IMAGE));
                String tag = cursor.getString(cursor.getColumnIndex(Asset.Columns.TAG));
                String serial = cursor.getString(cursor.getColumnIndex(Asset.Columns.SERIAL));
                String model = cursor.getString(cursor.getColumnIndex(Asset.Columns.MODEL_ID));
                String status = cursor.getString(cursor.getColumnIndex(Asset.Columns.STATUS_ID));
                String assignedTo = cursor.getString(cursor.getColumnIndex(Asset.Columns.ASSIGNED_TO_ID));
                String location = cursor.getString(cursor.getColumnIndex(Asset.Columns.LOCATION_ID));
                String category = cursor.getString(cursor.getColumnIndex(Asset.Columns.CATEGORY_ID));
                String manucaturer = cursor.getString(cursor.getColumnIndex(Asset.Columns.MANUFACTURER_ID));
                String eol = cursor.getString(cursor.getColumnIndex(Asset.Columns.EOL));
                String purchaseCost = cursor.getString(cursor.getColumnIndex(Asset.Columns.PURCHASE_COST));
                String purchaseDate = cursor.getString(cursor.getColumnIndex(Asset.Columns.PURCHASE_DATE));
                String notes = cursor.getString(cursor.getColumnIndex(Asset.Columns.NOTES));
                String orderNumber = cursor.getString(cursor.getColumnIndex(Asset.Columns.ORDER_NUMBER));
                String lastCheckout = cursor.getString(cursor.getColumnIndex(Asset.Columns.LAST_CHECKOUT));
                String expectedCheckin = cursor.getString(cursor.getColumnIndex(Asset.Columns.EXPECTED_CHECKIN));
                String createdAt = cursor.getString(cursor.getColumnIndex(Asset.Columns.CREATED_AT));
                String companyName = cursor.getString(cursor.getColumnIndex(Asset.Columns.COMPANY_NAME));

                asset = new Asset()
                        .setId(id)
                        .setImage(image)
                        .setName(name)
                        .setTag(tag)
                        .setSerial(serial)
                        .setModel(model)
                        .setStatus(status)
                        .setAssignedTo(assignedTo)
                        .setLocation(location)
                        .setCategory(category)
                        .setManufacturer(manucaturer)
                        .setEol(eol)
                        .setPurchaseCost(purchaseCost)
                        .setPurchaseDate(purchaseDate)
                        .setNotes(notes)
                        .setOrderNumber(orderNumber)
                        .setLastCheckout(lastCheckout)
                        .setExpectedCheckin(expectedCheckin)
                        .setCreatedAt(createdAt)
                        .setCompanyName(companyName);

            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for asset with name " + name + ": " +
                    e.getMessage());
            e.printStackTrace();
        }

        return asset;
    }

    @Override
    public List<Asset> findAll() {
        List<Asset> assets = new ArrayList<>();

        Cursor cursor;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_ASSETS, DB_PROJECTION, null, null,
                    null, null, Asset.Columns.ID, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(Asset.Columns.ID));
                String image = cursor.getString(cursor.getColumnIndex(Asset.Columns.IMAGE));
                String name = cursor.getString(cursor.getColumnIndex(Asset.Columns.NAME));
                String tag = cursor.getString(cursor.getColumnIndex(Asset.Columns.TAG));
                String serial = cursor.getString(cursor.getColumnIndex(Asset.Columns.SERIAL));
                String model = cursor.getString(cursor.getColumnIndex(Asset.Columns.MODEL_ID));
                String status = cursor.getString(cursor.getColumnIndex(Asset.Columns.STATUS_ID));
                String assignedTo = cursor.getString(cursor.getColumnIndex(Asset.Columns.ASSIGNED_TO_ID));
                String location = cursor.getString(cursor.getColumnIndex(Asset.Columns.LOCATION_ID));
                String category = cursor.getString(cursor.getColumnIndex(Asset.Columns.CATEGORY_ID));
                String manucaturer = cursor.getString(cursor.getColumnIndex(Asset.Columns.MANUFACTURER_ID));
                String eol = cursor.getString(cursor.getColumnIndex(Asset.Columns.EOL));
                String purchaseCost = cursor.getString(cursor.getColumnIndex(Asset.Columns.PURCHASE_COST));
                String purchaseDate = cursor.getString(cursor.getColumnIndex(Asset.Columns.PURCHASE_DATE));
                String notes = cursor.getString(cursor.getColumnIndex(Asset.Columns.NOTES));
                String orderNumber = cursor.getString(cursor.getColumnIndex(Asset.Columns.ORDER_NUMBER));
                String lastCheckout = cursor.getString(cursor.getColumnIndex(Asset.Columns.LAST_CHECKOUT));
                String expectedCheckin = cursor.getString(cursor.getColumnIndex(Asset.Columns.EXPECTED_CHECKIN));
                String createdAt = cursor.getString(cursor.getColumnIndex(Asset.Columns.CREATED_AT));
                String companyName = cursor.getString(cursor.getColumnIndex(Asset.Columns.COMPANY_NAME));

                Asset asset = new Asset()
                        .setId(id)
                        .setImage(image)
                        .setName(name)
                        .setTag(tag)
                        .setSerial(serial)
                        .setModel(model)
                        .setStatus(status)
                        .setAssignedTo(assignedTo)
                        .setLocation(location)
                        .setCategory(category)
                        .setManufacturer(manucaturer)
                        .setEol(eol)
                        .setPurchaseCost(purchaseCost)
                        .setPurchaseDate(purchaseDate)
                        .setNotes(notes)
                        .setOrderNumber(orderNumber)
                        .setLastCheckout(lastCheckout)
                        .setExpectedCheckin(expectedCheckin)
                        .setCreatedAt(createdAt)
                        .setCompanyName(companyName);

                assets.add(asset);

            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for assets: " +
                    e.getMessage());
            e.printStackTrace();
        }

        return assets;
    }

    public Asset findByTag(String tag) {
        String[] args = {tag};
        String selection = Asset.Columns.TAG + " = ?";
        Cursor cursor;
        Asset asset = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_ASSETS, DB_PROJECTION, selection, args,
                    null, null, Asset.Columns.ID, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(Asset.Columns.ID));
                String image = cursor.getString(cursor.getColumnIndex(Asset.Columns.IMAGE));
                String name = cursor.getString(cursor.getColumnIndex(Asset.Columns.NAME));
                String serial = cursor.getString(cursor.getColumnIndex(Asset.Columns.SERIAL));
                String model = cursor.getString(cursor.getColumnIndex(Asset.Columns.MODEL_ID));
                String status = cursor.getString(cursor.getColumnIndex(Asset.Columns.STATUS_ID));
                String assignedTo = cursor.getString(cursor.getColumnIndex(Asset.Columns.ASSIGNED_TO_ID));
                String location = cursor.getString(cursor.getColumnIndex(Asset.Columns.LOCATION_ID));
                String category = cursor.getString(cursor.getColumnIndex(Asset.Columns.CATEGORY_ID));
                String manucaturer = cursor.getString(cursor.getColumnIndex(Asset.Columns.MANUFACTURER_ID));
                String eol = cursor.getString(cursor.getColumnIndex(Asset.Columns.EOL));
                String purchaseCost = cursor.getString(cursor.getColumnIndex(Asset.Columns.PURCHASE_COST));
                String purchaseDate = cursor.getString(cursor.getColumnIndex(Asset.Columns.PURCHASE_DATE));
                String notes = cursor.getString(cursor.getColumnIndex(Asset.Columns.NOTES));
                String orderNumber = cursor.getString(cursor.getColumnIndex(Asset.Columns.ORDER_NUMBER));
                String lastCheckout = cursor.getString(cursor.getColumnIndex(Asset.Columns.LAST_CHECKOUT));
                String expectedCheckin = cursor.getString(cursor.getColumnIndex(Asset.Columns.EXPECTED_CHECKIN));
                String createdAt = cursor.getString(cursor.getColumnIndex(Asset.Columns.CREATED_AT));
                String companyName = cursor.getString(cursor.getColumnIndex(Asset.Columns.COMPANY_NAME));

                asset = new Asset()
                        .setId(id)
                        .setImage(image)
                        .setName(name)
                        .setTag(tag)
                        .setSerial(serial)
                        .setModel(model)
                        .setStatus(status)
                        .setAssignedTo(assignedTo)
                        .setLocation(location)
                        .setCategory(category)
                        .setManufacturer(manucaturer)
                        .setEol(eol)
                        .setPurchaseCost(purchaseCost)
                        .setPurchaseDate(purchaseDate)
                        .setNotes(notes)
                        .setOrderNumber(orderNumber)
                        .setLastCheckout(lastCheckout)
                        .setExpectedCheckin(expectedCheckin)
                        .setCreatedAt(createdAt)
                        .setCompanyName(companyName);

            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for asset with TAG " + tag + ": " +
                    e.getMessage());
            e.printStackTrace();
        }

        return asset;
    }
}
