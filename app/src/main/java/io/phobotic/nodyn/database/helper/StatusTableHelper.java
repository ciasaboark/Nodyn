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
import io.phobotic.nodyn.database.model.Status;


/**
 * Created by Jonathan Nelson on 9/10/17.
 */

public class StatusTableHelper extends TableHelper<Status> {
    private static final String TAG = StatusTableHelper.class.getSimpleName();

    public StatusTableHelper(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public void replace(List<Status> list) {
        clearTable();
        for (Status status : list) {
            insert(status);
        }
    }

    private void clearTable() {
        int rows = db.delete(DatabaseOpenHelper.TABLE_STATUS, "1", null);
        Log.d(TAG, "Deleted " + rows + " rows from status table");
    }

    @Override
    public void insert(Status item) {
        ContentValues cv = new ContentValues();
        cv.put(Status.Columns.ID, item.getId());
        cv.put(Status.Columns.NAME, item.getName());
        cv.put(Status.Columns.TYPE, item.getType());
        cv.put(Status.Columns.COLOR, item.getColor());

        long rowID = db.insertWithOnConflict(DatabaseOpenHelper.TABLE_STATUS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "inserted status " + item.getId() + " - " + item.getName() +
                " as row " + rowID);
    }

    @Override
    public Status findByID(int id) {
        String[] args = {String.valueOf(id)};
        String selection = Status.Columns.ID + " = ?";
        Cursor cursor = null;
        Status status = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_STATUS, null, selection, args,
                    null, null, Status.Columns.ID, null);
            while (cursor.moveToNext()) {
                status = getStatusFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for status with ID " + id + ": " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return status;
    }

    @Override
    public Status findByName(String name) {
        String[] args = {name};
        String selection = Status.Columns.ID + " = ?";
        Cursor cursor = null;
        Status status = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_STATUS, null, selection, args,
                    null, null, Status.Columns.ID, null);
            while (cursor.moveToNext()) {
                status = getStatusFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for status name " + name + ": " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return status;
    }

    @NotNull
    @Override
    public List<Status> findAll() {
        List<Status> statuses = new ArrayList<>();

        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_STATUS, null, null, null,
                    null, null, Status.Columns.ID, null);
            while (cursor.moveToNext()) {
                Status status = getStatusFromCursor(cursor);

                statuses.add(status);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for statuses: " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return statuses;
    }

    private Status getStatusFromCursor(Cursor cursor) throws Exception {
        Status status = null;
        int id = cursor.getInt(cursor.getColumnIndex(Status.Columns.ID));
        String name = cursor.getString(cursor.getColumnIndex(Status.Columns.NAME));
        String type = cursor.getString(cursor.getColumnIndex(Status.Columns.TYPE));
        String color = cursor.getString(cursor.getColumnIndex(Status.Columns.COLOR));

        status = new Status()
                .setId(id)
                .setName(name)
                .setType(type)
                .setColor(color);

        return status;
    }
}
