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
import io.phobotic.nodyn_app.database.audit.model.AuditDefinition;
import io.phobotic.nodyn_app.database.helper.TableHelper;

import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.DefinedAuditColumns.CREATE_TIMESTAMP;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.DefinedAuditColumns.DETAILS;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.DefinedAuditColumns.ID;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.DefinedAuditColumns.IS_BLIND_AUDIT;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.DefinedAuditColumns.LAST_COMPLETE_AUDIT;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.DefinedAuditColumns.META_STATUS;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.DefinedAuditColumns.NAME;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.DefinedAuditColumns.REQUIRED_MODELS;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.DefinedAuditColumns.REQUIRED_STATUSES;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.DefinedAuditColumns.REQUIRE_ALL_MODELS;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.DefinedAuditColumns.REQUIRE_ALL_STATUSES;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.DefinedAuditColumns.SCHEDULE;
import static io.phobotic.nodyn_app.database.audit.AuditDatabaseOpenHelper.DefinedAuditColumns.UPDATE_TIMESTAMP;

/**
 * Created by Jonathan Nelson on 1/23/18.
 */

public class DefinedAuditHelper extends TableHelper<AuditDefinition> {
    public static final String TAG = DefinedAuditHelper.class.getSimpleName();

    public DefinedAuditHelper(SQLiteDatabase db) {
        super(db);
    }

    @Override
    public void replace(@NotNull List<AuditDefinition> list) {
        clearTable();
        if (list == null) list = new ArrayList<>();
        for (AuditDefinition auditDefinition : list) {
            insert(auditDefinition);
        }
    }

    private void clearTable() {
        int rows = db.delete(AuditDatabaseOpenHelper.TABLE_AUDIT_DETAILS, "1", null);
        Log.d(TAG, "Deleted " + rows + " rows from defined audits table");
    }

    @Override
    public long insert(@NotNull AuditDefinition auditDefinition) {
        ContentValues cv = new ContentValues();
        if (auditDefinition.getId() != -1) {
            cv.put(ID, auditDefinition.getId());
        }
        cv.put(NAME, auditDefinition.getName());
        cv.put(CREATE_TIMESTAMP, auditDefinition.getCreateTimestamp());
        cv.put(UPDATE_TIMESTAMP, auditDefinition.getUpdateTimestamp());
        String requiredModels = "";
        String prefix = "";
        for (Integer modelID : auditDefinition.getRequiredModelIDs()) {
            requiredModels += prefix + modelID;
            prefix = ",";
        }
        cv.put(REQUIRED_MODELS, requiredModels);
        cv.put(REQUIRE_ALL_MODELS, auditDefinition.isAuditAllModels() ? "1" : "0");

        String requiredStatuses = "";
        prefix = "";
        for (Integer statusID : auditDefinition.getRequiredStatusIDs()) {
            requiredStatuses += prefix + statusID;
            prefix = ",";
        }
        cv.put(REQUIRED_STATUSES, requiredStatuses);
        cv.put(REQUIRE_ALL_STATUSES, auditDefinition.isAuditAllStatuses() ? "1" : "0");

        cv.put(DETAILS, auditDefinition.getDetails());
        cv.put(LAST_COMPLETE_AUDIT, auditDefinition.getLastAuditTimestamp());
        cv.put(IS_BLIND_AUDIT, auditDefinition.isBlindAudit() ? "1" : "0");
        cv.put(SCHEDULE, auditDefinition.getSchedule());
        cv.put(META_STATUS, auditDefinition.getMetaStatus());


        long rowID = db.insertWithOnConflict(AuditDatabaseOpenHelper.TABLE_DEFINED_AUDITS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "inserted defined audit" + auditDefinition.getId() + " as row " + rowID);
        return rowID;
    }

