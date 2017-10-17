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
import io.phobotic.nodyn.database.model.Category;


/**
 * Created by Jonathan Nelson on 7/10/17.
 */

public class CategoryTableHelper extends TableHelper<Category> {
    public static final String TAG = CategoryTableHelper.class.getSimpleName();

    public CategoryTableHelper(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public void replace(List<Category> list) {
        clearTable();
        for (Category category : list) {
            insert(category);
        }
    }

    private void clearTable() {
        int rows = db.delete(DatabaseOpenHelper.TABLE_CATEGORY, "1", null);
        Log.d(TAG, "Deleted " + rows + " rows from the categories table");
    }

    @Override
    public void insert(Category item) {
        ContentValues cv = new ContentValues();
        cv.put(Category.Columns.ID, item.getId());
        cv.put(Category.Columns.NAME, item.getName());
        cv.put(Category.Columns.CATEGORY_TYPE, item.getCategoryType());
        cv.put(Category.Columns.COUNT, item.getCount());
        cv.put(Category.Columns.ACCEPTANCE, item.isAcceptance());
        cv.put(Category.Columns.EULA_TEXT, item.getEulaText());
        cv.put(Category.Columns.USE_DEFAULT_EULA, item.isUseDefaultEula());

        long rowID = db.insertWithOnConflict(DatabaseOpenHelper.TABLE_CATEGORY, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "inserted category " + item.getId() + " - " + item.getName() +
                " as row " + rowID);
    }

    @Override
    public Category findByID(int id) {
        String[] args = {String.valueOf(id)};
        String selection = Category.Columns.ID + " = ?";
        Cursor cursor = null;
        Category category = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_CATEGORY, null, selection, args,
                    null, null, Category.Columns.ID, null);
            while (cursor.moveToNext()) {
                category = getCategoryFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for category with ID " + id + ": " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return category;
    }

    private Category getCategoryFromCursor(Cursor cursor) {
        Category category = null;
        int cid = cursor.getInt(cursor.getColumnIndex(Category.Columns.ID));
        String name = cursor.getString(cursor.getColumnIndex(Category.Columns.NAME));
        String categoryType = cursor.getString(cursor.getColumnIndex(Category.Columns.CATEGORY_TYPE));
        int count = cursor.getInt(cursor.getColumnIndex(Category.Columns.COUNT));
        int acceptance = cursor.getInt(cursor.getColumnIndex(Category.Columns.ACCEPTANCE));
        String eula = cursor.getString(cursor.getColumnIndex(Category.Columns.EULA_TEXT));
        int useDefaultEual = cursor.getInt(cursor.getColumnIndex(Category.Columns.USE_DEFAULT_EULA));

        category = new Category()
                .setId(cid)
                .setName(name)
                .setCategoryType(categoryType)
                .setCount(count)
                .setAcceptance(acceptance == 1)
                .setEulaText(eula)
                .setUseDefaultEula(useDefaultEual == 1);
        return category;
    }

    public Category findByName(String name) {
        String[] args = {name};
        String selection = Category.Columns.NAME + " = ?";
        Cursor cursor = null;
        Category category = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_CATEGORY, null, selection, args,
                    null, null, Category.Columns.ID, null);
            while (cursor.moveToNext()) {
                category = getCategoryFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for category with name " + name + ": " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return category;
    }

    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();

        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_CATEGORY, null, null, null,
                    null, null, Category.Columns.ID, null);
            while (cursor.moveToNext()) {
                Category category = getCategoryFromCursor(cursor);
                categories.add(category);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for categories: " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return categories;
    }
}
