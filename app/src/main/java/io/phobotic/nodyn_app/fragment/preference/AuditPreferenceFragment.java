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

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.activity.AuditDefinitionsActivity;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Group;
import io.phobotic.nodyn_app.preference.EmailRecipientsPreference;
import io.phobotic.nodyn_app.preference.EmailRecipientsPreferenceDialogFragmentCompat;

/**
 * Created by Jonathan Nelson on 12/21/17.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AuditPreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = AuditPreferenceFragment.class.getSimpleName();
    private Database db;
    private SharedPreferences prefs;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_audit, rootKey);
        setHasOptionsMenu(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        db = Database.getInstance(getContext());

        initListeners();
        initPreferences();
    }

    private void initListeners() {
        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        Preference allowedGroups = findPreference(getString(R.string.pref_key_audit_allowed_groups));
        allowedGroups.setOnPreferenceChangeListener(PreferenceListeners.groupsChangeListener);
        PreferenceListeners.groupsChangeListener.onPreferenceChange(allowedGroups,
                prefs.getStringSet(allowedGroups.getKey(), new HashSet<String>()));

        Preference recipientEmail = findPreference(getString(R.string.pref_key_audit_results_email));
        PreferenceListeners.bindPreferenceSummaryToValue(recipientEmail);
    }

    private void initPreferences() {
//        SwitchPreferenceCompat enableAuditsPreference = (SwitchPreferenceCompat) findPreference(
//                getString(R.string.pref_key_audit_enable_audits));
//        PreferenceListeners.bindPreferenceSummaryToValue(enableAuditsPreference);

        initGroupSelect();
        initDefiniedAudit();
    }

    private void initGroupSelect() {
        Set<String> chosenGroups = prefs.getStringSet(
                getString(R.string.pref_key_audit_allowed_groups), null);
        Log.d(TAG, "chosen audit groups: " + String.valueOf(chosenGroups));


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

        MultiSelectListPreference p = (MultiSelectListPreference) findPreference(
                getString(R.string.pref_key_audit_allowed_groups));
        p.setEntries(groupNames);
        p.setEntryValues(groupValues);
    }

    private void initDefiniedAudit() {
        Preference definedAuditPreference = findPreference(getString(R.string.pref_key_audit_define_audits));
        definedAuditPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getContext(), AuditDefinitionsActivity.class);
                getContext().startActivity(i);
                return false;
            }
        });
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Try if the preference is one of our custom Preferences
        DialogFragment dialogFragment = null;
        if (preference instanceof EmailRecipientsPreference) {
            // Create a new instance of EmailRecipientsPreferenceDialogFragmentCompat with the key of the related
            // Preference
            dialogFragment = EmailRecipientsPreferenceDialogFragmentCompat
                    .newInstance(preference.getKey());
        }

        // If it was one of our cutom Preferences, show its dialog
        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(),
                    "android.support.v7.preference" +
                            ".PreferenceFragment.DIALOG");
        }
        // Could not be handled here. Try with the super method.
        else {
            super.onDisplayPreferenceDialog(preference);
        }
    }
}