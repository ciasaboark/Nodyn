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

package io.phobotic.nodyn.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import org.jetbrains.annotations.NotNull;

import io.phobotic.nodyn.R;

/**
 * Created by Jonathan Nelson on 9/26/17.
 */

public class EnableKioskDialogView extends RelativeLayout {
    private static final String TAG = EnableKioskDialogView.class.getSimpleName();
    private final Context context;
    private View rootView;
    private EditText passwordInput;
    private OnPasswordChangedListener listener;

    public EnableKioskDialogView(@NotNull Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.dialog_enable_kiosk, this);
        passwordInput = (EditText) rootView.findViewById(R.id.password);

        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (listener != null) {
                    listener.onPasswordChanged(s.toString());
                }
            }
        });
    }

    public
    @Nullable
    String getPassword() {
        String password = null;
        if (passwordInput != null) {
            password = passwordInput.getText().toString();
        }

        return password;
    }

    public EnableKioskDialogView setOnPasswordChangedListener(OnPasswordChangedListener listener) {
        this.listener = listener;
        return this;
    }

    public EditText getPasswordInput() {
        return passwordInput;
    }

    public interface OnPasswordChangedListener {
        void onPasswordChanged(String newPassword);
    }
}
