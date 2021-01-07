/*
 * Copyright (c) 2020 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.database.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.DatabaseOpenHelper;
import io.phobotic.nodyn_app.database.exception.CompanyNotFoundException;
import io.phobotic.nodyn_app.database.exception.GroupNotFoundException;

import io.phobotic.nodyn_app.database.model.Company;
import io.phobotic.nodyn_app.database.model.User;


/**
 * Created by Jonathan Nelson on 7/10/17.
 */

public class CompanyTableHelper extends TableHelper<Company> {
    private static final String TAG = CompanyTableHelper.class.getSimpleName();

    /**
     * Returns the user's groups formatted by name
     *
     * @return
     */
    public static String getCompanyString(User user, Database db) {
        String companyString = null;
        int companyID = user.getCompanyID();
        companyString = "";
        try {
            Company c = db.findCompanyById(companyID);
            companyString = c.getName();
        } catch (CompanyNotFoundException e) {
            Log.d(TAG, String.format("Unable to find company id %d for user id %d", companyID, user.getId()));
        }

        return companyString;
    }

    public CompanyTableHelper(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public void replace(List<Company> list) {
        clearTable();
        for (Company company : list) {
            insert(company);
        }
    }

    private void clearTable() {
        int rows = db.delete(DatabaseOpenHelper.TABLE_COMPANY, "1", null);
        Log.d(TAG, "Deleted " + rows + " rows from company table");
    }

    @Override
    public long insert(Company item) {
        ContentValues cv = new ContentValues();
        cv.put(Company.Columns.ID, item.getId());
        cv.put(Company.Columns.NAME, item.getName());
        cv.put(Company.Columns.USER_COUNT, item.getUserCount());
        cv.put(Company.Columns.ASSET_COUNT, item.getAssetCount());
        cv.put(Company.Columns.IMAGE, item.getImage());
        cv.put(Company.Columns.CREATED_AT, item.getCreatedAt());

        long rowID = db.insertWithOnConflict(DatabaseOpenHelper.TABLE_COMPANY, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        return rowID;
    }

    @Override
    public Company findByID(int id) {
        String[] args = {String.valueOf(id)};
        String selection = Company.Columns.ID + " = ?";
        Cursor cursor = null;
        Company company = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_COMPANY, null, selection, args,
                    null, null, Company.Columns.ID, null);
            while (cursor.moveToNext()) {
                company = getCompanyFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for company with ID " + id + ": " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return company;
    }

    public Company findByName(String name) {
        String[] args = {name};
        String selection = Company.Columns.NAME + " = ?";
        Cursor cursor = null;
        Company company = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_COMPANY, null, selection, args,
                    null, null, Company.Columns.ID, null);
            while (cursor.moveToNext()) {
                company = getCompanyFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for company with name " + name + ": " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return company;
    }

    @NotNull
    @Override
    public List<Company> findAll() {
        List<Company> companies = new ArrayList<>();

        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_COMPANY, null, null, null,
                    null, null, Company.Columns.ID, null);
            while (cursor.moveToNext()) {
                Company company = getCompanyFromCursor(cursor);
                companies.add(company);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for companies: " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return companies;
    }

    private Company getCompanyFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(Company.Columns.ID));
        String cname = cursor.getString(cursor.getColumnIndex(Company.Columns.NAME));
        int userCount = cursor.getInt(cursor.getColumnIndex(Company.Columns.USER_COUNT));
        int assetCount = cursor.getInt(cursor.getColumnIndex(Company.Columns.ASSET_COUNT));
        String image = cursor.getString(cursor.getColumnIndex(Company.Columns.IMAGE));
        long createdAt = cursor.getLong(cursor.getColumnIndex(Company.Columns.CREATED_AT));

        Company company = new Company()
                .setId(id)
                .setName(cname)
                .setUserCount(userCount)
                .setAssetCount(assetCount)
                .setImage(image)
                .setCreatedAt(createdAt);

        return company;
    }

}
