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

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn_app.database.DatabaseOpenHelper;
import io.phobotic.nodyn_app.database.model.SyncHistory;

/**
 * Created by Jonathan Nelson on 9/3/17.
 */

public class SyncHistoryHelper extends TableHelper<SyncHistory> {
    public static final String TAG = SyncHistoryHelper.class.getSimpleName();

    public SyncHistoryHelper(@NotNull SQLiteDatabase db) {
        super(db);
    }

    @Override
    public void replace(@NotNull List<SyncHistory> list) {
        if (list == null) {
            throw new IllegalArgumentException("list can not be null");
        }

        Log.d(TAG, "Replacing all SyncHistory items with SyncHistory list of size " + list.size());

        //use '1' as the where clause so we can get a count of the rows deleted
        int count = db.delete(DatabaseOpenHelper.TABLE_SYNC_HISTORY, "1", null);
        Log.d(TAG, "Deleted " + count + " rows from sync history table");

        for (SyncHistory history : list) {
            insert(history);
        }

        Log.d(TAG, "Finished adding sync histories to table");
    }

    @Override
    public long insert(@NotNull SyncHistory item) {
        ContentValues cv = new ContentValues();

        if (item.getId() != -1) {
            cv.put(SyncHistory.Columns.ID, item.getId());
        }

        cv.put(SyncHistory.Columns.TIMESTAMP, item.getTimestamp());

        cv.put(SyncHistory.Columns.RESULT, item.getResult().toString());
        cv.put(SyncHistory.Columns.MESSAGE, item.getMessage());
        cv.put(SyncHistory.Columns.RESPOSE_CODE, item.getResposeCode());
        cv.put(SyncHistory.Columns.RESPONSE_MESSAGE, item.getResponseMessage());
        Exception e = item.getException();
        Gson gson = new Gson();
        String exceptionJson = gson.toJson(e);
        cv.put(SyncHistory.Columns.EXCEPTION, exceptionJson);
        cv.put(SyncHistory.Columns.RESPOSE_CODE, item.getResposeCode());
        cv.put(SyncHistory.Columns.RESPONSE_MESSAGE, item.getResponseMessage());

        long rowID = db.insertWithOnConflict(DatabaseOpenHelper.TABLE_SYNC_HISTORY, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "inserted sync history '" + item.toString() + "' as row " + rowID);
        return rowID;
    }

    @Override
    public SyncHistory findByID(@NotNull int id) {
        return null;
    }

    @Override
    public SyncHistory findByName(@NotNull String name) {
        return null;
    }

    @NotNull
    @Override
    public List<SyncHistory> findAll() {
        List<SyncHistory> SyncHistorys = new ArrayList<>();

        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseOpenHelper.TABLE_SYNC_HISTORY, null, null, null,
                    null, null, SyncHistory.Columns.TIMESTAMP, null);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(SyncHistory.Columns.ID));
                long timestamp = cursor.getLong(cursor.getColumnIndex(
                        SyncHistory.Columns.TIMESTAMP));
                String resultString = cursor.getString(cursor.getColumnIndex(
                        SyncHistory.Columns.RESULT));
                String message = cursor.getString(cursor.getColumnIndex(
                        SyncHistory.Columns.MESSAGE));
                String exceptionJson = cursor.getString(cursor.getColumnIndex(
                        SyncHistory.Columns.EXCEPTION));
                int responseCode = cursor.getInt(cursor.getColumnIndex(
                        SyncHistory.Columns.RESPOSE_CODE));
                String responseMessage = cursor.getString(cursor.getColumnIndex(
                        SyncHistory.Columns.RESPONSE_MESSAGE));

                SyncHistory.RESULT result = SyncHistory.RESULT.valueOf(resultString);
                Gson gson = new Gson();
                Exception e = gson.fromJson(exceptionJson, Exception.class);

                SyncHistory history = new SyncHistory(id, timestamp, result, message, e,
                        responseCode, responseMessage);
                SyncHistorys.add(history);

            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for sync histories: " +
                    e.getMessage());
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return SyncHistorys;
    }
}
