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
import io.phobotic.nodyn.database.model.Action;
import io.phobotic.nodyn.database.model.Asset;

/**
 * Created by Jonathan Nelson on 8/20/17.
 */

public class ActionTableHelper extends TableHelper<Action> {
    public static final String[] DB_PROJECTION = new String[]{
            Action.Columns.ID,
            Action.Columns.DIRECTION,
            Action.Columns.ASSET_ID,
            Action.Columns.USER_ID,
            Action.Columns.TIMESTAMP,
            Action.Columns.EXPECTED_CHECKIN,
            Action.Columns.SYNCED,
            Action.Columns.AUTHORIZATION,
            Action.Columns.VERIFIED
    };
    private static final String TAG = ActionTableHelper.class.getSimpleName();

    public ActionTableHelper(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public void replace(@NotNull List<Action> list) {
        Log.d(TAG, "Replacing all action items with action list of size " + list.size());

        //use '1' as the where clause so we can get a count of the rows deleted
        int count = db.delete(DatabaseOpenHelper.TABLE_ACTIONS, "1", null);
        Log.d(TAG, "Deleted " + count + " rows from assets table");

        for (Action action : list) {
            insert(action);
        }

        Log.d(TAG, "Finished adding actions to table");
    }

    @Override
    public void insert(Action action) {
        ContentValues cv = new ContentValues();

        if (action.getId() != null) {
            cv.put(Action.Columns.ID, action.getId());
        }
        cv.put(Action.Columns.DIRECTION, action.getDirection().toString());
        cv.put(Action.Columns.ASSET_ID, action.getAssetID());
        cv.put(Action.Columns.USER_ID, action.getUserID());
        cv.put(Action.Columns.TIMESTAMP, System.currentTimeMillis());
        cv.put(Action.Columns.EXPECTED_CHECKIN, action.getExpectedCheckin());
        cv.put(Action.Columns.SYNCED, action.isSynced() ? 1 : 0);
        cv.put(Action.Columns.AUTHORIZATION, action.getAuthorization());
        cv.put(Action.Columns.VERIFIED, action.isVerified() ? 1 : 0);

        long rowID = db.insertWithOnConflict(DatabaseOpenHelper.TABLE_ACTIONS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "inserted action '" + action.toString() + "' as row " + rowID);
    }

    @Override
    public Action findByID(int id) {
        return null;
    }

    @Override
    public Action findByName(String name) {
        return null;
    }

    @NotNull
    @Override
    public List<Action> findAll() {
        List<Action> actions = new ArrayList<>();

        Cursor cursor;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_ACTIONS, DB_PROJECTION, null, null,
                    null, null, Asset.Columns.ID, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(Action.Columns.ID));
                String direction = cursor.getString(cursor.getColumnIndex(Action.Columns.DIRECTION));
                int assetID = cursor.getInt(cursor.getColumnIndex(Action.Columns.ASSET_ID));
                int userID = cursor.getInt(cursor.getColumnIndex(Action.Columns.USER_ID));
                long timestamp = cursor.getLong(cursor.getColumnIndex(Action.Columns.TIMESTAMP));
                long expectedCheckin = cursor.getLong(cursor.getColumnIndex(
                        Action.Columns.EXPECTED_CHECKIN));
                boolean isSynced = cursor.getInt(cursor.getColumnIndex(Action.Columns.SYNCED)) == 1;
                String authorization = cursor.getString(cursor.getColumnIndex(
                        Action.Columns.AUTHORIZATION));
                boolean isVerified = cursor.getInt(cursor.getColumnIndex(Action.Columns.VERIFIED)) == 1;

                Action a = new Action(id, assetID, userID, timestamp, expectedCheckin,
                        Action.Direction.valueOf(direction), isSynced);
                a.setVerified(isVerified);
                a.setAuthorization(authorization);

                actions.add(a);

            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for actions: " +
                    e.getMessage());
            e.printStackTrace();
        }

        return actions;
    }
}
