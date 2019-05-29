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

package io.phobotic.nodyn_app.sync.adapter.snipeit4;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.Action;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Category;
import io.phobotic.nodyn_app.database.model.FullDataModel;
import io.phobotic.nodyn_app.database.model.Group;
import io.phobotic.nodyn_app.database.model.MaintenanceRecord;
import io.phobotic.nodyn_app.database.model.Manufacturer;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.service.SyncService;
import io.phobotic.nodyn_app.sync.ActionSyncListener;
import io.phobotic.nodyn_app.sync.CheckinException;
import io.phobotic.nodyn_app.sync.CheckoutException;
import io.phobotic.nodyn_app.sync.adapter.SyncAdapter;
import io.phobotic.nodyn_app.sync.adapter.SyncException;
import io.phobotic.nodyn_app.sync.adapter.SyncNotSupportedException;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.response.ActivityResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.response.AssetResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.response.CategoryResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.response.CheckoutResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.response.GroupResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.response.MaintenanceResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.response.ManufacturersResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.response.ModelResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.response.PaginatedResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.response.StatusesResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.response.UserResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow.Snipeit4Activity;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow.Snipeit4Asset;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow.Snipeit4Category;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow.Snipeit4Group;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow.Snipeit4MaintenanceRecord;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow.Snipeit4Manufacturer;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow.Snipeit4Model;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow.Snipeit4Status;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow.Snipeit4User;
import io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow.Snippet;


/**
 * Created by Jonathan Nelson on 9/12/17.
 */

public class SnipeIt4SyncAdapter implements SyncAdapter {
    public static final String TAG = SnipeIt4SyncAdapter.class.getSimpleName();
    private static final String ACTIVITY_URL_PART = "/api/v1/reports/activity?";
    private static final String ASSET_URL_PART = "/api/v1/hardware";
    private static final String MODELS_URL_PART = "/api/v1/models";
    private static final String USERS_URL_PART = "/api/v1/users";
    private static final String MANUFACTURER_URL_PART = "/api/v1/manufacturers";
    private static final String GROUPS_URL_PART = "/api/v1/groups";
    private static final String CATEGORIES_URL_PART = "/api/v1/categories";
    private static final String STATUS_URL_PART = "/api/v1/statuslabels";
    private static final String MAINT_URL_PART = "/api/v1/maintenances";
    private static final int SHORT_TIMEOUT = 1000 * 30;
    private static final Integer DEFAULT_CONNECTION_TIMEOUT = 1000 * 30;
    private static final Integer DEFAULT_READ_TIMEOUT = 1000 * 60;
    private static final int MAX_RECORD_LIMIT = 20;
    //the maximum number of times a connection will be attempted before being abandoned
    private static final int MAX_TRY_COUNT = 3;

    //the maximum number of records to pull while downloading the model data
    private static final int MAX_RECORD_DOWNLOAD = 50;
    private static final int PROGRESS_USERS = 20;
    private static final int PROGRESS_ASSETS = 10;
    private static final int PROGRESS_MODELS = 30;
    private static final int PROGRESS_CATEGORIES = 40;
    private static final int PROGRESS_GROUPS = 50;
    private static final int PROGRESS_STATUSES = 60;
    private static final int PROGRESS_MANUFACTURERS = 70;
    private static final int PROGRESS_DB_UPDATE = 90;
    private Gson gson = new Gson();
    private HttpURLConnection conn;

    private List<Asset> fetchAssets(final Context context) throws SyncException {
        final String message = "Fetching assets";
        sendMessageBroadcast(context, message);
        Log.i(TAG, "Beginning fetching list of assets");
        List<Asset> assets = new ArrayList<>();
        List<String> parts = fetchParts(context, ASSET_URL_PART, new ProgressListener() {
            @Override
            public void onProgressUpdate(int progress, int total) {
                float curProgress = ((float) progress / (float) total);
                curProgress *= 100f;
                sendProgressBroadcast(context, message, "Fetching parts", PROGRESS_ASSETS,
                        (int) curProgress, "fetch_assets");
            }
        });
        Log.i(TAG, String.format("Finished fetching asset list with a total of %d pages", parts.size()));
        //convert the JSON pages back into a list of users
        for (String part : parts) {
            AssetResponse response = gson.fromJson(part, AssetResponse.class);
            List<Snipeit4Asset> resultObjects = response.getAssets();
            sendDebugBroadcast(context, "Found " + resultObjects.size() + " assets");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean isServerUTC = prefs.getBoolean(context.getString(R.string.pref_key_snipeit4_utc_time),
                    Boolean.parseBoolean(context.getString(R.string.pref_default_snipeit4_utc_time)));

            Log.i(TAG, String.format("Found %d assets in this page.  Adding to existing list of " +
                    "%d", resultObjects.size(), assets.size()));
            for (Snipeit4Asset o : resultObjects) {
                assets.add(o.toAsset(isServerUTC));
            }
        }

        Log.i(TAG, String.format("Finished building list of assets. Found %d total assets.", assets.size()));
        return assets;
    }

