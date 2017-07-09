package io.phobotic.nodyn.database.model;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jonathan Nelson on 7/8/17.
 */

public class AssetDatabaseOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "nodyn";
    private static final String TABLE_ASSETS = "assets";
    private static final String TABLE_USER = "users";
    private static final String TABLE_MODEL = "models";
    private static final String TABLE_GROUP = "groups";

    private static final int VERSION = 1;

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ASSETS);
        createTable(db);
    }

    private void createTables() {
        createAssetsTable();
        createUsersTable();
        createModelsTable();
        createGroupsTable();
    }

    private void createAssetsTable() {
        
    }
}
