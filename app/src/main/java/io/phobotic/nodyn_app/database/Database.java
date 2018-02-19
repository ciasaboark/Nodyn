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

package io.phobotic.nodyn_app.database;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.exception.CategoryNotFoundException;
import io.phobotic.nodyn_app.database.exception.GroupNotFoundException;
import io.phobotic.nodyn_app.database.exception.ManufacturerNotFoundException;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.exception.StatusNotFoundException;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.helper.ActionTableHelper;
import io.phobotic.nodyn_app.database.helper.AssetTableHelper;
import io.phobotic.nodyn_app.database.helper.CategoryTableHelper;
import io.phobotic.nodyn_app.database.helper.GroupTableHelper;
import io.phobotic.nodyn_app.database.helper.ManufacturerTableHelper;
import io.phobotic.nodyn_app.database.helper.ModelTableHelper;
import io.phobotic.nodyn_app.database.helper.StatusTableHelper;
import io.phobotic.nodyn_app.database.helper.UserTableHelper;
import io.phobotic.nodyn_app.database.model.Action;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Category;
import io.phobotic.nodyn_app.database.model.FullDataModel;
import io.phobotic.nodyn_app.database.model.Group;
import io.phobotic.nodyn_app.database.model.Manufacturer;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 7/8/17.
 */

public class Database {
    public static final String BROADCAST_ASSET_CHECKOUT = "asset_checkout";
    public static final String BROADCASE_ASSET_CHECKIN = "asset_checkin";
    private static final String TAG = Database.class.getSimpleName();
    private static Database instance;
    private final Context context;
    private final SQLiteDatabase db;
    private final CategoryTableHelper categoryHelper;
    private final GroupTableHelper groupHelper;
    private final UserTableHelper userHelper;
    private final AssetTableHelper assetHelper;
    private final ModelTableHelper modelHelper;
    private final StatusTableHelper statusHelper;
    private final ManufacturerTableHelper manufacturerHelper;

    public static Database getInstance(Context context) {
        if (instance == null) {
            instance = new Database(context);
        }

        return instance;
    }

    private Database(Context context) {
        this.context = context;
        DatabaseOpenHelper helper = new DatabaseOpenHelper(context);
        this.db = helper.getWritableDatabase();
        categoryHelper = new CategoryTableHelper(db);
        groupHelper = new GroupTableHelper(db);
        userHelper = new UserTableHelper(db);
        assetHelper = new AssetTableHelper(db);
        modelHelper = new ModelTableHelper(db);
        statusHelper = new StatusTableHelper(db);
        manufacturerHelper = new ManufacturerTableHelper(db);
    }

    public void updateModel(FullDataModel model) {
        replaceAssets(model.getAssets());
        replaceUsers(model.getUsers());
        replaceGroups(model.getGroups());
        replaceCategories(model.getCategories());
        replaceModels(model.getModels());
        replaceStatus(model.getStatuses());
        replaceManufacturers(model.getManufacturers());
    }

    private void replaceAssets(List<Asset> assets) {
        assetHelper.replace(assets);
    }

    private void replaceUsers(List<User> users) {
        userHelper.replace(users);
    }

    private void replaceGroups(List<Group> groups) {
        groupHelper.replace(groups);
    }

    private void replaceCategories(List<Category> categories) {
        categoryHelper.replace(categories);
    }

    private void replaceModels(List<Model> models) {
        modelHelper.replace(models);
    }

    private void replaceStatus(List<Status> statuses) {
        statusHelper.replace(statuses);
    }

    private void replaceManufacturers(List<Manufacturer> manufacturers) {
        manufacturerHelper.replace(manufacturers);
    }

    public void dumpModel() {
        replaceAssets(new ArrayList<Asset>());
        replaceUsers(new ArrayList<User>());
        replaceGroups(new ArrayList<Group>());
        replaceCategories(new ArrayList<Category>());
        replaceModels(new ArrayList<Model>());
        replaceStatus(new ArrayList<Status>());
        replaceManufacturers(new ArrayList<Manufacturer>());
    }

    public List<Action> getActions() {
        ActionTableHelper helper = new ActionTableHelper(db);
        List<Action> actions = helper.findAll();
        return actions;
    }

    public List<Action> getActions(long maxTimestamp, int maxRecords) {
        ActionTableHelper helper = new ActionTableHelper(db);
        List<Action> actions = helper.find(maxTimestamp, maxRecords);
        return actions;
    }

    public List<Action> getUnsyncedActions() {
        ActionTableHelper helper = new ActionTableHelper(db);
        List<Action> actions = helper.findUnsyncedActions();
        return actions;
    }

    public List<Action> getSyncedActions() {
        ActionTableHelper helper = new ActionTableHelper(db);
        List<Action> actions = helper.findSyncedActions();
        return actions;
    }

    public void pruneSyncedActions() {
        ActionTableHelper helper = new ActionTableHelper(db);
        helper.pruneSyncedActions();
    }

    public List<Asset> getAssets() {
        List<Asset> assets = assetHelper.findAll();
        return assets;
    }

    public List<User> getUsers() {
        List<User> users = userHelper.findAll();
        return users;
    }

    public List<Model> getModels() {
        List<Model> models = modelHelper.findAll();
        return models;
    }

    public List<Group> getGroups() {
        List<Group> groups = groupHelper.findAll();
        return groups;
    }

