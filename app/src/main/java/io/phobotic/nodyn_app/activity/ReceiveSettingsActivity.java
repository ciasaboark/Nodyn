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

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.fragment.PreferenceReaderFragment;
import io.phobotic.nodyn_app.fragment.ReceiveSettingsQRCodeFragment;
import io.phobotic.nodyn_app.fragment.ShareSettingsChooserFragment;
import io.phobotic.nodyn_app.preference.OnPreferencesReadListener;
import io.phobotic.nodyn_app.preference.SettingsImporter;

public class ReceiveSettingsActivity extends AppCompatActivity implements ShareSettingsChooserFragment.OnShareMethodChosenListener, OnPreferencesReadListener {
    private static final String TAG = ReceiveSettingsActivity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_CAMERA = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_settings);
        setupActionBar();
        init();
    }

    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setFocusable(false);

    }

    private void init() {
        loadChooserFragment();
    }

    private void loadChooserFragment() {
        Fragment chooserFragment = ShareSettingsChooserFragment.newInstance();
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().add(R.id.frame, chooserFragment).commit();
    }


    @Override
    public void onMethodChosen(ShareSettingsChooserFragment.ShareMethod shareMethod) {
        switch (shareMethod) {
            case NFC:
                Intent i = new Intent(this, ReceiveSettingsNFCActivity.class);
                startActivity(i);
                break;
            case QRCODE:
                //check if we have permission to access the camera before we load the fragment
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    //permission has been previously granted, go ahead and load the QR scanner fragment
                    PreferenceReaderFragment qrFragment = ReceiveSettingsQRCodeFragment.newInstance();
                    loadFragment(qrFragment);
                } else {
                    // Permission is not granted.  Ask for permission and listen for the results
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_ACCESS_CAMERA);
                }

                break;
            default:
                Log.e(TAG, "Unknown share type: " + shareMethod.toString());
        }
    }

    private void loadFragment(PreferenceReaderFragment fragment) {
        fragment.setListener(this);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.frame, fragment).addToBackStack(fragment.getTag()).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PreferenceReaderFragment qrFragment = ReceiveSettingsQRCodeFragment.newInstance();
                    loadFragment(qrFragment);
                } else {
                    Toast.makeText(this, "Camera permission is required for QR code scanning", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onPreferencesRead(int versionCode, Map<String, Object> map) {
        SettingsImporter importer = new SettingsImporter(this);
        importer.setListener(this);
        importer.importSettings(versionCode, map);
        finish();
    }

    @Override
    public void onPreferencesRead(int versionCode, String json) {
        SettingsImporter importer = new SettingsImporter(this);
        try {
            importer.setListener(this);
            importer.importSettings(versionCode, json);
        } catch (Exception e) {
            //nothing to do here
        }
    }

    @Override
    public void onPreferenceImportComplete(boolean imported) {
        finish();
    }

}
