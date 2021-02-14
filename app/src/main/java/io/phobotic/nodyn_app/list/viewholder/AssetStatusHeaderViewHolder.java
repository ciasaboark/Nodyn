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

package io.phobotic.nodyn_app.list.viewholder;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.captain_miao.optroundcardview.OptRoundCardView;

import androidx.recyclerview.widget.RecyclerView;
import io.phobotic.nodyn_app.R;

/**
 * Created by Jonathan Nelson on 9/9/17.
 */

public class AssetStatusHeaderViewHolder extends RecyclerView.ViewHolder {
    public ImageView icon;
    public OptRoundCardView card;
    public TextView status;
    public TextView countTotal;

    public AssetStatusHeaderViewHolder(View view) {
        super(view);
        icon = view.findViewById(R.id.icon);
        card = view.findViewById(R.id.card);
        status = view.findViewById(R.id.status);
        countTotal = view.findViewById(R.id.count_total);
    }
}