    @Override
    public AuditDefinition findByID(@NotNull int id) {
        String[] args = {String.valueOf(id)};
        String selection = ID + " = ?";
        Cursor cursor = null;
        AuditDefinition auditDefinition = null;

        try {
            cursor = db.query(AuditDatabaseOpenHelper.TABLE_DEFINED_AUDITS, null, selection, args,
                    null, null, ID, null);
            while (cursor.moveToNext()) {
                auditDefinition = getDefinedAuditFromCursor(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for defined audit with ID " + id +
                    ": " + e.getMessage());
            e.printStackTrace();
            Crashlytics.logException(e);
        }

        return auditDefinition;
    }

    @Override
    public AuditDefinition findByName(@NotNull String name) {
        return null;
    }

    @NotNull
    @Override
    public List<AuditDefinition> findAll() {
        List<AuditDefinition> auditDefinitions = new ArrayList<>();

        Cursor cursor = null;

        try {
            cursor = db.query(AuditDatabaseOpenHelper.TABLE_DEFINED_AUDITS, null, null, null,
                    null, null, CREATE_TIMESTAMP, null);
            while (cursor.moveToNext()) {
                AuditDefinition auditDefinition = getDefinedAuditFromCursor(cursor);

                auditDefinitions.add(auditDefinition);
            }
        } catch (Exception e) {
            Log.e(TAG, "Caught exception while searching for defined audits: " +
                    e.getMessage());
            e.printStackTrace();
            Crashlytics.logException(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return auditDefinitions;
    }


    private AuditDefinition getDefinedAuditFromCursor(Cursor cursor) throws Exception {
        AuditDefinition auditDefinition = null;
        int id = cursor.getInt(cursor.getColumnIndex(ID));
        String name = cursor.getString(cursor.getColumnIndex(NAME));
        long createTimestamp = cursor.getLong(cursor.getColumnIndex(CREATE_TIMESTAMP));
        long updateTimestamp = cursor.getLong(cursor.getColumnIndex(UPDATE_TIMESTAMP));
        String details = cursor.getString(cursor.getColumnIndex(DETAILS));
        long lastAuditTimestamp = cursor.getLong(cursor.getColumnIndex(LAST_COMPLETE_AUDIT));
        boolean auditAllModels = cursor.getInt(cursor.getColumnIndex(REQUIRE_ALL_MODELS)) == 1;
        boolean auditAllStatuses = cursor.getInt(cursor.getColumnIndex(REQUIRE_ALL_STATUSES)) == 1;
        boolean isBlindAudit = cursor.getInt(cursor.getColumnIndex(IS_BLIND_AUDIT)) == 1;
        String metaStatus = cursor.getString(cursor.getColumnIndex(META_STATUS));
        String schedule = cursor.getString(cursor.getColumnIndex(SCHEDULE));


        String requiredStatusesString = cursor.getString(cursor.getColumnIndex(REQUIRED_STATUSES));
        List<Integer> requiredStatusIDs = new ArrayList<>();
        if (requiredStatusesString != null && requiredStatusesString.length() > 0) {
            String[] statuses = requiredStatusesString.split(",");
            for (String sta : statuses) {
                try {
                    Integer i = Integer.parseInt(sta);
                    requiredStatusIDs.add(i);
                } catch (NumberFormatException e) {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                    Log.e(TAG, String.format("Unable to convert string '%s to status ID integer.  " +
                            "This value should not have been stored in defined audits table", sta));
                }
            }
        }

        String requiredModelsString = cursor.getString(cursor.getColumnIndex(REQUIRED_MODELS));
        List<Integer> requiredModelIDs = new ArrayList<>();
        if (requiredModelsString != null && requiredModelsString.length() > 0) {
            String[] models = requiredModelsString.split(",");
            for (String mid : models) {
                try {
                    Integer i = Integer.parseInt(mid);
                    requiredModelIDs.add(i);
                } catch (NumberFormatException e) {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                    Log.e(TAG, String.format("Unable to convert string '%s to model ID integer.  " +
                            "This value should not have been stored in defined audits table", mid));
                }
            }
        }

        auditDefinition = new AuditDefinition()
                .setId(id)
                .setName(name)
                .setCreateTimestamp(createTimestamp)
                .setUpdateTimestamp(updateTimestamp)
                .setLastAuditTimestamp(lastAuditTimestamp)
                .setRequiredModelIDs(requiredModelIDs)
                .setAuditAllModels(auditAllModels)
                .setRequiredStatusIDs(requiredStatusIDs)
                .setAuditAllStatuses(auditAllStatuses)
                .setMetaStatus(metaStatus)
                .setBlindAudit(isBlindAudit)
                .setSchedule(schedule);

        return auditDefinition;
    }

    public void remove(int auditID) {
        String[] args = {String.valueOf(auditID)};
        String selection = ID + " = ?";
        Cursor cursor = null;

        try {
            int rows = db.delete(AuditDatabaseOpenHelper.TABLE_DEFINED_AUDITS, selection, args);
            Log.d(TAG, "Deleted " + rows + " rows from defined audits table for ID " + auditID);
        } catch (Exception e) {
            Log.e(TAG, "Caught exception deleting rows from defined audits table: " + e.getMessage());
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }
}

