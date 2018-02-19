/*
 * Copyright (c) 2018 Jonathan Nelson <ciasaboark@gmail.com>
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

import io.phobotic.nodyn_app.database.DatabaseOpenHelper;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 7/9/17.
 */

public class UserTableHelper extends TableHelper<User> {
    private static final String TAG = UserTableHelper.class.getSimpleName();

    public UserTableHelper(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public void replace(List<User> list) {
        clearTable();
        for (User user : list) {
            insert(user);
        }
    }

    @Override
    public long insert(User item) {
        ContentValues cv = new ContentValues();
        cv.put(User.Columns.ID, item.getId());
        cv.put(User.Columns.NAME, item.getName());
        cv.put(User.Columns.JOB_TITLE, item.getJobTitle());
        cv.put(User.Columns.EMAIL, item.getEmail());
        cv.put(User.Columns.USERNAME, item.getUsername());
        cv.put(User.Columns.LOCATION_ID, item.getLocationID());
        cv.put(User.Columns.MANAGER_ID, item.getManagerID());
        cv.put(User.Columns.NUM_ASSETS, item.getNumAssets());
        cv.put(User.Columns.EMPLOYEE_NUM, item.getEmployeeNum());

        String groupsString = "";
        String prefix = "";
        int[] groupIDs = item.getGroupsIDs();
        if (groupIDs != null) {
            for (int g : groupIDs) {
                groupsString += prefix + String.valueOf(g);
                prefix = ",";
            }
        }

        cv.put(User.Columns.GROUP_IDS, groupsString);
        cv.put(User.Columns.NOTES, item.getNotes());
        cv.put(User.Columns.COMPANY_ID, item.getCompanyID());

        long rowID = db.insertWithOnConflict(DatabaseOpenHelper.TABLE_USER, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
//        Log.d(TAG, "inserted user " + item.getId() + " - " + item.getName() +
//                " as row " + rowID);
        return rowID;
    }

    @Override
    public User findByID(int id) {
        String[] args = {String.valueOf(id)};
        String selection = User.Columns.ID + " = ?";
        Cursor cursor = null;
        User user = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_USER, null, selection, args,
                    null, null, User.Columns.ID, null);
            while (cursor.moveToNext()) {
                user = getUserFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for user with ID " + id + ": " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return user;
    }

    public User findByName(String name) {
        String[] args = {name};
        String selection = User.Columns.NAME + " = ?";
        Cursor cursor = null;
        User user = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_USER, null, selection, args,
                    null, null, User.Columns.ID, null);
            while (cursor.moveToNext()) {
                user = getUserFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for user with name " + name + ": " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return user;
    }

    @NotNull
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_USER, null, null, null,
                    null, null, User.Columns.ID, null);
            while (cursor.moveToNext()) {
                User user = getUserFromCursor(cursor);
                users.add(user);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for users: " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return users;
    }

    private User getUserFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(User.Columns.ID));
        String cname = cursor.getString(cursor.getColumnIndex(User.Columns.NAME));
        String jobTitle = cursor.getString(cursor.getColumnIndex(User.Columns.JOB_TITLE));
        String email = cursor.getString(cursor.getColumnIndex(User.Columns.EMAIL));
        String username = cursor.getString(cursor.getColumnIndex(User.Columns.USERNAME));
        int locationID = cursor.getInt(cursor.getColumnIndex(User.Columns.LOCATION_ID));
        int managerID = cursor.getInt(cursor.getColumnIndex(User.Columns.MANAGER_ID));
        int numAssets = cursor.getInt(cursor.getColumnIndex(User.Columns.NUM_ASSETS));
        String groupsString = cursor.getString(cursor.getColumnIndex(User.Columns.GROUP_IDS));
        String notes = cursor.getString(cursor.getColumnIndex(User.Columns.NOTES));
        int companyID = cursor.getInt(cursor.getColumnIndex(User.Columns.COMPANY_ID));

        String[] numParts = groupsString.split(",");
        int[] groupIDs = new int[numParts.length];
        if (numParts.length > 0) {
            for (int i = 0; i < groupIDs.length; i++) {
                String part = numParts[i];
                try {
                    int num = Integer.parseInt(part);
                    groupIDs[i] = num;
                } catch (NumberFormatException e) {
                    Log.d(TAG, "Unable to convert group ID: '" + part + "' into an int group " +
                            "ID, skipping");
                }
            }
        }

        return new User()
                .setId(id)
                .setName(cname)
                .setJobTitle(jobTitle)
                .setEmail(email)
                .setUsername(username)
                .setLocationID(locationID)
                .setManagerID(managerID)
                .setNumAssets(numAssets)
                .setGroupsIDs(groupIDs)
                .setNotes(notes)
                .setCompanyID(companyID);
    }

    private void clearTable() {
        int rowsDeleted = db.delete(DatabaseOpenHelper.TABLE_USER, "1", null);
        Log.d(TAG, "deleted " + rowsDeleted + " from users table");
    }

    public User findByUsername(String username) {
        User user = null;

        String[] args = {username};
        String selection = User.Columns.USERNAME + " = ?";
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_USER, null, selection, args,
                    null, null, User.Columns.ID, null);
            while (cursor.moveToNext()) {
                user = getUserFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for user with username " + username +
                    ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return user;
    }
}
