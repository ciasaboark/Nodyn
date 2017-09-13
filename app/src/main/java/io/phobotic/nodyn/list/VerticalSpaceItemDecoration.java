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

package io.phobotic.nodyn.list;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Jonathan Nelson on 7/29/17.
 */

public class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final float verticalSpaceBottom;
    private final float verticalSpaceTop;

    public VerticalSpaceItemDecoration(float verticalSpaceTop, float verticalSpaceBottom) {
        this.verticalSpaceTop = verticalSpaceTop;
        this.verticalSpaceBottom = verticalSpaceBottom;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        outRect.top = (int) verticalSpaceTop;

        if (parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = (int) verticalSpaceBottom;
        }
    }
}
