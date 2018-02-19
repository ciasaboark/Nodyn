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
import io.phobotic.nodyn_app.database.audit.model.Audit;
import io.phobotic.nodyn_app.database.helper.TableHelper;
import io.phobotic.nodyn_app.database.model.Status;

import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.AuditColumns.AUDIT_DEFINITION_ID;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.AuditColumns.COMPLETED;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.AuditColumns.EXTRACTED;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.AuditColumns.ID;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.AuditColumns.IS_BLIND_AUDIT;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.AuditColumns.MODEL_IDS;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.AuditColumns.STATUS_IDS;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.AuditColumns.TIMESTAMP_BEGIN;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.AuditColumns.TIMESTAMP_END;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.AuditColumns.USER_ID;

/**
 * Created by Jonathan Nelson on 1/14/18.
 */

public class AuditHelper extends TableHelper<Audit> {
    public static final String TAG = AuditHelper.class.getSimpleName();

    public AuditHelper(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public void replace(@NotNull List<Audit> list) {
        clearTable();
        if (list == null) list = new ArrayList<>();
        for (Audit audit : list) {
            insert(audit);
        }
    }

    private void clearTable() {
        int rows = db.delete(AuditDatabaseOpenHelper.TABLE_AUDITS, "1", null);
        Log.d(TAG, "Deleted " + rows + " rows from audit header table");
    }

    @Override
    public long insert(@NotNull Audit audit) {
        ContentValues cv = new ContentValues();
        if (audit.getId() != -1) {
            cv.put(ID, audit.getId());
        }
        cv.put(AUDIT_DEFINITION_ID, audit.getDefinedAuditID());
        cv.put(TIMESTAMP_BEGIN, audit.getBegin());
        cv.put(TIMESTAMP_END, audit.getEnd());
        cv.put(USER_ID, audit.getUserID());
        cv.put(COMPLETED, audit.isCompleted() ? 1 : 0);
        cv.put(EXTRACTED, audit.isExtracted() ? 1 : 0);
        cv.put(IS_BLIND_AUDIT, audit.isBlindAudit() ? 1 : 0);

        String modelIDs = "";
        String prefix = "";
        for (Integer modelID : audit.getModelIDs()) {
            modelIDs += prefix + modelID;
            prefix = ",";
        }
        cv.put(MODEL_IDS, modelIDs);

        String statusIDs = "";
        prefix = "";
        for (Integer id : audit.getStatusIDs()) {
            statusIDs += prefix + id;
            prefix = ",";
        }
        cv.put(STATUS_IDS, statusIDs);

        long rowID = db.insertWithOnConflict(AuditDatabaseOpenHelper.TABLE_AUDITS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "inserted audit header" + audit.getId() + " as row " + rowID);
        return rowID;
    }

    @Override
    public Audit findByID(@NotNull int id) {
        String[] args = {String.valueOf(id)};
        String selection = ID + " = ?";
        Cursor cursor = null;
        Audit audit = null;

        try {
            cursor = db.query(AuditDatabaseOpenHelper.TABLE_AUDITS, null, selection, args,
                    null, null, ID, null);
            while (cursor.moveToNext()) {
                audit = getAuditFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for audit header with ID " + id +
                    ": " + e.getMessage());
            e.printStackTrace();
            Crashlytics.logException(e);
        }


        return audit;
    }

    @Override
    public Audit findByName(@NotNull String name) {
        return null;
    }

    @NotNull
    @Override
    public List<Audit> findAll() {
        List<Audit> audits = new ArrayList<>();

        Cursor cursor = null;

        try {
            cursor = db.query(AuditDatabaseOpenHelper.TABLE_AUDITS, null, null, null,
                    null, null, Status.Columns.ID, null);
            while (cursor.moveToNext()) {
                Audit audit = getAuditFromCursor(cursor);

                audits.add(audit);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for audit headers: " +
                    e.getMessage());
            e.printStackTrace();
            Crashlytics.logException(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return audits;
    }


    private Audit getAuditFromCursor(Cursor cursor) throws Exception {
        Audit audit = null;
        int id = cursor.getInt(cursor.getColumnIndex(ID));
        int definedAuditID = cursor.getInt(cursor.getColumnIndex(AUDIT_DEFINITION_ID));
        long begin = cursor.getLong(cursor.getColumnIndex(TIMESTAMP_BEGIN));
        long end = cursor.getLong(cursor.getColumnIndex(TIMESTAMP_END));
        int userID = cursor.getInt(cursor.getColumnIndex(USER_ID));
        boolean completed = cursor.getInt(cursor.getColumnIndex(COMPLETED)) == 1;
        boolean extracted = cursor.getInt(cursor.getColumnIndex(EXTRACTED)) == 1;
        boolean isBlind = cursor.getInt(cursor.getColumnIndex(IS_BLIND_AUDIT)) == 1;

        String modelsString = cursor.getString(cursor.getColumnIndex(MODEL_IDS));
        List<Integer> modelIDs = new ArrayList<>();
        if (modelsString != null) {
            String[] models = modelsString.split(",");
            for (String mid : models) {
                try {
                    Integer i = Integer.parseInt(mid);
                    modelIDs.add(i);
                } catch (NumberFormatException e) {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                    Log.e(TAG, String.format("Unable to convert string '%s to model ID integer.  " +
                            "This value should not have been stored in audit table", mid));
                }
            }
        }

        String statusString = cursor.getString(cursor.getColumnIndex(STATUS_IDS));
        List<Integer> statusIDs = new ArrayList<>();
        if (statusIDs != null) {
            String[] statuses = statusString.split(",");
            for (String sta : statuses) {
                try {
                    Integer i = Integer.parseInt(sta);
                    statusIDs.add(i);
                } catch (NumberFormatException e) {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                    Log.e(TAG, String.format("Unable to convert string '%s to status ID integer.  " +
                            "This value should not have been stored in audit table", sta));
                }
            }
        }
        audit = new Audit(id, begin, end, userID, modelIDs, statusIDs, completed, extracted,
                definedAuditID, isBlind);

        return audit;
    }

    public List<Audit> findExtracted() {
        List<Audit> extractedHeaders = new ArrayList<>();

        String[] args = {"1"};
        String selection = EXTRACTED + " = ?";
        Cursor cursor = null;
        Audit audit = null;

        try {
            cursor = db.query(AuditDatabaseOpenHelper.TABLE_AUDITS, null, selection, args,
                    null, null, ID, null);
            while (cursor.moveToNext()) {
                audit = getAuditFromCursor(cursor);
                extractedHeaders.add(audit);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for extracted audit headers: " +
                    e.getMessage());
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return extractedHeaders;
    }

    public void remove(int auditID) {
        String[] args = {String.valueOf(auditID)};
        String selection = ID + " = ?";
        Cursor cursor = null;

        try {
            int rows = db.delete(AuditDatabaseOpenHelper.TABLE_AUDITS, selection, args);
            Log.d(TAG, "Deleted " + rows + " rows from audit headers table for audit ID " + auditID);
        } catch (Exception e) {
            Log.e(TAG, "Caught exception deleting rows from audit details table: " + e.getMessage());
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }
}
