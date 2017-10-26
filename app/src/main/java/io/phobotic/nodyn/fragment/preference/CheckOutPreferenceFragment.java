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

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.Verifiable;
import io.phobotic.nodyn.database.model.Group;
import io.phobotic.nodyn.database.model.Model;
import io.phobotic.nodyn.database.model.User;

/**
 * Created by Jonathan Nelson on 8/23/17.
 */

public class CheckOutPreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = CheckOutPreferenceFragment.class.getSimpleName();
    private SharedPreferences prefs;
    private Database db;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_check_out, rootKey);
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
        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_check_out_scan_field)));

        Preference allowedGroups = findPreference(getString(R.string.pref_key_check_out_authorization_groups));
        allowedGroups.setOnPreferenceChangeListener(PreferenceListeners.groupsChangeListener);
        PreferenceListeners.groupsChangeListener.onPreferenceChange(allowedGroups,
                prefs.getStringSet(allowedGroups.getKey(), new HashSet<String>()));

        Preference allowedModels = findPreference(getString(R.string.pref_key_check_out_models));
        allowedModels.setOnPreferenceChangeListener(PreferenceListeners.modelsChangeListener);
        PreferenceListeners.modelsChangeListener.onPreferenceChange(allowedModels,
                prefs.getStringSet(allowedModels.getKey(), new HashSet<String>()));

        Preference eulaPreference = findPreference(getString(R.string.pref_key_check_out_eula));
        eulaPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                View v = getLayoutInflater(null).inflate(R.layout.view_eula_text, null);
                final EditText input = (EditText) v.findViewById(R.id.input);
                TextView info = (TextView) v.findViewById(R.id.info);
                info.setMovementMethod(new LinkMovementMethod());
                String curEula = prefs.getString(getString(R.string.pref_key_check_out_eula),
                        getString(R.string.pref_default_check_out_eula));
                input.setText(curEula);

                final AlertDialog d = new AlertDialog.Builder(getContext())
                        .setTitle("Asset Checkout EULA")
                        .setView(v)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String eulaText = input.getText().toString();
                                if (eulaText.length() == 0) {
                                    eulaText = null;
                                }

                                prefs.edit()
                                        .putString(getString(R.string.pref_key_check_out_eula), eulaText)
                                        .apply();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //nothing to do here
                            }
                        })
                        .create();

                d.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        final Button positveButton = d.getButton(DialogInterface.BUTTON_POSITIVE);
                        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                            @Override
                            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                if (actionId == EditorInfo.IME_ACTION_DONE) {
                                    positveButton.callOnClick();
                                    return true;
                                }
                                return false;
                            }
                        });

                        d.getWindow().setSoftInputMode(
                                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                    }
                });

                d.show();

                return true;
            }
        });
    }

    private void initPreferences() {
        initGroupSelect();
        initScanFieldSelect();
        initModelSelect();
    }

    private void initGroupSelect() {
        Set<String> chosenGroups = prefs.getStringSet(
                getString(R.string.pref_key_check_out_authorization_groups), null);
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

        MultiSelectListPreference p = (MultiSelectListPreference) findPreference(
                getString(R.string.pref_key_check_out_authorization_groups));
        p.setEntries(groupNames);
        p.setEntryValues(groupValues);
    }

    private void initScanFieldSelect() {
        ListPreference lp = (ListPreference) findPreference(getResources()
                .getString(R.string.pref_key_check_out_scan_field));

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

    private void initModelSelect() {
        Set<String> chosenModels = prefs.getStringSet(getString(
                R.string.pref_key_check_out_models), null);
        Log.d(TAG, "chosen models: " + String.valueOf(chosenModels));

        List<Model> models = db.getModels();
        String[] modelNames = new String[models.size()];
        String[] modelValues = new String[models.size()];

        for (int i = 0; i < models.size(); i++) {
            Model model = models.get(i);
            String name = model.getName();
            String value = String.valueOf(model.getId());

            modelNames[i] = name;
            modelValues[i] = value;
        }

        MultiSelectListPreference modelSelect = (MultiSelectListPreference) findPreference(
                getString(R.string.pref_key_check_out_models));
        modelSelect.setEntries(modelNames);
        modelSelect.setEntryValues(modelValues);
    }

    private void showNoBackEndError() {
        AlertDialog d = new AlertDialog.Builder(getContext())
                .setTitle("No Backend Configured")
                .setMessage("No backend has been configured yet.  This setting will become available after the backend service is configured and after a successfull sync")
                .setPositiveButton(android.R.string.ok, null)
                .create();
        d.show();
    }

    private void showNotSyncedError() {
        AlertDialog d = new AlertDialog.Builder(getContext())
                .setTitle("Not Yet Synced")
                .setMessage("This setting will become avaiable after a successfull sync with the backend service")
                .setPositiveButton(android.R.string.ok, null)
                .create();
        d.show();
    }
}
