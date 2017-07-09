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

import java.util.List;

import io.phobotic.nodyn.database.helper.AssetTableHelper;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.Category;
import io.phobotic.nodyn.database.model.FullDataModel;
import io.phobotic.nodyn.database.model.Group;
import io.phobotic.nodyn.database.model.Model;
import io.phobotic.nodyn.database.model.User;

/**
 * Created by Jonathan Nelson on 7/8/17.
 */

public class Database {
    private static final String TAG = Database.class.getSimpleName();
    private final Context context;
    private static Database instance;
    private final SQLiteDatabase db;

    private Database(Context context) {
        this.context = context;
        AssetDatabaseOpenHelper helper = new AssetDatabaseOpenHelper(context);
        this.db = helper.getWritableDatabase();
    }

    private static Database getInstance(Context context) {
        if (instance == null) {
            instance = new Database(context);
        }

        return instance;
    }

    public void updateModel(FullDataModel model) {

    }

    private void replaceAssets(List<Asset> assets) {
        AssetTableHelper helper = new AssetTableHelper(db);
    }

    public List<Asset> getAssets() {
        //// TODO: 7/8/17
        return null;
    }

    public List<User> getUsers() {
        // TODO: 7/8/17
        return null;
    }

    public List<Model> getModels() {
        // TODO: 7/8/17
        return null;
    }

    private List<Group> getGroups() {
        // TODO: 7/8/17
        return null;
    }

    private List<Category> getCategories() {
        // TODO: 7/8/17
        return null;
    }


}
