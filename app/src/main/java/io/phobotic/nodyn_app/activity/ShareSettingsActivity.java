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

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.fragment.ShareSettingsChooserFragment;
import io.phobotic.nodyn_app.fragment.ShareSettingsFileFragment;
import io.phobotic.nodyn_app.fragment.ShareSettingsNFCFragment;
import io.phobotic.nodyn_app.fragment.ShareSettingsQRCodeFragment;

public class ShareSettingsActivity extends AppCompatActivity implements ShareSettingsChooserFragment.OnShareMethodChosenListener {

    private static final String TAG = ShareSettingsActivity.class.getSimpleName();
    private ImageView qrcode;
    private View progressBox;
    private ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_settings);
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
                Fragment nfcFragment = ShareSettingsNFCFragment.newInstance();
                loadFragment(nfcFragment);
                break;
            case QRCODE:
                Fragment qrFragment = ShareSettingsQRCodeFragment.newInstance();
                loadFragment(qrFragment);
                break;
            case FILE_SHARE:
                Fragment fileFragment = ShareSettingsFileFragment.newInstance();
                loadFragment(fileFragment);
                break;
            default:
                Log.e(TAG, "Unknown share type: " + shareMethod.toString());
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.frame, fragment).commit();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
