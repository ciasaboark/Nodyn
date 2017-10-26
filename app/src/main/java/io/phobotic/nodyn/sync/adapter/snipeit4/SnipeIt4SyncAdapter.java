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

package io.phobotic.nodyn.sync.adapter.snipeit4;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.exception.AssetNotFoundException;
import io.phobotic.nodyn.database.exception.UserNotFoundException;
import io.phobotic.nodyn.database.model.Action;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.Category;
import io.phobotic.nodyn.database.model.FullDataModel;
import io.phobotic.nodyn.database.model.Group;
import io.phobotic.nodyn.database.model.MaintenanceRecord;
import io.phobotic.nodyn.database.model.Manufacturer;
import io.phobotic.nodyn.database.model.Model;
import io.phobotic.nodyn.database.model.Status;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.sync.CheckinException;
import io.phobotic.nodyn.sync.CheckoutException;
import io.phobotic.nodyn.sync.SyncErrorListener;
import io.phobotic.nodyn.sync.adapter.SyncAdapter;
import io.phobotic.nodyn.sync.adapter.SyncException;
import io.phobotic.nodyn.sync.adapter.SyncNotSupportedException;
import io.phobotic.nodyn.sync.adapter.snipeit4.response.ActivityResponse;
import io.phobotic.nodyn.sync.adapter.snipeit4.response.AssetResponse;
import io.phobotic.nodyn.sync.adapter.snipeit4.response.CategoryResponse;
import io.phobotic.nodyn.sync.adapter.snipeit4.response.CheckoutResponse;
import io.phobotic.nodyn.sync.adapter.snipeit4.response.GroupResponse;
import io.phobotic.nodyn.sync.adapter.snipeit4.response.ManufacturersResponse;
import io.phobotic.nodyn.sync.adapter.snipeit4.response.ModelResponse;
import io.phobotic.nodyn.sync.adapter.snipeit4.response.StatusesResponse;
import io.phobotic.nodyn.sync.adapter.snipeit4.response.UserResponse;
import io.phobotic.nodyn.sync.adapter.snipeit4.shadow.Snipeit4Activity;
import io.phobotic.nodyn.sync.adapter.snipeit4.shadow.Snipeit4Asset;
import io.phobotic.nodyn.sync.adapter.snipeit4.shadow.Snipeit4Category;
import io.phobotic.nodyn.sync.adapter.snipeit4.shadow.Snipeit4Group;
import io.phobotic.nodyn.sync.adapter.snipeit4.shadow.Snipeit4Manufacturer;
import io.phobotic.nodyn.sync.adapter.snipeit4.shadow.Snipeit4Model;
import io.phobotic.nodyn.sync.adapter.snipeit4.shadow.Snipeit4Status;
import io.phobotic.nodyn.sync.adapter.snipeit4.shadow.Snipeit4User;


/**
 * Created by Jonathan Nelson on 9/12/17.
 */

public class SnipeIt4SyncAdapter implements SyncAdapter {
    public static final String TAG = SnipeIt4SyncAdapter.class.getSimpleName();
    private static final String ACTIVITY_URL_PART = "/api/v1/reports/activity";
    private static final String ASSET_URL_PART = "/api/v1/hardware?limit=9999";
    private static final String MODELS_URL_PART = "/api/v1/models?limit=9999";
    private static final String USERS_URL_PART = "/api/v1/users?limit=9999";
    private static final String MANUFACTURER_URL_PART = "/api/v1/manufacturers?limit=9999";
    private static final String GROUPS_URL_PART = "/api/v1/groups?limit=9999";
    private static final String CATEGORIES_URL_PART = "/api/v1/categories?limit=9999";
    private static final String STATUS_URL_PART = "/api/v1/statuslabels?limit=9999";
    private Gson gson = new Gson();
    private HttpURLConnection conn;

