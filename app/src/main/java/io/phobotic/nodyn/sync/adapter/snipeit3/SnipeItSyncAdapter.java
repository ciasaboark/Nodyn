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

package io.phobotic.nodyn.sync.adapter.snipeit3;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.Category;
import io.phobotic.nodyn.database.model.FullDataModel;
import io.phobotic.nodyn.database.model.Group;
import io.phobotic.nodyn.database.model.Model;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.sync.CheckinException;
import io.phobotic.nodyn.sync.CheckoutException;
import io.phobotic.nodyn.sync.HtmlEncoded;
import io.phobotic.nodyn.sync.SyncException;
import io.phobotic.nodyn.sync.adapter.SyncAdapter;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public class SnipeItSyncAdapter extends SyncAdapter {
    private static final String TAG = SnipeItSyncAdapter.class.getSimpleName();

    private List<String> cookies;
    private HttpURLConnection conn;

    private final String USER_AGENT = "Mozilla/5.0";

    private String protocol = "http://";
    private String host = "192.168.0.128";

    private Gson gson = new Gson();

    private String getUrl(String url) {
        return protocol + host + url;
    }

    private void convertHtmlFields(Object obj) throws IllegalAccessException {
        List<Field> htmlFields = FieldUtils.getFieldsListWithAnnotation(obj.getClass(), HtmlEncoded.class);

        for (Field field: htmlFields) {
            if (!field.getType().equals(String.class)) {
                System.err.println("Field: " + field.getName() + " is marked as @HtmlEncoded, but is not of type String");
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

    private String sendPost(String url, String postParams) throws Exception {
        Log.d(TAG, "Sending 'POST' request to URL : " + url);
        URL obj = new URL(url);
        conn = (HttpURLConnection) obj.openConnection();

        // Acts like a browser
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Host", "accounts.google.com");
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

        conn.setDoOutput(true);
        conn.setDoInput(true);

        // Send post request
        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
        wr.writeBytes(postParams);
        wr.flush();
        wr.close();

        int responseCode = conn.getResponseCode();

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

    public String getFormParams(String html, Map<String, String> formData)
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
        for (String formDataKey: copy.keySet()) {
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

    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

    @Override
    public List<Asset> fetchAssets() throws SyncException {
        String assetsUrl = "/api/hardware/list?limit=9999&offset=0";
        try {
            loginIfNeeded();
            String assetResult = getPageContent(getUrl(assetsUrl));
            AssetResponse assetsResponse = gson.fromJson(assetResult, AssetResponse.class);
            List<Asset> assets = assetsResponse.getAssets();

            //the JSON returned by snipeit will have some properties wrapped in HTML.
            for (Asset asset: assets) {
                convertHtmlFields(asset);
            }

            return assets;
        } catch (Exception e) {
            String message = "Unable to login to service: " + e.getMessage();
            Log.e(TAG, message);
            throw new SyncException(message);
        }
    }

    private void loginIfNeeded() throws Exception {
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
                    for (String sessionPart: sessionParts) {
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
          login();
      }
    }

    private void login() throws Exception {
        String loginUrl = "/login";

        // make sure cookies are turn on
        CookieHandler.setDefault(new CookieManager());

        // 1. Send a "GET" request, so that you can extract the form's data.
        String page = getPageContent(getUrl(loginUrl));
        Map<String, String> loginMap = new HashMap<>();
        loginMap.put("username", "086749");
        loginMap.put("password", "shiver");
        String postParams = getFormParams(page, loginMap);

        // 2. Construct above post's content and then send a POST request for
        // authentication
        sendPost(getUrl(loginUrl), postParams);
    }

    @Override
    public List<Model> fetchModels() throws SyncException {
        String modelsUrl = "/api/models/list?limit=9999&offset=0";

        try {
            loginIfNeeded();
            String modelResult = getPageContent(getUrl(modelsUrl));
            ModelResponse modelResponse = gson.fromJson(modelResult, ModelResponse.class);
            List<Model> models = modelResponse.getModels();

            //the JSON returned by snipeit will have some properties wrapped in HTML.
            for (Model model: models) {
                convertHtmlFields(model);
            }

            return models;
        } catch (Exception e) {
            e.printStackTrace();
            String message = "Unable to fetch models: " + e.getMessage();
            throw new SyncException(message);
        }
    }

    @Override
    public List<User> fetchUsers() throws SyncException {
        String usersUrl = "/api/users/list?limit=9999&offset=0";

        try {
            loginIfNeeded();
            String usersResult = getPageContent(getUrl(usersUrl));
            UserResponse userResponse = gson.fromJson(usersResult, UserResponse.class);
            List<User> users = userResponse.getUsers();

            //the JSON returned by snipeit will have some properties wrapped in HTML.
            for (User user: users) {
                convertHtmlFields(user);
            }

            return users;

        } catch (Exception e) {
            e.printStackTrace();
            String message = "Unable to fetch users: " + e.getMessage();
            throw new SyncException(message);
        }
    }


    @Override
    public List<Category> fetchCategories() throws SyncException {
        String categoriesURL = "/api/categories/list?limit=20&offset=0";

        try {
            loginIfNeeded();
            String categoriesResult = getPageContent(getUrl(categoriesURL));
            CategoryResponse catoriesResponse = gson.fromJson(categoriesResult, CategoryResponse.class);
            List<Category> categories = catoriesResponse.getCategories();

            //the JSON returned by snipeit will have some properties wrapped in HTML.
            for (Category category: categories) {
                convertHtmlFields(category);
            }

            return categories;

        } catch (Exception e) {
            e.printStackTrace();
            String message = "Unable to fetch users: " + e.getMessage();
            throw new SyncException(message);
        }
    }

    @Override
    public List<Group> fetchGroups() throws SyncException {
        String groupsUrl = "/api/groups/list?limit=9999&offset=0";

        try {
            loginIfNeeded();
            String groupsResult = getPageContent(getUrl(groupsUrl));
            GroupResponse groupResponse = gson.fromJson(groupsResult, GroupResponse.class);
            List<Group> groups = groupResponse.getGroups();

            //the JSON returned by snipeit will have some properties wrapped in HTML.
            for (Group group: groups) {
                convertHtmlFields(group);
            }

            return groups;

        } catch (Exception e) {
            e.printStackTrace();
            String message = "Unable to fetch users: " + e.getMessage();
            throw new SyncException(message);
        }
    }


    @Override
    public FullDataModel fetchFullModel() throws SyncException {
        try {
            List<Asset> assets = fetchAssets();
            List<User> users = fetchUsers();
            List<Model> models = fetchModels();

            Map<String, Object> map = new HashMap<>();
            map.put("users", users);
            map.put("models", models);
            map.put("assets", assets);

            Gson gsonPrinter = new GsonBuilder().setPrettyPrinting().create();
            String jsonString = gsonPrinter.toJson(map);
            Log.d(TAG, jsonString);

            FullDataModel model = new FullDataModel();
            model.setAssets(assets);
            model.setUsers(users);
            model.setModels(models);

            return model;
        } catch (Exception e) {
            throw new SyncException();
        }
    }

    @Override
    public void checkoutAssetTo(int assetID, int userID, @Nullable String checkoutDate,
                                @Nullable String expectedCheckin, @Nullable String notes)
            throws CheckoutException {
        String checkoutFormUrl = "/hardware/" + assetID + "/checkout";
        String checkoutPostUrl = "/hardware/" + assetID + "/checkout";

        Log.d(TAG, "Attempting to check out asset " + assetID + " to user " + userID);

        try {
            String checkoutPage = getPageContent(getUrl(checkoutFormUrl));
            Map<String, String> checkoutMap = new HashMap();
            checkoutMap.put("name", "07BTR137");
            checkoutMap.put("assigned_to", "1");
            checkoutMap.put("checkout_at", "2017-07-10 15:04:43");
            checkoutMap.put("expected_checkin", "2017-07-11 15:04:43");
            checkoutMap.put("note", "api based checkout");

            String checkoutPostParams = getFormParams(checkoutPage, checkoutMap);

            String response = sendPost(getUrl(checkoutPostUrl), checkoutPostParams);
            Log.d(TAG, "Checkout POST response:");
            Log.d(TAG, response);

            if (response.contains("Asset checked out successfully")) {
                Log.d(TAG, "Asset was checked out successfully");
            } else {
                Log.d(TAG, "Error checking out asset");
            }
        } catch (Exception e) {
            throw new CheckoutException();
        }
    }

    @Override
    public void checkinAsset(int assetID, @Nullable String checkinDate, @Nullable String notes) throws CheckinException {

    }
}
