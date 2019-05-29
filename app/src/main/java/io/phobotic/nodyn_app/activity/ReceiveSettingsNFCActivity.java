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

package io.phobotic.nodyn_app.activity;


import android.app.Activity;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Toast;

import com.google.gson.Gson;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.preference.SettingsImporter;
import io.phobotic.nodyn_app.preference.SettingsPage;
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
        PulsatorLayout pulsator = findViewById(R.id.pulse);
        pulsator.start();

        Intent intent = getIntent();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage message = (NdefMessage) rawMessages[0]; // only one message transferred
            String data = new String(message.getRecords()[0].getPayload());
            Gson gson = new Gson();


            try {
                //There should not be any trouble transferring the entire settings json within a
                //+ single settings page.
                SettingsPage settingsPage = gson.fromJson(data, SettingsPage.class);
                SettingsImporter importer = new SettingsImporter(this);
                importer.importSettings(settingsPage.getVersionCode(), data);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Unable to import settings", Toast.LENGTH_LONG).show();
            }
        }
    }

}
