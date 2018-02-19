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

package io.phobotic.nodyn_app.database.audit.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper;
import io.phobotic.nodyn_app.database.audit.model.AuditDetailRecord;
import io.phobotic.nodyn_app.database.helper.TableHelper;

/**
 * Created by Jonathan Nelson on 1/14/18.
 */

public class AuditDetailHelper extends TableHelper<AuditDetailRecord> {
    public static final String TAG = AuditDetailHelper.class.getSimpleName();

    public AuditDetailHelper(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public void replace(@NotNull List<AuditDetailRecord> list) {
        clearTable();
        if (list == null) list = new ArrayList<>();
        for (AuditDetailRecord detailRecord : list) {
            insert(detailRecord);
        }
    }

    private void clearTable() {
        int rows = db.delete(AuditDatabaseOpenHelper.TABLE_AUDIT_DETAILS, "1", null);
        Log.d(TAG, "Deleted " + rows + " rows from audit details table");
    }

    @Override
    public long insert(@NotNull AuditDetailRecord detailRecord) {
        ContentValues cv = new ContentValues();
        if (detailRecord.getId() != null) {
            cv.put(AuditDatabaseOpenHelper.DetailColumns.ID, detailRecord.getId());
        }

        cv.put(AuditDatabaseOpenHelper.DetailColumns.AUDIT_ID, detailRecord.getAuditID());
        cv.put(AuditDatabaseOpenHelper.DetailColumns.ASSET_ID, detailRecord.getAssetID());
        cv.put(AuditDatabaseOpenHelper.DetailColumns.TIMESTAMP, detailRecord.getTimestamp());
        cv.put(AuditDatabaseOpenHelper.DetailColumns.STATUS, detailRecord.getStatus().toString());
        cv.put(AuditDatabaseOpenHelper.DetailColumns.NOTES, detailRecord.getNotes());
        cv.put(AuditDatabaseOpenHelper.DetailColumns.EXTRACTED, detailRecord.isExtracted() ? 1 : 0);

        long rowID = db.insertWithOnConflict(AuditDatabaseOpenHelper.TABLE_AUDIT_DETAILS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "inserted audit detail record" + detailRecord.getId() + " as row " + rowID);

        return rowID;
    }

    @Override
    public AuditDetailRecord findByID(@NotNull int id) {
        String[] args = {String.valueOf(id)};
        String selection = AuditDatabaseOpenHelper.DetailColumns.ID + " = ?";
        Cursor cursor = null;
        AuditDetailRecord detailRecord = null;

        try {
            cursor = db.query(AuditDatabaseOpenHelper.TABLE_AUDIT_DETAILS, null, selection, args,
                    null, null, AuditDatabaseOpenHelper.AuditColumns.ID, null);
            while (cursor.moveToNext()) {
                detailRecord = getAuditDetailRecordFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for audit detail record with detail ID " + id +
                    ": " + e.getMessage());
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return detailRecord;
    }

    @Override
    public AuditDetailRecord findByName(@NotNull String name) {
        return null;
    }

    @NotNull
    @Override
    public List<AuditDetailRecord> findAll() {
        List<AuditDetailRecord> detailRecords = new ArrayList<>();

        Cursor cursor = null;

        try {
            cursor = db.query(AuditDatabaseOpenHelper.TABLE_AUDIT_DETAILS, null, null, null,
                    null, null, AuditDatabaseOpenHelper.DetailColumns.ID, null);
            while (cursor.moveToNext()) {
                AuditDetailRecord detailRecord = getAuditDetailRecordFromCursor(cursor);

                detailRecords.add(detailRecord);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for audit detail records: " +
                    e.getMessage());
            e.printStackTrace();
            Crashlytics.logException(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return detailRecords;
    }

    private AuditDetailRecord getAuditDetailRecordFromCursor(Cursor cursor) throws Exception {
        AuditDetailRecord detailRecord = null;
        int id = cursor.getInt(cursor.getColumnIndex(AuditDatabaseOpenHelper.DetailColumns.ID));
        int auditID = cursor.getInt(cursor.getColumnIndex(AuditDatabaseOpenHelper.DetailColumns.AUDIT_ID));
        int assetID = cursor.getInt(cursor.getColumnIndex(AuditDatabaseOpenHelper.DetailColumns.ASSET_ID));
        long timestamp = cursor.getLong(cursor.getColumnIndex(AuditDatabaseOpenHelper.DetailColumns.TIMESTAMP));
        String statusString = cursor.getString(cursor.getColumnIndex(AuditDatabaseOpenHelper.DetailColumns.STATUS));
        AuditDetailRecord.Status status = AuditDetailRecord.Status.valueOf(statusString);
        String notes = cursor.getString(cursor.getColumnIndex(AuditDatabaseOpenHelper.DetailColumns.NOTES));
        boolean extracted = cursor.getInt(cursor.getColumnIndex(AuditDatabaseOpenHelper.DetailColumns.EXTRACTED)) == 1;

        detailRecord = new AuditDetailRecord(id, auditID, assetID, timestamp, status, notes, extracted);

        return detailRecord;
    }

    public void removeAuditDetails(int auditID) {
        String[] args = {String.valueOf(auditID)};
        String selection = AuditDatabaseOpenHelper.DetailColumns.AUDIT_ID + " = ?";
        Cursor cursor = null;

        try {
            int rows = db.delete(AuditDatabaseOpenHelper.TABLE_AUDIT_DETAILS, selection, args);
            Log.d(TAG, "Deleted " + rows + " rows from audit details table for audit ID " + auditID);
        } catch (Exception e) {
            Log.e(TAG, "Caught exception deleting rows from audit details table: " + e.getMessage());
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    public List<AuditDetailRecord> findByAuditID(@NotNull int id) {
        String[] args = {String.valueOf(id)};
        String selection = AuditDatabaseOpenHelper.DetailColumns.AUDIT_ID + " = ?";
        Cursor cursor = null;
        List<AuditDetailRecord> detailRecords = new ArrayList<>();

        try {
            cursor = db.query(AuditDatabaseOpenHelper.TABLE_AUDIT_DETAILS, null, selection, args,
                    null, null, AuditDatabaseOpenHelper.AuditColumns.ID, null);
            while (cursor.moveToNext()) {
                AuditDetailRecord detailRecord = getAuditDetailRecordFromCursor(cursor);
                detailRecords.add(detailRecord);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for audit detail records with audit ID " + id +
                    ": " + e.getMessage());
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return detailRecords;
    }
}
