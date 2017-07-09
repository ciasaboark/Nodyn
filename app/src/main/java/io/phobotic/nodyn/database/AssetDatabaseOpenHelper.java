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

package io.phobotic.nodyn.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.Category;
import io.phobotic.nodyn.database.model.Group;
import io.phobotic.nodyn.database.model.Model;
import io.phobotic.nodyn.database.model.User;

/**
 * Created by Jonathan Nelson on 7/8/17.
 */

public class AssetDatabaseOpenHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "nodyn";
    public static final String TABLE_ASSETS = "assets";
    public static final String TABLE_USER = "users";
    public static final String TABLE_MODEL = "models";
    public static final String TABLE_GROUP = "groups";
    public static final String TABLE_CATEGORY = "category";

    private static final int VERSION = 1;

    public AssetDatabaseOpenHelper(Context ctx) {
        super(ctx, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GROUP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODEL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        createAssetsTable(db);
        createUsersTable(db);
        createModelsTable(db);
        createGroupsTable(db);
        createCategoriesTable(db);
    }

    private void createAssetsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ASSETS + " ( " +
                Asset.Columns.ID + " integer primary key not null, " +
                Asset.Columns.IMAGE + " varchar(100), " +
                Asset.Columns.NAME + " varchar(100), " +
                Asset.Columns.TAG + " varchar(100), " +
                Asset.Columns.SERIAL + " varchar(100), " +
                Asset.Columns.MODEL + " varchar(100), " +
                Asset.Columns.STATUS + " varchar(100), " +
                Asset.Columns.ASSIGNED_TO + " varchar(100), " +
                Asset.Columns.LOCATION + " varchar(100), " +
                Asset.Columns.CATEGORY + " varchar(100), " +
                Asset.Columns.MANUFACTURER + " varchar(100), " +
                Asset.Columns.EOL + " varchar(100)" +
                Asset.Columns.PURCHASE_COST + " varchar(100), " +
                Asset.Columns.PURCHASE_DATE + " varchar(100), " +
                Asset.Columns.NOTES + " varchar(100), " +
                Asset.Columns.ORDER_NUMBER + " varchar(100), " +
                Asset.Columns.LAST_CHECKOUT + " varchar(100), " +
                Asset.Columns.EXPECTED_CHECKIN + " varchar(100), " +
                Asset.Columns.CREATED_AT + " varchar(100), " +
                Asset.Columns.COMPANY_NAME + " varchar(100), " +
                ")");
    }

    private void createUsersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USER + " ( " +
                User.Columns.ID + " integer primary key not null, " +
                User.Columns.NAME + " varchar(100), " +
                User.Columns.JOB_TITLE + " varchar(100), " +
                User.Columns.EMAIL + " varchar(100), " +
                User.Columns.USERNAME + " varchar(100), " +
                User.Columns.LOCATION + " varchar(100), " +
                User.Columns.MANAGER + " varchar(100), " +
                User.Columns.NUM_ASSETS + " integer default 0, " +
                User.Columns.EMPLOYEE_NUM + " varchar(100), " +
                User.Columns.GROUPS + " varchar(100), " +
                User.Columns.NOTES + " varchar(100), " +
                User.Columns.COMPANY_NAME + " varchar(100)" +
                ")");
    }

    private void createGroupsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USER + " ( " +
                Group.Columns.ID + " integer primary key not null, " +
                Group.Columns.NAME + " varchar(100), " +
                Group.Columns.USERS + " varchar(100), " +
                Group.Columns.CREATED_AT + " varchar(100), " +
                ")");
    }

    private void createModelsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_MODEL + " ( " +
                Model.Columns.ID + " integer primary key not null, " +
                Model.Columns.MANUFACTURER + " varchar(100), " +
                Model.Columns.NAME + " varchar(100), " +
                Model.Columns.IMAGE + " varchar(100), " +
                Model.Columns.MODEL_NUMBER + " varchar(100), " +
                Model.Columns.NUM_ASSETS + " integer default 0, " +
                Model.Columns.DEPRECIATION + " varchar(100), " +
                Model.Columns.CATEGORY+ " varchar(100), " +
                Model.Columns.EOL + " varchar(100), " +
                Model.Columns.NOTE + " varchar(100), " +
                Model.Columns.FIELDSET + " varchar(100)" +
                ")");
    }

    private void createCategoriesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CATEGORY + " ( " +
                Category.Columns.ID + " integer primary key not null, " +
                Category.Columns.NAME + " varchar(100), " +
                Category.Columns.CATEGORY_TYPE + " varchar(100), " +
                Category.Columns.COUNT + " integer default 0, " +
                Category.Columns.ACCEPTANCE + " varchar(100), " +
                Category.Columns.EULA + " varchar(100), " +
                ")");
    }
}
