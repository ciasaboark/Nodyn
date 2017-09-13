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
import io.phobotic.nodyn.database.model.Model;


/**
 * Created by Jonathan Nelson on 7/10/17.
 */

public class ModelTableHelper extends TableHelper<Model> {
    private static final String TAG = ModelTableHelper.class.getSimpleName();

    public ModelTableHelper(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public void replace(List<Model> list) {
        clearTable();
        for (Model model : list) {
            insert(model);
        }
    }

    private void clearTable() {
        int rows = db.delete(DatabaseOpenHelper.TABLE_MODEL, "1", null);
        Log.d(TAG, "Deleted " + rows + " rows from models table");
    }

    @Override
    public void insert(Model item) {
        ContentValues cv = new ContentValues();
        cv.put(Model.Columns.ID, item.getId());
        cv.put(Model.Columns.MANUFACTURER_ID, item.getManufacturer());
        cv.put(Model.Columns.NAME, item.getName());
        cv.put(Model.Columns.IMAGE, item.getImage());
        cv.put(Model.Columns.MODEL_NUMBER, item.getModelnumber());
        cv.put(Model.Columns.NUM_ASSETS, item.getNumassets());
        cv.put(Model.Columns.DEPRECIATION, item.getDepreciation());
        cv.put(Model.Columns.CATEGORY_ID, item.getCategory());
        cv.put(Model.Columns.EOL, item.getEol());
        cv.put(Model.Columns.NOTE, item.getNote());
        cv.put(Model.Columns.FIELDSET, item.getFieldset());

        long rowID = db.insertWithOnConflict(DatabaseOpenHelper.TABLE_MODEL, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "inserted model " + item.getId() + " - " + item.getName() +
                " as row " + rowID);
    }

    @Override
    public Model findByID(int id) {
        String[] args = {String.valueOf(id)};
        String selection = Model.Columns.ID + " = ?";
        Cursor cursor;
        Model model = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_MODEL, null, selection, args,
                    null, null, Model.Columns.ID, null);
            while (cursor.moveToNext()) {
                model = getModelFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for model with ID " + id + ": " +
                    e.getMessage());
            e.printStackTrace();
        }

        return model;
    }

    @Override
    public Model findByName(String name) {
        String[] args = {name};
        String selection = Model.Columns.ID + " = ?";
        Cursor cursor;
        Model model = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_MODEL, null, selection, args,
                    null, null, Model.Columns.ID, null);
            while (cursor.moveToNext()) {
                model = getModelFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for model name " + name + ": " +
                    e.getMessage());
            e.printStackTrace();
        }

        return model;
    }

    @NotNull
    @Override
    public List<Model> findAll() {
        List<Model> models = new ArrayList<>();

        Cursor cursor;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_MODEL, null, null, null,
                    null, null, Model.Columns.ID, null);
            while (cursor.moveToNext()) {
                Model model = getModelFromCursor(cursor);

                models.add(model);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for models: " +
                    e.getMessage());
            e.printStackTrace();
        }

        return models;
    }

    private Model getModelFromCursor(Cursor cursor) throws Exception {
        Model model = null;
        int id = cursor.getInt(cursor.getColumnIndex(Model.Columns.ID));
        String manufacturer = cursor.getString(cursor.getColumnIndex(Model.Columns.MANUFACTURER_ID));
        String name = cursor.getString(cursor.getColumnIndex(Model.Columns.NAME));
        String image = cursor.getString(cursor.getColumnIndex(Model.Columns.IMAGE));
        String modelNumber = cursor.getString(cursor.getColumnIndex(Model.Columns.MODEL_NUMBER));
        int numAssets = cursor.getInt(cursor.getColumnIndex(Model.Columns.NUM_ASSETS));
        String depreciation = cursor.getString(cursor.getColumnIndex(Model.Columns.DEPRECIATION));
        String category = cursor.getString(cursor.getColumnIndex(Model.Columns.CATEGORY_ID));
        String eol = cursor.getString(cursor.getColumnIndex(Model.Columns.EOL));
        String note = cursor.getString(cursor.getColumnIndex(Model.Columns.NOTE));
        String fieldset = cursor.getString(cursor.getColumnIndex(Model.Columns.FIELDSET));

        model = new Model()
                .setId(id)
                .setManufacturer(manufacturer)
                .setName(name)
                .setImage(image)
                .setModelnumber(modelNumber)
                .setNumassets(numAssets)
                .setDepreciation(depreciation)
                .setCategory(category)
                .setEol(eol)
                .setNote(note)
                .setFieldset(fieldset);

        return model;
    }

}
