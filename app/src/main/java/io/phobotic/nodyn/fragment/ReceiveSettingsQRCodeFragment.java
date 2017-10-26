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

package io.phobotic.nodyn.fragment;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.phobotic.nodyn.GzipUtil;
import io.phobotic.nodyn.R;
import io.phobotic.nodyn.activity.MainActivity;

public class ReceiveSettingsQRCodeFragment extends Fragment {
    private static final String TAG = ReceiveSettingsQRCodeFragment.class.getSimpleName();
    private View rootView;

    public static ReceiveSettingsQRCodeFragment newInstance() {
        ReceiveSettingsQRCodeFragment fragment = new ReceiveSettingsQRCodeFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public ReceiveSettingsQRCodeFragment() {
        // Required empty public constructor
    }

    // Get the results:
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getContext(), "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                try {
                    String base64encoded = result.getContents();
                    byte[] compressedBytes = GzipUtil.base64Decode(base64encoded);
                    String normalized = GzipUtil.decompress(compressedBytes);

                    Gson gson = new Gson();
                    Map<String, ?> m = new HashMap<>();
                    final Map<String, ?> map = gson.fromJson(normalized, m.getClass());
                    Log.d(TAG, "Normalized text: " + normalized);
                    Log.d(TAG, "Map size: " + map.size());

                    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                    prefs.edit().clear().commit();
                    PreferenceManager.setDefaultValues(getContext(), R.xml.pref_general, false);
                    PreferenceManager.setDefaultValues(getContext(), R.xml.pref_data_sync, false);
                    PreferenceManager.setDefaultValues(getContext(), R.xml.pref_users, false);
                    PreferenceManager.setDefaultValues(getContext(), R.xml.pref_assets, false);
                    PreferenceManager.setDefaultValues(getContext(), R.xml.pref_sync_snipeit_3, false);
                    PreferenceManager.setDefaultValues(getContext(), R.xml.pref_check_in, false);
                    PreferenceManager.setDefaultValues(getContext(), R.xml.pref_check_out, false);
                    PreferenceManager.setDefaultValues(getContext(), R.xml.pref_email, false);

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
                    Toast.makeText(getContext(), "Copied " + map.size() + " settings", Toast.LENGTH_LONG).show();

                    Intent i = new Intent(getContext(), MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra(MainActivity.SYNC_NOW, true);
                    startActivity(i);
                    getActivity().finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

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
        rootView = inflater.inflate(R.layout.fragment_receive_settings_qrcode, container, false);
        init();

        return rootView;
    }

    private void init() {
//        DecoratedBarcodeView decoder = (DecoratedBarcodeView) findViewById(R.id.scanner);
//        decoder.decodeContinuous(new BarcodeCallback() {
//            @Override
//            public void barcodeResult(BarcodeResult result) {
//                String text = result.getText();
//                Toast.makeText(ReceiveSettingsActivity.this, text, Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void possibleResultPoints(List<ResultPoint> resultPoints) {
//
//            }
//        });

        ImageButton scanButton = (ImageButton) rootView.findViewById(R.id.scanbutton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator.forSupportFragment(ReceiveSettingsQRCodeFragment.this).initiateScan();
            }
        });
    }


}
