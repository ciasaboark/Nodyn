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
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import io.phobotic.nodyn.R;

/**
 * Created by Jonathan Nelson on 10/26/17.
 */

public class LoadingView extends RelativeLayout {
    private static final String TAG = LoadingView.class.getSimpleName();
    private final View rootView;

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        rootView = inflate(context, R.layout.view_loading, this);
    }


}
