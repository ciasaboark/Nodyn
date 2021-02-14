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
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.helper.AnimationHelper;

/**
 * Created by Jonathan Nelson on 3/2/19.
 */
public class StepView extends LinearLayout {
    private final View rootView;
    String title;
    private ProgressBar progress;
    private ImageView check;
    private TextView text;
    private boolean isComplete = false;
    private boolean isInProgress = false;
    @ColorInt
    private int activeTextColor;
    @ColorInt
    private int inactiveTextColor;
    @ColorInt
    private int completeTextColor;
    @ColorInt
    private int failedTextColor;
    private float inactiveAlpha = 1.0f;
    private float activeAlpha = 1.0f;
    private int curTextColor;
    private float curAlpha;

    public StepView(Context context) {
        this(context, null);
    }

    public StepView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        rootView = inflate(context, R.layout.view_step, this);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs) {
        findViews();
        initViews(attrs);
    }

    private void findViews() {
        progress = rootView.findViewById(R.id.progress);
        check = rootView.findViewById(R.id.check);
        text = rootView.findViewById(R.id.text);
    }

    /**
     * By default this view will begin in a disabled state
     */
    private void initViews(@Nullable AttributeSet attrs) {
        //set some sensible defaults
        activeTextColor = getContext().getResources().getColor(android.R.color.primary_text_light);
        completeTextColor = activeTextColor;
        inactiveTextColor = activeTextColor;


        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.StepView);
            activeTextColor = ta.getColor(R.styleable.StepView_active_text_color, activeTextColor);
            inactiveTextColor = ta.getColor(R.styleable.StepView_inactive_text_color, inactiveTextColor);
            completeTextColor = ta.getColor(R.styleable.StepView_complete_text_color, completeTextColor);
            failedTextColor = getContext().getResources().getColor(R.color.warning_strong);

            activeAlpha = ta.getFloat(R.styleable.StepView_alpha_active, activeAlpha);
            inactiveAlpha = ta.getFloat(R.styleable.StepView_alpha_inactive, inactiveAlpha);


            title = ta.getString(R.styleable.StepView_text);
        }

        curAlpha = inactiveAlpha;
        curTextColor = inactiveTextColor;

        text.setText(title);

        setState(State.Inactive, false);


    }

    private void setState(State state, boolean animateChanges) {
        @ColorInt int newTextColor;
        float newAlpha;
        boolean hideProgress = false;
        boolean hideCheck = true;

        Drawable d = null;

        switch (state) {
            case Active:
                newTextColor = activeTextColor;
                newAlpha = activeAlpha;
                hideCheck = true;
                hideProgress = false;
                break;
            case Inactive:
                newTextColor = inactiveTextColor;
                newAlpha = inactiveAlpha;
                hideCheck = true;
                hideProgress = true;
                break;
            case Complete:
                d = getResources().getDrawable(R.drawable.animated_check);
                newTextColor = completeTextColor;
                newAlpha = activeAlpha;
                hideCheck = false;
                hideProgress = true;
                break;
            case Failed:
                d = getResources().getDrawable(R.drawable.exclamation);
                d.setTint(failedTextColor);
                newTextColor = failedTextColor;
                newAlpha = activeAlpha;
                hideCheck = false;
                hideProgress = true;
                break;
            default:
                newTextColor = activeTextColor;
                newAlpha = activeAlpha;
                hideCheck = true;
                hideProgress = false;
        }

        check.setImageDrawable(d);


        if (!animateChanges) {
            text.setTextColor(newTextColor);
            text.setAlpha(newAlpha);

            if (hideProgress) {
                progress.setVisibility(View.GONE);
            } else {
                progress.setVisibility(View.VISIBLE);
            }

            if (hideCheck) {
                check.setVisibility(View.GONE);
            } else {
                check.setVisibility(View.VISIBLE);
                if (check.getDrawable() instanceof Animatable) {
                    ((Animatable) check.getDrawable()).start();
                }
            }
        } else {
            ObjectAnimator colorAnim = ObjectAnimator.ofInt(text, "textColor",
                    curTextColor, newTextColor);
            colorAnim.setEvaluator(new ArgbEvaluator());
            colorAnim.setDuration(300);
            colorAnim.start();

            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(text, "alpha",
                    curAlpha, newAlpha);
            alphaAnim.setEvaluator(new FloatEvaluator());
            alphaAnim.setDuration(300);
            alphaAnim.start();

            if (hideProgress) {
//                AnimationHelper.fadeOut(getContext(), progress);
                progress.setVisibility(View.GONE);
            } else {
                AnimationHelper.fadeIn(getContext(), progress);
            }

            if (hideCheck) {
                AnimationHelper.fadeOut(getContext(), check);
            } else {
                AnimationHelper.fadeIn(getContext(), check);
                if (check.getDrawable() instanceof Animatable) {
                    ((Animatable) check.getDrawable()).start();
                }
            }
        }


        curTextColor = newTextColor;
        curAlpha = newAlpha;
    }

    public void fail() {
        setState(State.Failed);
    }

    private void setState(State state) {
        setState(state, true);
    }

    public void reset() {
        setState(State.Inactive);
    }

    public void start() {
        setState(State.Active);
    }

    public void complete() {
        setState(State.Complete);
    }

    private enum State {
        Inactive,
        Active,
        Complete,
        Failed
    }
}
