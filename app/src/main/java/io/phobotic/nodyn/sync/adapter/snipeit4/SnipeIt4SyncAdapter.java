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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import io.phobotic.nodyn.database.model.Action;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.AssetHistoryRecord;
import io.phobotic.nodyn.database.model.Category;
import io.phobotic.nodyn.database.model.FullDataModel;
import io.phobotic.nodyn.database.model.Group;
import io.phobotic.nodyn.database.model.MaintenanceRecord;
import io.phobotic.nodyn.database.model.Model;
import io.phobotic.nodyn.database.model.Status;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.database.model.UserHistoryRecord;
import io.phobotic.nodyn.sync.CheckinException;
import io.phobotic.nodyn.sync.CheckoutException;
import io.phobotic.nodyn.sync.SyncErrorListener;
import io.phobotic.nodyn.sync.adapter.SyncAdapter;
import io.phobotic.nodyn.sync.adapter.SyncException;
import io.phobotic.nodyn.sync.adapter.SyncNotSupportedException;
import io.phobotic.nodyn.sync.adapter.snipeit3.response.AssetResponse;

/**
 * Created by Jonathan Nelson on 9/12/17.
 */

public class SnipeIt4SyncAdapter implements SyncAdapter {
    public static final String TAG = SnipeIt4SyncAdapter.class.getSimpleName();
    private static final String API_KEY = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjhkMjJlOTk1NDkwNDlkNmUyNmQzYTEzNzQ0NTFiZDU0ODhlZDQ2ZjE5MDI0NmM0Y2IwYmRmOGI4NzU0MjQ2YTUwMDE1NzdiNmQ2NTQ3YTYzIn0.eyJhdWQiOiIyIiwianRpIjoiOGQyMmU5OTU0OTA0OWQ2ZTI2ZDNhMTM3NDQ1MWJkNTQ4OGVkNDZmMTkwMjQ2YzRjYjBiZGY4Yjg3NTQyNDZhNTAwMTU3N2I2ZDY1NDdhNjMiLCJpYXQiOjE1MDUyNTc4MzcsIm5iZiI6MTUwNTI1NzgzNywiZXhwIjoxODIwNzkwNjM3LCJzdWIiOiIxIiwic2NvcGVzIjpbXX0.o22lbB8gZsJQZkuh01riSMWMeFkWRUkPyrq93WlG_GmFVcufYWaXVrbTBSeFAnyceZXzE2v3H1XJaq-7GZciEXWBfXUXKmGnHHuWDjDhg4UapizxGh241xSQ_nUzQp0afzQV-gJDaKAx8p0EtqageVKD8hPrcl5ZPpDMFK7YLfL6Eqq5UDYSeuntaqmA1aCHd86jWkzxcYzbjlGdDmHM8jV1MjPoLqbh0MEwZ-WfAkzAoLZ3Aml15dKgS_sWSBDJzrSc2wpEnwBu0yD1RMP1I9QpdU9yssZfCWf40XG5VwiDklAi_fG5hn_raqfX-txGvNBa-k1QsnYXrZsPH6Ee7KxfX2yT45q-Q5F90N-MXptac_xPWODR5rj2VRjTgZSNh-PVJOwrAarzjanMUWhiI9T5a8SubusHi8wiwGUgiYvFFA3QAbR3GaqEXTJffyLSCTMmRXkq0y403MV2izvNm7nfRCfrauDVM3AxYYPUclRNuspNBZm7jQ0oAB3U-Q0BMqZ-xR3QnmSh7kaMapi3VwPx2CI58T9MxxiPPtYLiqRsyJ6b_nTkEhFMbGGXkFhDdl1lfAUwm7DdhaVx1B2xQDXMCT3QPgdPBTQMwi7_9n2WYGaZ6-l5WzcVkZtP1idnTl8dykWVj7NKeA4ja-Ar2wBIUTvaeXzCw20AP_zkZQU";
    private Gson gson = new Gson();
    private HttpURLConnection conn;

    @Override
    public List<Asset> fetchAssets(Context context) throws SyncException {
        String assetsUrl = "/api/v1/hardware/";
        List<Asset> assets = null;

        try {
            String assetResult = getPageContent(getUrl(context, assetsUrl));
            AssetResponse assetResponse = gson.fromJson(assetResult, AssetResponse.class);
            assets = assetResponse.getAssets();
        } catch (Exception e) {
            throw new SyncException("Unable to fetch assets");
        }

        return assets;
    }

    private String getProtocol(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String protocol = prefs.getString("pref_protocol", "http://");
        return protocol;
    }

    private String getHost(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String host = prefs.getString("pref_host", "nohost");
        return host;
    }

    private int getPort(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String portString = prefs.getString("pref_port", "80");
        int port = Integer.valueOf(portString);
        return port;
    }

    private String getUrl(Context context, String url) {
        String completeUrl = getProtocol(context) + getHost(context) + ":" + getPort(context) + url;
        return completeUrl;
    }

    private String getPageContent(String url) throws Exception {

        URL obj = new URL(url);
        conn = (HttpURLConnection) obj.openConnection();

        // default is GET
        conn.setRequestMethod("GET");

        conn.setUseCaches(false);
        conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
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

    @Override
    public List<Model> fetchModels(Context context) throws SyncException {
        return null;
    }

    @Override
    public List<User> fetchUsers(Context context) throws SyncException {
        return null;
    }

    @Override
    public List<Group> fetchGroups(Context context) throws SyncException {
        return null;
    }

    @Override
    public List<Category> fetchCategories(Context context) throws SyncException {
        return null;
    }

    @Override
    public List<Status> fetchStatuses(Context context) throws SyncException {
        return null;
    }

    @Override
    public FullDataModel fetchFullModel(Context context) throws SyncException {
        FullDataModel fullDataModel = new FullDataModel()
                .setAssets(fetchAssets(context))
                .setCategories(fetchCategories(context))
                .setGroups(fetchGroups(context))
                .setModels(fetchModels(context))
                .setUsers(fetchUsers(context))
                .setStatuses(fetchStatuses(context));

        return fullDataModel;
    }

    @Override
    public void checkoutAssetTo(Context context, int assetID, String assetTag, int userID, @Nullable Long checkout, @Nullable Long expectedCheckin, @Nullable String notes) throws CheckoutException {

    }

    @Override
    public void checkinAsset(Context context, int assetID, String assetTag, @Nullable Long checkinDate, @Nullable String notes) throws CheckinException {

    }

    @Override
    public void syncActionItems(Context context, SyncErrorListener listener) throws SyncException {

    }

    @Override
    public void markActionItemsSynced(Context context, List<Action> actions) {

    }

    @Override
    public List<MaintenanceRecord> getMaintenanceRecords(Context context, Asset asset) throws SyncException, SyncNotSupportedException {
        return null;
    }

    @Override
    public List<AssetHistoryRecord> getHistory(Context context, Asset asset) throws SyncException, SyncNotSupportedException {
        return null;
    }

    @Override
    public List<UserHistoryRecord> getHistory(Context context, User user) throws SyncException, SyncNotSupportedException {
        return null;
    }

    @Override
    public List<Asset> getAssets(Context context, User user) throws SyncException, SyncNotSupportedException {
        return null;
    }

    @Nullable
    @Override
    public DialogFragment getConfigurationDialogFragment(Context context) {
        return null;
    }
}
