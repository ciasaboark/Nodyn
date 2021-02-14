/*
 * Copyright (c) 2020 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.database.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.UserNotAuthenticatedException;
import android.util.Log;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.FullDataModel;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.database.sync.Action;

/**
 * Created by Jonathan Nelson on 11/12/20.
 */
public class FilterHelper {
    private static final String TAG = FilterHelper.class.getSimpleName();

    /**
     * Filter the list of action items _in_place_.  Actions will be removed from the list if
     * the user does not belong to a selected company and the asset does not belong to a
     * selected company or model
     * @param context
     * @param actions
     */
    public static void filterActions(Context context, List<Action> actions) {
        boolean syncAllCompanies = isAllCompaniesAllowed(context);
        boolean syncAllModels = isAllModelsAllowed(context);

        if (actions == null || actions.size() == 0) {
            Log.d(TAG, "Action list null or empty, skipping action filter");
            return;
        }

        if (syncAllCompanies && syncAllModels) {
            Log.d(TAG, "All models and companies are allowed. Skipping action filter");
            return;
        }

        Set<String> allowedCompanies = getSelectedCompanies(context);
        Set<String> allowedModels = getSelectedModels(context);
        Database db = Database.getInstance(context);
        Iterator<Action> it = actions.iterator();
        while (it.hasNext()) {
            Action action = it.next();
            boolean isActionUserAllowed = isActionUserAllowed(context, db, action, syncAllCompanies, allowedCompanies);
            boolean isActionAssetAllowed = isActionAssetAllowed(context, db, action, syncAllCompanies, allowedCompanies, syncAllModels, allowedModels);
            if (!isActionAssetAllowed || !isActionUserAllowed) {
                it.remove();
            }
        }
    }

    private static boolean isActionAssetAllowed(Context context, Database db, Action action,
                        boolean syncAllCompanies, Set<String> allowedCompanies,
                        boolean syncAllModels, Set<String> allowedModels) {
        boolean isAssetAllowed = false;
        try {
            Asset a = db.findAssetByID(action.getAssetID());
            boolean isAllowedByCompany = syncAllCompanies || allowedCompanies.contains(String.valueOf(a.getCompanyID()));
            boolean isAllowedByModel = syncAllModels || allowedModels.contains(String.valueOf(a.getModelID()));
            isAssetAllowed = isAllowedByCompany && isAllowedByModel;
        } catch (AssetNotFoundException e) {
            Log.e(TAG, String.format("Unable to find asset with id %d. Assuming action should be filtered", action.getAssetID()));
        }

        return isAssetAllowed;
    }

    private static boolean isActionUserAllowed(Context context, Database db, Action action, boolean syncAllCompanies, Set<String> allowedCompanies) {
        boolean isUserAllowed = false;
        try {
            User u = db.findUserByID(action.getUserID());
            if (syncAllCompanies || allowedCompanies.contains(String.valueOf(u.getCompanyID()))) {
                isUserAllowed = true;
            }
        } catch (UserNotFoundException e) {
            Log.e(TAG, String.format("Unable to find user with id %d. Assuming action should be filtered", action.getUserID()));
        }

        return isUserAllowed;
    }

    /**
     * Filter the list of assets _in_place_ so that only assets in the selected model list remain.
     * @parm context
     * @param model
     */
    public static void filterModels(Context context, List<Asset> assets) {
        if (assets == null || assets.size() == 0) {
            Log.d(TAG, "Null or empty asset list. Skipping model filter");
            return;
        }

        boolean isAllModelsAllowed = isAllModelsAllowed(context);

        if (isAllModelsAllowed) {
            Log.d(TAG, "All models are allowed.  Skipping filter.");
        } else {
            Set<String> chosenModels = getSelectedModels(context);

            Log.d(TAG, String.format("Filtering asset list to selected models: %s", chosenModels.toString()));

            int beforeFilterCount = assets.size();

            //the sync adapter may not have been able to filter the asset list by model
            Iterator<Asset> it = assets.iterator();

            while (it.hasNext()) {
                Asset a = it.next();
                String modelId = String.valueOf(a.getModelID());
                if (!chosenModels.contains(modelId)) {
                    it.remove();
                }

            }

            Log.d(TAG, String.format("Before model filter: %d assets. After filter %d assets", beforeFilterCount, assets.size()));
        }
    }

    private static Set<String> getSelectedModels(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getStringSet(context.getString(
                R.string.pref_key_check_out_models), new HashSet<String>());
    }

    private static boolean isAllModelsAllowed(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_key_check_out_all_models),
                    Boolean.parseBoolean(context.getString(R.string.pref_default_check_out_all_models)));
    }

    /**
     * Filter the list of assets _in_place_ so that only assets belonging to the selected company remain.
     * @parm context
     * @param model
     */
    public static void filterCompanies(Context context, List<Asset> assets) {
        if (assets == null || assets.size() == 0) {
            Log.d(TAG, "Null or empty asset list. Skipping company filter");
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        boolean isAllCompaniesAllowed = isAllCompaniesAllowed(context);

        if (isAllCompaniesAllowed) {
            Log.d(TAG, "All companies are allowed.  Skipping filter.");
        } else {
            Set<String> chosenCompanies = getSelectedCompanies(context);
            Log.d(TAG, String.format("Filtering asset list to selected companies: %s", chosenCompanies.toString()));

            int beforeFilterCount = assets.size();

            Iterator<Asset> it = assets.iterator();

            while (it.hasNext()) {
                Asset a = it.next();
                String companyID = String.valueOf(a.getCompanyID());
                if (!chosenCompanies.contains(companyID)) {
                    it.remove();
                }
            }

            Log.d(TAG, String.format("Before company filter: %d assets. After filter %d assets", beforeFilterCount, assets.size()));
        }
    }

    private static Set<String> getSelectedCompanies(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getStringSet(context.getString(
                R.string.pref_key_sync_companies), new HashSet<String>());
    }

    private static boolean isAllCompaniesAllowed(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_key_sync_all_companies),
                    Boolean.parseBoolean(context.getString(R.string.pref_default_sync_all_companies)));
    }
}