    private List<Asset> fetchAssets(Context context) throws SyncException {
        List<Asset> assets = new ArrayList<>();
        try {
            String assetResult = getPageContent(context, getUrl(context, ASSET_URL_PART));
            AssetResponse assetResponse = gson.fromJson(assetResult, AssetResponse.class);
            List<Snipeit4Asset> snipeit4Assets = assetResponse.getAssets();

            for (Snipeit4Asset snipeit4Asset : snipeit4Assets) {
                assets.add(snipeit4Asset.toAsset());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SyncException("Unable to fetch assets: " + e.getMessage());
        }

        return assets;
    }

    private List<Model> fetchModels(Context context) throws SyncException {
        List<Model> models = new ArrayList<>();

        try {
            String modelResult = getPageContent(context, getUrl(context, MODELS_URL_PART));
            ModelResponse modelResponse = gson.fromJson(modelResult, ModelResponse.class);
            List<Snipeit4Model> snipeit4Models = modelResponse.getModels();

            for (Snipeit4Model snipeit4Model : snipeit4Models) {
                models.add(snipeit4Model.toModel());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SyncException("Unable to fetch models");
        }

        return models;
    }

    private List<User> fetchUsers(Context context) throws SyncException {
        List<User> users = new ArrayList<>();

        try {
            String modelResult = getPageContent(context, getUrl(context, USERS_URL_PART));
            UserResponse userResponse = gson.fromJson(modelResult, UserResponse.class);
            List<Snipeit4User> snipeit4Users = userResponse.getUsers();

            for (Snipeit4User snipeit4User : snipeit4Users) {
                users.add(snipeit4User.toUser());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SyncException("Unable to fetch users");
        }

        return users;
    }

    private List<Group> fetchGroups(Context context) throws SyncException {
        List<Group> groups = new ArrayList<>();

        try {
            String groupResult = getPageContent(context, getUrl(context, GROUPS_URL_PART));
            GroupResponse groupResponse = gson.fromJson(groupResult, GroupResponse.class);
            List<Snipeit4Group> snipeit4Groups = groupResponse.getGroups();

            for (Snipeit4Group snipeit4Group : snipeit4Groups) {
                groups.add(snipeit4Group.toGroup());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SyncException("Unable to fetch groups");
        }

        return groups;
    }

    private List<Category> fetchCategories(Context context) throws SyncException {
        List<Category> categories = new ArrayList<>();

        try {
            String categoryResult = getPageContent(context, getUrl(context, CATEGORIES_URL_PART));
            CategoryResponse groupResponse = gson.fromJson(categoryResult, CategoryResponse.class);
            List<Snipeit4Category> shadowCategories = groupResponse.getCategories();

            for (Snipeit4Category snipeit4Category : shadowCategories) {
                categories.add(snipeit4Category.toCategory());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SyncException("Unable to fetch categories");
        }

        return categories;
    }

    private List<Status> fetchStatuses(Context context) throws SyncException {
        List<Status> statuses = new ArrayList<>();

        try {
            String statusResult = getPageContent(context, getUrl(context, STATUS_URL_PART));
            StatusesResponse statusesResponse = gson.fromJson(statusResult, StatusesResponse.class);
            List<Snipeit4Status> snipeit4Statuses = statusesResponse.getStatuses();

            for (Snipeit4Status snipeit4Status : snipeit4Statuses) {
                statuses.add(snipeit4Status.toStatus());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SyncException("Unable to fetch statuses");
        }

        return statuses;
    }

    private List<Manufacturer> fetchManufacturers(Context context) throws SyncException {
        List<Manufacturer> manufacturers = new ArrayList<>();

        try {
            String manufacturerResult = getPageContent(context, getUrl(context, MANUFACTURER_URL_PART));
            ManufacturersResponse manufacturersResponse = gson.fromJson(manufacturerResult, ManufacturersResponse.class);
            List<Snipeit4Manufacturer> snipeit4Manufacturers = manufacturersResponse.getManufacturers();

            for (Snipeit4Manufacturer snipeit4Manufacturer : snipeit4Manufacturers) {
                manufacturers.add(snipeit4Manufacturer.toManufacturer());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SyncException("Unable to fetch manufacturers");
        }

        return manufacturers;
    }

    @Override
    public FullDataModel fetchFullModel(Context context) throws SyncException {
        FullDataModel fullDataModel = new FullDataModel()
                .setAssets(fetchAssets(context))
                .setCategories(fetchCategories(context))
                .setGroups(fetchGroups(context))
                .setModels(fetchModels(context))
                .setUsers(fetchUsers(context))
                .setStatuses(fetchStatuses(context))
                .setManufacturers(fetchManufacturers(context));

        return fullDataModel;
    }

    @Override
    public void checkoutAssetTo(Context context, int assetID, String assetTag, int userID,
                                @Nullable Long checkout, @Nullable Long expectedCheckin,
                                @Nullable String notes) throws Exception {
        String checkoutURL = "/api/v1/hardware/" + assetID + "/checkout";
        String params = "user_id=" + userID;

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
    public void syncActionItems(Context context, SyncErrorListener listener) throws SyncException {
        Database db = Database.getInstance(context);
        List<Action> actionList = db.getActions();
        Iterator<Action> it = actionList.iterator();

        //remove all of the action items that have already been synced
        while (it.hasNext()) {
            Action a = it.next();
            if (a.isSynced()) {
                it.remove();
            }
        }

        //sort the remaining list by the timestamp
        Collections.sort(actionList, new Comparator<Action>() {
            @Override
            public int compare(Action o1, Action o2) {
                return ((Long) o1.getTimestamp()).compareTo(o2.getTimestamp());
            }
        });

        for (Action action : actionList) {
            try {
                Asset asset = db.findAssetByID(action.getAssetID());
                StringBuilder notes = new StringBuilder("Nodyn ");
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
            } catch (UserNotFoundException e) {
                e.printStackTrace();
                listener.onActionSyncError(action, e, "Unable to find user in database with " +
                        "username: '" + action.getUserID() + "'");
            } catch (AssetNotFoundException e) {
                e.printStackTrace();
                listener.onActionSyncError(action, e, "Unable to find asset in database with " +
                        "tag: '" + action.getAssetID() + "'");
            } catch (CheckinException e) {
                e.printStackTrace();
                listener.onActionSyncError(action, e, "Unable to check in asset with tag: '" +
                        action.getAssetID() + "'");
            } catch (CheckoutException e) {
                e.printStackTrace();
                listener.onActionSyncError(action, e, "Unable to check out asset with tag: '" +
                        action.getAssetID() + "' to user ID " + action.getUserID());
            } catch (Exception e) {
                //all other exceptions we will keep the action un-synced so we can try again later
                Log.d(TAG, "Caught non-fatal error pusing action item, this action will " +
                        "remain unsynced " + action.toString() +
                        ": [" + e.getClass().getSimpleName() + "->" + e.getMessage() + "]");
                action.setSynced(false);
                db.insertAction(action);
            }
        }
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
        throw new SyncNotSupportedException("Sync adapter does not support pulling maintenance records",
                "SnipeIt version 4.x does not support pulling maintenance records");

    }

    @Override
    public List<Action> getAssetActivity(Context context, Asset asset) throws SyncException,
            SyncNotSupportedException {
        String filter = "?item_id=" + asset.getId() + "&item_type=asset&order=desc";
        return getActivityWithFilter(context, filter);
    }

    public List<Action> getActivityWithFilter(@NotNull Context context, @Nullable String filterText) throws SyncException {
        List<Action> actionList = new ArrayList<>();

        try {
            String url = ACTIVITY_URL_PART + (filterText == null ? "" : filterText);
            String statusResult = getPageContent(context, getUrl(context, url));
            ActivityResponse statusesResponse = gson.fromJson(statusResult, ActivityResponse.class);
            List<Snipeit4Activity> snipeit4ActivityList = statusesResponse.getActivityList();

            for (Snipeit4Activity snipeit4Activity : snipeit4ActivityList) {
                actionList.add(snipeit4Activity.toAction());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new SyncException("Unable to fetch activity");
        }

        return actionList;
    }

    private String getPageContent(Context context, String url) throws Exception {

        URL obj = new URL(url);
        conn = (HttpURLConnection) obj.openConnection();

        // default is GET
        conn.setRequestMethod("GET");

        conn.setUseCaches(false);
        conn.setRequestProperty("Authorization", "Bearer " + getAPIKey(context));
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        int responseCode = conn.getResponseCode();
        Log.d(TAG, "Sending 'GET' request to URL : " + url);
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

    private String getUrl(Context context, String url) {
        String completeUrl = getProtocol(context) + getHost(context) + ":" + getPort(context) + url;
        return completeUrl;
    }

    private String getAPIKey(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = prefs.getString(context.getString(R.string.pref_key_snipeit_4_api_key),
                context.getString(R.string.pref_default_snipeit_4_api_key));
        return key;
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

    @Override
    public List<Action> getUserActivity(Context context, User user) throws SyncException, SyncNotSupportedException {
        String filter = "?target_id=" + user.getId() + "&target_type=user&order=desc";
        return getActivityWithFilter(context, filter);

    }

    @Override
    public List<Action> getActivity(Context context) throws SyncException,
            SyncNotSupportedException {
        return getActivityWithFilter(context, null);
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
}
