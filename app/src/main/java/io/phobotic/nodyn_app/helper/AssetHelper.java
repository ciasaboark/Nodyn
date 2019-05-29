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

package io.phobotic.nodyn_app.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.crashlytics.android.Crashlytics;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.model.Asset;

/**
 * Created by Jonathan Nelson on 2019-05-12.
 */
public class AssetHelper {

    public boolean isAssetUsable(Context context, Asset asset) {
        return modelCanBeCheckedOut(context, asset.getModelID()) && isAssetStatusValid(context, asset);
    }

    /**
     * Return true if all asset models are allowed to be checked out, or if this asset model is
     * one of the chosen models that can be checked out
     *
     * @param asset
     * @return
     */
    public boolean modelCanBeCheckedOut(Context context, int modelID) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean modelValid = false;

        boolean allModelsValid = prefs.getBoolean(context.getString(
                R.string.pref_key_check_out_all_models), Boolean.parseBoolean(
                context.getString(R.string.pref_default_check_out_all_models)));
        if (allModelsValid) {
            modelValid = true;
        } else {
            Set<Integer> allowedModelIDs = getAllowedModelIDs(context);
            if (allowedModelIDs.contains(modelID)) {
                modelValid = true;
            }
        }

        return modelValid;
    }

    /**
     * Checks to see if the given asset's status is valid for use on this device
     *
     * @param asset
     * @return true only if this asset's status will allow it to be checked out or checked in
     */
    public boolean isAssetStatusValid(Context context, Asset asset) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean statusValid = false;

        boolean allStatusesAllowed = prefs.getBoolean(context.getString(
                R.string.pref_key_asset_status_allow_all), Boolean.parseBoolean(
                context.getString(R.string.pref_default_asset_status_allow_all)));
        if (allStatusesAllowed) {
            statusValid = true;
        } else {
            Set<String> allowedStatusIDs = getAllowedStatusIDs(context);
            if (allowedStatusIDs.contains(String.valueOf(asset.getStatusID()))) {
                statusValid = true;
            }
        }

        return statusValid;
    }

    public Set<Integer> getAllowedModelIDs(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Set<String> stringSet = prefs.getStringSet(context.getString(R.string.pref_key_check_out_models),
                new HashSet<String>());
        Set<Integer> modelIDs = new HashSet<>();
        for (String s : stringSet) {
            try {
                modelIDs.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                Crashlytics.logException(e);
            }
        }

        return modelIDs;
    }

    @NonNull
    public Set<String> getAllowedStatusIDs(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getStringSet(context.getString(
                R.string.pref_key_asset_status_allowed_statuses), new HashSet<String>());
    }
}
