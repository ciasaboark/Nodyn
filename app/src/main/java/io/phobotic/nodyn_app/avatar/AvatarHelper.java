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

package io.phobotic.nodyn_app.avatar;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.model.User;

import static android.graphics.Bitmap.createBitmap;

/**
 * Created by Jonathan Nelson on 3/19/18.
 */

public class AvatarHelper {
    private static final String TAG = AvatarHelper.class.getSimpleName();

    private List<AvatarProvider> getLoaders(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String enabledProvidersString = prefs.getString(context.getString(
                R.string.user_avatars_enabled_providers), "");
        ArrayList<String> enabledProviders = new ArrayList<>(Arrays.asList(enabledProvidersString.split(",")));

        List<AvatarProvider> loaders = new ArrayList<>();
        for (String className : enabledProviders) {
            if (className == null || className.equals("")) {
                continue;
            }
            try {
                Class<?> clazz = Class.forName(className);
                Constructor<?> ctor = clazz.getConstructor();
                Object object = ctor.newInstance();
                loaders.add((AvatarProvider) object);
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
            }
        }

        return loaders;
    }


    public void loadAvater(@NotNull Context context, @NotNull User user,
                           @NotNull ImageView imageView, @NotNull int size) {
        final List<AvatarProvider> avatarProviders = getLoaders(context);

        loadAvater(context, user, imageView, size, avatarProviders);

    }

    private void loadDefaultAvatar(Context context, ImageView imageView, int size) {
//        Picasso.with(context)
//                .load(R.drawable.default_user_avitar)
//                .fit()
//                .transform(getTransformation(context, size))
//                .into(imageView);
        imageView.setImageDrawable(context.getDrawable(R.drawable.default_user_avitar));
    }

    private void loadAvater(final Context context, final User user, final ImageView imageView,
                            final int size, final List<AvatarProvider> avatarProviders) {
        if (avatarProviders == null || avatarProviders.size() == 0) {
            loadDefaultAvatar(context, imageView, size);
        } else {
            AvatarProvider loader = avatarProviders.remove(0);
            String source = loader.fetchUserAvatar(user, size);

            //Picasso requires a non-empty path.  Just rely on the error handling
            if (source == null || source.equals("")) {
                source = "-";
            }

            Log.d(TAG, "Attempting to fetch avatar for user " + user.getUsername() + " from: " + source);

            final String finalSource = source;
            Picasso.with(context)
                    .load(source)
                    .resize(size, size)
                    .placeholder(R.drawable.grey_circle)
                    .transform(getTransformation(context, size))
//                    .transform(getSecondTransformation(context))
//                    .transform(new DropShadowTransformation())
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, String.format("Avatar for user: %s loaded successfully " +
                                    "from: %s", user.getUsername(), finalSource));
                        }

                        @Override
                        public void onError() {
                            Log.d(TAG, String.format("Failed to load avatar for user: %s " +
                                    "from: %s", user.getUsername(), finalSource));
                            loadAvater(context, user, imageView, size, avatarProviders);
                        }
                    });
        }
    }

    @NonNull
    private Transformation getTransformation(Context context, int size) {
        float borderWidth;
        if (size <= 48) {
            borderWidth = context.getResources().getDimension(R.dimen.picasso_tiny_image_circle_border_width);
        } else if (size > 48 && size <= 90) {
            borderWidth = context.getResources().getDimension(R.dimen.picasso_small_image_circle_border_width);
        } else if (size > 90 && size <= 128) {
            borderWidth = context.getResources().getDimension(R.dimen.picasso_large_image_circle_border_width);
        } else {
            borderWidth = context.getResources().getDimension(R.dimen.picasso_large_image_circle_border_width);
        }

        return new RoundedTransformationBuilder()
                .borderColor(context.getResources().getColor(R.color.white))
                .borderWidthDp(borderWidth)
                .oval(true)
                .build();
    }

    @NonNull
    private Transformation getSecondTransformation(Context context) {
        return new RoundedTransformationBuilder()
                .borderColor(Color.parseColor("#000000"))
                .borderWidthDp(.5f)
                .oval(true)
                .build();
    }

    public enum Size {
        TINY,
        SMALL,
        LARGE,
        HUGE
    }

    public class AlphaTransformation implements Transformation {

        /* The shade alpha of black to apply */
        private int mAlpha;

        /**
         * Integer Constructor
         *
         * @param alpha the alpha shade to apply
         */
        public AlphaTransformation(int alpha) {
            // Clamp the alpha value to 0..255
            mAlpha = Math.max(0, Math.min(255, alpha));
        }

        /**
         * Float Constructor
         *
         * @param percent the alpha percentage from 0..1
         */
        public AlphaTransformation(float percent) {
            // Clamp the float value to 0..1
            mAlpha = (int) ((Math.max(0, Math.min(1, percent))) * 255);
        }

        /**
         * Transform the source bitmap into a new bitmap. If you create a new bitmap instance, you must
         * call {@link android.graphics.Bitmap#recycle()} on {@code source}. You may return the original
         * if no transformation is required.
         */
        @Override
        public Bitmap transform(Bitmap source) {
            Paint paint = new Paint();
            Bitmap output =
                    createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);

            // Create canvas and draw the source image
            Canvas canvas = new Canvas(output);
            canvas.drawBitmap(source, 0, 0, paint);

            // Setup the paint for painting the shade
            paint.setColor(Color.BLACK);
            paint.setAlpha(mAlpha);

            // Paint the shade
            canvas.drawPaint(paint);

            // Recycle and return
            source.recycle();
            return output;
        }

        /**
         * Returns a unique key for the transformation, used for caching purposes. If the transformation
         * has parameters (e.g. size, scale factor, etc) then these should be part of the key.
         */
        @Override
        public String key() {
            return "shade:" + mAlpha;
        }
    }


    public class DropShadowTransformation implements Transformation {


        @Override
        public Bitmap transform(Bitmap source) {
            Bitmap result = source.copy(source.getConfig(), true);
            final Bitmap shadow = addShadow(result, source.getHeight(), source.getWidth(), Color.BLACK, 1, 1, 1);


            source.recycle();
            result.recycle();
            return shadow;
        }

        @Override
        public String key() {
            return "dropshadow";
        }

        public Bitmap addShadow(final Bitmap bm, final int dstHeight, final int dstWidth, int color, int size, float dx, float dy) {
            final Bitmap mask = createBitmap(dstWidth, dstHeight, Bitmap.Config.ALPHA_8);

            final Matrix scaleToFit = new Matrix();
            final RectF src = new RectF(0, 0, bm.getWidth(), bm.getHeight());
            final RectF dst = new RectF(0, 0, dstWidth - dx, dstHeight - dy);
            scaleToFit.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);

            final Matrix dropShadow = new Matrix(scaleToFit);
            dropShadow.postTranslate(dx, dy);

            final Canvas maskCanvas = new Canvas(mask);
            final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            maskCanvas.drawBitmap(bm, scaleToFit, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
            maskCanvas.drawBitmap(bm, dropShadow, paint);

            final BlurMaskFilter filter = new BlurMaskFilter(size, BlurMaskFilter.Blur.NORMAL);
            paint.reset();
            paint.setAntiAlias(true);
            paint.setColor(color);
            paint.setMaskFilter(filter);
            paint.setFilterBitmap(true);

            final Bitmap ret = createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
            final Canvas retCanvas = new Canvas(ret);
            retCanvas.drawBitmap(mask, 0, 0, paint);
            retCanvas.drawBitmap(bm, scaleToFit, null);
            mask.recycle();
            return ret;
        }
    }
}

