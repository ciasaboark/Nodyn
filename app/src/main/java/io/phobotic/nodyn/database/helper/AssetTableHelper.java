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
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.phobotic.nodyn.database.AssetDatabaseOpenHelper;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.model.Asset;

/**
 * Created by Jonathan Nelson on 7/8/17.
 */

public class AssetTableHelper {
    private static final String TAG = AssetTableHelper.class.getSimpleName();
    public static final String[] DB_PROJECTION = new String[]{
            Asset.Columns.ID,
            Asset.Columns.IMAGE,
            Asset.Columns.NAME,
            Asset.Columns.TAG,
            Asset.Columns.SERIAL,
            Asset.Columns.MODEL,
            Asset.Columns.STATUS,
            Asset.Columns.ASSIGNED_TO,
            Asset.Columns.LOCATION,
            Asset.Columns.CATEGORY,
            Asset.Columns.MANUFACTURER,
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

    private static final int DB_PROJECTION_ID = 1;
    private static final int DB_PROJECTION_IMAGE = 2;
    private static final int DB_PROJECTION_NAME = 3;
    private static final int DB_PROJECTION_TAG = 4;
    private static final int DB_PROJECTION_SERIAL = 5;
    private static final int DB_PROJECTION_MODEL = 6;
    private static final int DB_PROJECTION_STATUS = 7;
    private static final int DB_PROJECTION_ASSIGNED_TO = 8;
    private static final int DB_PROJECTION_LOCATION = 9;
    private static final int DB_PROJECTION_CATEGORY = 10;
    private static final int DB_PROJECTION_MANUFACTURER = 11;
    private static final int DB_PROJECTION_EOL = 12;
    private static final int DB_PROJECTION_PURCHASE_COST = 13;
    private static final int DB_PROJECTION_PURCHASE_DATE = 14;
    private static final int DB_PROJECTION_NOTES = 15;
    private static final int DB_PROJECTION_ORDER_NUMBER = 16;
    private static final int DB_PROJECTION_LAST_CHECKOUT = 17;
    private static final int DB_PROJECTION_EXPECTED_CHECKIN = 18;
    private static final int DB_PROJECTION_CREATED_AT = 19;
    private static final int DB_PROJECTION_COMPANY_NAME = 20;


    private SQLiteDatabase db;

    public AssetTableHelper(SQLiteDatabase db) {
        this.db = db;
    }

    public List<Asset> getAssets() {
        // TODO: 7/8/17
        return null;
    }

    public Asset getAssetByID(int id) {
        // TODO: 7/8/17
        return null;
    }

    public Asset getAssetByTag(String tag) {
        // TODO: 7/8/17
        return null;
    }
    
    public void replaceAssets(@NotNull List<Asset> assets) {
        Log.d(TAG, "Replacing all assets with asset list of size " + assets.size());

        //use '1' as the where clause so we can get a count of the rows deleted
        int count = db.delete(AssetDatabaseOpenHelper.TABLE_ASSETS, "1", null);
        Log.d(TAG, "Deleted " + count + " rows from assets table");

        for (Asset asset: assets) {
            insertAsset(asset);
        }

        Log.d(TAG, "Finished adding assets to table");
    }

    private long insertAsset(Asset asset) {
        ContentValues cv = new ContentValues();
        cv.put(Asset.Columns.ID, asset.getId());
        cv.put(Asset.Columns.IMAGE, asset.getImage());
        cv.put(Asset.Columns.NAME, asset.getName());
        cv.put(Asset.Columns.TAG, asset.getTag());
        cv.put(Asset.Columns.SERIAL, asset.getSerial());
        cv.put(Asset.Columns.MODEL, asset.getModel());
        cv.put(Asset.Columns.STATUS, asset.getStatus());
        cv.put(Asset.Columns.ASSIGNED_TO, asset.getAssignedTo());
        cv.put(Asset.Columns.LOCATION, asset.getLocation());
        cv.put(Asset.Columns.CATEGORY, asset.getCategory());
        cv.put(Asset.Columns.MANUFACTURER, asset.getManufacturer());
        cv.put(Asset.Columns.EOL, asset.getEol());
        cv.put(Asset.Columns.PURCHASE_COST, asset.getPurchaseCost());
        cv.put(Asset.Columns.PURCHASE_DATE, asset.getPurchaseDate());
        cv.put(Asset.Columns.NOTES, asset.getNotes());
        cv.put(Asset.Columns.ORDER_NUMBER, asset.getOrderNumber());
        cv.put(Asset.Columns.LAST_CHECKOUT, asset.getLastCheckout());
        cv.put(Asset.Columns.EXPECTED_CHECKIN, asset.getExpectedCheckin());
        cv.put(Asset.Columns.CREATED_AT, asset.getCreatedAt());
        cv.put(Asset.Columns.COMPANY_NAME, asset.getCompanyName());

        long rowID = db.insertWithOnConflict(AssetDatabaseOpenHelper.TABLE_ASSETS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "inserted asset with tag " + asset.getTag() + " as row " + rowID);

        return rowID;
    }
}
