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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.activity.SettingsActivity;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.sync.SyncManager;
import io.phobotic.nodyn.sync.adapter.SyncAdapter;

/**
 * This fragment shows data and sync preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DataSyncPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_data_sync, rootKey);
        setHasOptionsMenu(true);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences
        // to their values. When their values change, their summaries are
        // updated to reflect the new value, per the Android Design
        // guidelines.
        SettingsActivity.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_sync_frequency)));
        SettingsActivity.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_sync_backend)));

        Preference backendConfigurePreference = findPreference(
                getString(R.string.pref_key_sync_backend_configure));
        final FragmentManager fm = getFragmentManager();
        backendConfigurePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                SyncAdapter syncAdapter = SyncManager.getPrefferedSyncAdapter(getActivity());
                DialogFragment dialog = syncAdapter.getConfigurationDialogFragment(getActivity());

                if (dialog == null) {
                    AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.sync_backend_dialog_title))
                            .setMessage(getString(R.string.sync_backend_dialog_no_configure))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog d = b.create();
                    d.show();
                } else {
                    // TODO: 7/27/17 find a way to use the support fragments librart to display the FragmentDialog
                    dialog.show(fm, "dialog");
                }
                return true;
            }
        });

        //if the user changed the backed then the data model should be dumped, but the history should be kept
        Preference backendPreference = findPreference(getString(R.string.pref_key_sync_backend));
        backendPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Database db = Database.getInstance(getActivity());
                db.dumpModel();

                //Let the static listener update the summary.  This is ugly, but it will do
                SettingsActivity.sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, newValue);
                return true;
            }
        });
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
