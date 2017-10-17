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
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import io.phobotic.nodyn.R;

/**
 * Created by Jonathan Nelson on 8/20/17.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class EmailPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_email, rootKey);
        setHasOptionsMenu(true);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_email_exceptions_addresses)));
        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_email_sync_fail_addresses)));
        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_email_server)));
        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_email_port)));
        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_email_username)));
        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_email_password)));
    }
}
