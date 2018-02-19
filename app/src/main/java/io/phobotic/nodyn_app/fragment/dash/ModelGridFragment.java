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

package io.phobotic.nodyn_app.fragment.dash;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.ManufacturerNotFoundException;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Manufacturer;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.helper.ColorHelper;
import io.phobotic.nodyn_app.view.OverviewCountView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


public class ModelGridFragment extends Fragment {

    private static final String TAG = ModelGridFragment.class.getSimpleName();
    private View rootView;
    private GridLayout gridView;
    private Database db;
    private GridLayout gridOverflowView;
    private Button button;
    private int[] colors;
    private HashMap<Model, Integer> overFlowModels;
    private View overflowBox;

    public static ModelGridFragment newInstance(String param1, String param2) {
        ModelGridFragment fragment = new ModelGridFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ModelGridFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_overview_grid, container, false);
        init();

        return rootView;
    }

    private void init() {
        db = Database.getInstance(getContext());
        gridView = (GridLayout) rootView.findViewById(R.id.grid);
        gridOverflowView = (GridLayout) rootView.findViewById(R.id.grid_overflow);
        button = (Button) rootView.findViewById(R.id.more_button);
        overflowBox = rootView.findViewById(R.id.overflow_box);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (overflowBox.getVisibility() == View.VISIBLE) {
                    AnimationHelper.collapse(overflowBox);
                    button.setText(R.string.view_more);
                } else {
                    AnimationHelper.expand(overflowBox);
                    button.setText(R.string.view_less);
                }
            }
        });

        initColors();
    }

    private void initColors() {
        TypedArray ta = getContext().getResources().obtainTypedArray(R.array.material_colors);
        colors = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            colors[i] = ta.getColor(i, 0);
        }
        ta.recycle();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        overFlowModels = new HashMap<Model, Integer>();
        gridView.removeAllViews();
        gridOverflowView.removeAllViews();
        List<Asset> allAssets = db.getAssets();
        Map<Model, Integer> modelCount = new HashMap<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        boolean allStatusesAllowed = prefs.getBoolean(getString(
                R.string.pref_key_asset_status_allow_all), Boolean.parseBoolean(
                getString(R.string.pref_default_asset_status_allow_all)));
        Set<String> allowedStatusIDs = prefs.getStringSet(getString(
                R.string.pref_key_asset_status_allowed_statuses), new HashSet<String>());

        for (Asset asset : allAssets) {
            int modelId = asset.getModelID();
            if (modelId == -1) {
                Log.d(TAG, "Asset " + asset.getTag() + " has no assigned model, skipping");
            } else if (!isAssetStatusValid(asset)) {
                Log.d(TAG, "Asset " + asset.getTag() + " does not have an allowed status, skipping");
            } else {
                try {
                    Model m = db.findModelByID(modelId);
                    Integer count = modelCount.get(m);
                    if (count == null) count = 0;

                    count++;
                    modelCount.put(m, count);
                } catch (ModelNotFoundException e) {
                    Log.e(TAG, "Model with id: " + modelId + " could not be found, asset " +
                            asset.getTag() + " will be skipped");
                }
            }
        }

        Random r = new Random(System.currentTimeMillis());


        int maxItems = 8;
        int itemCount = 0;
        for (Map.Entry<Model, Integer> entry : modelCount.entrySet()) {
            Model model = entry.getKey();
            int count = entry.getValue();
            if (itemCount >= maxItems) {
                addModelToGrid(gridOverflowView, count, model);
            } else {
                addModelToGrid(gridView, count, model);

            }
            itemCount++;
        }

        if (itemCount <= maxItems) {
            button.setVisibility(View.GONE);
            gridOverflowView.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.VISIBLE);
            gridOverflowView.setVisibility(View.VISIBLE);
        }
    }

    private boolean isAssetStatusValid(Asset asset) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean statusValid = false;

        boolean allStatusesAllowed = prefs.getBoolean(getString(
                R.string.pref_key_asset_status_allow_all), Boolean.parseBoolean(
                getString(R.string.pref_default_asset_status_allow_all)));
        if (allStatusesAllowed) {
            statusValid = true;
        } else {
            Set<String> allowedStatusIDs = getAllowedStatusIDs();
            if (allowedStatusIDs.contains(String.valueOf(String.valueOf(asset.getStatusID())))) {
                statusValid = true;
            }
        }

        return statusValid;
    }

    private void addModelToGrid(GridLayout gridLayout, int count, Model model) {
        LinearLayout container = null;
        if (gridLayout.getChildCount() == 0) {
            container = getNewContainer();
            gridLayout.addView(container);
        } else {
            LinearLayout l = (LinearLayout) gridLayout.getChildAt(gridLayout.getChildCount() - 1);
            if (l.getChildCount() > 1) {
                container = getNewContainer();
                gridLayout.addView(container);
            } else {
                container = l;
            }
        }

        String manufacturer = "";
        final OverviewCountView overviewCountView = new OverviewCountView(getContext(),
                model.getName(), count);

        try {
            Manufacturer man = db.findManufacturerByID(model.getManufacturerID());
            manufacturer = man.getName();
        } catch (ManufacturerNotFoundException e) {
            //just use a blank manufacturer field
        }

        overviewCountView.setManufacturer(manufacturer);

        int color;
        if (colors != null && colors.length > 0) {
            int hash = Math.abs(model.hashCode());
            int index = hash % colors.length;
            Log.d(TAG, model.getName() + " hash '" + hash + "' using color index " + index);
            color = colors[index];
        } else {
            TypedValue typedValue = new TypedValue();
            Resources.Theme theme = getContext().getTheme();
            theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
            color = typedValue.data;
        }

        int textColor = ColorHelper.getValueTextColorForBackground(getContext(), color);
        overviewCountView.setTextColor(textColor);
        overviewCountView.setColor(color);

        overviewCountView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1f));
        overviewCountView.setVisibility(View.INVISIBLE);
        container.addView(overviewCountView);


//            Animation swingIn = AnimationUtils.loadAnimation(getContext(), R.anim.swinging);
//            overviewCountView.startAnimation(swingIn);


        ValueAnimator offsetX = ValueAnimator.ofFloat(.5f, 0);
        offsetX.setTarget(overviewCountView);
        offsetX.setDuration(300);
        offsetX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                int height = overviewCountView.getHeight();
                float offset = -(height * val);
                overviewCountView.setTranslationY(offset);
            }
        });

        ObjectAnimator flipIn = ObjectAnimator.ofFloat(overviewCountView, "rotationX", -90, 0)
                .setDuration(300);
        flipIn.setInterpolator(new LinearInterpolator());

        AnimatorSet set = new AnimatorSet();
        Random random = new Random(System.currentTimeMillis());
        long delay = (long) random.nextInt(500);
        set.setStartDelay(delay);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                overviewCountView.setVisibility(View.VISIBLE);
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
        set.playTogether(flipIn, offsetX);
        set.start();
    }

    @NonNull
    private Set<String> getAllowedStatusIDs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        return prefs.getStringSet(getString(
                R.string.pref_key_asset_status_allowed_statuses), new HashSet<String>());
    }

    private LinearLayout getNewContainer() {
        LinearLayout container;
        container = new LinearLayout(getContext());
        container.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 2f));
        container.setOrientation(LinearLayout.HORIZONTAL);
        return container;
    }

}
