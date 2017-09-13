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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.activity.SettingsActivity;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.Verifiable;
import io.phobotic.nodyn.database.model.Group;
import io.phobotic.nodyn.database.model.User;

/**
 * Created by Jonathan Nelson on 8/8/17.
 */

public class CheckInPreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = CheckInPreferenceFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_check_in, rootKey);
        setHasOptionsMenu(true);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
//        SettingsActivity.bindPreferenceSummaryToValue(findPreference("check_in_scan_group"));
        SettingsActivity.bindPreferenceSummaryToValue(findPreference("check_in_scan_field"));
        MultiSelectListPreference p = (MultiSelectListPreference) findPreference("check_in_scan_group");
        Database db = Database.getInstance(getContext());
        List<Group> groupList = db.getGroups();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Set<String> chosenGroups = prefs.getStringSet("check_in_scan_group", null);
        Log.d(TAG, "chosen groups: " + String.valueOf(chosenGroups));


        List<String> groupNamesList = new ArrayList<>();
        for (Group group : groupList) {
            groupNamesList.add(group.toString());
        }

        String[] groupNames = groupNamesList.toArray(new String[]{});

        p.setEntries(groupNames);
        p.setEntryValues(groupNames);

        ListPreference lp = (ListPreference) findPreference("check_in_scan_field");
        List<String> fieldStringList = new ArrayList<>();
        List<Field> fields = FieldUtils.getFieldsListWithAnnotation(User.class, Verifiable.class);

        for (Field field : fields) {
            String fieldValue = field.getAnnotation(Verifiable.class).value();
            fieldStringList.add(fieldValue);
        }

        String[] selectableFields = fieldStringList.toArray(new String[]{});
        lp.setEntries(selectableFields);
        lp.setEntryValues(selectableFields);
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
