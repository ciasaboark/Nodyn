/*
 * Copyright (c) 2020 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.helper;

import android.content.Context;
import android.graphics.drawable.Drawable;


import androidx.annotation.ColorInt;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import io.phobotic.nodyn_app.R;

/**
 * Created by Jonathan Nelson on 2020-02-17.
 * Loop through all icons in the given preference and change the icon tint
 */
public class PreferenceHelper {
    public static void tintIcons(Context context, Preference preference) {
        if (preference == null) {
            return;
        }

        if (preference instanceof PreferenceGroup) {
            PreferenceGroup g = (PreferenceGroup) preference;
            for (int i = 0; i < ((PreferenceGroup) preference).getPreferenceCount(); i++) {
                Preference p = ((PreferenceGroup) preference).getPreference(i);
                tintIcons(context, p);
            }
        } else {
            Drawable d = preference.getIcon();
            if (d != null) {
                @ColorInt int tint = context.getResources().getColor(R.color.pref_icon_tint);
                d.setTint(tint);
            }
        }
    }
}
