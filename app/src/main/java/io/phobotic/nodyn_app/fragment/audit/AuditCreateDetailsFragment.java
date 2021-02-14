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

package io.phobotic.nodyn_app.fragment.audit;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import io.phobotic.nodyn_app.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AuditCreateDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AuditCreateDetailsFragment extends Fragment {
    private AuditCreationListener listener;
    private View rootView;
    private EditText name;
    private EditText description;
    private CheckBox blindCheckbox;

    public static AuditCreateDetailsFragment newInstance() {
        AuditCreateDetailsFragment fragment = new AuditCreateDetailsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public AuditCreateDetailsFragment() {
        // Required empty public constructor
    }

    public AuditCreateDetailsFragment setListener(AuditCreationListener listener) {
        this.listener = listener;
        return this;
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
        rootView = inflater.inflate(R.layout.fragment_audit_create_details, container, false);
        init();

        return rootView;
    }

    private void init() {
        findViews();
        initListeners();
    }

    private void findViews() {
        name = rootView.findViewById(R.id.name);
        description = rootView.findViewById(R.id.description);
        blindCheckbox = rootView.findViewById(R.id.blind_checkbox);
    }

    private void initListeners() {
        blindCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onDetailsChanged();
            }
        });

        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                onDetailsChanged();
            }
        });

        description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                onDetailsChanged();
            }
        });
    }

    private void onDetailsChanged() {
        if (listener != null) {
            listener.onAuditDetailsEntered(name.getText().toString(), description.getText().toString(),
                    blindCheckbox.isChecked());
        }
    }


}
