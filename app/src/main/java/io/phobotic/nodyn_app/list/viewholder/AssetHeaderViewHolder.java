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

package io.phobotic.nodyn_app.list.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import io.phobotic.nodyn_app.R;

/**
 * Created by Jonathan Nelson on 9/9/17.
 */

public class AssetHeaderViewHolder extends RecyclerView.ViewHolder {
    public TextView manufacturer;
    public TextView model;
    public TextView countAvailable;
    public TextView countAssigned;

    public AssetHeaderViewHolder(View view) {
        super(view);

        manufacturer = (TextView) view.findViewById(R.id.manufacturer);
        model = (TextView) view.findViewById(R.id.model);
        countAvailable = (TextView) view.findViewById(R.id.count_available);
        countAssigned = (TextView) view.findViewById(R.id.count_assigned);
    }
}
