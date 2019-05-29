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

package io.phobotic.nodyn_app.preference;

import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;

import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;
import io.phobotic.nodyn_app.R;


/**
 * Created by Jonathan Nelson on 1/23/18.
 */

public class NumberPickerPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    private static final int MIN_VALUE = 0;
    private static final int MAX_VALUE = 30;
    private static final String ARG_DURATION_TEXT_SINGLE = "duration_text_single";
    private static final String ARG_DURATION_TEXT_MULTI = "duration_text_multi";
    private static final String ARG_ZERO_VAL_TEXT = "zero_val_text";
    private View rootView;
    private NumberPicker picker;
    private int value = MIN_VALUE;
    private String durationTextSingle = "";
    private String durationTextMulti = "";
    private String zeroValText;

    public static NumberPickerPreferenceDialogFragmentCompat newInstance(
            String key, String durationTextSingle, String durationTextMulti, String zeroValText) {
        NumberPickerPreferenceDialogFragmentCompat fragment = new NumberPickerPreferenceDialogFragmentCompat();
        Bundle b = new Bundle();
        b.putString(ARG_KEY, key);
        b.putString(ARG_DURATION_TEXT_SINGLE, durationTextSingle);
        b.putString(ARG_DURATION_TEXT_MULTI, durationTextMulti);
        b.putString(ARG_ZERO_VAL_TEXT, zeroValText);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            durationTextSingle = getArguments().getString(ARG_DURATION_TEXT_SINGLE, "");
            durationTextMulti = getArguments().getString(ARG_DURATION_TEXT_MULTI, "");
            zeroValText = getArguments().getString(ARG_ZERO_VAL_TEXT, null);
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        rootView = view;

        init();
    }

    private void init() {
        DialogPreference preference = getPreference();
        if (preference instanceof NumberPickerPreference) {
            value = ((NumberPickerPreference) preference).getSelectedValue();

        }

        findViews();

        initPicker();
    }

    private void findViews() {
        picker = rootView.findViewById(R.id.picker);
    }

    private void initPicker() {
        picker.setWrapSelectorWheel(false);
        picker.setMinValue(MIN_VALUE);
        picker.setMaxValue(MAX_VALUE);
        picker.setValue(value);

        String[] valueNames = new String[31];
        for (int i = 0; i < 31; i++) {
            String val;
            if (i == 0) {
                if (zeroValText == null) {
                    val = "Never";
                } else {
                    val = zeroValText;
                }
            } else if (i == 1) {
                val = String.format("%d %s", i, durationTextSingle);
            } else {
                val = String.format("%d %s", i, durationTextMulti);
            }
            valueNames[i] = val;
        }

        picker.setDisplayedValues(valueNames);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {

            DialogPreference preference = getPreference();
            if (preference instanceof NumberPickerPreference) {
                int val = picker.getValue();
                if (preference.callChangeListener(val)) {
                    // Save the value
                    ((NumberPickerPreference) preference).setSelectedValue(val);
                }
            }
        }
    }
}