    private List<Model> fetchModels(Context context) throws SyncException {
        sendMessageBroadcast(context, "Fetching models");

        List<Model> models = new ArrayList<>();
        List<String> parts = fetchParts(context, MODELS_URL_PART, null);
        //convert the JSON pages back into a list of users
        for (String part : parts) {
            ModelResponse response = gson.fromJson(part, ModelResponse.class);
            List<Snipeit4Model> resultObjects = response.getModels();
            sendDebugBroadcast(context, "Found " + resultObjects.size() + " models");

            Log.i(TAG, String.format("Found %d users in this page.  Adding to existing list of " +
                    "%d", resultObjects.size(), models.size()));
            for (Snipeit4Model o : resultObjects) {
                models.add(o.toModel());
            }
        }

        return models;
    }

    private List<User> fetchUsers(final Context context) throws SyncException {
        final String message = "Fetching users";
        sendMessageBroadcast(context, message);

        List<User> users = new ArrayList<>();
        List<String> parts = fetchParts(context, USERS_URL_PART, new ProgressListener() {
            @Override
            public void onProgressUpdate(int progress, int total) {
                float curProgress = ((float) progress / (float) total);
                curProgress *= 100f;
                sendProgressBroadcast(context, message, "Fetching parts",
                        PROGRESS_USERS, (int) curProgress, "fetch_users");
            }
        });
        //convert the JSON pages back into a list of users
        for (String part : parts) {
            UserResponse response = gson.fromJson(part, UserResponse.class);
            List<Snipeit4User> resultObjects = response.getUsers();
            sendDebugBroadcast(context, "Found " + resultObjects.size() + " users");

            Log.i(TAG, String.format("Found %d users in this page.  Adding to existing list of " +
                    "%d", resultObjects.size(), users.size()));
            for (Snipeit4User o : resultObjects) {
                users.add(o.toUser());
            }
        }

        return users;
    }

    /**
     * Recursively pull the list of models
     *
     * @param context
     * @param assets
     * @param offset
     * @param tryCount
     * @throws SyncException
     */

    private List<String> fetchParts(final Context context, String baseURL,
                                    ProgressListener listener) throws SyncException {
        List<String> parts = new ArrayList<>();
        fetchParts(context, parts, baseURL, 0, 0, listener);
        return parts;
    }

    private void fetchParts(Context context, List<String> parts, String baseURL, int offset,
                            int tryCount, @Nullable ProgressListener listener) throws SyncException {
        try {
            Log.i(TAG, String.format("Fetching data from %s with offset of %d and max records of " +
                    "%d", baseURL, offset, MAX_RECORD_DOWNLOAD));
            StringBuilder sb = new StringBuilder(baseURL);
            sb.append("?limit=" + MAX_RECORD_DOWNLOAD);
            sb.append("&offset=" + offset);

            String result = getPageContent(context, getUrl(context, sb.toString()));
            //just check that the result is proper JSON and that it contains a total record count
            PaginatedResponse response = gson.fromJson(result, PaginatedResponse.class);

            parts.add(result);

            //are there any more pages of data left?  If so call outselves recursivly with a reset try count
            //we can assume that we have reached this record number
            int curProgress = offset + MAX_RECORD_DOWNLOAD;
            if (listener != null) {
                listener.onProgressUpdate(curProgress, response.getTotal());
            }

            if (curProgress >= response.getTotal()) {
                Log.i(TAG, "Fetched last page of asset data");
            } else {
                fetchParts(context, parts, baseURL, offset + MAX_RECORD_DOWNLOAD, 0, listener);
            }
        } catch (Exception e) {
            sendDebugBroadcast(context, "Caught exception: " + e.getMessage());
            Log.w(TAG, String.format("Fetching data failed, trying again [this was try %d of %d]", tryCount, MAX_TRY_COUNT));
            e.printStackTrace();
            //if we are under the try count limit attempt to fetch this page of assets again,
            //+ otherwise throw an exception
            if (tryCount >= MAX_TRY_COUNT) {
                throw new SyncException("Unable to fetch data.  Max try limit reached: " + e.getMessage());
            } else {
                tryCount += 1;
                fetchParts(context, parts, baseURL, offset, tryCount, listener);
            }
        }
    }

    private List<Group> fetchGroups(Context context) throws SyncException {
        sendMessageBroadcast(context, "Fetching groups");
        List<Group> groups = new ArrayList<>();

        try {
            String groupResult = getPageContent(context, getUrl(context, GROUPS_URL_PART));
            GroupResponse groupResponse = gson.fromJson(groupResult, GroupResponse.class);
            List<Snipeit4Group> snipeit4Groups = groupResponse.getGroups();
            sendDebugBroadcast(context, "Found " + snipeit4Groups.size() + " groups");

            for (Snipeit4Group snipeit4Group : snipeit4Groups) {
                groups.add(snipeit4Group.toGroup());
            }
        } catch (ForbiddenException e) {
            //access to group information was not allowed.  Usually this means that the API key used
            //+ belongs to a non superuser.  We can approximate pulling the user group information
            //+ by pulling a list of all users

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Boolean useWorkaround = prefs.getBoolean(
                    context.getString(R.string.pref_key_snipeit4_workarounds),
                    Boolean.parseBoolean(context.getString(R.string.pref_default_snipeit4_workarounds)));
            if (useWorkaround) {
                try {
                    groups = fetchWorkaroundGroups(context);
                } catch (Exception e1) {
                    Crashlytics.logException(e1);
                    e1.printStackTrace();
                    throw new SyncException("Unable to fetch groups using workaround");
                }
            }
        } catch (Exception e) {
            sendDebugBroadcast(context, "Caught exception: " + e.getMessage());
            Crashlytics.logException(e);
            e.printStackTrace();
            throw new SyncException("Unable to fetch groups");
        }

        return groups;
    }

