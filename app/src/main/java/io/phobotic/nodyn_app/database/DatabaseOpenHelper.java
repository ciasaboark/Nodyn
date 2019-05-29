/*
 * Copyright (c) 2019 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.phobotic.nodyn_app.database.model.Action;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Category;
import io.phobotic.nodyn_app.database.model.Group;
import io.phobotic.nodyn_app.database.model.Manufacturer;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.database.model.SyncHistory;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 7/8/17.
 */

public class DatabaseOpenHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "nodyn";
    public static final String TABLE_ASSETS = "assets";
    public static final String TABLE_USER = "users";
    public static final String TABLE_MODEL = "models";
    public static final String TABLE_MANUFACTURER = "manufacturer";
    public static final String TABLE_GROUP = "groups";
    public static final String TABLE_CATEGORY = "category";
    public static final String TABLE_STATUS = "status";
    public static final String TABLE_ACTIONS = "actions";
    public static final String TABLE_SYNC_HISTORY = "sync_history";

    private static final int VERSION = 16;

    public DatabaseOpenHelper(Context ctx) {
        super(ctx, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODEL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MANUFACTURER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATUS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SYNC_HISTORY);
        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        createAssetsTable(db);
        createUsersTable(db);
        createModelsTable(db);
        createManufacturersTable(db);
        createGroupsTable(db);
        createCategoriesTable(db);
        createStatusTable(db);
        createActionsTable(db);
        createSyncHistoryTable(db);
    }

    private void createAssetsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ASSETS + " ( " +
                Asset.Columns.ID + " integer primary key not null, " +
                Asset.Columns.IMAGE + " varchar(100), " +
                Asset.Columns.NAME + " varchar(100), " +
                Asset.Columns.TAG + " varchar(100), " +
                Asset.Columns.SERIAL + " varchar(100), " +
                Asset.Columns.MODEL_ID + " integer, " +
                Asset.Columns.STATUS_ID + " integer, " +
                Asset.Columns.ASSIGNED_TO_ID + " integer, " +
                Asset.Columns.LOCATION_ID + " integer, " +
                Asset.Columns.CATEGORY_ID + " integer, " +
                Asset.Columns.MANUFACTURER_ID + " integer, " +
                Asset.Columns.EOL + " varchar(100)," +
                Asset.Columns.PURCHASE_COST + " varchar(100), " +
                Asset.Columns.PURCHASE_DATE + " varchar(100), " +
                Asset.Columns.NOTES + " varchar(100), " +
                Asset.Columns.ORDER_NUMBER + " varchar(100), " +
                Asset.Columns.LAST_CHECKOUT + " integer, " +
                Asset.Columns.EXPECTED_CHECKIN + " integer, " +
                Asset.Columns.CREATED_AT + " integer, " +
                Asset.Columns.COMPANY_ID + " varchar(100) " +
                ")");
    }

    private void createUsersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USER + " ( " +
                User.Columns.ID + " integer primary key not null, " +
                User.Columns.NAME + " varchar(100), " +
                User.Columns.JOB_TITLE + " varchar(100), " +
                User.Columns.EMAIL + " varchar(100), " +
                User.Columns.USERNAME + " varchar(100), " +
                User.Columns.LOCATION_ID + " integer, " +
                User.Columns.MANAGER_ID + " integer, " +
                User.Columns.NUM_ASSETS + " integer default 0, " +
                User.Columns.EMPLOYEE_NUM + " varchar(100), " +
                User.Columns.GROUP_IDS + " varchar(200), " +
                User.Columns.NOTES + " varchar(100), " +
                User.Columns.COMPANY_ID + " integer, " +
                User.Columns.AVATAR_URL + " varchar(100)" +
                ")");
    }

    private void createModelsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_MODEL + " ( " +
                Model.Columns.ID + " integer primary key not null, " +
                Model.Columns.MANUFACTURER_ID + " integer, " +
                Model.Columns.NAME + " varchar(100), " +
                Model.Columns.IMAGE + " varchar(100), " +
                Model.Columns.MODEL_NUMBER + " varchar(100), " +
                Model.Columns.NUM_ASSETS + " integer default 0, " +
                Model.Columns.DEPRECIATION + " varchar(100), " +
                Model.Columns.CATEGORY_ID + " integer, " +
                Model.Columns.EOL + " varchar(100), " +
                Model.Columns.NOTES + " varchar(100), " +
                Model.Columns.FIELDSET_ID + " integer, " +
                Model.Columns.CREATED_AT + " varchar(100)" +
                ")");
    }

    private void createManufacturersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_MANUFACTURER + " ( " +
                Manufacturer.Columns.ID + " integer primary key not null, " +
                Manufacturer.Columns.NAME + " varchar(100), " +
                Manufacturer.Columns.CREATED_AT + " varchar(100), " +
                Manufacturer.Columns.SUPPORT_EMAIL + " varchar(100), " +
                Manufacturer.Columns.SUPPORT_PHONE + " varchar(100), " +
                Manufacturer.Columns.SUPPORT_URL + " varchar(100), " +
                Manufacturer.Columns.URL + " varchar(100)" +
                ")");
    }

    private void createGroupsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_GROUP + " ( " +
                Group.Columns.ID + " integer primary key not null, " +
                Group.Columns.NAME + " varchar(100), " +
                Group.Columns.USER_COUNT + " varchar(100), " +
                Group.Columns.CREATED_AT + " varchar(100) " +
                ")");
    }

    private void createCategoriesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CATEGORY + " ( " +
                Category.Columns.ID + " integer primary key not null, " +
                Category.Columns.NAME + " varchar(100), " +
                Category.Columns.CATEGORY_TYPE + " varchar(100), " +
                Category.Columns.COUNT + " integer default 0, " +
                Category.Columns.ACCEPTANCE + " integer, " +
                Category.Columns.USE_DEFAULT_EULA + " integer, " +
                Category.Columns.EULA_TEXT + " varchar(1000) " +
                ")");
    }

    private void createStatusTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_STATUS + " ( " +
                Status.Columns.ID + " integer primary key not null, " +
                Status.Columns.NAME + " varchar(100), " +
                Status.Columns.TYPE + " varchar(100), " +
                Status.Columns.COLOR + " varchar(100) " +
                ")");
    }

    private void createActionsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ACTIONS + " ( " +
                Action.Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                Action.Columns.DIRECTION + " varchar(100), " +
                Action.Columns.AUTHORIZATION + " varchar(100), " +
                Action.Columns.ASSET_ID + " varchar(100), " +
                Action.Columns.USER_ID + " varchar(100), " +
                Action.Columns.TIMESTAMP + " integer not null, " +
                Action.Columns.EXPECTED_CHECKIN + " integer default -1, " +
                Action.Columns.SYNCED + " integer default 0, " +
                Action.Columns.VERIFIED + " integer default 0 " +
                ")");
    }

    private void createSyncHistoryTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_SYNC_HISTORY + " ( " +
                SyncHistory.Columns.ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                SyncHistory.Columns.TIMESTAMP + " INTEGER DEFAULT -1, " +
                SyncHistory.Columns.RESULT + "  VARCHAR(100), " +
                SyncHistory.Columns.MESSAGE + " VARCHAR(100), " +
                SyncHistory.Columns.RESPOSE_CODE + " INTEGER DEFAULT -1, " +
                SyncHistory.Columns.RESPONSE_MESSAGE + " varchar(500), " +
                SyncHistory.Columns.EXCEPTION + " VARCHAR(1000)" +
                ")");
    }
}
