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

package io.phobotic.nodyn_app.fragment.dash;


import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.ManufacturerNotFoundException;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Manufacturer;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.helper.ColorHelper;
import io.phobotic.nodyn_app.view.ModelOverviewCountView;

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
        rootView = inflater.inflate(R.layout.fragment_model_grid, container, false);
        init();

        return rootView;
    }

    private void init() {
        db = Database.getInstance(getContext());
        gridView = rootView.findViewById(R.id.grid);
        gridOverflowView = rootView.findViewById(R.id.grid_overflow);
        button = rootView.findViewById(R.id.more_button);
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

        boolean allowAllModels = prefs.getBoolean(getString(R.string.pref_key_check_out_all_models),
                false);
        Set<String> allowedModels = prefs.getStringSet(getString(R.string.pref_key_check_out_models),
                new HashSet<String>());

        boolean allStatusesAllowed = prefs.getBoolean(getString(
                R.string.pref_key_asset_status_allow_all), Boolean.parseBoolean(
                getString(R.string.pref_default_asset_status_allow_all)));
        Set<String> allowedStatusIDs = prefs.getStringSet(getString(
                R.string.pref_key_asset_status_allowed_statuses), new HashSet<String>());

        for (Asset asset : allAssets) {
            int modelId = asset.getModelID();
            if (allowAllModels || allowedModels.contains(String.valueOf(modelId))) {
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
            } else {
                Log.d(TAG, String.format("Model id %d is not allowed, skipping", modelId));
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
            if (allowedStatusIDs.contains(String.valueOf(asset.getStatusID()))) {
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
        final ModelOverviewCountView modelOverviewCountView = new ModelOverviewCountView(getContext(),
                model.getName(), count);

        try {
            Manufacturer man = db.findManufacturerByID(model.getManufacturerID());
            manufacturer = man.getName();
        } catch (ManufacturerNotFoundException e) {
            //just use a blank manufacturer field
        }

        modelOverviewCountView.setManufacturer(manufacturer);

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
        modelOverviewCountView.setTextColor(textColor);
        modelOverviewCountView.setColor(color);

        modelOverviewCountView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1f));
        modelOverviewCountView.setVisibility(View.INVISIBLE);
        container.addView(modelOverviewCountView);

        Animation inFromLeft = AnimationUtils.loadAnimation(getContext(), R.anim.enter_from_left);
        Random random = new Random(System.currentTimeMillis());
        long delay = (long) random.nextInt(250);
        inFromLeft.setStartOffset(delay);
        inFromLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                modelOverviewCountView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        modelOverviewCountView.setAnimation(inFromLeft);
        modelOverviewCountView.animate();
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
