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

package io.phobotic.nodyn_app.activity;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import io.phobotic.nodyn_app.R;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;


public class ReceiveSettingsNFCActivity extends Activity {
    private static final String TAG = ReceiveSettingsNFCActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_settings_nfc);
        init();
    }

    private void init() {
        PulsatorLayout pulsator = (PulsatorLayout) findViewById(R.id.pulse);
        pulsator.start();

        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
            String data = new String(message.getRecords()[0].getPayload());

            Gson gson = new Gson();
            Map<String, ?> m = new HashMap<>();
            final Map<String, ?> map = gson.fromJson(data, m.getClass());
            Log.d(TAG, "Normalized text: " + data);
            Log.d(TAG, "Map size: " + map.size());

            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().clear().commit();
            PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
            PreferenceManager.setDefaultValues(this, R.xml.pref_data_sync, false);
            PreferenceManager.setDefaultValues(this, R.xml.pref_users, false);
            PreferenceManager.setDefaultValues(this, R.xml.pref_assets, false);
            PreferenceManager.setDefaultValues(this, R.xml.pref_sync_snipeit_3, false);
            PreferenceManager.setDefaultValues(this, R.xml.pref_check_in, false);
            PreferenceManager.setDefaultValues(this, R.xml.pref_check_out, false);
            PreferenceManager.setDefaultValues(this, R.xml.pref_email, false);

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
                } else if (value instanceof Set) {
                    editor.putStringSet(key, (Set<String>) value);
                }
            }
            editor.apply();
            Toast.makeText(this, "Copied " + map.size() + " settings", Toast.LENGTH_LONG).show();

            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra(MainActivity.SYNC_NOW, true);
            startActivity(i);
            finish();
        }
    }

}
