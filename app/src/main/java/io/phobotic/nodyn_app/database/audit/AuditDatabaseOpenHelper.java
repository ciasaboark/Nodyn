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

package io.phobotic.nodyn_app.database.audit;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jonathan Nelson on 1/14/18.
 */

public class AuditDatabaseOpenHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "audits";
    public static final String TABLE_AUDITS = "audits";
    public static final String TABLE_AUDIT_DETAILS = "audit_details";
    public static final String TABLE_DEFINED_AUDITS = "defined_audits";

    public static final int VERSION = 7;

    public AuditDatabaseOpenHelper(Context ctx) {
        super(ctx, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUDITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AUDIT_DETAILS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEFINED_AUDITS);
        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        createAuditsTable(db);
        createAuditDetailsTable(db);
        createAuditDefinitionsTable(db);
    }

    private void createAuditsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_AUDITS + " ( " +
                AuditColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                AuditColumns.AUDIT_DEFINITION_ID + " INTEGER default -1, " +
                AuditColumns.TIMESTAMP_BEGIN + " integer not null, " +
                AuditColumns.TIMESTAMP_END + " integer not null, " +
                AuditColumns.USER_ID + " integer, " +
                AuditColumns.MODEL_IDS + " varchar(300), " +
                AuditColumns.STATUS_IDS + " varchar(300), " +
                AuditColumns.COMPLETED + " integer not null, " +
                AuditColumns.EXTRACTED + " integer not null, " +
                AuditColumns.IS_BLIND_AUDIT + " integer not null " +
                ")");
    }

    private void createAuditDetailsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_AUDIT_DETAILS + " ( " +
                DetailColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                DetailColumns.AUDIT_ID + " INTEGER NOT NULL, " +
                DetailColumns.ASSET_ID + " integer not null, " +
                DetailColumns.STATUS + " varchar(100), " +
                DetailColumns.NOTES + " varchar(1000), " +
                DetailColumns.TIMESTAMP + " integer not null, " +
                DetailColumns.EXTRACTED + " integer not null " +
                ")");
    }

    private void createAuditDefinitionsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_DEFINED_AUDITS + " ( " +
                DefinedAuditColumns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                DefinedAuditColumns.NAME + " varchar(100), " +
                DefinedAuditColumns.CREATE_TIMESTAMP + " integer not null, " +
                DefinedAuditColumns.UPDATE_TIMESTAMP + " integer not null, " +
                DefinedAuditColumns.REQUIRED_MODELS + " varchar(1000), " +
                DefinedAuditColumns.REQUIRE_ALL_MODELS + " integer not null, " +
                DefinedAuditColumns.REQUIRED_STATUSES + " varchar(100), " +
                DefinedAuditColumns.REQUIRE_ALL_STATUSES + " integer not null, " +
                DefinedAuditColumns.META_STATUS + " varchar(100), " +
                DefinedAuditColumns.LAST_COMPLETE_AUDIT + " integer default -1, " +
                DefinedAuditColumns.IS_BLIND_AUDIT + " integer not null, " +
                DefinedAuditColumns.SCHEDULE + " varchar(100), " +
                DefinedAuditColumns.DETAILS + " varchar(100) " +
                ")");
    }

    public class AuditColumns {
        public static final String ID = "id";
        public static final String AUDIT_DEFINITION_ID = "audit_definition_id";
        public static final String TIMESTAMP_BEGIN = "begin";
        public static final String TIMESTAMP_END = "end";
        public static final String USER_ID = "user_id";
        public static final String MODEL_IDS = "model_ids";
        public static final String STATUS_IDS = "status_ids";
        public static final String COMPLETED = "completed";
        public static final String EXTRACTED = "extracted";
        public static final String IS_BLIND_AUDIT = "blind";
    }

    public class DetailColumns {
        public static final String ID = "id";
        public static final String AUDIT_ID = "audit_id";
        public static final String ASSET_ID = "asset_id";
        public static final String STATUS = "status";
        public static final String NOTES = "notes";
        public static final String TIMESTAMP = "timestamp";
        public static final String EXTRACTED = "extracted";
    }

    public class DefinedAuditColumns {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String CREATE_TIMESTAMP = "create_timestamp";
        public static final String UPDATE_TIMESTAMP = "update_timestamp";
        public static final String REQUIRED_MODELS = "required_models";
        public static final String REQUIRE_ALL_MODELS = "require_all_models";
        public static final String REQUIRED_STATUSES = "required_statuses";
        public static final String META_STATUS = "meta_status";
        public static final String REQUIRE_ALL_STATUSES = "require_all_statuses";
        public static final String DETAILS = "details";
        public static final String LAST_COMPLETE_AUDIT = "last_complete_audit";
        public static final String SCHEDULE = "schedule";
        public static final String IS_BLIND_AUDIT = "is_blind_audit";
    }
}
