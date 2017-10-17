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

package io.phobotic.nodyn.list;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.fragment.SimplifiedAsset;

/**
 * Created by Jonathan Nelson on 9/17/17.
 */

public class AssetListFilterer {
    private static final String TAG = AssetListFilterer.class.getSimpleName();
    private Context context;
    private List<SimplifiedAsset> assets;

    public AssetListFilterer(@NotNull Context context, @NotNull List<SimplifiedAsset> assets) {
        this.context = context;
        this.assets = assets;
    }

    public void filterList(String filter, boolean useDefaultFilter, @NotNull AssetListFilterListener listener) {
        FilterListTask task = new FilterListTask(context, useDefaultFilter, filter, listener);
        task.execute();
    }


    public interface AssetListFilterListener {
        void onAssetListBeginFilter();

        void onAssetListFinishFilter(List<SimplifiedAsset> filteredAssets);
    }


    public class FilterListTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        private boolean useDefaultFilter;
        private String filterText;
        private AssetListFilterListener listener;

        public FilterListTask(Context context, boolean useDefaultFilter, String filterText,
                              AssetListFilterListener listener) {
            this.context = context;
            this.useDefaultFilter = useDefaultFilter;
            this.filterText = filterText;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (listener != null) {
                listener.onAssetListBeginFilter();
            }

            List<SimplifiedAsset> assetList = new ArrayList<>();
            assetList.addAll(assets);

            //if we were given some filter text use that
            if (filterText != null && !filterText.equals("")) {
                assetList = filterList(assetList, filterText);
                notifyFilterFinished(assetList);
            } else {
                //otherwise we need to either apply the default filter or just return the same list we
                //+ were given
                if (!useDefaultFilter) {
                    notifyFilterFinished(assets);
                } else {
                    assetList = applyDefaultFilter(context, assetList);
                    notifyFilterFinished(assetList);
                }
            }

            notifyFilterFinished(assetList);

            return null;
        }

        private List<SimplifiedAsset> filterList(@NotNull List<SimplifiedAsset> assets,
                                                 @NotNull String filter) {
            Log.d(TAG, "Filtering asset list by term '" + filter + "'");
            List<SimplifiedAsset> filteredList = new ArrayList<>();

            for (SimplifiedAsset asset : assets) {
                if (assetMatchesFilter(asset, filter)) {
                    filteredList.add(asset);
                }
            }

            return filteredList;
        }

        private void notifyFilterFinished(List<SimplifiedAsset> assetList) {
            if (listener != null) {
                listener.onAssetListFinishFilter(assetList);
            }
        }

        //filter the default asset listview to include only the status selected in settings
        private List<SimplifiedAsset> applyDefaultFilter(Context context, List<SimplifiedAsset> assets) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean showAll = prefs.getBoolean(context.getString(R.string.pref_key_asset_status_show_all),
                    Boolean.valueOf(context.getString(R.string.pref_default_asset_status_show_all)));
            if (showAll) {
                return assets;
            } else {
                List<SimplifiedAsset> filteredList = new ArrayList<>();
                Set<String> chosenStatuses = prefs.getStringSet(context.getString(
                        R.string.pref_key_asset_status_selected_statuses), new HashSet<String>());
                for (SimplifiedAsset asset : assets) {
                    if (chosenStatuses.contains(String.valueOf(asset.getStatusID()))) {
                        filteredList.add(asset);
                    }
                }

                return filteredList;
            }
        }

        private boolean assetMatchesFilter(@NotNull SimplifiedAsset asset, @NotNull String filter) {
            boolean filterMatches = false;
            //do all the matching case insensitive
            filter = filter.toUpperCase();

            if (asset.getName().toUpperCase().contains(filter)) {
                return true;
            } else if (asset.getStatusName().toUpperCase().contains(filter)) {
                return true;
            } else if (asset.getAssignedToName().toUpperCase().contains(filter)) {
                return true;
            } else if (asset.getModelName().toUpperCase().contains(filter)) {
                return true;
            } else if (asset.getManufacturerName().toUpperCase().contains(filter)) {
                return true;
            } else if (asset.getTag().toUpperCase().contains(filter)) {
                return true;
            } else if (asset.getCategoryName().toUpperCase().contains(filter)) {
                return true;
            } else if (asset.getCompanyName().contains(filter)) {
                return true;
            } else if (String.valueOf(asset.getId()).contains(filter)) {
                return true;
            } else if (asset.getSerial().toUpperCase().contains(filter)) {
                return true;
            } else if (asset.getNotes().toUpperCase().contains(filter)) {
                return true;
            }

            return filterMatches;
        }
    }
}
