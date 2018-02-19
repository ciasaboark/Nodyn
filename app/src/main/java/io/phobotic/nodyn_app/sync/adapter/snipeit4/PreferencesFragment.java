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

package io.phobotic.nodyn_app.sync.adapter.snipeit4;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.fragment.preference.PreferenceListeners;

/**
 * Created by Jonathan Nelson on 7/27/17.
 */

public class PreferencesFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_sync_snipeit_4, rootKey);
        setHasOptionsMenu(true);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_snipeit_4_protocol)));
        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_snipeit_4_host)));
        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_snipeit_4_port)));
        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_snipeit_4_api_key)));
    }
}
