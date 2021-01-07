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

package io.phobotic.nodyn_app.fragment.asset;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.palette.graphics.Palette;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.StatusNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.fragment.ActionHistoryFragment;
import io.phobotic.nodyn_app.helper.ColorHelper;
import io.phobotic.nodyn_app.transformer.RoundedTransformation;
import io.phobotic.nodyn_app.transformer.ZoomOutPageTransformer;

/**
 * Created by Jonathan Nelson on 7/15/17.
 */

public class AssetDetailsFragment extends DialogFragment {
    private static final String TAG = AssetDetailsFragment.class.getSimpleName();
    private Asset asset;
    private View rootView;

    private TextView tag;
    private TextView status;
    private ImageView image;
    private ViewPager pager;
    private TabLayout tabs;
    private View headerBox;

    public static AssetDetailsFragment newInstance(Asset asset) {
        AssetDetailsFragment f = new AssetDetailsFragment();
        f.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        Bundle bundle = new Bundle();
        bundle.putSerializable("asset", asset);
        f.setArguments(bundle);

        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_asset_details, container, false);
        init();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void init() {
        tag = rootView.findViewById(R.id.tag);
        headerBox = rootView.findViewById(R.id.header_box);

        status = rootView.findViewById(R.id.status);

        image = rootView.findViewById(R.id.image);

        tabs = rootView.findViewById(R.id.tabs);
        pager = rootView.findViewById(R.id.pager);
        FragmentManager fm = getChildFragmentManager();
        PagerAdapter pagerAdapter = new AssetPagerAdapter(fm);
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(1);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        pager.setPageTransformer(true, new ZoomOutPageTransformer());

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        setFields();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean useStatusColor = prefs.getBoolean(getResources().getString(
                R.string.pref_key_asset_status_color), Boolean.parseBoolean(
                getResources().getString(R.string.pref_default_asset_status_color)));

//        if (useStatusColor) {
//            fadeInStatusColor();
//        }

    }

    private void setFields() {
        unHideAllViews();
        if (asset != null) {
            setTextOrHide(tag, tag, asset.getTag());
            Database db = Database.getInstance(getContext());
            String statusText = null;
            try {
                Status s = db.findStatusByID(asset.getStatusID());
                statusText = s.getName();
            } catch (StatusNotFoundException e) {
                Log.d(TAG, "Unknown status: " + asset.getStatusID());
            }
            setTextOrHide(status, status, statusText);

            loadImage();
        }
    }

//    private void fadeInStatusColor() {
//        TypedValue typedValue = new TypedValue();
//        Resources.Theme theme = getContext().getTheme();
//        theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
//        @ColorInt int color = typedValue.data;
//        try {
//            Database db = Database.getInstance(getContext());
//            List<Status> statuses = db.getStatuses();
//            for (Status status : statuses) {
//                if (status.getId() == asset.getStatusID()) {
//                    color = Color.parseColor(status.getColor());
//                    break;
//                }
//            }
//
//            int[] colors = {
//                    color
//            };
//            Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
//            bitmap.eraseColor(color);
//            Palette p = Palette.from(bitmap).generate();
//
//            Palette.Swatch swatch = p.getVibrantSwatch();
//            if (swatch == null) {
//                swatch = p.getDominantSwatch();
//            }
//
//            if (swatch == null) {
//                swatch = p.getMutedSwatch();
//            }
//
//            if (swatch == null) {
//                swatch = p.getDarkMutedSwatch();
//            }
//
//            int textColor = swatch.getBodyTextColor();
//            int backgroundColor = swatch.getRgb();
//
//
//            ValueAnimator colorFade = ValueAnimator.ofObject(new ArgbEvaluator(),
//                    getResources().getColor(android.R.color.white), backgroundColor);
//            colorFade.setDuration(1000);
//            colorFade.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    int color = (int) animation.getAnimatedValue();
//                    headerBox.setBackgroundColor(color);
//                }
//            });
//            colorFade.start();
//
//            int curTextColor = tag.getCurrentTextColor();
//            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), curTextColor, textColor);
//            colorAnimation.setDuration(1000); // milliseconds
//            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    int color = (int) animation.getAnimatedValue();
//                    tag.setTextColor(color);
//                    status.setTextColor(color);
//                }
//            });
//            colorAnimation.start();
//        } catch (Exception e) {
//        }
//    }

