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

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Group;

import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.helper.PreferenceHelper;
import io.phobotic.nodyn_app.preference.EmailRecipientsPreference;
import io.phobotic.nodyn_app.preference.EmailRecipientsPreferenceDialogFragmentCompat;
import io.phobotic.nodyn_app.preference.NumberPickerPreference;
import io.phobotic.nodyn_app.preference.NumberPickerPreferenceDialogFragmentCompat;

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

        PreferenceHelper.tintIcons(getContext(), getPreferenceScreen());
    }

    private void initListeners() {
        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
                getString(R.string.pref_key_email_past_due_addresses)));

        Preference allowedStatuses = findPreference(getString(
                R.string.pref_key_asset_status_allowed_statuses));
        allowedStatuses.setOnPreferenceChangeListener(PreferenceListeners.statusChangeListener);
        PreferenceListeners.statusChangeListener.onPreferenceChange(allowedStatuses,
                prefs.getStringSet(allowedStatuses.getKey(), new HashSet<String>()));

        Preference allowedGroups = findPreference(getString(R.string.pref_key_check_out_authorization_groups));
        allowedGroups.setOnPreferenceChangeListener(PreferenceListeners.groupsChangeListener);
        PreferenceListeners.groupsChangeListener.onPreferenceChange(allowedGroups,
                prefs.getStringSet(allowedGroups.getKey(), new HashSet<String>()));



        Preference eulaPreference = findPreference(getString(R.string.pref_key_check_out_eula));
        eulaPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                View v = getLayoutInflater(null).inflate(R.layout.view_eula_text, null);
                final EditText input = v.findViewById(R.id.input);
                TextView info = v.findViewById(R.id.info);
                info.setMovementMethod(new LinkMovementMethod());
                String curEula = prefs.getString(getString(R.string.pref_key_check_out_eula),
                        getString(R.string.pref_default_check_out_eula));
                input.setText(curEula);

                final AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                        .setTitle("Asset Checkout EULA")
                        .setView(v)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String eulaText = input.getText().toString();

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
                        .setNeutralButton(R.string.reset, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String defaultEula = prefs.getString(getResources()
                                        .getString(R.string.pref_default_check_out_eula), null);
                                prefs.edit().putString(getString(R.string.pref_key_check_out_eula),
                                        defaultEula).apply();
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
        initTimeout();
        initDuration();
        initAllowedStatusesSelect();
    }

    private void initGroupSelect() {
        Set<String> chosenGroups = prefs.getStringSet(
                getString(R.string.pref_key_check_out_authorization_groups), null);
        Log.d(TAG, "chosen groups: " + chosenGroups);


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



    private void initTimeout() {
        Preference timeoutLimitPreference = findPreference(getString(
                R.string.pref_key_check_out_timeout_limit));
        timeoutLimitPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String val = newValue.toString();
                String summary;
                if (val.equals("0")) {
                    summary = getString(R.string.pref_summary_check_out_no_timeout_limit);
                } else {
                    String tail = "";
                    try {
                        int i = Integer.parseInt(val);
                        if (i > 1) {
                            tail = "s";
                        }
                    } catch (NumberFormatException e) {
                    }

                    summary = getString(R.string.pref_summary_check_out_timeout_limit);
                    summary = String.format(summary, val, tail);
                }
                preference.setSummary(summary);
                return true;
            }
        });

        timeoutLimitPreference.callChangeListener(PreferenceManager.getDefaultSharedPreferences(
                getContext()).getString(getString(R.string.pref_key_check_out_timeout_limit),
                getString(R.string.pref_default_check_out_timeout_limit)));

    }

    private void initDuration() {
        Preference durationPreference = findPreference(getString(
                R.string.pref_key_check_out_duration));
        durationPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String val = newValue.toString();
                String summary;
                if (val.equals("0")) {
                    summary = getString(R.string.pref_summary_check_out_no_duration);
                } else {
                    String tail = "";
                    try {
                        int i = Integer.parseInt(val);
                        if (i > 1) {
                            tail = "s";
                        }
                    } catch (NumberFormatException e) {
                    }

                    summary = getString(R.string.pref_summary_check_out_duration);
                    summary = String.format(summary, val, tail);
                }
                preference.setSummary(summary);
                return true;
            }
        });

        durationPreference.callChangeListener(PreferenceManager.getDefaultSharedPreferences(
                getContext()).getString(getString(R.string.pref_key_check_out_duration),
                getString(R.string.pref_default_check_out_duration)));

    }

    private void initAllowedStatusesSelect() {
        Set<String> chosenStatuses = prefs.getStringSet(getString(
                R.string.pref_key_asset_status_allowed_statuses), null);
        Log.d(TAG, "allowed statuses: " + chosenStatuses);

        List<Status> statuses = db.getStatuses();
        //sort the status alphabetically so the list stays in the same general order after every sync
        Collections.sort(statuses, new Comparator<Status>() {
            @Override
            public int compare(Status o1, Status o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        String[] statusNames = new String[statuses.size()];
        String[] statusValues = new String[statuses.size()];

        for (int i = 0; i < statuses.size(); i++) {
            Status status = statuses.get(i);
            String name = status.getName();
            String value = String.valueOf(status.getId());

            statusNames[i] = name;
            statusValues[i] = value;
        }

        MultiSelectListPreference statusSelect = (MultiSelectListPreference) findPreference(
                getString(R.string.pref_key_asset_status_allowed_statuses));
        statusSelect.setEntries(statusNames);
        statusSelect.setEntryValues(statusValues);

    }

    private void showNoBackEndError() {
        AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                .setTitle("No Backend Configured")
                .setMessage("No backend has been configured yet.  This setting will become available after the backend service is configured and after a successfull sync")
                .setPositiveButton(android.R.string.ok, null)
                .create();
        d.show();
    }

    private void showNotSyncedError() {
        AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                .setTitle("Not Yet Synced")
                .setMessage("This setting will become avaiable after a successfull sync with the backend service")
                .setPositiveButton(android.R.string.ok, null)
                .create();
        d.show();
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Try if the preference is one of our custom Preferences
        DialogFragment dialogFragment = null;
        Preference timeoutLimitPreference = findPreference(getString(
                R.string.pref_key_check_out_timeout_limit));
        Preference durationPreference = findPreference(getString(
                R.string.pref_key_check_out_duration));
        if (preference instanceof NumberPickerPreference) {
            // Create a new instance of NumberPickerPreferenceDialogFragmentCompat with the key of the related
            // Preference
            if (preference.equals(timeoutLimitPreference)) {
                dialogFragment = NumberPickerPreferenceDialogFragmentCompat
                        .newInstance(preference.getKey(), "minute", "minutes", "Never");
            } else if (preference.equals(durationPreference)) {
                dialogFragment = NumberPickerPreferenceDialogFragmentCompat
                        .newInstance(preference.getKey(), "day", "days", "Indefinately");
            }
        } else if (preference instanceof EmailRecipientsPreference) {
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
