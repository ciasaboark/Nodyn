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

package io.phobotic.nodyn_app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Created by Jonathan Nelson on 2/24/19.
 */
public class CenteredImageView extends AppCompatImageView {


    public CenteredImageView(Context context) {
        super(context);
    }

    public CenteredImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CenteredImageView(Context context, AttributeSet attrs, int id) {
        super(context, attrs, id);
    }

    @Override
    public void onMeasure(int measuredWidth, int measuredHeight) {
        super.onMeasure(measuredWidth, measuredHeight);
        ViewGroup parent = (ViewGroup) getParent();
        if (parent != null) {
            int width = parent.getMeasuredWidth() / 2;
            int height = parent.getMeasuredHeight() / 2;
            int size = Math.max(width, height);
            setMeasuredDimension(size, size);
        }
    }
}