    private void unHideAllViews() {
        tag.setVisibility(View.VISIBLE);
        status.setVisibility(View.VISIBLE);
    }

    private void setTextOrHide(View view, TextView tv, @Nullable String text) {
        if (text == null || text.equals("")) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
    }

    private void loadImage() {
        String imageURL = asset.getImage();
        //Picasso requires a non-empty path.  Just rely on the error handling
        if (imageURL == null || imageURL.equals("")) {
            imageURL = "foobar";
        }

        Transformation backgroundTransformation = new RoundedTransformation();

        float borderWidth = getResources().getDimension(R.dimen.picasso_small_image_circle_border_width);

        Transformation borderTransformation = new RoundedTransformationBuilder()
                .borderColor(getResources().getColor(R.color.circleBorderLarge))
                .borderWidthDp(borderWidth)
                .cornerRadiusDp(175)
                .oval(false)
                .build();

        List<Transformation> transformations = new ArrayList<>();
        transformations.add(backgroundTransformation);
        transformations.add(borderTransformation);

        Picasso.with(getContext())
                .load(imageURL)
                .transform(new ArrayList<Transformation>())
                .transform(transformations)
                .into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        image.setColorFilter(null);
                        image.setImageDrawable(new BitmapDrawable(bitmap));
                        updateHeaderColor(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        Drawable d = getResources().getDrawable(R.drawable.monitor_cellphone_star, null);
                        Drawable b = headerBox.getBackground();
                        if (b instanceof ColorDrawable) {
                            int color = ((ColorDrawable) b).getColor();
                            int tintColor = ColorHelper.getSecondaryValueTextColorForBackground(getContext(), color);
                            image.setColorFilter(tintColor);
                        }
                        image.setImageDrawable(d);
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                        Drawable d = getResources().getDrawable(R.drawable.monitor_cellphone_star, null);
                        Drawable b = headerBox.getBackground();
                        if (b instanceof ColorDrawable) {
                            int color = ((ColorDrawable) b).getColor();
                            int tintColor = ColorHelper.getSecondaryValueTextColorForBackground(getContext(), color);
                            image.setColorFilter(tintColor);
                        }
                        image.setImageDrawable(d);
                    }
                });
    }

    private void updateHeaderColor(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@Nullable Palette palette) {
                @ColorInt int defaultColor = getResources().getColor(R.color.default_accent);
                Palette.Swatch swatch = palette.getVibrantSwatch();
                if (swatch != null) {
                    applySwatch(swatch);
                }
            }
        });
    }

    private void applySwatch(@NotNull Palette.Swatch swatch) {
        final int duration = 400;

        int fromBackground = getResources().getColor(R.color.default_accent);
        Drawable d = headerBox.getBackground();
        if (d != null && d instanceof ColorDrawable) {
            fromBackground = ((ColorDrawable) d).getColor();
        }

        int fromTextColor = tag.getCurrentTextColor();

        int toBackground = swatch.getRgb();
        int toTextColor = swatch.getTitleTextColor();

        ValueAnimator backgroundAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                fromBackground, toBackground);
        backgroundAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int color = (int) animation.getAnimatedValue();
                headerBox.setBackgroundColor(color);
            }
        });
        backgroundAnimation.setDuration(duration);
        backgroundAnimation.start();


        ValueAnimator textAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                fromTextColor, toTextColor);
        textAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int color = (int) animation.getAnimatedValue();
                tag.setTextColor(color);
                status.setTextColor(color);
            }
        });
        textAnimation.setDuration(duration);
        textAnimation.start();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);


    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStyle(STYLE_NO_TITLE, 0);
        asset = (Asset) getArguments().getSerializable("asset");
    }

    private class AssetPagerAdapter extends FragmentStatePagerAdapter {
        public AssetPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    AssetExtendedDetailsFragment fragment = AssetExtendedDetailsFragment.newInstance(asset);
                    return fragment;
                case 1:
                    AssetStatisticsFragment statisticsFragment = AssetStatisticsFragment.newInstance(asset);
                    return statisticsFragment;
                case 2:
                    AssetMaintenanceFragment maintFragment = AssetMaintenanceFragment.newInstance(asset);
                    return maintFragment;
                case 3:
                    ActionHistoryFragment historyFragment = ActionHistoryFragment.newInstance(asset);
                    return historyFragment;
                default:
                    return new Fragment();
            }

        }

        @Override
        public int getCount() {
            return 4;
        }
    }
}