    private List<Group> fetchWorkaroundGroups(Context context) throws Exception {
        sendDebugBroadcast(context, "Using permission workaround to fetch groups list");
        List<Group> groups = new ArrayList<>();

        try {
            String usersResult = getPageContent(context, getUrl(context, USERS_URL_PART));
            UserResponse userResponse = gson.fromJson(usersResult, UserResponse.class);
            List<Snipeit4User> snipeit4Users = userResponse.getUsers();
            sendDebugBroadcast(context, "Found " + groups.size() + " users");

            Set<Group> groupSet = new HashSet<>();

            for (Snipeit4User snipeit4User : snipeit4Users) {
                Map<String, Object> userGroups = snipeit4User.getGroups();

                if (userGroups != null) {
                    try {
                        List<Map> l = (List) userGroups.get("rows");
                        for (Map m : l) {
                            int groupID = (int) Double.parseDouble(String.valueOf(m.get("id")));
                            String groupName = (String) m.get("name");
                            Group g = new Group()
                                    .setId(groupID)
                                    .setName(groupName);
                            groupSet.add(g);
                        }

                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        e.printStackTrace();
                        String message = "Unable to parse group information: " + e.getMessage();
                        Log.d(TAG, message);
                        throw new Exception(message);
                    }
                }

            }

            sendDebugBroadcast(context, "Found " + groupSet.size() + " groups from users list");

            if (!groupSet.isEmpty()) {
                groups.addAll(groupSet);
            }
        } catch (Exception e) {
            sendDebugBroadcast(context, "Caught exception: " + e.getMessage());
            e.printStackTrace();
            throw new SyncException("Unable to fetch users");
        }

        return groups;
    }

    private List<Manufacturer> fetchWorkaroundManufacturers(Context context) throws Exception {
        sendDebugBroadcast(context, "Using permission workaround to fetch manufacturers list");
        List<Manufacturer> manufacturers = new ArrayList<>();

        try {
            String modelResult = getPageContent(context, getUrl(context, MODELS_URL_PART));
            ModelResponse modelResponse = gson.fromJson(modelResult, ModelResponse.class);
            List<Snipeit4Model> snipeit4Models = modelResponse.getModels();
            sendDebugBroadcast(context, "Found " + snipeit4Models.size() + " models");

            Set<Manufacturer> manufacturerSet = new HashSet<>();


            for (Snipeit4Model snipeit4Model : snipeit4Models) {
                Snippet snippet = snipeit4Model.getManufacturer();

                if (snippet != null) {
                    try {
                        Manufacturer manufacturer = new Manufacturer()
                                .setId(snippet.getId())
                                .setName(snippet.getName());
                        manufacturerSet.add(manufacturer);
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        e.printStackTrace();
                        String message = "Unable to parse group information: " + e.getMessage();
                        Log.d(TAG, message);
                        throw new Exception(message);
                    }
                }

            }

            sendDebugBroadcast(context, "Found " + manufacturerSet.size() + " manufacturers from models list");

            if (!manufacturerSet.isEmpty()) {
                manufacturers.addAll(manufacturerSet);
            }
        } catch (Exception e) {
            sendDebugBroadcast(context, "Caught exception: " + e.getMessage());
            e.printStackTrace();
            throw new SyncException("Unable to fetch users");
        }

        return manufacturers;
    }

    private List<Category> fetchCategories(Context context) throws SyncException {
        sendMessageBroadcast(context, "Fetching categories");
        List<Category> categories = new ArrayList<>();

        try {
            String categoryResult = getPageContent(context, getUrl(context, CATEGORIES_URL_PART));
            CategoryResponse groupResponse = gson.fromJson(categoryResult, CategoryResponse.class);
            List<Snipeit4Category> shadowCategories = groupResponse.getCategories();
            sendDebugBroadcast(context, "Found " + shadowCategories.size() + " categories");

            for (Snipeit4Category snipeit4Category : shadowCategories) {
                categories.add(snipeit4Category.toCategory());
            }
        } catch (Exception e) {
            sendDebugBroadcast(context, "Caught exception: " + e.getMessage());
            e.printStackTrace();
            throw new SyncException("Unable to fetch categories");
        }

        return categories;
    }

