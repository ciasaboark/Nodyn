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

package io.phobotic.nodyn.preference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.phobotic.nodyn.R;

/**
 * Created by Jonathan Nelson on 10/24/17.
 */

public class WarningPreference extends Preference {
    private View rootView;
    private TextView topMessage;
    private TextView bottomMessage;


    public WarningPreference(Context context) {
        this(context, null);
    }

    public WarningPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        super.onCreateView(parent);

        LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootView = li.inflate(R.layout.preference_warning, parent, false);
        init();

        return rootView;
    }

    private void init() {
        topMessage = (TextView) rootView.findViewById(R.id.message_top);
        bottomMessage = (TextView) rootView.findViewById(R.id.message_bottom);
    }

    public void setTopMessage(String message) {
        topMessage.setText(message);
    }

    public void setBottomMessage(String message) {
        bottomMessage.setText(message);
    }

}
