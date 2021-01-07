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
import io.phobotic.nodyn_app.database.model.Company;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.helper.PreferenceHelper;

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

        PreferenceHelper.tintIcons(getContext(), getPreferenceScreen());
    }

    private void initListeners() {
        Preference allowedModels = findPreference(getString(R.string.pref_key_check_out_models));
        allowedModels.setOnPreferenceChangeListener(PreferenceListeners.modelsChangeListener);
        PreferenceListeners.modelsChangeListener.onPreferenceChange(allowedModels,
                prefs.getStringSet(allowedModels.getKey(), new HashSet<String>()));


        Preference allowedCompanies = findPreference(getString(R.string.pref_key_sync_companies));
        allowedCompanies.setOnPreferenceChangeListener(PreferenceListeners.companiesChangeListener);
        PreferenceListeners.companiesChangeListener.onPreferenceChange(allowedCompanies,
                prefs.getStringSet(allowedCompanies.getKey(), new HashSet<String>()));
    }

    private void initPreferences() {
        initModelSelect();
        initCompanySelect();
    }

    private void initModelSelect() {
        Set<String> chosenModels = prefs.getStringSet(getString(
                R.string.pref_key_check_out_models), null);
        Log.d(TAG, "chosen models: " + chosenModels);

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

    private void initCompanySelect() {
        Set<String> chosenCompany = prefs.getStringSet(getString(
                R.string.pref_key_sync_companies), null);
        Log.d(TAG, "chosen companies: " + chosenCompany);

        List<Company> companies = db.getCompanies();
        String[] companyNames = new String[companies.size()];
        String[] companyValues = new String[companies.size()];

        for (int i = 0; i < companies.size(); i++) {
            Company company = companies.get(i);
            String name = company.getName();
            String value = String.valueOf(company.getId());

            companyNames[i] = name;
            companyValues[i] = value;
        }

        MultiSelectListPreference companySelect = (MultiSelectListPreference) findPreference(
                getString(R.string.pref_key_sync_companies));
        companySelect.setEntries(companyNames);
        companySelect.setEntryValues(companyValues);
    }

}
