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
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import io.phobotic.nodyn_app.R;


/**
 * Created by Jonathan Nelson on 10/8/17.
 */

public class ModelOverviewCountView extends RelativeLayout {
    private final Context context;
    private View rootView;
    private TextView model;
    private TextView manufacturer;
    private TextView count;
    private View wrapper;
    private CardView card;

    public ModelOverviewCountView(Context context, String name, int count) {
        this(context, name, count, null);
    }

    public ModelOverviewCountView(Context context, String name, int count, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
        setModel(name);
        setCount(count);
    }

    private void init() {
        rootView = inflate(context, R.layout.view_model_overview_count, this);
        wrapper = rootView.findViewById(R.id.wrapper);
        count = rootView.findViewById(R.id.count);
        model = rootView.findViewById(R.id.model);
        manufacturer = rootView.findViewById(R.id.manufacturer);
        card = rootView.findViewById(R.id.card);
    }

    public void setModel(String model) {
        this.model.setText(model);
    }

    public void setCount(int count) {
        this.count.setText(String.valueOf(count));
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer.setText(manufacturer);
    }

    public void setColor(String color) throws IllegalArgumentException {
        int c = Color.parseColor(color);
        setColor(c);
    }

    public void setColor(int color) {
        if (card != null) {
            card.setCardBackgroundColor(color);
        } else {
            wrapper.setBackgroundColor(color);
        }
    }

    public void setTextColor(int color) {
        count.setTextColor(color);
        model.setTextColor(color);
        manufacturer.setTextColor(color);
    }

}
