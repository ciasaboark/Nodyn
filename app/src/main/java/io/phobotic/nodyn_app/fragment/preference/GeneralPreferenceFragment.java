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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;
import java.util.UUID;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.preference.EmailRecipientsPreference;
import io.phobotic.nodyn_app.preference.EmailRecipientsPreferenceDialogFragmentCompat;
import io.phobotic.nodyn_app.view.EnableKioskDialogView;
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


//        PreferenceListeners.bindPreferenceSummaryToValue(findPreference(
//                getString(R.string.pref_key_equipment_managers_addresses)));

        final Preference deviceNamePreference = findPreference(getString(R.string.pref_key_general_id));
        PreferenceListeners.bindPreferenceSummaryToValue(deviceNamePreference);
        deviceNamePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                View v = getLayoutInflater(null).inflate(R.layout.view_device_name, null);
                final EditText input = v.findViewById(R.id.input);

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

        final SwitchPreferenceCompat inputHardware = (SwitchPreferenceCompat) findPreference(
                getResources().getString(R.string.pref_key_general_kiosk_input_mode_hardware));

        final SwitchPreferenceCompat inputOSK = (SwitchPreferenceCompat) findPreference(
                getResources().getString(R.string.pref_key_general_kiosk_input_mode_osk));

        final SwitchPreferenceCompat inputCamera = (SwitchPreferenceCompat) findPreference(
                getResources().getString(R.string.pref_key_general_kiosk_input_mode_camera));

        Preference.OnPreferenceChangeListener inputMethodListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                //only allow this switch to be disabled so long as we have at least one other switch enabled
                boolean changeAllowed = false;
                if ((Boolean) newValue == true) {
                    changeAllowed = true;
                } else {
                    int activeInputs = 0;
                    if (inputHardware.isChecked()) activeInputs++;
                    if (inputOSK.isChecked()) activeInputs++;
                    if (inputCamera.isChecked()) activeInputs++;

                    if (activeInputs >= 2) {
                        changeAllowed = true;
                    } else {
                        final AlertDialog d = new AlertDialog.Builder(getContext())
                                .setTitle("Input mode restriction")
                                .setMessage("At least one input mode must remain active.")
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .create();

                        d.show();
                    }
                }

                return changeAllowed;
            }
        };

        inputHardware.setOnPreferenceChangeListener(inputMethodListener);
        inputOSK.setOnPreferenceChangeListener(inputMethodListener);
        inputCamera.setOnPreferenceChangeListener(inputMethodListener);

        SwitchPreferenceCompat lockSettingsSwitch = (SwitchPreferenceCompat) findPreference(
                getString(R.string.pref_key_general_kiosk_lock_settings));
        lockSettingsSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enabled = (boolean) newValue;

                //if kiosk mode was switched off we need to make sure to clear any previously
                //+ set passwords
                if (!enabled) {
                    disableLockedSettingsMode();
                } else {
                    showLockedSettingsChangePasswordDialog();
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
                showLockedSettingsChangePasswordDialog();
                return true;
            }
        });
    }

    /**
     * Disable kiosk mode by changing values in settings and unchecking the main switch
     */
    private void disableLockedSettingsMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefs.edit().putBoolean(getString(R.string.pref_key_general_kiosk_lock_settings), false).apply();
        prefs.edit().putString(getString(R.string.pref_key_general_kiosk_password), null).apply();

        SwitchPreferenceCompat lockSettingsSwitch = (SwitchPreferenceCompat) findPreference(
                getString(R.string.pref_key_general_kiosk_lock_settings));

        //temporarily disable the listener to avoid a callback loop
        Preference.OnPreferenceChangeListener oldListener = lockSettingsSwitch.getOnPreferenceChangeListener();
        lockSettingsSwitch.setOnPreferenceChangeListener(null);
        lockSettingsSwitch.setChecked(false);
        lockSettingsSwitch.setOnPreferenceChangeListener(oldListener);
    }

    private void showLockedSettingsChangePasswordDialog() {
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
                        prefs.edit().putBoolean(getString(R.string.pref_key_general_kiosk_lock_settings), false).apply();
                        disableLockedSettingsMode();
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