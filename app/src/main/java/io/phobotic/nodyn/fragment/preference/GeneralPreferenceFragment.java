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
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;
import java.util.UUID;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.view.EnableKioskDialogView;
import main.java.com.maximeroussy.invitrode.RandomWord;

/**
 * Created by Jonathan Nelson on 9/26/17.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class GeneralPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_general, rootKey);
        setHasOptionsMenu(true);

        final Preference deviceNamePreference = findPreference(getString(R.string.pref_key_general_id));
        PreferenceListeners.bindPreferenceSummaryToValue(deviceNamePreference);
        deviceNamePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                View v = getLayoutInflater(null).inflate(R.layout.view_device_name, null);
                final EditText input = (EditText) v.findViewById(R.id.input);

                String curDeviceName = prefs.getString(getString(R.string.pref_key_general_id),
                        getString(R.string.pref_default_general_id));
                input.setText(curDeviceName);

                final AlertDialog d = new AlertDialog.Builder(getContext())
                        .setTitle("Device Name")
                        .setView(v)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newDeviceName = input.getText().toString();
                                if (newDeviceName.length() == 0) {
                                    newDeviceName = null;
                                }

                                deviceNamePreference.setSummary(newDeviceName);
                                prefs.edit()
                                        .putString(getString(R.string.pref_key_general_id), newDeviceName)
                                        .apply();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //nothing to do here
                            }
                        })
                        .setNeutralButton("generate", null)
                        .create();

                d.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        Button generateButton = d.getButton(DialogInterface.BUTTON_NEUTRAL);
                        generateButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    Random random = new Random(System.currentTimeMillis());
                                    int length1 = random.nextInt(13) + 3;
                                    int length2 = random.nextInt(13) + 3;
                                    String randomWord1 = RandomWord.getNewWord(length1);
                                    String randomWord2 = RandomWord.getNewWord(length2);
                                    input.setText(randomWord1 + " " + randomWord2);
                                } catch (Exception e) {
                                    String uuid = UUID.randomUUID().toString();
                                    input.setText(uuid);
                                }
                            }
                        });

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

        SwitchPreferenceCompat kioskSwitch = (SwitchPreferenceCompat) findPreference(
                getString(R.string.pref_key_general_kiosk));
        kioskSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enabled = (boolean) newValue;

                //if kiosk mode was switched off we need to make sure to clear any previously
                //+ set passwords
                if (!enabled) {
                    disableKioskMode();
                } else {
                    showKioskPasswordChangeDialog();
                }

                //go ahead and notify the preference manager that kiosk mode should be enabled
                //+ If the user fails to set a password in the dialog then it will be disabled
                //+ there
                return true;
            }
        });

        Preference resetPasswordButton = findPreference(
                getString(R.string.pref_key_general_kiosk_reset_password));
        resetPasswordButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showKioskPasswordChangeDialog();
                return true;
            }
        });
    }

    /**
     * Disable kiosk mode by changing values in settings and unchecking the main switch
     */
    private void disableKioskMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean(getString(R.string.pref_key_general_kiosk), false).apply();
        prefs.edit().putString(getString(R.string.pref_key_general_kiosk_password), null).apply();

        SwitchPreferenceCompat kioskSwitch = (SwitchPreferenceCompat) findPreference(
                getString(R.string.pref_key_general_kiosk));

        //temporarily disable the listener to avoid a callback loop
        Preference.OnPreferenceChangeListener oldListener = kioskSwitch.getOnPreferenceChangeListener();
        kioskSwitch.setOnPreferenceChangeListener(null);
        kioskSwitch.setChecked(false);
        kioskSwitch.setOnPreferenceChangeListener(oldListener);
    }

    private void showKioskPasswordChangeDialog() {
        final EnableKioskDialogView view = new EnableKioskDialogView(getContext(), null);
        final AlertDialog d = new AlertDialog.Builder(getContext())
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //pull the password from the view and set it here
                        String password = view.getPassword();
                        setKioskPassword(password);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        prefs.edit().putBoolean(getString(R.string.pref_key_general_kiosk), false).apply();
                        disableKioskMode();
                    }
                })
                .create();


        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button positveButton = d.getButton(DialogInterface.BUTTON_POSITIVE);
                positveButton.setEnabled(false);

                view.setOnPasswordChangedListener(new EnableKioskDialogView.OnPasswordChangedListener() {
                    @Override
                    public void onPasswordChanged(String newPassword) {
                        if (newPassword != null && newPassword.length() > 0) {
                            positveButton.setEnabled(true);
                        } else {
                            positveButton.setEnabled(false);
                        }
                    }
                });

                view.getPasswordInput().setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            positveButton.callOnClick();
                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        d.show();
    }

    private void setKioskPassword(String password) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putString(getString(R.string.pref_key_general_kiosk_password), password).apply();
    }
}