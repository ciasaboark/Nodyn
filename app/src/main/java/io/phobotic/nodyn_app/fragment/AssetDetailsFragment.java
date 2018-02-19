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

package io.phobotic.nodyn_app.fragment;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.StatusNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Status;
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
        tag = (TextView) rootView.findViewById(R.id.tag);
        headerBox = rootView.findViewById(R.id.header_box);

        status = (TextView) rootView.findViewById(R.id.status);

        image = (ImageView) rootView.findViewById(R.id.image);

        tabs = (TabLayout) rootView.findViewById(R.id.tabs);
        pager = (ViewPager) rootView.findViewById(R.id.pager);
        FragmentManager fm = getChildFragmentManager();
        PagerAdapter pagerAdapter = new AssetPagerAdapter(fm);
        pager.setAdapter(pagerAdapter);
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

        if (useStatusColor) {
            fadeInStatusColor();
        }

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

    private void fadeInStatusColor() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        @ColorInt int color = typedValue.data;
        try {
            Database db = Database.getInstance(getContext());
            List<Status> statuses = db.getStatuses();
            for (Status status : statuses) {
                if (status.getId() == asset.getStatusID()) {
                    color = Color.parseColor(status.getColor());
                    break;
                }
            }

            int[] colors = {
                    color
            };
            Bitmap bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            bitmap.eraseColor(color);
            Palette p = Palette.from(bitmap).generate();

            Palette.Swatch swatch = p.getVibrantSwatch();
            if (swatch == null) {
                swatch = p.getDominantSwatch();
            }

            if (swatch == null) {
                swatch = p.getMutedSwatch();
            }

            if (swatch == null) {
                swatch = p.getDarkMutedSwatch();
            }

            int textColor = swatch.getBodyTextColor();
            int backgroundColor = swatch.getRgb();


            ValueAnimator colorFade = ValueAnimator.ofObject(new ArgbEvaluator(),
                    getResources().getColor(android.R.color.white), backgroundColor);
            colorFade.setDuration(1000);
            colorFade.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int color = (int) animation.getAnimatedValue();
                    headerBox.setBackgroundColor(color);
                }
            });
            colorFade.start();

            int curTextColor = tag.getCurrentTextColor();
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), curTextColor, textColor);
            colorAnimation.setDuration(1000); // milliseconds
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int color = (int) animation.getAnimatedValue();
                    tag.setTextColor(color);
                    status.setTextColor(color);
                }
            });
            colorAnimation.start();
        } catch (Exception e) {
        }
    }

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
        String image = asset.getImage();
        //Picasso requires a non-empty path.  Just rely on the error handling
        if (image == null || image.equals("")) {
            image = "foobar";
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
                .load(image)
                .placeholder(R.drawable.devices_large)
                .error(R.drawable.ic_important_devices_black_24dp)
                .transform(new ArrayList<Transformation>())
                .fit()
                .transform(transformations)
                .into(this.image);
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
                    AssetMaintenanceFragment maintFragment = AssetMaintenanceFragment.newInstance(asset);
                    return maintFragment;
                case 2:
                    ActionHistoryFragment historyFragment = ActionHistoryFragment.newInstance(asset);
                    return historyFragment;
                default:
                    return new Fragment();
            }

        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