    private List<Status> fetchStatuses(Context context) throws SyncException {
        sendMessageBroadcast(context, "Fetching statuses");
        List<Status> statuses = new ArrayList<>();

        try {
            String statusResult = getPageContent(context, getUrl(context, STATUS_URL_PART));
            StatusesResponse statusesResponse = gson.fromJson(statusResult, StatusesResponse.class);
            List<Snipeit4Status> snipeit4Statuses = statusesResponse.getStatuses();
            sendDebugBroadcast(context, "Found " + snipeit4Statuses.size() + " statuses");

            for (Snipeit4Status snipeit4Status : snipeit4Statuses) {
                statuses.add(snipeit4Status.toStatus());
            }
        } catch (Exception e) {
            sendDebugBroadcast(context, "Caught exception: " + e.getMessage());
            e.printStackTrace();
            throw new SyncException("Unable to fetch statuses");
        }

        return statuses;
    }

    private List<Manufacturer> fetchManufacturers(Context context) throws SyncException {
        sendMessageBroadcast(context, "Fetching manufacturers");
        List<Manufacturer> manufacturers = new ArrayList<>();

        try {
            String manufacturerResult = getPageContent(context, getUrl(context, MANUFACTURER_URL_PART));
            ManufacturersResponse manufacturersResponse = gson.fromJson(manufacturerResult, ManufacturersResponse.class);
            List<Snipeit4Manufacturer> snipeit4Manufacturers = manufacturersResponse.getManufacturers();
            sendDebugBroadcast(context, "Found " + snipeit4Manufacturers.size() + " manufacturers");

            for (Snipeit4Manufacturer snipeit4Manufacturer : snipeit4Manufacturers) {
                manufacturers.add(snipeit4Manufacturer.toManufacturer());
            }
        } catch (ForbiddenException e) {
            //access to manufacturer information was not allowed.  Usually this means that the API key used
            //+ belongs to a non superuser.  We can approximate pulling manufacturer information
            //+ by pulling a list of all models and aggregating manufacturer information
            sendDebugBroadcast(context, "Caught ForbiddenException fetching manufacturers");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Boolean useWorkaround = prefs.getBoolean(
                    context.getString(R.string.pref_key_snipeit4_workarounds),
                    Boolean.parseBoolean(context.getString(R.string.pref_default_snipeit4_workarounds)));
            if (useWorkaround) {
                try {
                    manufacturers = fetchWorkaroundManufacturers(context);
                } catch (Exception e1) {
                    Crashlytics.logException(e1);
                    e1.printStackTrace();
                    throw new SyncException("Unable to fetch manufacturers using workaround");
                }
            }
        } catch (Exception e) {
            sendDebugBroadcast(context, "Caught exception: " + e.getMessage());
            e.printStackTrace();
            throw new SyncException("Unable to fetch manufacturers");
        }

        return manufacturers;
    }

    @Override
    public String getAdapterName() {
        return "Snipe-it";
    }

    @Override
    public FullDataModel fetchFullModel(Context context) throws SyncException {
        try {
            sendDebugBroadcast(context, this.getClass().getSimpleName() + " fetching full data model");
            sendProgressBroadcast(context, PROGRESS_ASSETS);
            List<Asset> assets = fetchAssets(context);

            sendProgressBroadcast(context, PROGRESS_USERS);
            List<User> users = fetchUsers(context);

            sendProgressBroadcast(context, PROGRESS_MODELS);
            List<Model> models = fetchModels(context);

            sendProgressBroadcast(context, PROGRESS_CATEGORIES);
            List<Category> categories = fetchCategories(context);

            sendProgressBroadcast(context, PROGRESS_GROUPS);
            List<Group> groups = fetchGroups(context);

            sendProgressBroadcast(context, PROGRESS_STATUSES);
            List<Status> statuses = fetchStatuses(context);

            sendProgressBroadcast(context, PROGRESS_MANUFACTURERS);
            List<Manufacturer> manufacturers = fetchManufacturers(context);


            sendProgressBroadcast(context, PROGRESS_DB_UPDATE);
            sendMessageBroadcast(context, "Updating database");
            FullDataModel model = new FullDataModel()
                    .setAssets(assets)
                    .setUsers(users)
                    .setModels(models)
                    .setManufacturers(manufacturers)
                    .setCategories(categories)
                    .setGroups(groups)
                    .setStatuses(statuses);

            sendProgressBroadcast(context, 100);

            Log.d(TAG, "Finished pulling full data model: " + model.toString());
            return model;
        } catch (Exception e) {
            throw new SyncException(e.getMessage());
        }
    }

