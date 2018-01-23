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

package io.phobotic.nodyn.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

import io.phobotic.nodyn.R;

/**
 * Created by Jonathan Nelson on 1/22/18.
 */

public class EmailRecipentsPreference extends DialogPreference {

    private String recipentsString;

    public EmailRecipentsPreference(Context context) {
        this(context, null);
    }

    public EmailRecipentsPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public EmailRecipentsPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, defStyleAttr);
    }

    public EmailRecipentsPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public String getRecipientsString() {
        return recipentsString;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        // Default value from attribute. Fallback value is set to 0.
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue,
                                     Object defaultValue) {
        // Read the value. Use the default value if it is not possible.
        if (restorePersistedValue) {
            setRecipentsString(getPersistedString(null));
        } else {
            setRecipentsString((String) defaultValue);
        }
    }

    public void setRecipentsString(String recipents) {
        this.recipentsString = recipents;
        persistString(recipents);
        setSummary(recipents);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.preference_email_recipients_dialog;
    }
}
