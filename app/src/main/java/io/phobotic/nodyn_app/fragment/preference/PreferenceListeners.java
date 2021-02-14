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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.CompanyNotFoundException;
import io.phobotic.nodyn_app.database.exception.GroupNotFoundException;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.exception.StatusNotFoundException;
import io.phobotic.nodyn_app.database.model.Company;
import io.phobotic.nodyn_app.database.model.Group;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.model.Status;

/**
 * Created by Jonathan Nelson on 9/16/17.
 */

public class PreferenceListeners {
    private static final String TAG = PreferenceListeners.class.getSimpleName();

    /**
     * A preference change listener specifically for converting group IDs into the group names
     */
    public static Preference.OnPreferenceChangeListener groupsChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            Database db = Database.getInstance(preference.getContext());
            String stringValue = value.toString();

            String summary = "";
            String prefix = "";

            if (value instanceof Set) {
                Set<String> values = (Set<String>) value;
                for (String s : values) {
                    try {
                        int id = Integer.parseInt(s);
                        Group group = db.findGroupByID(id);
                        summary += prefix + group.getName();
                        prefix = ", ";
                    } catch (GroupNotFoundException e) {
                        Log.d(TAG, "Unable to find group with ID :'" + s + "', this value will " +
                                "not be reflected in preference summary");
                    }
                }
            }

            if (summary.length() == 0) {
                summary = "No user groups selected";
            }

            preference.setSummary(summary);

            return true;
        }
    };

    /**
     * A preference change listener specifically for converting company IDs into the company names
     */
    public static Preference.OnPreferenceChangeListener companiesChangeListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    Database db = Database.getInstance(preference.getContext());
                    String stringValue = value.toString();

                    String summary = "";
                    String prefix = "";

                    if (value instanceof Set) {
                        Set<String> values = (Set<String>) value;
                        for (String s : values) {
                            try {
                                int id = Integer.parseInt(s);
                                Company company = db.findCompanyById(id);
                                summary += prefix + company.getName();
                                prefix = ", ";
                            } catch (CompanyNotFoundException e) {
                                Log.d(TAG, "Unable to find company with ID :'" + s + "', this value will " +
                                        "not be reflected in preference summary");
                            }
                        }
                    }

                    if (summary.length() == 0) {
                        summary = "No companies selected";
                    }

                    preference.setSummary(summary);

                    return true;
                }
            };

    /**
     * A preference change listener specifically for converting asset model IDs into the model names
     */
    public static Preference.OnPreferenceChangeListener modelsChangeListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    Database db = Database.getInstance(preference.getContext());
                    String stringValue = value.toString();

                    String summary = "";
                    String prefix = "";

                    if (value instanceof Set) {
                        Set<String> values = (Set<String>) value;
                        for (String s : values) {
                            try {
                                int id = Integer.parseInt(s);
                                Model model = db.findModelByID(id);
                                summary += prefix + model.getName();
                                prefix = ", ";
                            } catch (ModelNotFoundException e) {
                                Log.d(TAG, "Unable to find model with ID :'" + s + "', this value will " +
                                        "not be reflected in preference summary");
                            }
                        }
                    }

                    if (summary.length() == 0) {
                        summary = "No models selected";
                    }

                    preference.setSummary(summary);

                    return true;
                }
            };

    /**
     * A preference change listener specifically for converting status IDs into the status names
     */
    public static Preference.OnPreferenceChangeListener statusChangeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            Database db = Database.getInstance(preference.getContext());
            String stringValue = value.toString();

            String summary = "";
            String prefix = "";

            if (value instanceof Set) {
                Set<String> values = (Set<String>) value;
                for (String s : values) {
                    try {
                        int id = Integer.parseInt(s);
                        Status status = db.findStatusByID(id);
                        summary += prefix + status.getName();
                        prefix = ", ";
                    } catch (StatusNotFoundException e) {
                        Log.d(TAG, "Unable to find status with ID :'" + s + "', this value will " +
                                "not be reflected in preference summary");
                    }
                }
            }

            if (summary.length() == 0) {
                summary = "No statuses selected";
            }

            preference.setSummary(summary);

            return true;
        }
    };


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.  This listener should only be used with generic preferences
     * where the preference summary should be the literal string value stored
     */
    public static Preference.OnPreferenceChangeListener sGenericPreferenceListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    String stringValue = value.toString();
                    Context context = preference.getContext();
                    if (preference instanceof ListPreference) {
                        // For list preferences, look up the correct display value in
                        // the preference's 'entries' list.
                        ListPreference listPreference = (ListPreference) preference;
                        int index = listPreference.findIndexOfValue(stringValue);

                        // Set the summary to reflect the new value.
                        preference.setSummary(
                                index >= 0
                                        ? listPreference.getEntries()[index]
                                        : null);

                    } else if (preference instanceof MultiSelectListPreference) {
                        //just use the default toString() value for String sets
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                        preference.setSummary(stringValue);
                    } else {
                        //this is rediculous, but the support library EditTextPreference has no
                        //+ getEditText() method so we can check the input type.
                        if (preference.getKey().equals(context.getResources().getString(
                                R.string.pref_key_email_password))) {
                            if (stringValue.length() == 0) {
                                preference.setSummary("");
                            } else {
                                preference.setSummary("●●●●●●●●●●●●");
                            }

                        } else {
                            // For all other preferences, set the summary to the value's
                            // simple string representation.
                            preference.setSummary(stringValue);
                        }
                    }
                    return true;
                }
            };


    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sGenericPreferenceListener
     */
    public static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sGenericPreferenceListener);

        // Trigger the listener immediately with the preference's
        // current value.
        if (preference instanceof MultiSelectListPreference) {
            sGenericPreferenceListener.onPreferenceChange(preference,
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                            .getStringSet(preference.getKey(), new HashSet<String>()));
        } else {
            sGenericPreferenceListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }
    }

//    public static void bindPreferenceSummaryToValue(Preference preference, String formatText) {
//        // Set the listener to watch for value changes.
//        preference.setOnPreferenceChangeListener(sGenericPreferenceListener);
//
//        // Trigger the listener immediately with the preference's
//        // current value.
//        if (preference instanceof MultiSelectListPreference) {
//            sGenericPreferenceListener.onPreferenceChange(preference,
//                    PreferenceManager.getDefaultSharedPreferences(preference.getContext())
//                            .getStringSet(preference.getKey(), new HashSet<String>()));
//        } else {
//            sGenericPreferenceListener.onPreferenceChange(preference,
//                    PreferenceManager
//                            .getDefaultSharedPreferences(preference.getContext())
//                            .getString(preference.getKey(), ""));
//        }
//    }
}
