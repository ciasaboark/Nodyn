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
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.service.StatisticsService;

/**
 * Created by Jonathan Nelson on 2/3/18.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class BetaPreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = BetaPreferenceFragment.class.getSimpleName();
    private SharedPreferences prefs;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_beta, rootKey);
        setHasOptionsMenu(true);

        init();
    }

    private void init() {
        final Preference rebuildStatisticsPreference = findPreference(getString(R.string.pref_key_beta_rebuild_statistics));
        rebuildStatisticsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(getContext(), StatisticsService.class);
                getContext().startService(i);
                rebuildStatisticsPreference.setEnabled(false);
                return true;
            }
        });
    }
}