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

import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.DatabaseOpenHelper;
import io.phobotic.nodyn.database.exception.GroupNotFoundException;
import io.phobotic.nodyn.database.model.Group;
import io.phobotic.nodyn.database.model.User;


/**
 * Created by Jonathan Nelson on 7/10/17.
 */

public class GroupTableHelper extends TableHelper<Group> {
    private static final String TAG = GroupTableHelper.class.getSimpleName();

    /**
     * Returns the user's groups formatted by name
     *
     * @return
     */
    public static String getGroupString(User user, Database db) {
        String groupsString = null;
        int[] groupIDs = user.getGroupsIDs();
        if (groupIDs == null || groupIDs.length == 0) {
            //just return a null string
        } else {
            groupsString = "";
            String prefix = "";
            for (int id : groupIDs) {
                try {
                    Group g = db.findGroupByID(id);
                    groupsString += prefix + g.getName();
                    prefix = ", ";
                } catch (GroupNotFoundException e) {
                    Log.d(TAG, "Unable to find group with ID: '" + id + "', skipping");
                }
            }
        }

        return groupsString;
    }

    public GroupTableHelper(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public void replace(List<Group> list) {
        clearTable();
        for (Group group : list) {
            insert(group);
        }
    }

    private void clearTable() {
        int rows = db.delete(DatabaseOpenHelper.TABLE_GROUP, "1", null);
        Log.d(TAG, "Deleted " + rows + " rows from group table");
    }

    @Override
    public void insert(Group item) {
        ContentValues cv = new ContentValues();
        cv.put(Group.Columns.ID, item.getId());
        cv.put(Group.Columns.NAME, item.getName());
        cv.put(Group.Columns.USER_COUNT, item.getUserCount());
        cv.put(Group.Columns.CREATED_AT, item.getCreatedAt());

        long rowID = db.insertWithOnConflict(DatabaseOpenHelper.TABLE_GROUP, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "inserted group " + item.getId() + " - " + item.getName() +
                " as row " + rowID);
    }

    @Override
    public Group findByID(int id) {
        String[] args = {String.valueOf(id)};
        String selection = Group.Columns.ID + " = ?";
        Cursor cursor = null;
        Group group = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_GROUP, null, selection, args,
                    null, null, Group.Columns.ID, null);
            while (cursor.moveToNext()) {
                group = getGroupFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for group with ID " + id + ": " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return group;
    }

    public Group findByName(String name) {
        String[] args = {name};
        String selection = Group.Columns.NAME + " = ?";
        Cursor cursor = null;
        Group group = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_GROUP, null, selection, args,
                    null, null, Group.Columns.ID, null);
            while (cursor.moveToNext()) {
                group = getGroupFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for group with name " + name + ": " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return group;
    }

    @NotNull
    @Override
    public List<Group> findAll() {
        List<Group> groups = new ArrayList<>();

        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_GROUP, null, null, null,
                    null, null, Group.Columns.ID, null);
            while (cursor.moveToNext()) {
                Group group = getGroupFromCursor(cursor);
                groups.add(group);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for groups: " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return groups;
    }

    private Group getGroupFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(Group.Columns.ID));
        String cname = cursor.getString(cursor.getColumnIndex(Group.Columns.NAME));
        int users = cursor.getInt(cursor.getColumnIndex(Group.Columns.USER_COUNT));
        String createdAt = cursor.getString(cursor.getColumnIndex(Group.Columns.CREATED_AT));

        Group group = new Group()
                .setId(id)
                .setName(cname)
                .setUserCount(users)
                .setCreatedAt(createdAt);

        return group;
    }

}
