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

package io.phobotic.nodyn_app.helper;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by Jonathan Nelson on 2/4/18.
 */

public class GridColumnHelper {
    private int width, height, remaining;
    private DisplayMetrics displayMetrics;

    public GridColumnHelper(Context context, int viewId) {
        View view = View.inflate(context, viewId, null);
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        width = view.getMeasuredWidth();
        height = view.getMeasuredHeight();
        displayMetrics = context.getResources().getDisplayMetrics();
    }

    public int calculateSpacing() {

        int numberOfColumns = calculateNoOfColumns();
        return remaining / (2 * numberOfColumns);
    }

    public int calculateNoOfColumns() {
        int numberOfColumns = displayMetrics.widthPixels / width;
        remaining = displayMetrics.widthPixels - (numberOfColumns * width);
        if (remaining / (2 * numberOfColumns) < 15) {
            numberOfColumns--;
            remaining = displayMetrics.widthPixels - (numberOfColumns * width);
        }
        return numberOfColumns;
    }
}
