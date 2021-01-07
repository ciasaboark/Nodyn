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

package io.phobotic.nodyn_app.helper;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.phobotic.nodyn_app.R;

/**
 * Created by Jonathan Nelson on 10/29/17.
 */

public class AnimationHelper {
    public static void fadeOut(@NotNull final Context context, @NotNull final View v) {
        fadeOut(context, v, View.GONE, null);
    }

    public static void fadeOut(@NotNull final Context context, @NotNull final View v, @Nullable AnimateListener listener) {
        fadeOut(context, v, View.GONE, listener);
    }

    public static void scaleIn(@NotNull View view) {
        if (view.getVisibility() == View.VISIBLE) {
            return;
        }

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0, 1);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0, 1);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(scaleX, scaleY);
        animSetXY.setInterpolator(new DecelerateInterpolator());
        animSetXY.setDuration(300);
        view.setVisibility(View.VISIBLE);
        animSetXY.start();
    }

    public static void scaleOut(@NotNull final View view) {
        if (view.getVisibility() == View.GONE) {
            return;
        }
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1, 0);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1, 0);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(scaleX, scaleY);
        animSetXY.setInterpolator(new DecelerateInterpolator());
        animSetXY.setDuration(300);
        animSetXY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSetXY.start();
    }

    public interface AnimateListener {
        void onAnimationFinished();
    }

    private static void fadeOut(@NotNull final Context context, @NotNull final View v, final int mode, @Nullable final AnimateListener listener) {
        if (v.getVisibility() != View.VISIBLE) {
            if (listener != null) listener.onAnimationFinished();
            return;
        }

        Animation fadeOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(mode);
                if (listener != null) listener.onAnimationFinished();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(fadeOut);
    }

    public static void fadeOutInvisible(@NotNull final Context context, @NotNull final View v) {
        fadeOutInvisible(context, v, null);
    }
    public static void fadeOutInvisible(@NotNull final Context context, @NotNull final View v, AnimateListener listener) {
        fadeOut(context, v, View.INVISIBLE, listener);
    }



    public static void scaleUp(@NonNull final Context context, @NotNull final View v) {
        if (v.getVisibility() == View.VISIBLE) {
            return;
        }

        Animation scaleUp = AnimationUtils.loadAnimation(context, R.anim.scale_up);
        scaleUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        v.startAnimation(scaleUp);
    }

    public static void scaleDown(@NonNull final Context context, @NotNull final View v) {
        if (v.getVisibility() == View.VISIBLE) {
            return;
        }

        Animation scaleUp = AnimationUtils.loadAnimation(context, R.anim.scale_down);
        scaleUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        v.startAnimation(scaleUp);
    }

    public static void expandAndFadeIn(@NotNull final Context context, @NotNull final View v) {
        if (v.getVisibility() == View.VISIBLE) {
            return;
        }

        long duration = 300;
        Animation fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        fadeIn.setDuration(duration);

        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int height = v.getMeasuredHeight();
        ValueAnimator expandAnimator = ValueAnimator.ofInt(0, height);
        expandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                v.getLayoutParams().height = value.intValue();
                v.requestLayout();

                float alpha = ((float) height / (float) value);
                v.setAlpha(alpha);
            }
        });
        expandAnimator.setDuration(duration);


        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        v.startAnimation(fadeIn);
        expandAnimator.start();
    }

    public static void collapseAndFadeOut(@NotNull final Context context, @NotNull final View v) {
        if (v.getVisibility() != View.VISIBLE) {
            return;
        }

        long duration = 300;
        Animation fadeOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        fadeOut.setDuration(duration);

        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int height = v.getMeasuredHeight();
        ValueAnimator collapseAnimator = ValueAnimator.ofInt(height, 0);
        collapseAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                v.getLayoutParams().height = value.intValue();
                v.requestLayout();

                float alpha = ((float) height / (float) value);
                v.setAlpha(alpha);
            }
        });
        collapseAnimator.setDuration(duration);

        collapseAnimator.addListener(new Animator.AnimatorListener() {
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

        v.startAnimation(fadeOut);
        collapseAnimator.start();
    }

    public static void fadeIn(@NotNull final Context context, @NotNull final View v) {
        fadeIn(context, v, null);
    }
    public static void fadeIn(@NotNull final Context context, @NotNull final View v, @Nullable final AnimateListener listener) {
        if (v.getVisibility() == View.VISIBLE) {
            if (listener != null) listener.onAnimationFinished();
            return;
        }

        Animation fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (listener != null) listener.onAnimationFinished();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(fadeIn);
    }

    public static void expand(final View v) {
        if (v.getVisibility() == View.VISIBLE) {
            return;
        }

        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int height = v.getMeasuredHeight();
        ValueAnimator anim = ValueAnimator.ofInt(0, height);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                v.getLayoutParams().height = value.intValue();
                v.requestLayout();

                float alpha = ((float) height / (float) value);
                v.setAlpha(alpha);
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

        anim.start();
    }

    public static void collapse(final View v) {
        if (v.getVisibility() != View.VISIBLE) {
            return;
        }

        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int height = v.getMeasuredHeight();
        ValueAnimator anim = ValueAnimator.ofInt(height, 0);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                v.getLayoutParams().height = value.intValue();
                v.requestLayout();

                float alpha = ((float) height / (float) value);
                v.setAlpha(alpha);
            }
        });

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(View.GONE);
                v.setAlpha(1.0f);
                v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                v.getLayoutParams().height = v.getMeasuredHeight();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        anim.start();
    }
}
