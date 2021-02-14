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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Group;
import io.phobotic.nodyn_app.helper.PreferenceHelper;

/**
 * Created by Jonathan Nelson on 8/8/17.
 */

public class CheckInPreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = CheckInPreferenceFragment.class.getSimpleName();
    private Database db;
    private SharedPreferences prefs;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_check_in, rootKey);
        setHasOptionsMenu(true);
        db = Database.getInstance(getContext());
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        initPreferences();

        PreferenceHelper.tintIcons(getContext(), getPreferenceScreen());
    }



    private void initPreferences() {
        Preference verificationTextPreference = findPreference(getString(R.string.pref_key_check_in_verification_text));
        verificationTextPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                View v = getLayoutInflater(null).inflate(R.layout.view_eula_text, null);
                final EditText input = v.findViewById(R.id.input);
                TextView info = v.findViewById(R.id.info);
                info.setMovementMethod(new LinkMovementMethod());
                String curVerificationText = prefs.getString(getString(R.string.pref_key_check_in_verification_text),
                        getString(R.string.pref_default_check_in_verification_text));
                input.setText(curVerificationText);

                final AlertDialog d = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                        .setTitle("Asset Check-in Verification Text")
                        .setView(v)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newText = input.getText().toString();

                                prefs.edit()
                                        .putString(getString(R.string.pref_key_check_in_verification_text), newText)
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
                                String defaultText = prefs.getString(getResources()
                                        .getString(R.string.pref_default_check_in_verification_text), null);
                                prefs.edit().putString(getString(R.string.pref_key_check_in_verification_text),
                                        defaultText).apply();
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
}
