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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn.database.DatabaseOpenHelper;
import io.phobotic.nodyn.database.model.Manufacturer;

/**
 * Created by Jonathan Nelson on 9/14/17.
 */

public class ManufacturerTableHelper extends TableHelper<Manufacturer> {
    private static final String TAG = ManufacturerTableHelper.class.getSimpleName();

    public ManufacturerTableHelper(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public void replace(List<Manufacturer> list) {
        clearTable();
        for (Manufacturer manufacturer : list) {
            insert(manufacturer);
        }
    }

    private void clearTable() {
        int rows = db.delete(DatabaseOpenHelper.TABLE_MANUFACTURER, "1", null);
        Log.d(TAG, "Deleted " + rows + " rows from manufacturers table");
    }

    @Override
    public void insert(Manufacturer item) {
        ContentValues cv = new ContentValues();
        cv.put(Manufacturer.Columns.ID, item.getId());
        cv.put(Manufacturer.Columns.NAME, item.getName());
        cv.put(Manufacturer.Columns.CREATED_AT, item.getCreatedAt());
        cv.put(Manufacturer.Columns.SUPPORT_EMAIL, item.getSupportEmail());
        cv.put(Manufacturer.Columns.SUPPORT_PHONE, item.getSupportPhone());
        cv.put(Manufacturer.Columns.SUPPORT_URL, item.getSupportURL());
        cv.put(Manufacturer.Columns.URL, item.getURL());

        long rowID = db.insertWithOnConflict(DatabaseOpenHelper.TABLE_MANUFACTURER, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "inserted manufacturer " + item.getId() + " - " + item.getName() +
                " as row " + rowID);
    }

    @Override
    public Manufacturer findByID(int id) {
        String[] args = {String.valueOf(id)};
        String selection = Manufacturer.Columns.ID + " = ?";
        Cursor cursor = null;
        Manufacturer manufacturer = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_MANUFACTURER, null, selection, args,
                    null, null, Manufacturer.Columns.ID, null);
            while (cursor.moveToNext()) {
                manufacturer = getManufacturerFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for manufacturer with ID " + id + ": " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return manufacturer;
    }

    @Override
    public Manufacturer findByName(String name) {
        String[] args = {name};
        String selection = Manufacturer.Columns.ID + " = ?";
        Cursor cursor = null;
        Manufacturer manufacturer = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_MANUFACTURER, null, selection, args,
                    null, null, Manufacturer.Columns.ID, null);
            while (cursor.moveToNext()) {
                manufacturer = getManufacturerFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for manufacturer name " + name + ": " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return manufacturer;
    }

    @NotNull
    @Override
    public List<Manufacturer> findAll() {
        List<Manufacturer> manufacturers = new ArrayList<>();

        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_MANUFACTURER, null, null, null,
                    null, null, Manufacturer.Columns.ID, null);
            while (cursor.moveToNext()) {
                Manufacturer manufacturer = getManufacturerFromCursor(cursor);

                manufacturers.add(manufacturer);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for manufacturers: " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return manufacturers;
    }

    private Manufacturer getManufacturerFromCursor(Cursor cursor) throws Exception {
        Manufacturer manufacturer = null;
        int id = cursor.getInt(cursor.getColumnIndex(Manufacturer.Columns.ID));
        String name = cursor.getString(cursor.getColumnIndex(Manufacturer.Columns.NAME));
        String createdAt = cursor.getString(cursor.getColumnIndex(Manufacturer.Columns.CREATED_AT));
        String supportEmail = cursor.getString(cursor.getColumnIndex(Manufacturer.Columns.SUPPORT_EMAIL));
        String supportPhone = cursor.getString(cursor.getColumnIndex(Manufacturer.Columns.SUPPORT_PHONE));
        String supportURL = cursor.getString(cursor.getColumnIndex(Manufacturer.Columns.SUPPORT_URL));
        String url = cursor.getString(cursor.getColumnIndex(Manufacturer.Columns.URL));

        manufacturer = new Manufacturer()
                .setId(id)
                .setName(name)
                .setCreatedAt(createdAt)
                .setSupportEmail(supportEmail)
                .setSupportPhone(supportPhone)
                .setSupportURL(supportURL)
                .setURL(url);

        return manufacturer;
    }

}

