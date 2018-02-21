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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.fragment.ShareSettingsChooserFragment;
import io.phobotic.nodyn_app.fragment.ShareSettingsNFCFragment;
import io.phobotic.nodyn_app.fragment.ShareSettingsQRCodeFragment;

public class ShareSettingsActivity extends AppCompatActivity implements ShareSettingsChooserFragment.OnFragmentInteractionListener {

    private static final String TAG = ShareSettingsActivity.class.getSimpleName();
    private ImageView qrcode;
    private View progressBox;
    private ProgressBar progressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_settings);

        init();
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
    public void onMethodChosen(ShareSettingsChooserFragment.Method method) {
        switch (method) {
            case NFC:
                Fragment nfcFragment = ShareSettingsNFCFragment.newInstance();
                loadFragment(nfcFragment);
                break;
            case QRCODE:
                Fragment qrFragment = ShareSettingsQRCodeFragment.newInstance();
                loadFragment(qrFragment);
                break;
            default:
                Log.e(TAG, "Unknown share type: " + method.toString());
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.frame, fragment).addToBackStack(fragment.getTag()).commit();
    }
}