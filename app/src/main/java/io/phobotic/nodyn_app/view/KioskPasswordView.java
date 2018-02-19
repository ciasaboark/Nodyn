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

package io.phobotic.nodyn_app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import io.phobotic.nodyn_app.R;

/**
 * Created by Jonathan Nelson on 10/1/17.
 */

public class KioskPasswordView extends RelativeLayout {
    private final Context context;
    private View rootView;
    private EditText input;

    public KioskPasswordView(Context context) {
        this(context, null);
    }

    public KioskPasswordView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_kiosk_password, this);
        input = (EditText) rootView.findViewById(R.id.input);
    }

    public String getPassword() {
        return input.getText().toString();
    }

    public EditText getInput() {
        return input;
    }
}
