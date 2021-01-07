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

package io.phobotic.nodyn_app.sync.adapter.snipeit3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.sun.jersey.core.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.audit.model.Audit;
import io.phobotic.nodyn_app.database.audit.model.AuditHeader;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.Company;
import io.phobotic.nodyn_app.database.sync.Action;
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
import io.phobotic.nodyn_app.sync.HtmlEncoded;
import io.phobotic.nodyn_app.sync.Image;
import io.phobotic.nodyn_app.sync.Link;
import io.phobotic.nodyn_app.sync.adapter.SyncAdapter;
import io.phobotic.nodyn_app.sync.adapter.SyncException;
import io.phobotic.nodyn_app.sync.adapter.SyncNotSupportedException;
import io.phobotic.nodyn_app.sync.adapter.snipeit3.response.AssetResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit3.response.CategoryResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit3.response.GroupResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit3.response.ModelResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit3.response.StatusesResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit3.response.UserResponse;
import io.phobotic.nodyn_app.sync.adapter.snipeit3.shadow.Snipeit3Asset;
import io.phobotic.nodyn_app.sync.adapter.snipeit3.shadow.Snipeit3Category;
import io.phobotic.nodyn_app.sync.adapter.snipeit3.shadow.Snipeit3Group;
import io.phobotic.nodyn_app.sync.adapter.snipeit3.shadow.Snipeit3Model;
import io.phobotic.nodyn_app.sync.adapter.snipeit3.shadow.Snipeit3Status;
import io.phobotic.nodyn_app.sync.adapter.snipeit3.shadow.Snipeit3User;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public class SnipeIt3SyncAdapter implements SyncAdapter {
    private static final String TAG = SnipeIt3SyncAdapter.class.getSimpleName();
    private final String USER_AGENT = "Mozilla/5.0";
    private List<String> cookies;
    private HttpURLConnection conn;
    private Gson gson = new Gson();

    /**
     * Converts any fields marked with {@link HtmlEncoded} or {@link io.phobotic.nodyn_app.sync.Link}
     * into their appropriate forms
     *
     * @param obj
     * @throws IllegalAccessException
     */
    private void convertFields(Context context, Object obj) throws IllegalAccessException {
        convertHtmlFields(context, obj);
        convertLinkFields(context, obj);
        convertImageFields(context, obj);
    }

    private void convertHtmlFields(Context context, Object obj) throws IllegalAccessException {
        List<Field> fields = FieldUtils.getFieldsListWithAnnotation(obj.getClass(),
                HtmlEncoded.class);

        for (Field field : fields) {
            if (!field.getType().equals(String.class)) {
                System.err.println("Field: " + field.getName() + " is marked as @HtmlEncoded, " +
                        "but is not of type String");
            } else {
                field.setAccessible(true);
                String val = (String) field.get(obj);
                if (val != null) {
                    val = Jsoup.parse(val).text();
                    field.set(obj, val);
                }
            }
        }
    }

    private void convertLinkFields(Context context, Object obj) throws IllegalAccessException {
        List<Field> fields = FieldUtils.getFieldsListWithAnnotation(obj.getClass(), Link.class);

        for (Field field : fields) {
            if (!field.getType().equals(String.class)) {
                System.err.println("Field: " + field.getName() + " is marked as @Link, but is " +
                        "not of type String");
            } else {
                field.setAccessible(true);
                String val = (String) field.get(obj);
                if (val != null) {
                    Document doc = Jsoup.parse(val);
                    Elements links = doc.select("a");
                    if (links.isEmpty()) {
                        Log.e(TAG, "Field: " + field.getName() + " is marked as @Link, but no <a> " +
                                "element could be found");
                    } else {
                        Element link = links.get(0);
                        String href = link.attr("href");

                        //rewrite the URL to make sure it points to the remote host
                        try {
                            URL url = new URL(href);
                            URL redirectedURL = new URL(url.getProtocol(), getHost(context),
                                    getPort(context), url.getFile());
                            href = redirectedURL.toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        field.set(obj, href);
                    }
                }
            }
        }
    }

    private void convertImageFields(Context context, Object obj) throws IllegalAccessException {
        List<Field> fields = FieldUtils.getFieldsListWithAnnotation(obj.getClass(), Image.class);

        for (Field field : fields) {
            if (!field.getType().equals(String.class)) {
                System.err.println("Field: " + field.getName() + " is marked as @Image, but is " +
                        "not of type String");
            } else {
                field.setAccessible(true);
                String val = (String) field.get(obj);
                if (val != null) {
                    Document doc = Jsoup.parse(val);

                    Elements images = doc.select("img");
                    if (images.isEmpty()) {
                        Log.e(TAG, "Field: " + field.getName() + " is marked as @Image, but no " +
                                "<img> element could be found");
                    } else {
                        Element image = images.get(0);
                        String src = image.attr("src");

                        //rewrite the URL so it points to the remote host
                        try {
                            URL url = new URL(src);
                            URL redirectedURL = new URL(url.getProtocol(), getHost(context),
                                    getPort(context), url.getFile());
                            src = redirectedURL.toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        field.set(obj, src);
                    }
                }
            }
        }
    }

    private List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

    private List<Asset> fetchAssets(Context context) throws SyncException {
        String assetsUrl = "/api/hardware/list?limit=9999&offset=0";
        try {
            loginIfNeeded(context);
            sendMessageBroadcast(context, "Fetching assets");
            String assetResult = getPageContent(getUrl(context, assetsUrl));
            AssetResponse assetsResponse = gson.fromJson(assetResult, AssetResponse.class);
            List<Snipeit3Asset> snipeit3Assets = assetsResponse.getAssets();
            List<Asset> assets = convertShadowAssets(context, snipeit3Assets);


            return assets;
        } catch (Exception e) {
            String message = "Unable to login to service: " + e.getMessage();
            Log.e(TAG, message);
            throw new SyncException(message);
        }
    }

    private List<Model> fetchModels(Context context) throws SyncException {
        String modelsUrl = "/api/models/list?limit=9999&offset=0";

        try {
            loginIfNeeded(context);

            sendMessageBroadcast(context, "Fetching models");
            String modelResult = getPageContent(getUrl(context, modelsUrl));
            ModelResponse modelResponse = gson.fromJson(modelResult, ModelResponse.class);
            List<Snipeit3Model> snipeit3Models = modelResponse.getModels();
            List<Model> models = convertModels(context, snipeit3Models);


            return models;
        } catch (Exception e) {
            e.printStackTrace();
            String message = "Unable to fetch models: " + e.getMessage();
            throw new SyncException(message);
        }
    }

    private List<User> fetchUsers(Context context) throws SyncException {
        String usersUrl = "/api/users/list?limit=9999&offset=0";

        try {
            loginIfNeeded(context);
            sendMessageBroadcast(context, "Fetching users");
            String usersResult = getPageContent(getUrl(context, usersUrl));
            UserResponse userResponse = gson.fromJson(usersResult, UserResponse.class);
            List<Snipeit3User> snipeit3Users = userResponse.getUsers();
            List<User> users = convertUsers(context, snipeit3Users);

            return users;

        } catch (Exception e) {
            e.printStackTrace();
            String message = "Unable to fetch users: " + e.getMessage();
            throw new SyncException(message);
        }
    }

    private List<Group> fetchGroups(Context context) throws SyncException {
        String groupsUrl = "/api/groups/list?limit=9999&offset=0";

        try {
            loginIfNeeded(context);
            sendMessageBroadcast(context, "Fetching groups");
            String groupsResult = getPageContent(getUrl(context, groupsUrl));
            GroupResponse groupResponse = gson.fromJson(groupsResult, GroupResponse.class);
            List<Snipeit3Group> snipeit3Groups = groupResponse.getGroups();
            List<Group> groups = convertGroups(context, snipeit3Groups);

            return groups;

        } catch (Exception e) {
            e.printStackTrace();
            String message = "Unable to fetch users: " + e.getMessage();
            throw new SyncException(message);
        }
    }

    private List<Category> fetchCategories(Context context) throws SyncException {
        String categoriesURL = "/api/categories/list?limit=9999&offset=0";

        try {
            loginIfNeeded(context);
            sendMessageBroadcast(context, "Fetching categories");

            String categoriesResult = getPageContent(getUrl(context, categoriesURL));
            CategoryResponse catoriesResponse = gson.fromJson(categoriesResult, CategoryResponse.class);
            List<Snipeit3Category> shadowCategories = catoriesResponse.getCategories();
            List<Category> categories = convertCategories(context, shadowCategories);

            return categories;

        } catch (Exception e) {
            e.printStackTrace();
            String message = "Unable to fetch categories: " + e.getMessage();
            throw new SyncException(message);
        }
    }

    private List<Status> fetchStatuses(Context context) throws SyncException {
        String categoriesURL = "/api/statuslabels/list?order=asc&offset=0";

        try {
            loginIfNeeded(context);
            sendMessageBroadcast(context, "Fetching statuses");

            String statusResult = getPageContent(getUrl(context, categoriesURL));
            StatusesResponse statusesResponse = gson.fromJson(statusResult, StatusesResponse.class);
            List<Snipeit3Status> snipeit3Statuses = statusesResponse.getStatuses();
            List<Status> statuses = convertStatuses(context, snipeit3Statuses);

            return statuses;

        } catch (Exception e) {
            e.printStackTrace();
            String message = "Unable to fetch statuses: " + e.getMessage();
            throw new SyncException(message);
        }
    }

    // TODO: 9/14/17 can this be pulled with version 3.x?
    private List<Manufacturer> fetchManufacturers(Context context) throws SyncException {
        return new ArrayList<>();
    }

    @Override
    public String getAdapterName() {
        return "Snipe-it 3.x";
    }

    @Override
    public FullDataModel fetchFullModel(Context context) throws SyncException {
        try {
            sendProgressBroadcast(context, 0);
            loginIfNeeded(context);

            sendProgressBroadcast(context, 10);
            List<Asset> assets = fetchAssets(context);

            sendProgressBroadcast(context, 20);
            List<User> users = fetchUsers(context);

            sendProgressBroadcast(context, 40);
            List<Model> models = fetchModels(context);

            sendProgressBroadcast(context, 60);
            List<Category> categories = fetchCategories(context);

            sendProgressBroadcast(context, 80);
            List<Group> groups = fetchGroups(context);

            sendProgressBroadcast(context, 90);
            List<Status> statuses = fetchStatuses(context);

            sendMessageBroadcast(context, "Updating database");
            FullDataModel model = new FullDataModel()
                    .setAssets(assets)
                    .setUsers(users)
                    .setModels(models)
                    .setCategories(categories)
                    .setGroups(groups)
                    .setStatuses(statuses);

            //no support for fetching companies
            model.setCompanies(new ArrayList<Company>());

            sendProgressBroadcast(context, 100);

            Log.d(TAG, "Finished pulling full data model: " + model.toString());
            return model;
        } catch (Exception e) {
            throw new SyncException(e.getMessage());
        }
    }

    @Override
    public void checkoutAssetTo(Context context, int assetID, String assetTag, int userID,
                                @Nullable Long checkoutDate, @Nullable Long expectedCheckin,
                                @Nullable String notes)
            throws CheckoutException {
        String checkoutFormUrl = "/hardware/" + assetID + "/checkout";
        String checkoutPostUrl = "/hardware/" + assetID + "/checkout";

        Log.d(TAG, "Attempting to check out asset " + assetID + " to user " + userID);

        try {
            login(context);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String checkoutString = "";
            if (checkoutDate != null) {
                checkoutString = df.format(new Date(checkoutDate));
            }

            String expectedCheckinString = "";
            if (expectedCheckin != null) {
                expectedCheckinString = df.format(new Date(expectedCheckin));
            }

            String checkoutPage = getPageContent(getUrl(context, checkoutFormUrl));
            Map<String, String> checkoutMap = new HashMap();
            checkoutMap.put("name", assetTag);
            checkoutMap.put("assigned_to", String.valueOf(userID));
            checkoutMap.put("checkout_at", checkoutString);
            checkoutMap.put("expected_checkin", expectedCheckinString);
            checkoutMap.put("note", notes == null ? "" : notes);

            String checkoutPostParams = getFormParams(checkoutPage, checkoutMap);

            String response = sendPost(context, getUrl(context, checkoutPostUrl), checkoutPostParams);
            Log.d(TAG, "Checkout POST response:");
            Log.d(TAG, response);

            if (response.contains("Asset checked out successfully")) {
                Log.d(TAG, "Asset was checked out successfully");
            } else {
                Log.e(TAG, "Error checking out asset");
                throw new CheckoutException("Error checking out asset.  Server responded " +
                        "correctly.  Perhaps asset is already checked out to another user?");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof CheckoutException) {
                throw (CheckoutException) e;
            } else {
                throw new CheckoutException("Data transmission exception during asset check " +
                        "out " + e.getMessage());
            }
        }
    }

    @Override
    public void checkinAsset(Context context, int assetID, String assetTag, @Nullable Long checkinTimestamp,
                             @Nullable String notes) throws CheckinException {

        String checkoutFormUrl = "/hardware/" + assetID + "/checkin";
        String checkoutPostUrl = "/hardware/" + assetID + "/checkin";

        Log.d(TAG, "Attempting to check in asset '" + assetID + "'");

        try {
            login(context);

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String checkinString = "";
            if (checkinTimestamp == null) {
                checkinTimestamp = System.currentTimeMillis();
            }

            checkinString = df.format(new Date(checkinTimestamp));

            String checkoutPage = getPageContent(getUrl(context, checkoutFormUrl));
            Map<String, String> checkoutMap = new HashMap();
            checkoutMap.put("name", assetTag);
            checkoutMap.put("checkin_at", checkinString);
            checkoutMap.put("note", notes == null ? "" : notes);

            String checkoutPostParams = getFormParams(checkoutPage, checkoutMap);

            String response = sendPost(context, getUrl(context, checkoutPostUrl), checkoutPostParams);
            Log.d(TAG, "Checkout POST response:");
            Log.d(TAG, response);

            if (response.contains("Asset checked in successfully")) {
                Log.d(TAG, "Asset was checked in successfully");
            } else {
                Log.d(TAG, "Error checking in asset");
                throw new CheckinException("Error checking in asset. Server response normal. Is " +
                        "asset not checked out?");
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e instanceof CheckinException) {
                throw (CheckinException) e;
            } else {
                throw new CheckinException("Data transmission exception during asset check " +
                        "in " + e.getMessage());
            }
        }

    }

    @Override
    public void syncActionItems(Context context, List<Action> unsyncedActions,
                                ActionSyncListener listener) throws SyncException {
        Database db = Database.getInstance(context);
        Iterator<Action> it = unsyncedActions.iterator();

        //remove all of the action items that have already been synced
        while (it.hasNext()) {
            Action a = it.next();
            if (a.isSynced()) {
                it.remove();
            }
        }

        //sort the remaining list by the timestamp
        Collections.sort(unsyncedActions, new Comparator<Action>() {
            @Override
            public int compare(Action o1, Action o2) {
                return ((Long) o1.getTimestamp()).compareTo(o2.getTimestamp());
            }
        });

        for (Action action : unsyncedActions) {
            boolean actionSynced = true;
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

                        //did the authorizing user have to verify the asset checkin?
                        notes.append(" Verified: " + action.isVerified() + ".");

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
                        notes.append(" Verified: " + action.isVerified() + ".");
                        checkoutAssetTo(context, asset.getId(), asset.getTag(), user.getId(),
                                action.getTimestamp(), action.getExpectedCheckin(), notes.toString());
                        break;
                    default:
                        listener.onActionSyncFatalError(action, null, "Unknown direction " +
                                action.getDirection());
                }

                listener.onActionSyncSuccess(action);

            } catch (UserNotFoundException e) {
                e.printStackTrace();
                listener.onActionSyncFatalError(action, e, "Unable to find user in database with " +
                        "username: '" + action.getUserID() + "'");
            } catch (AssetNotFoundException e) {
                e.printStackTrace();
                listener.onActionSyncFatalError(action, e, "Unable to find asset in database with " +
                        "tag: '" + action.getAssetID() + "'");
            } catch (CheckinException e) {
                e.printStackTrace();
                listener.onActionSyncFatalError(action, e, "Unable to check in asset with tag: '" +
                        action.getAssetID() + "'");
            } catch (CheckoutException e) {
                e.printStackTrace();
                listener.onActionSyncFatalError(action, e, "Unable to check out asset with tag: '" +
                        action.getAssetID() + "' to user ID " + action.getUserID());
            } catch (Exception e) {
                //all other exceptions we will keep the action un-synced so we can try again later
                listener.onActionSyncRecoverableError(action, e, "Unknown exception");
            }
        }
    }

    @Override
    public List<MaintenanceRecord> getMaintenanceRecords(Context context, Asset asset) throws SyncException,
            SyncNotSupportedException {
        throw new SyncNotSupportedException("Sync adapter does not support pulling maintenance records",
                "SnipeIt version 3.x does not support pulling maintenance records");

    }

    @Override
    public Asset getAsset(Context context, Asset asset) throws SyncNotSupportedException, SyncException {
        throw new SyncNotSupportedException("Unsupported",
                "SnipeIt version 3.x does not support pulling individual asset records");
    }

    @Override
    public List<Action> getAssetActivity(Context context, Asset asset, int page) throws SyncException,
            SyncNotSupportedException {
        throw new SyncNotSupportedException("Sync adapter does not support pulling asset history records",
                "SnipeIt version 3.x does not support pulling asset history records");
    }

    @Override
    public List<Action> getUserActivity(Context context, User user, int page) throws SyncException, SyncNotSupportedException {
        throw new SyncNotSupportedException("Sync adapter does not support pulling user history records",
                "SnipeIt version 3.x does not support pulling user history records");
    }

    @Override
    public List<Action> getActivity(Context context, int page) throws SyncException, SyncNotSupportedException {
        throw new SyncNotSupportedException("Sync adapter does not support pulling asset history records",
                "SnipeIt version 3.x does not support pulling history records");
    }

    @Override
    public List<Action> getActivity(@NotNull Context context, long cutoff) throws SyncException, SyncNotSupportedException {
        throw new SyncNotSupportedException("Sync adapter does not support pulling asset history records",
                "SnipeIt version 3.x does not support pulling history records");
    }

    @Override
    public List<Action> getThirtyDayActivity(@NotNull Context context) throws SyncException, SyncNotSupportedException {
        throw new SyncNotSupportedException("Sync adapter does not support pulling asset history records",
                "SnipeIt version 3.x does not support pulling history records");
    }

    @Override
    public List<Asset> getAssets(Context context, User user) throws SyncException, SyncNotSupportedException {
        throw new SyncNotSupportedException("Sync adapter does not support pulling user asset records",
                "SnipeIt version 3.x does not support pulling user asset records");
    }

    @Override
    public DialogFragment getConfigurationDialogFragment(Context context) {
        DialogFragment dialog = ConfigurationDialogFragment.newInstance();
        return dialog;
    }

    /**
     * Not supported by Snipeit v3
     * @param context
     * @param audit
     */
    @Override
    public void recordAudit(@NotNull Context context, @NotNull Audit audit) {

    }

    private List<Asset> convertShadowAssets(Context context, List<Snipeit3Asset> snipeit3Assets) {
        //the JSON returned by snipeit will have some properties wrapped in HTML.
        for (Snipeit3Asset snipeit3Asset : snipeit3Assets) {
            try {
                convertFields(context, snipeit3Asset);
            } catch (Exception e) {
            }
        }

        // TODO: 9/13/17
        return new ArrayList<>();
    }

    private List<Model> convertModels(Context context, List<Snipeit3Model> snipeit3Models) {
        //the JSON returned by snipeit will have some properties wrapped in HTML.
        for (Snipeit3Model snipeit3Model : snipeit3Models) {
            try {
                convertFields(context, snipeit3Model);
            } catch (Exception e) {
            }
        }

        // TODO: 9/13/17
        return new ArrayList<>();
    }

    private List<User> convertUsers(Context context, List<Snipeit3User> snipeit3Users) {
        //the JSON returned by snipeit will have some properties wrapped in HTML.
        for (Snipeit3User snipeit3User : snipeit3Users) {
            try {
                convertFields(context, snipeit3User);
            } catch (Exception e) {
            }
        }

        // TODO: 9/13/17
        return new ArrayList<>();
    }

    private List<Group> convertGroups(Context context, List<Snipeit3Group> snipeit3Groups) {
        //the JSON returned by snipeit will have some properties wrapped in HTML.
        for (Snipeit3Group snipeit3Group : snipeit3Groups) {
            try {
                convertFields(context, snipeit3Group);
            } catch (Exception e) {
            }
        }

        // TODO: 9/13/17
        return new ArrayList<>();
    }

    private List<Category> convertCategories(Context context, List<Snipeit3Category> shadowCategories) {
        //the JSON returned by snipeit will have some properties wrapped in HTML.
        for (Snipeit3Category snipeit3Category : shadowCategories) {
            try {
                convertFields(context, shadowCategories);
            } catch (Exception e) {
            }
        }

        // TODO: 9/13/17
        return new ArrayList<>();
    }

    private List<Status> convertStatuses(Context context, List<Snipeit3Status> snipeit3Statuses) {
        List<Status> statuses = new ArrayList<>();

        //the JSON returned by snipeit will have some properties wrapped in HTML.
        for (Snipeit3Status snipeit3Status : snipeit3Statuses) {
            try {
                convertFields(context, snipeit3Status);
            } catch (Exception e) {
            }
        }

        //Snipeit uses a meta status of 'Deployed' for assets that really have a status of
        //+ 'Ready to Deploy' but that have an assigned user.  Adding that meta status manually
        //+ for now

        boolean deployedRequired = true;
        for (Status s : statuses) {
            if (s.getName().equals("Deployed")) {
                deployedRequired = false;
                break;
            }
        }

        if (deployedRequired) {
            statuses.add(new Status("Deployed", "Ready to Deploy", null));
        }


        return new ArrayList<>();
    }

    private void sendMessageBroadcast(Context context, @NotNull String message) {
        Intent i = getBroadcastIntent();
        i.putExtra(SyncService.BROADCAST_SYNC_MESSAGE, message);
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        broadcastManager.sendBroadcast(i);
    }

    private void sendProgressBroadcast(Context context, int progress) {
        Intent i = getBroadcastIntent();
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
        i.putExtra(SyncService.BROADCAST_SYNC_PROGRESS_MAIN, progress);
        broadcastManager.sendBroadcast(i);
    }

    @NonNull
    private Intent getBroadcastIntent() {
        return new Intent(SyncService.BROADCAST_SYNC_UPDATE);
    }

    private String getPageContent(String url) throws Exception {

        URL obj = new URL(url);
        conn = (HttpURLConnection) obj.openConnection();

        // default is GET
        conn.setRequestMethod("GET");

        conn.setUseCaches(false);

        // act like a browser
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        if (cookies != null) {
            for (String cookie : this.cookies) {
                conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
            }
        }
        int responseCode = conn.getResponseCode();
        Log.d(TAG, "Sending 'GET' request to URL : " + url);
        Log.d(TAG, "Response Code : " + responseCode);

        BufferedReader in =
                new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Get the response cookies
        setCookies(conn.getHeaderFields().get("Set-Cookie"));

        return response.toString();

    }

    private String getUrl(Context context, String url) {
        String completeUrl = getProtocol(context) + getHost(context) + ":" + getPort(context) + url;
        return completeUrl;
    }

    private String getFormParams(String html, Map<String, String> formData)
            throws UnsupportedEncodingException {

        Log.d(TAG, "Extracting form's data...");

        Document doc = Jsoup.parse(html);

        //create a shallow copy of the form data
        Map<String, String> copy = new HashMap<>();
        copy.putAll(formData);

        Element loginform = doc.select("form").last();
        Elements inputElements = loginform.getElementsByTag("input");
        List<String> paramList = new ArrayList<>();

        for (Element inputElement : inputElements) {
            String key = inputElement.attr("name");
            String value = inputElement.attr("value");

            Iterator<Map.Entry<String, String>> it = copy.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                String formDataKey = entry.getKey();

                if (key.equals(formDataKey)) {
                    value = copy.get(formDataKey);
                    it.remove();
                }
            }

            paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));

        }

        //add all key value pairs remaining from the provided formData
        for (String formDataKey : copy.keySet()) {
            paramList.add(formDataKey + "=" + URLEncoder.encode(copy.get(formDataKey), "UTF-8"));
        }

        // build parameters list
        StringBuilder result = new StringBuilder();
        for (String param : paramList) {
            if (result.length() == 0) {
                result.append(param);
            } else {
                result.append("&" + param);
            }
        }
        return result.toString();
    }

    private String sendPost(Context context, String url, String postParams) throws Exception {
        Log.d(TAG, "Sending 'POST' request to URL : " + url);
        URL obj = new URL(url);
        conn = (HttpURLConnection) obj.openConnection();

        // Acts like a browser
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Language", "en-US");
        conn.setRequestProperty("Host", getHost(context));
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setRequestProperty("Accept",
                "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        for (String cookie : this.cookies) {
            conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
        }
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Referer", "http://localhost/login");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));
        String encodedAuthorizedUser = getAuthantication(getUsername(context), getPassword(context));
        conn.setRequestProperty("Authorization", "Basic " + encodedAuthorizedUser);

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

    private String getProtocol(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String protocol = prefs.getString(context.getString(R.string.pref_key_snipeit_3_protocol),
                context.getString(R.string.pref_default_snipeit_3_protocol));
        return protocol;
    }

    private String getHost(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String host = prefs.getString(context.getString(R.string.pref_key_snipeit_3_host), "");
        return host;
    }

    private int getPort(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String portString = prefs.getString(context.getString(R.string.pref_key_snipeit_3_port),
                context.getString(R.string.pref_default_snipeit_3_port));
        int port = Integer.valueOf(portString);
        return port;
    }

    public String getAuthantication(String username, String password) {
        String auth = new String(Base64.encode(username + ":" + password));
        return auth;
    }

    private String getUsername(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String username = prefs.getString(context.getString(R.string.pref_key_snipeit_3_username),
                "");
        return username;
    }

    private String getPassword(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String password = prefs.getString(context.getString(R.string.pref_key_snipeit_3_password),
                "");

        return password;
    }

    private void loginIfNeeded(Context context) throws Exception {
        boolean loginRequired = true;


        //try to pull the expiration time from the cookies to see if the session is still valid
        String session = null;
        List<String> cookies = getCookies();
        if (cookies != null) {
            for (String cookie : getCookies()) {
                List<HttpCookie> c = HttpCookie.parse(cookie);

                if (cookie.startsWith("snipeit_session")) {
                    session = cookie;

                    String[] sessionParts = session.split(";");
                    for (String sessionPart : sessionParts) {
                        //remove leading spaces
                        sessionPart = StringUtils.removeStart(sessionPart, " ");

                        if (sessionPart.startsWith("expires=")) {
                            String[] expirationParts = sessionPart.split("=");
                            String expiration = expirationParts[1];
                            Log.d(TAG, "Found login session expiration of " + expiration);
                            //convert the expiration string into a Java date

                            SimpleDateFormat format = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z");

                            Date date = new Date();
                            date = format.parse(expiration);

                            long expireTimestamp = date.getTime();
                            long now = System.currentTimeMillis();

                            SimpleDateFormat df = new SimpleDateFormat();
                            Date d = new Date();
                            d.setTime(expireTimestamp);

                            Log.d(TAG, "Session expires " + df.format(d));
                            d.setTime(now);
                            Log.d(TAG, "Currently " + df.format(d));

                            if (now < expireTimestamp) {
                                Log.d(TAG, "Session is still valid");
                                loginRequired = false;
                            } else {
                                Log.d(TAG, "Login required");
                            }

                            break;
                        }
                    }

                }
            }
        }

        if (loginRequired) {
            login(context);
        }
    }

    private void login(Context context) throws Exception {
        String loginUrl = "/login";
        sendMessageBroadcast(context, "Logging in");
        // make sure cookies are turn on
        CookieHandler.setDefault(new CookieManager());

        // 1. Send a "GET" request, so that you can extract the form's data.
        String url = getUrl(context, loginUrl);
        String page = getPageContent(url);
        Map<String, String> loginMap = new HashMap<>();
        loginMap.put("username", getUsername(context));
        loginMap.put("password", getPassword(context));
        String postParams = getFormParams(page, loginMap);

        // 2. Construct above post's content and then send a POST request for
        // authentication
        sendPost(context, getUrl(context, loginUrl), postParams);
    }
}
