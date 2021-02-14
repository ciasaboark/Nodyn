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

package io.phobotic.nodyn_app.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.fragment.preference.SettingsQRCodeFragment;
import io.phobotic.nodyn_app.helper.StringHelper;

/**
 * TODO: document your custom view class.
 */
public class ShareSettingsQRCodeFragment extends Fragment {
    private static final String TAG = ShareSettingsQRCodeFragment.class.getSimpleName();
    private View rootView;
    private ImageView qrcode;
    private ProgressBar progressBar;
    private View progressBox;
    private int totalPages = 0;
    private List<String> parts;
    private boolean isCompressed = false;
    private ViewPager pager;

    public static ShareSettingsQRCodeFragment newInstance() {
        ShareSettingsQRCodeFragment fragment = new ShareSettingsQRCodeFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public ShareSettingsQRCodeFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_share_settings_qrcode, container, false);
        init();

        return rootView;
    }


    private void init() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Map<String, ?> allPrefs = prefs.getAll();
        Gson gson = new Gson();
        String json = gson.toJson(allPrefs);
        String compressedJson = "";
        try {
            compressedJson = StringHelper.compress(json);
            isCompressed = true;
        } catch (IOException e) {
        }

        if (isCompressed) {
            json = compressedJson;
        }

        //find out how many QR codes we need to generate.
        int maxLength = 200;
        parts = StringHelper.splitEqually(json, maxLength);
        totalPages = parts.size();

        pager = rootView.findViewById(R.id.pager);
        FragmentManager fm = getChildFragmentManager();
        PagerAdapter pagerAdapter = new SettingsQRCodePagerAdapter(fm);
        pager.setAdapter(pagerAdapter);
    }


    private class SettingsQRCodePagerAdapter extends FragmentStatePagerAdapter {
        public SettingsQRCodePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            String data = parts.get(position);
            if (data == null) {
                return new Fragment();
            } else {
                return SettingsQRCodeFragment.newInstance(data, position + 1, totalPages, isCompressed);
            }
        }

        @Override
        public int getCount() {
            return totalPages;
        }
    }
}
