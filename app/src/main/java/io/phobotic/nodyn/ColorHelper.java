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

package io.phobotic.nodyn;

import android.content.Context;
import android.graphics.Color;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Jonathan Nelson on 10/11/17.
 */

public class ColorHelper {
    public static int getValueTextColorForBackground(@NotNull Context context, int statusColor) {
        int red = Color.red(statusColor);
        int green = Color.green(statusColor);
        int blue = Color.blue(statusColor);
        int alpha = Color.alpha(statusColor);

        double a = 1 - (0.299 * red + 0.587 * green + 0.114 * blue) / 255;

        if (a < 0.5) {
            return context.getResources().getColor(android.R.color.primary_text_light);
        } else {
            return context.getResources().getColor(android.R.color.primary_text_dark);
        }
    }
}
