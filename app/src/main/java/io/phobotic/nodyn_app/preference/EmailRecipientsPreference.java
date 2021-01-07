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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;
import io.phobotic.nodyn_app.R;

/**
 * Created by Jonathan Nelson on 1/22/18.
 */

public class EmailRecipientsPreference extends DialogPreference {

    private String recipientsString;

    public EmailRecipientsPreference(Context context) {
        this(context, null);
    }

    public EmailRecipientsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public String getRecipientsString() {
        return recipientsString;
    }

    public void setRecipientsString(String recipents) {
        this.recipientsString = recipents;
        persistString(recipents);

        setSummary((recipents == null || recipents.length() == 0) ? "No recipients selected" : recipents);
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
            setRecipientsString(getPersistedString(null));
        } else {
            setRecipientsString((String) defaultValue);
        }
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.view_email_recipients_dialog;
    }
}
