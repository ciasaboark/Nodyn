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

package io.phobotic.nodyn_app.list.decorator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by Jonathan Nelson on 3/11/19.
 */
public class SectionedListDecoration extends RecyclerView.ItemDecoration {
    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};
    private final Set<Class> classesToDecorate;

    private Drawable divider;
    private int leftAddition;
    private int rightAddition;

    public SectionedListDecoration(Context context, float leftAdditionDp, float rightAdditionDp,
                                   Class... classesToDecorate) {
        if (classesToDecorate == null) {
            classesToDecorate = new Class[]{};
        }

        Set<Class> classes = new HashSet<>();
        classes.addAll(Arrays.asList(classesToDecorate));

        this.classesToDecorate = classes;

        TypedArray a = context.obtainStyledAttributes(ATTRS);
        divider = a.getDrawable(0);
        a.recycle();

        this.leftAddition = (int) (context.getResources().getDisplayMetrics().density * leftAdditionDp);
        this.rightAddition = (int) (context.getResources().getDisplayMetrics().density * rightAdditionDp);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent) {
        int left = parent.getPaddingLeft() + leftAddition;
        int right = parent.getWidth() - parent.getPaddingRight() - rightAddition;

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            //do not add the decoration to the last item in the list
            View child = parent.getChildAt(i);
            if (parent.getChildAdapterPosition(child) == parent.getAdapter().getItemCount() - 1) {
                continue;
            }

            //do not add decoration to any child views types we have been instructed to ignore
            Class childClass = child.getClass();
            if (classesToDecorate.contains(childClass)) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + divider.getIntrinsicHeight();

                divider.setBounds(left, top, right, bottom);
                divider.draw(c);
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        outRect.set(0, 0, 0, divider.getIntrinsicHeight());
    }
}