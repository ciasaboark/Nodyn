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

package io.phobotic.nodyn_app.preference;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.BuildConfig;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.activity.MainActivity;

/**
 * Created by Jonathan Nelson on 2/27/19.
 */
public class SettingsImporter {
    private static final String TAG = SettingsImporter.class.getSimpleName();
    private OnPreferencesReadListener listener;
    private Activity activity;

    public SettingsImporter(Activity activity) {
        this.activity = activity;
    }

    public void setListener(OnPreferencesReadListener listener) {
        this.listener = listener;
    }

    public void importSettings(int versionCode, String jsonString) throws Exception {
        Gson gson = new Gson();
        Map<String, Object> m = new HashMap<>();
        final Map<String, Object> map = gson.fromJson(jsonString, m.getClass());
        Log.d(TAG, "Map size: " + map.size());
        verifyAndImport(versionCode, map);
    }

    private void verifyAndImport(int versionCode, final Map<String, Object> map) {
        View v = LayoutInflater.from(activity).inflate(R.layout.dialog_settings_import, null);
        TextView importCount = v.findViewById(R.id.import_count);
        importCount.setText(String.format(activity.getString(R.string.import_settings_read), map.size()));

        //if the imported settings have a password set to access the settings activity then we need
        //+ to show a warning.
        String klKey = activity.getResources().getString(R.string.pref_key_general_kiosk_lock_settings);
        String spKey = activity.getResources().getString(R.string.pref_key_general_kiosk_password);
        Object klO = map.get(klKey);
        Object spO = map.get(spKey);
        boolean showWarning = false;

        if ((klO != null && klO.equals(true)) || spO != null) {
            showWarning = true;
            View warning = v.findViewById(R.id.warning_password);
            warning.setVisibility(View.VISIBLE);
        }

        if (BuildConfig.VERSION_CODE != versionCode) {
            showWarning = true;
            TextView warning = v.findViewById(R.id.warning_version);
            warning.setVisibility(View.VISIBLE);
            String str = null;
            if (BuildConfig.VERSION_CODE > versionCode) {
                str = activity.getString(R.string.older);
            } else {
                str = activity.getString(R.string.newer);
            }

            warning.setText(String.format(activity.getString(R.string.import_warning_version), str));
        }

        final AlertDialog d = new MaterialAlertDialogBuilder(activity, R.style.Widgets_Dialog)
                .setTitle(R.string.import_dialog_title)
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        applySettings(map);
                        listener.onPreferenceImportComplete(true);
                    }
                })
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onPreferenceImportComplete(false);
                    }
                })
                .create();
        final TextView waitTextView = v.findViewById(R.id.wait);
        final boolean requirePause = showWarning;

        //disable the positive button for a few seconds if we need to show the password warning.
        //+ Hopefully this will help prevent locking anyone out of their settings
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            final long WAIT_MS = 5000;

            @Override
            public void onShow(DialogInterface dialog) {
                if (requirePause) {
                    waitTextView.setVisibility(View.VISIBLE);
                    final Button b = d.getButton(DialogInterface.BUTTON_POSITIVE);
                    b.setEnabled(false);
                    if (waitTextView != null) {
                        setWaitText(WAIT_MS);
                    }

                    CountDownTimer timer = new CountDownTimer(WAIT_MS, 300) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            if (waitTextView != null) {
                                setWaitText(millisUntilFinished);
                            }
                        }

                        @Override
                        public void onFinish() {
                            if (b != null) b.setEnabled(true);
                            if (waitTextView != null) {
                                waitTextView.setVisibility(View.INVISIBLE);
                            }
                        }
                    };
                    timer.start();
                }
            }

            private void setWaitText(long millisUntilFinished) {
                String waitText = activity.getString(R.string.please_wait_timer);
                int seconds = Math.round((float) millisUntilFinished / 1000);
                waitText = String.format(waitText, seconds);
                waitTextView.setText(waitText);
            }
        });
        d.show();

    }

    private void applySettings(Map<String, Object> map) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);

        //reset all preference values back to their default values before overriding
        //+ with preferences read from the target device
        prefs.edit().clear().commit();
        PreferenceManager.setDefaultValues(activity, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(activity, R.xml.pref_data_sync, false);
        PreferenceManager.setDefaultValues(activity, R.xml.pref_users, false);
        PreferenceManager.setDefaultValues(activity, R.xml.pref_assets, false);
        PreferenceManager.setDefaultValues(activity, R.xml.pref_sync_snipeit_3, false);
        PreferenceManager.setDefaultValues(activity, R.xml.pref_check_in, false);
        PreferenceManager.setDefaultValues(activity, R.xml.pref_check_out, false);
        PreferenceManager.setDefaultValues(activity, R.xml.pref_email, false);

        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Boolean) {
                editor.putBoolean(key, (Boolean) value);
            } else if (value instanceof Integer) {
                editor.putInt(key, (Integer) value);
            } else if (value instanceof String) {
                editor.putString(key, (String) value);
            } else if (value instanceof Float) {
                editor.putFloat(key, (Float) value);
            } else if (value instanceof Long) {
                editor.putLong(key, (Long) value);
            } else if (value instanceof List) {
                Set<String> valueSet = new HashSet<>();
                for (Object o : (List) value) {
                    if (o instanceof String) {
                        valueSet.add((String) o);
                    }
                }
                editor.putStringSet(key, valueSet);
            }
        }
        editor.apply();
        Toast.makeText(activity, "Imported " + map.size() + " settings", Toast.LENGTH_LONG).show();

        Intent i = new Intent(activity, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(MainActivity.SYNC_NOW, true);
        activity.startActivity(i);
        //todo notify that import is finished
    }

    public void importSettings(int versionCode, Map<String, Object> settingsMap) {
        verifyAndImport(versionCode, settingsMap);
    }

    public void importSettings(File file) {
        //todo
    }
}
