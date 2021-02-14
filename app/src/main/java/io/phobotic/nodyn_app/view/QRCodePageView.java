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

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.phobotic.nodyn_app.R;

/**
 * Created by Jonathan Nelson on 2/23/19.
 */
public class QRCodePageView extends RelativeLayout {
    private final View rootView;
    private TextView label;
    private ImageView check;
    private int pageNumber;
    private boolean isScanned = false;
    private View circle;

    public QRCodePageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        rootView = inflate(context, R.layout.view_qr_page, this);
        init();
    }

    private void init() {
        label = rootView.findViewById(R.id.label);
        check = rootView.findViewById(R.id.check);
        circle = rootView.findViewById(R.id.circle);
        setScanned(false);
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        label.setText(String.valueOf(pageNumber));
    }

    public boolean isScanned() {
        return isScanned;
    }

    public void setScanned(boolean isScanned) {
        if (this.isScanned == isScanned) {
            //avoid any unnecessary view changes
            return;
        }

        int startColor;
        int endColor;
        if (isScanned) {
            startColor = getResources().getColor(R.color.qr_unread);
            endColor = getResources().getColor(R.color.qr_read);
            check.setVisibility(View.VISIBLE);
        } else {
            startColor = getResources().getColor(R.color.qr_read);
            endColor = getResources().getColor(R.color.qr_unread);
            check.setVisibility(View.INVISIBLE);
        }

        final ObjectAnimator animator = ObjectAnimator.ofInt(circle, "backgroundTint", startColor, endColor);
        animator.setDuration(2000L);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setInterpolator(new DecelerateInterpolator(2));
        animator.addUpdateListener(new ObjectAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                circle.setBackgroundTintList(ColorStateList.valueOf(animatedValue));
            }
        });
        animator.start();
    }
}