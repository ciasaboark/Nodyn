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
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.preference.EmailRecipientsPreference;
import io.phobotic.nodyn_app.preference.EmailRecipientsPreferenceDialogFragmentCompat;

/**
 * Created by Jonathan Nelson on 8/20/17.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class EmailPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_email, rootKey);
        setHasOptionsMenu(true);

        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_email_server)));
        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_email_port)));
        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_email_username)));
        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_email_password)));

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

        // If it was one of our custom Preferences, show its dialog
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