    public List<Category> getCategories() {
        List<Category> categories = categoryHelper.findAll();
        return categories;
    }

    public List<Status> getStatuses() {
        List<Status> statuses = statusHelper.findAll();
        return statuses;
    }

    public User findUserByUsername(String login) throws UserNotFoundException {
        UserTableHelper helper = new UserTableHelper(db);
        User user = helper.findByUsername(login);
        if (user == null) {
            throw new UserNotFoundException();
        }

        return user;
    }

    public User findUserByID(int id) throws UserNotFoundException {
        User user = userHelper.findByID(id);
        if (user == null) {
            throw new UserNotFoundException();
        }

        return user;
    }

    public Manufacturer findManufacturerByID(int id) throws ManufacturerNotFoundException {
        Manufacturer manufacturer = manufacturerHelper.findByID(id);
        if (manufacturer == null) {
            throw new ManufacturerNotFoundException();
        }

        return manufacturer;
    }

    public Asset findAssetByTag(@NotNull String tag) throws AssetNotFoundException {
        Asset asset = assetHelper.findByTag(tag);
        if (asset == null) {
            throw new AssetNotFoundException();
        }

        return asset;
    }

    public Asset findAssetByID(int id) throws AssetNotFoundException {
        Asset asset = assetHelper.findByID(id);
        if (asset == null) {
            throw new AssetNotFoundException();
        }

        return asset;
    }

    public List<Asset> findAssetByUserID(int userID) {
        return assetHelper.findAssetByUserID(userID);
    }

    public Category findCategoryByID(int id) throws CategoryNotFoundException {
        Category category = categoryHelper.findByID(id);
        if (category == null) {
            throw new CategoryNotFoundException();
        }

        return category;
    }

    public Status findStatusByID(int id) throws StatusNotFoundException {
        Status status = statusHelper.findByID(id);
        if (status == null) {
            throw new StatusNotFoundException();
        }

        return status;
    }

    public Group findGroupByID(int id) throws GroupNotFoundException {
        Group group = groupHelper.findByID(id);

        if (group == null) {
            throw new GroupNotFoundException();
        }

        return group;
    }

    public Model findModelByID(int id) throws ModelNotFoundException {
        Model model = modelHelper.findByID(id);

        if (model == null) {
            throw new ModelNotFoundException();
        }

        return model;
    }


    public void checkoutAssetsToUser(@NotNull User user, @NotNull List<Asset> assetList,
                                     long expectedCheckin, @Nullable User authorizingUser,
                                     boolean isVerified) throws
            UserNotFoundException, AssetNotFoundException {
        if (assetList == null || user == null) {
            throw new IllegalArgumentException("Asset list and user must not be null");
        }

        //write the action records to the database
        for (Asset asset : assetList) {
            Action action = new Action(asset, user,
                    System.currentTimeMillis(), expectedCheckin, Action.Direction.CHECKOUT, false);
            if (authorizingUser != null) {
                action.setAuthorization(authorizingUser.getName());
            }

            action.setVerified(isVerified);
            insertActionItem(action);
        }


        //update the internal asset data to reflect the checkout
        for (Asset asset : assetList) {
            checkoutAssetToUser(asset, user, expectedCheckin);
        }

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        Intent i = new Intent(BROADCAST_ASSET_CHECKOUT);
        broadcastManager.sendBroadcast(i);
    }

    private void insertActionItem(Action action) {
        ActionTableHelper helper = new ActionTableHelper(db);
        helper.insert(action);
    }

    private void checkoutAssetToUser(@NotNull Asset asset, @NotNull User user,
                                     long expectedCheckin) {
        if (asset == null || user == null) {
            throw new IllegalArgumentException("Asset and user must not be null");
        }

        AssetTableHelper helper = new AssetTableHelper(db);
        Asset a = helper.findByTag(asset.getTag());
        a.setAssignedToID(user.getId());
        a.setExpectedCheckin(expectedCheckin);
        a.setLastCheckout(System.currentTimeMillis());
        // TODO: 8/24/17 how do we set the status now?
//        a.setStatus("Deployed");
        helper.insert(a);
    }

    public void checkinAssets(@NotNull List<Asset> assets, @Nullable User user,
                              @Nullable long checkinTimestamp, @Nullable User authorizingUser,
                              boolean isVerified) {
        if (assets == null) {
            throw new IllegalArgumentException("Asset list can not be null");
        }

        //write the action records to the database
        for (Asset asset : assets) {
            Action action = new Action(asset, user, checkinTimestamp, null,
                    Action.Direction.CHECKIN, false);
            if (authorizingUser != null) {
                action.setAuthorization(authorizingUser.getName());
            }
            action.setVerified(isVerified);

            insertActionItem(action);

            //update the internal model to immediately reflect the new asset status
            checkinAsset(asset, checkinTimestamp);
        }
    }

    private void checkinAsset(@NotNull Asset asset, @Nullable Long checkinTimestamp) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must not be null");
        }

        AssetTableHelper helper = new AssetTableHelper(db);
        Asset a = helper.findByTag(asset.getTag());
        a.setAssignedToID(-1);
        a.setExpectedCheckin(-1);
        a.setLastCheckout(-1);

        // TODO: 8/24/17 how do we set the status now?
//        a.setStatus("Ready to Deploy");
        helper.insert(a);
    }

    public void insertAction(Action action) {
        ActionTableHelper helper = new ActionTableHelper(db);
        helper.insert(action);
    }


}

