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


import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.helper.SettingsHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BackendErrorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BackendErrorFragment extends Fragment {
    private View rootView;
    private ExtendedFloatingActionButton buttonSettings;

    public static BackendErrorFragment newInstance() {
        BackendErrorFragment fragment = new BackendErrorFragment();
        return fragment;
    }

    public BackendErrorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_backend_error, container, false);
        init();

        return rootView;
    }

    private void init() {
        initFab();
        initTextViews();
    }

    private void initFab() {
        buttonSettings = rootView.findViewById(R.id.settings_button);

        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsHelper.loadKioskSettings(getActivity(), null);
            }
        });
    }

    private void initTextViews() {
        TextView info1 = rootView.findViewById(R.id.info1);
        info1.setMovementMethod(LinkMovementMethod.getInstance());

        TextView info2 = rootView.findViewById(R.id.info2);
        info2.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