    @Override
    public void checkoutAssetTo(Context context, int assetID, String assetTag, int userID,
                                @Nullable Long checkout, @Nullable Long expectedCheckin,
                                @Nullable String notes) throws Exception {
        String checkoutURL = "/api/v1/hardware/" + assetID + "/checkout";
        String params = "checkout_to_type=user&assigned_user=" + userID;

        //if the backend has been configured to change the asset's name during checkout then go
        //+ ahead and apply that change
        String nameChangeFormat = PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.pref_key_snipeit_4_name_change), null);
        String name = null;
        if (nameChangeFormat == null || nameChangeFormat.isEmpty()) {
            Log.d(TAG, "skipping name change for asset " + assetTag + ", no format has been defined");
        } else {
            name = getFormattedName(context, assetID, userID, nameChangeFormat);
        }

        if (name != null) {
            params += "&name=" + name;
        }

        if (notes != null) {
            try {
                params += "&note=" + URLEncoder.encode(notes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e(TAG, "Unable to encode notes as query param '" + notes + "', will not be " +
                        "included with checkout data");
            }
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useUTC = prefs.getBoolean(context.getString(R.string.pref_key_snipeit4_utc_time),
                Boolean.parseBoolean(context.getString(R.string.pref_default_snipeit4_utc_time)));
        if (useUTC) {
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        if (checkout != null) {
            Date d = new Date(checkout);
            String checkoutDateString = df.format(d);
            try {
                checkoutDateString = URLEncoder.encode(checkoutDateString, "UTF-8");
                params += "&checkout_at=" + checkoutDateString;
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unable to encode checkout date as query parm '" + checkoutDateString +
                        "', will not be included with checkout data");
            }
        }

        if (expectedCheckin != null) {
            Date d = new Date(expectedCheckin);
            String expectedCheckinString = df.format(d);
            try {
                expectedCheckinString = URLEncoder.encode(expectedCheckinString, "UTF-8");
                params += "&expected_checkin=" + expectedCheckinString;
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unable to encode expected checkin date as qury parm '" +
                        expectedCheckinString + "', will not be included with checkout data");
            }
        }


        String result = sendPost(context, getUrl(context, checkoutURL), params);
        Gson gson = new Gson();
        CheckoutResponse response = gson.fromJson(result, CheckoutResponse.class);
        if (response.getStatus().equals("success")) {
            //Yea!
        } else if (response.getStatus().equals("error")) {
            Log.e(TAG, "Caught exception checking out asset id '" + assetID + "' to user id '" +
                    userID + "': " + "message: " + response.getMessages());
            throw new CheckoutException(response.getMessages());
        } else {
            throw new CheckoutException("Unknown error '" + result + "'");
        }
    }

    @Override
    public void checkinAsset(Context context, int assetID, String assetTag,
                             @Nullable Long checkinDate, @Nullable String notes)
            throws Exception {
        String checkoutURL = "/api/v1/hardware/" + assetID + "/checkin";
        String params = "";

        if (notes != null) {
            try {
                params += "&note=" + URLEncoder.encode(notes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.e(TAG, "Unable to encode notes as query param '" + notes + "', will not be " +
                        "included with checkout data");
            }
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useUTC = prefs.getBoolean(context.getString(R.string.pref_key_snipeit4_utc_time),
                Boolean.parseBoolean(context.getString(R.string.pref_default_snipeit4_utc_time)));
        if (useUTC) {
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        if (checkinDate != null) {
            Date d = new Date(checkinDate);
            String checkoutDateString = df.format(d);
            try {
                checkoutDateString = URLEncoder.encode(checkoutDateString, "UTF-8");
                params += "&checkout_at=" + checkoutDateString;
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unable to encode checkout date as query parm '" + checkoutDateString +
                        "', will not be included with checkout data");
            }
        }


        String result = sendPost(context, getUrl(context, checkoutURL), params);
        Gson gson = new Gson();
        CheckoutResponse response = gson.fromJson(result, CheckoutResponse.class);
        if (response.getStatus().equals("success")) {
            //Yea!
        } else if (response.getStatus().equals("error")) {
            throw new CheckinException(response.getMessages());
        } else {
            throw new CheckinException("Unknown error '" + result + "'");
        }
    }

    @Override
    public void syncActionItems(Context context, @NotNull List<Action> unsyncedActions,
                                @NotNull ActionSyncListener listener) throws SyncException {
        final String key = "action_items";
        final String message = "Pushing local data";
        sendProgressBroadcast(context, message, null, 0, 0, key);
        sendMessageBroadcast(context, message);
        Database db = Database.getInstance(context);

        float percent = 100f / unsyncedActions.size();
        float subProgress = 0f;
        for (Action action : unsyncedActions) {
            String subMessage = getSubMessage(context, action);
            sendProgressBroadcast(context, message, subMessage, 0, (int) subProgress, key);

            try {
                Asset asset = db.findAssetByID(action.getAssetID());

                StringBuilder notes = new StringBuilder("Nodyn ");
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                String deviceName = prefs.getString(context.getString(R.string.pref_key_general_id),
                        context.getString(R.string.pref_default_general_id));
                if (deviceName != null && deviceName.length() > 0) {
                    notes.append("<" + deviceName + "> ");
                }

                switch (action.getDirection()) {
                    case CHECKIN:
                        notes.append("checkin.");

                        //who (if anyone) authorized the asset check in?
                        if (action.getAuthorization() != null) {
                            notes.append(" Authorization: '" + action.getAuthorization() + "'.");
                        }

                        //did the authorizing user verify the asset was undamaged during checkin?
                        notes.append(" Asset verified undamaged: " + action.isVerified() + ".");

                        checkinAsset(context, asset.getId(), asset.getTag(), action.getTimestamp(),
                                notes.toString());
                        break;
                    case CHECKOUT:
                        notes.append("checkout.");

                        //who (if anyone) authorized the asset check out?
                        if (action.getAuthorization() != null) {
                            notes.append(" Authorization: '" + action.getAuthorization() + "'.");
                        }

                        User user = db.findUserByID(action.getUserID());
                        //did the associate checking out the asset have to agree to the eula?
                        notes.append(" EULA accepted: " + action.isVerified() + ".");
                        checkoutAssetTo(context, asset.getId(), asset.getTag(), user.getId(),
                                action.getTimestamp(), action.getExpectedCheckin(), notes.toString());
                        break;
                    default:
                        listener.onActionSyncError(action, null, "Unknown direction " +
                                action.getDirection());
                }

                action.setSynced(true);
                db.insertAction(action);
            }

            //The server may respond with a known error.  If so we can mark this action item as synced
            //+ and continue on the the next one
            catch (UserNotFoundException e) {
                e.printStackTrace();
                listener.onActionSyncError(action, e, "Unable to find user in database with " +
                        "username: '" + action.getUserID() + "'");
            } catch (AssetNotFoundException e) {
                e.printStackTrace();
                listener.onActionSyncError(action, e, "Unable to find asset in database with " +
                        "tag: '" + action.getAssetID() + "'");
            } catch (CheckinException e) {
                e.printStackTrace();
                listener.onActionSyncError(action, e, "Unable to check in asset with ID: '" +
                        action.getAssetID() + "'");
            } catch (CheckoutException e) {
                e.printStackTrace();
                listener.onActionSyncError(action, e, "Unable to check out asset with ID: '" +
                        action.getAssetID() + "' to user ID " + action.getUserID());
            }

            //all other exceptions we will keep the action un-synced so we can try again later
            catch (ParseException e) {
                e.printStackTrace();
                Log.e(TAG, "Caught parse exception reading response from server.  Action " +
                        "will remain unsynced and will be included in sync exception report");
                action.setSynced(false);
                db.insertAction(action);
                listener.onActionSyncError(action, e, "Caught parse exception reading " +
                        "response from server.");
            } catch (Exception e) {

                Log.d(TAG, "Caught non-fatal error pushing action item, this action will " +
                        "remain unsynced " + action.toString() +
                        ": [" + e.getClass().getSimpleName() + "->" + e.getMessage() + "]");
                action.setSynced(false);
                db.insertAction(action);
            }
            subProgress += percent;

        }

        sendProgressBroadcast(context, message, null, 0, 100, key);
    }

    @Override
    public void markActionItemsSynced(Context context, List<Action> actions) {
        Database db = Database.getInstance(context);

        for (Action action : actions) {
            action.setSynced(true);
            db.insertAction(action);
        }
    }

    @Override
    public List<MaintenanceRecord> getMaintenanceRecords(Context context, Asset asset) throws SyncException,
            SyncNotSupportedException {
        // TODO: 10/29/17 As of now the v1 API has no way to prefilter based off asset ID.  Just fetch everything and filter manually for now
        List<MaintenanceRecord> maintenanceRecords = new ArrayList<>();

        try {
            String maintenancesResult = getPageContent(context, getUrl(context, MAINT_URL_PART));
            MaintenanceResponse maintenanceResponse = gson.fromJson(maintenancesResult, MaintenanceResponse.class);
            List<Snipeit4MaintenanceRecord> snipeit4MaintenanceRecords = maintenanceResponse.getMaintenanceList();

            for (Snipeit4MaintenanceRecord record : snipeit4MaintenanceRecords) {
                MaintenanceRecord maintenanceRecord = record.toMaintenanceRecord();
                if (asset.getId() == maintenanceRecord.getAssetID()) {
                    maintenanceRecords.add(maintenanceRecord);
                }
            }
        } catch (IOException e) {
            //will be thrown if there is a general error connecting to the host
            e.printStackTrace();
            String origMessage = e.getMessage();
            throw new SyncException(String.format("Unable to fetch maintenance records. %s", origMessage));
        } catch (Exception e) {
            e.printStackTrace();
            throw new SyncException("Unable to fetch maintenance records.  Unknown error");
        }

        return maintenanceRecords;
    }

    @Override
    public List<Action> getAssetActivity(Context context, Asset asset, int page) throws SyncException,
            SyncNotSupportedException {
        String filter = "item_id=" + asset.getId() + "&item_type=asset&order=desc";
        return getActivityWithFilter(context, filter, page);
    }

    public List<Action> getActivityWithFilter(@NotNull Context context, @Nullable String filterText, int page) throws SyncException {
        //convert the page into a record offset
        final int pageSize = 50;
        int offset = page * pageSize;

        List<Action> actionList = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isServerUTC = prefs.getBoolean(context.getString(R.string.pref_key_snipeit4_utc_time),
                Boolean.parseBoolean(context.getString(R.string.pref_default_snipeit4_utc_time)));
        try {
            String url = ACTIVITY_URL_PART;
            if (filterText != null && filterText.length() > 0) {
                url += "&" + filterText;
            }
            url += "&offset=" + offset;
            url += "&limit=" + pageSize;


            String statusResult = getPageContent(context, getUrl(context, url));
            ActivityResponse activityResponse = gson.fromJson(statusResult, ActivityResponse.class);
            List<Snipeit4Activity> snipeit4ActivityList = activityResponse.getActivityList();

            for (Snipeit4Activity snipeit4Activity : snipeit4ActivityList) {
                actionList.add(snipeit4Activity.toAction(isServerUTC));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SyncException("Unable to fetch activity records: " + e.getMessage());
        }

        return actionList;
    }

    @Override
    public List<Action> getUserActivity(Context context, User user, int page) throws SyncException, SyncNotSupportedException {
        String filter = "target_id=" + user.getId() + "&target_type=user&order=desc";
        return getActivityWithFilter(context, filter, page);

    }

    @Override
    public List<Action> getActivity(Context context, int page) throws SyncException,
            SyncNotSupportedException {
        return getActivityWithFilter(context, "order=desc", page);
    }

    @Override
    public List<Action> getThirtyDayActivity(@NotNull Context context) throws SyncException, SyncNotSupportedException {
        //convert the page into a record offset
        final int pageSize = 50;
        int page = 0;


        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        long cutoff = calendar.getTimeInMillis();

        List<Action> actionList = new ArrayList<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isServerUTC = prefs.getBoolean(context.getString(R.string.pref_key_snipeit4_utc_time),
                Boolean.parseBoolean(context.getString(R.string.pref_default_snipeit4_utc_time)));
        boolean keepFetching = true;
        while (keepFetching) {
            try {
                int offset = page * pageSize;
                String url = ACTIVITY_URL_PART;
                url += "&offset=" + offset;
                url += "&limit=" + pageSize;
                String statusResult = getPageContent(context, getUrl(context, url));
                ActivityResponse activityResponse = gson.fromJson(statusResult, ActivityResponse.class);
                List<Snipeit4Activity> snipeit4ActivityList = activityResponse.getActivityList();

                if (snipeit4ActivityList.isEmpty()) {
                    keepFetching = false;
                    break;
                }

                for (Snipeit4Activity snipeit4Activity : snipeit4ActivityList) {
                    Action action = snipeit4Activity.toAction(isServerUTC);
                    //stop fetching as soon as we hit the first action record that occurred before
                    //+ the cutoff
                    if (action.getTimestamp() < cutoff) {
                        keepFetching = false;
                        break;
                    } else {
                        actionList.add(action);
                    }
                }

                page++;
            } catch (Exception e) {
                e.printStackTrace();
                throw new SyncException("Unable to fetch activity records: " + e.getMessage());
            }
        }

        return actionList;
    }

    @Override
    public List<Asset> getAssets(Context context, User user) throws SyncException, SyncNotSupportedException {
        throw new SyncNotSupportedException("Sync adapter does not support pulling user asset records",
                "SnipeIt version 4.x does not support pulling user asset records");
    }

    @Nullable
    @Override
    public DialogFragment getConfigurationDialogFragment(Context context) {
        ConfigurationDialogFragment d = ConfigurationDialogFragment.newInstance();
        return d;
    }

    private String getPageContent(Context context, String url) throws Exception {
        return getPageContent(context, url, DEFAULT_CONNECTION_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    private String getUrl(Context context, String url) {
        String completeUrl = getProtocol(context) + getHost(context) + ":" + getPort(context) + url;
        return completeUrl;
    }

    private void sendProgressBroadcast(Context context, String message, String subMessage, int progress, int subProgress,
                                       String subProgressKey) {
        Intent i = getBroadcastIntent();
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        i.putExtra(SyncService.BROADCAST_SYNC_MESSAGE, message);
        i.putExtra(SyncService.BROADCAST_SYNC_SUB_MESSAGE, subMessage);
        i.putExtra(SyncService.BROADCAST_SYNC_PROGRESS_MAIN, progress);
        i.putExtra(SyncService.BROADCAST_SYNC_PROGRESS_SUB, subProgress);
        i.putExtra(SyncService.BROADCAST_SYNC_PROGRESS_SUB_KEY, subProgressKey);
        broadcastManager.sendBroadcast(i);
    }

    private String getProtocol(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String protocol = prefs.getString(context.getString(R.string.pref_key_snipeit_4_protocol),
                context.getString(R.string.pref_default_snipeit_4_protocol));
        return protocol;
    }

    private String getHost(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String host = prefs.getString(context.getString(R.string.pref_key_snipeit_4_host), "");
        return host;
    }

    private int getPort(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String portString = prefs.getString(context.getString(R.string.pref_key_snipeit_4_port),
                context.getString(R.string.pref_default_snipeit_4_port));
        int port = Integer.valueOf(portString);
        return port;
    }

    private void sendDebugBroadcast(Context context, @NotNull String message) {
        Intent i = getBroadcastDebugIntent();
        i.putExtra(SyncService.BROADCAST_SYNC_MESSAGE, message);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        broadcastManager.sendBroadcast(i);
    }

    private String getAPIKey(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = prefs.getString(context.getString(R.string.pref_key_snipeit_4_api_key),
                context.getString(R.string.pref_default_snipeit_4_api_key));
        return key;
    }

    @NonNull
    private Intent getBroadcastDebugIntent() {
        return new Intent(SyncService.BROADCAST_SYNC_DEBUG);
    }

    private String getSubMessage(Context c, Action a) {
        if (a == null) return null;

        Database db = Database.getInstance(c);

        StringBuilder sb = new StringBuilder();
        switch (a.getDirection()) {
            case CHECKIN:
                sb.append("Check in ");
                break;
            case CHECKOUT:
                sb.append("Check out ");
                break;
            default:
                sb.append(a.getDirection().toString() + " ");
        }

        try {
            Asset asset = db.findAssetByID(a.getAssetID());
            sb.append(asset.getTag());
        } catch (AssetNotFoundException e) {
            sb.append("unknown asset");
        }

        switch (a.getDirection()) {
            case CHECKIN:
                sb.append(" from ");
                break;
            case CHECKOUT:
                sb.append(" to ");
                break;
            default:
                sb.append(" ");
        }

        try {
            User user = db.findUserByID(a.getUserID());
            sb.append(user.getName());
        } catch (UserNotFoundException e) {
            sb.append("unknown user");
        }

        return sb.toString();
    }

    private String getPageContent(Context context, String url, Integer connectionTimeout,
                                  Integer readTimeout) throws Exception {
        sendDebugBroadcast(context, "Fetching json data from: " + url);

        URL obj = new URL(url);
        Log.d(TAG, "Sending 'GET' request to URL : " + url);
        conn = (HttpURLConnection) obj.openConnection();

        // default is GET
        conn.setRequestMethod("GET");

        conn.setUseCaches(false);
        if (connectionTimeout != null) {
            conn.setConnectTimeout(connectionTimeout);
        }

        if (readTimeout != null) {
            conn.setReadTimeout(readTimeout);
        }

        conn.setRequestProperty("Authorization", "Bearer " + getAPIKey(context));
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
            throw new ForbiddenException("HTTP 403 error.  Access forbidden");
        } else if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception(String.format("Server responded with error code %d", responseCode));
        }

        Log.d(TAG, "Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    private String sendPost(Context context, String url, String postParams) throws Exception {
        Log.d(TAG, "Sending 'POST' request to URL : " + url);
        URL obj = new URL(url);
        conn = (HttpURLConnection) obj.openConnection();

        // Acts like a browser
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + getAPIKey(context));
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        conn.setDoOutput(true);
        conn.setDoInput(true);

        String requestProperties = conn.getRequestProperties().toString();

        // Send post request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(postParams);
        wr.flush();
        wr.close();

        int responseCode = conn.getResponseCode();

        Log.d(TAG, "Request properties: " + requestProperties);
        Log.d(TAG, "Post parameters : " + postParams);
        Log.d(TAG, "Response Code : " + responseCode);

        BufferedReader in =
                new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    private void sendMessageBroadcast(Context context, @NotNull String message) {
        Intent i = getBroadcastIntent();
        i.putExtra(SyncService.BROADCAST_SYNC_MESSAGE, message);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        broadcastManager.sendBroadcast(i);
    }

    private String getFormattedName(Context context, int assetID, int userID, String nameChangeFormat) {
        String name = nameChangeFormat;

        try {
            Asset a = Database.getInstance(context).findAssetByID(assetID);
            name = name.replaceAll("%tag%", a.getTag());
            name = name.replaceAll("%serial%", a.getSerial());

            try {
                Model m = Database.getInstance(context).findModelByID(a.getModelID());
                name = name.replaceAll("%model%", m.getName());
            } catch (ModelNotFoundException e) {
                Log.e(TAG, "Unable to find model with id " + a.getModelID() + ", will not be " +
                        "able to use model fields to modify asset name");
            }
        } catch (AssetNotFoundException e) {
            Log.e(TAG, "Unable to find asset with id " + assetID + ", will not be able to " +
                    "use asset fields to modify asset name");
        }

        try {
            User u = Database.getInstance(context).findUserByID(userID);
            name = name.replaceAll("%username%", u.getUsername());
            name = name.replaceAll("%name%", u.getName());
            name = name.replaceAll("%email%", u.getEmail());
            name = name.replaceAll("%employee%", u.getEmployeeNum());
        } catch (UserNotFoundException e) {
            Log.e(TAG, "Unable to find user with id " + userID + ", will not be able to use " +
                    "user fields to modify asset name");
        }

        return name;
    }

    private void sendProgressBroadcast(Context context, int progress) {
        sendProgressBroadcast(context, null, null, progress, 0, null);
    }

    @NonNull
    private Intent getBroadcastIntent() {
        return new Intent(SyncService.BROADCAST_SYNC_UPDATE);
    }

    private interface ProgressListener {
        void onProgressUpdate(int progress, int total);
    }

    //access to a resource was not allowed
    private class ForbiddenException extends IOException {
        public ForbiddenException() {
            super();
        }

        public ForbiddenException(String message) {
            super(message);
        }
    }

}
