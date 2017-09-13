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

package io.phobotic.nodyn.fragment.preference;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.activity.SettingsActivity;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.model.Status;

/**
 * Created by Jonathan Nelson on 9/11/17.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AssetPreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = AssetPreferenceFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_assets, rootKey);
        setHasOptionsMenu(true);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        SettingsActivity.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_asset_status_selected_statuses)));

        initStatusesSelect();
    }

    private void initStatusesSelect() {
        Database db = Database.getInstance(getContext());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        List<Status> statuses = db.getStatuses();
        Set<String> chosenStatuses = prefs.getStringSet(getString(
                R.string.pref_key_asset_status_selected_statuses), null);
        Log.d(TAG, "chosen statuses: " + String.valueOf(chosenStatuses));

        List<String> statusList = new ArrayList<>();
        for (Status status : statuses) {
            statusList.add(status.getName());
        }

        //sort the status alphabetically so the list stays in the same general order after every sync
        Collections.sort(statusList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        String[] statusNames = statusList.toArray(new String[]{});

        MultiSelectListPreference modelSelect = (MultiSelectListPreference) findPreference(
                getString(R.string.pref_key_asset_status_selected_statuses));
        modelSelect.setEntries(statusNames);
        modelSelect.setEntryValues(statusNames);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
