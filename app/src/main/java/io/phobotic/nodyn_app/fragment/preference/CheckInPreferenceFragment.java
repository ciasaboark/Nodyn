/*
 * Copyright (c) 2018 Jonathan Nelson <ciasaboark@gmail.com>
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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Group;

/**
 * Created by Jonathan Nelson on 8/8/17.
 */

public class CheckInPreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = CheckInPreferenceFragment.class.getSimpleName();
    private Database db;
    private SharedPreferences prefs;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_check_in, rootKey);
        setHasOptionsMenu(true);
        db = Database.getInstance(getContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        initListeners();
        initPreferences();
    }

    private void initListeners() {
        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        MultiSelectListPreference allowedGroups =
                (MultiSelectListPreference) findPreference(
                        getString(R.string.pref_key_check_in_authenticating_groups));
        allowedGroups.setOnPreferenceChangeListener(PreferenceListeners.groupsChangeListener);
        PreferenceListeners.groupsChangeListener.onPreferenceChange(allowedGroups,
                prefs.getStringSet(allowedGroups.getKey(), new HashSet<String>()));
    }

    private void initPreferences() {
        initGroupPreference();
    }

    private void initGroupPreference() {
        MultiSelectListPreference allowedGroups =
                (MultiSelectListPreference) findPreference(
                        getString(R.string.pref_key_check_in_authenticating_groups));
        Set<String> chosenGroups = prefs.getStringSet(
                getString(R.string.pref_key_check_in_authenticating_groups), null);
        Log.d(TAG, "chosen groups: " + String.valueOf(chosenGroups));


        List<Group> groupList = db.getGroups();
        String[] groupNames = new String[groupList.size()];
        String[] groupValues = new String[groupList.size()];

        for (int i = 0; i < groupList.size(); i++) {
            Group group = groupList.get(i);
            String name = group.getName();
            String value = String.valueOf(group.getId());

            groupNames[i] = name;
            groupValues[i] = value;
        }

        allowedGroups.setEntries(groupNames);
        allowedGroups.setEntryValues(groupValues);
    }
}
