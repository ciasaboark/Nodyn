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
import io.phobotic.nodyn.database.model.Group;


/**
 * Created by Jonathan Nelson on 7/10/17.
 */

public class GroupTableHelper extends TableHelper<Group> {
    private static final String TAG = GroupTableHelper.class.getSimpleName();

    private static final String[] DB_PROJECTION = {
            Group.Columns.ID,
            Group.Columns.NAME,
            Group.Columns.USERS,
            Group.Columns.CREATED_AT
    };

    private static final int DB_PROJECTION_ID = 0;
    private static final int DB_PROJECTION_NAME = 1;
    private static final int DB_PROJECTION_USERS = 2;
    private static final int DB_PROJECTION_CREATED_AT = 3;

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
        cv.put(Group.Columns.USERS, item.getUsers());
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
        Cursor cursor;
        Group group = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_GROUP, DB_PROJECTION, selection, args,
                    null, null, Group.Columns.ID, null);
            while (cursor.moveToNext()) {
                int cid = cursor.getInt(cursor.getColumnIndex(Group.Columns.ID));
                String name = cursor.getString(cursor.getColumnIndex(Group.Columns.NAME));
                int users = cursor.getInt(cursor.getColumnIndex(Group.Columns.USERS));
                String createdAt = cursor.getString(cursor.getColumnIndex(Group.Columns.CREATED_AT));

                group = new Group()
                        .setId(cid)
                        .setName(name)
                        .setUsers(users)
                        .setCreatedAt(createdAt);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for group with ID " + id + ": " +
                    e.getMessage());
            e.printStackTrace();
        }

        return group;
    }

    public Group findByName(String name) {
        String[] args = {name};
        String selection = Group.Columns.NAME + " = ?";
        Cursor cursor;
        Group group = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_GROUP, DB_PROJECTION, selection, args,
                    null, null, Group.Columns.ID, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(DB_PROJECTION_ID);
                String cname = cursor.getString(DB_PROJECTION_NAME);
                int users = cursor.getInt(DB_PROJECTION_USERS);
                String createdAt = cursor.getString(DB_PROJECTION_CREATED_AT);

                group = new Group()
                        .setId(id)
                        .setName(cname)
                        .setUsers(users)
                        .setCreatedAt(createdAt);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for group with name " + name + ": " +
                    e.getMessage());
            e.printStackTrace();
        }

        return group;
    }

    @NotNull
    @Override
    public List<Group> findAll() {
        List<Group> groups = new ArrayList<>();

        Cursor cursor;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_GROUP, DB_PROJECTION, null, null,
                    null, null, Group.Columns.ID, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(DB_PROJECTION_ID);
                String cname = cursor.getString(DB_PROJECTION_NAME);
                int users = cursor.getInt(DB_PROJECTION_USERS);
                String createdAt = cursor.getString(DB_PROJECTION_CREATED_AT);

                Group group = new Group()
                        .setId(id)
                        .setName(cname)
                        .setUsers(users)
                        .setCreatedAt(createdAt);
                groups.add(group);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for groups: " +
                    e.getMessage());
            e.printStackTrace();
        }

        return groups;
    }


}
