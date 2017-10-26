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

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Random;

import io.phobotic.nodyn.R;

/**
 * Created by Jonathan Nelson on 10/24/17.
 */

public class PreferenceSection extends LinearLayout {
    private static final String TAG = ActionView.class.getSimpleName();
    private final Context context;
    private View rootView;
    private TextView titleTextView;
    private ImageView iconImageView;
    private String title;
    private int iconResID;
    private boolean highlighted = false;
    private ImageView backArrow;

    public PreferenceSection(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        init(attrs);
    }

    private void init(AttributeSet attrs) {
        rootView = inflate(context, R.layout.view_pref_section, this);
        backArrow = (ImageView) rootView.findViewById(R.id.back);
        iconImageView = (ImageView) rootView.findViewById(R.id.icon);
        titleTextView = (TextView) rootView.findViewById(R.id.title);

        TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.section);
        title = arr.getString(R.styleable.section_title);
        iconResID = arr.getResourceId(R.styleable.section_icon, R.drawable.ic_alert_outline_white_36dp);

        arr.recycle();


        setFields();
    }

    private void setFields() {
        if (!isInEditMode()) {
            Drawable d = getResources().getDrawable(iconResID, null);
            this.iconImageView.setImageDrawable(d);
//            iconImageView.setColorFilter(new PorterDuffColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY));
            this.titleTextView.setText(title);
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(l);
//        rootView.setOnClickListener(l);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            return performClick();
        }
        return true;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;

        if (highlighted) {
            showBackArrow();
        } else {
            hideBackArrow();
        }
    }

    private void showBackArrow() {
        if (backArrow.getVisibility() == View.VISIBLE) {
            return;
        }

        expand(backArrow);
    }

    private void hideBackArrow() {
        if (backArrow.getVisibility() != View.VISIBLE) {
            return;
        }

        collapse(backArrow);
    }


    public void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final float height = v.getMeasuredHeight();
        final float width = v.getMeasuredWidth();

        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.setDuration(400);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                float calculatedWidth = value * width;
                float calculatedHeight = value * height;
                float calculatedAplha = value;

                v.getLayoutParams().height = (int) calculatedHeight;
                v.getLayoutParams().width = (int) calculatedWidth;
                v.requestLayout();

                v.setAlpha(calculatedAplha);
            }
        });

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        Random random = new Random();
        long offset = random.nextInt(500);
        anim.setStartDelay(offset);

        anim.start();
    }

    public void collapse(final View v) {
        v.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final float height = v.getMeasuredHeight();
        final float width = v.getMeasuredWidth();

        ValueAnimator anim = ValueAnimator.ofFloat(1, 0);
        anim.setDuration(400);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                float calculatedWidth = value * width;
                float calculatedHeight = value * height;
                float calculatedAplha = value;

                v.getLayoutParams().height = (int) calculatedHeight;
                v.getLayoutParams().width = (int) calculatedWidth;
                v.requestLayout();

                v.setAlpha(calculatedAplha);
            }
        });

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        Random random = new Random();
        long offset = random.nextInt(500);
        anim.setStartDelay(offset);

        anim.start();
    }
}

