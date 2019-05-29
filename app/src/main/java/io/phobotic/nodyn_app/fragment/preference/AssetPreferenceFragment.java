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

package io.phobotic.nodyn_app.fragment.preference;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Status;

/**
 * Created by Jonathan Nelson on 9/11/17.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AssetPreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = AssetPreferenceFragment.class.getSimpleName();
    private SharedPreferences prefs;
    private Database db;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_assets, rootKey);
        setHasOptionsMenu(true);


        db = Database.getInstance(getActivity());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        initListeners();
        initPreferences();
    }

    private void initListeners() {
        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        Preference visibleStatuses = findPreference(getString(
                R.string.pref_key_asset_status_selected_statuses));
        visibleStatuses.setOnPreferenceChangeListener(PreferenceListeners.statusChangeListener);

        //set the summary now
        PreferenceListeners.statusChangeListener.onPreferenceChange(visibleStatuses,
                prefs.getStringSet(visibleStatuses.getKey(), new HashSet<String>()));

    }

    private void initPreferences() {
        initVisibleStatusesSelect();
    }

    private void initVisibleStatusesSelect() {
        Set<String> chosenStatuses = prefs.getStringSet(getString(
                R.string.pref_key_asset_status_selected_statuses), null);
        Log.d(TAG, "visible statuses: " + chosenStatuses);

        List<Status> statuses = db.getStatuses();
        //sort the status alphabetically so the list stays in the same general order after every sync
        Collections.sort(statuses, new Comparator<Status>() {
            @Override
            public int compare(Status o1, Status o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        String[] statusNames = new String[statuses.size()];
        String[] statusValues = new String[statuses.size()];

        for (int i = 0; i < statuses.size(); i++) {
            Status status = statuses.get(i);
            String name = status.getName();
            String value = String.valueOf(status.getId());

            statusNames[i] = name;
            statusValues[i] = value;
        }

        MultiSelectListPreference statusSelect = (MultiSelectListPreference) findPreference(
                getString(R.string.pref_key_asset_status_selected_statuses));
        statusSelect.setEntries(statusNames);
        statusSelect.setEntryValues(statusValues);

    }


}
