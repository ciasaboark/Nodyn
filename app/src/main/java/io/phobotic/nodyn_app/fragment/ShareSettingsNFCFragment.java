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

package io.phobotic.nodyn_app.fragment;


import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import io.phobotic.nodyn_app.R;
import pl.bclogic.pulsator4droid.library.PulsatorLayout;


public class ShareSettingsNFCFragment extends Fragment implements NfcAdapter.CreateNdefMessageCallback {

    private View rootView;
    private NfcAdapter mNfcAdapter;
    private View error;
    private TextView errorMessage;
    private View share;
    private View progress;

    public static ShareSettingsNFCFragment newInstance() {
        ShareSettingsNFCFragment fragment = new ShareSettingsNFCFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public ShareSettingsNFCFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_share_settings_nfc, container, false);
        init();

        return rootView;
    }

    private void init() {
        progress = rootView.findViewById(R.id.progress);
        error = rootView.findViewById(R.id.error);
        errorMessage = (TextView) error.findViewById(R.id.message);
        share = rootView.findViewById(R.id.share);
        PulsatorLayout pulsator = (PulsatorLayout) rootView.findViewById(R.id.pulse);
        pulsator.start();

        // NFC isn't available on the device
        PackageManager pm = getContext().getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            showError("This device does not support NFC");
            // Android Beam file transfer isn't supported
        } else if (Build.VERSION.SDK_INT <
                Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // If Android Beam isn't available, don't continue.
            showError("Android Beam is not available on this version of Android");
        } else {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(getContext());

            if (!mNfcAdapter.isEnabled()) {
                showError("Please turn on NFC in the system settings");
            } else {
                mNfcAdapter.setNdefPushMessageCallback(this, getActivity());
                showShare();
            }

        }
    }

    private void showError(String message) {
        progress.setVisibility(View.GONE);
        error.setVisibility(View.VISIBLE);
        errorMessage.setText(message);
        share.setVisibility(View.GONE);
    }

    private void showShare() {
        progress.setVisibility(View.GONE);
        error.setVisibility(View.GONE);
        share.setVisibility(View.VISIBLE);
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        String message = getSettingsAsJson();

        NdefRecord ndefRecord = NdefRecord.createMime("text/plain", message.getBytes());
        NdefMessage ndefMessage = new NdefMessage(ndefRecord);
        return ndefMessage;
    }

    private String getSettingsAsJson() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Map<String, ?> allPrefs = prefs.getAll();
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            String key = entry.getKey();

        }

        allPrefs.remove("check_out_eula");
        allPrefs.remove("pref_snipeit_4_api_key");

        Map<String, Object> transferPrefs = new HashMap<>();


        Gson gson = new Gson();
        String json = gson.toJson(allPrefs);

        return json;
    }
}
