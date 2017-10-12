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

package io.phobotic.nodyn.fragment.dash;


import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.phobotic.nodyn.ColorHelper;
import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.exception.ManufacturerNotFoundException;
import io.phobotic.nodyn.database.exception.ModelNotFoundException;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.Manufacturer;
import io.phobotic.nodyn.database.model.Model;
import io.phobotic.nodyn.view.OverviewCountView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


public class OverviewGridFragment extends Fragment {

    private static final String TAG = OverviewGridFragment.class.getSimpleName();
    private View rootView;
    private GridLayout gridView;

    public static OverviewGridFragment newInstance(String param1, String param2) {
        OverviewGridFragment fragment = new OverviewGridFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public OverviewGridFragment() {
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
        gridView = (GridLayout) rootView.findViewById(R.id.grid);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        gridView.removeAllViews();

        Database db = Database.getInstance(getContext());
        List<Asset> allAssets = db.getAssets();
        Map<Model, Integer> modelCount = new HashMap<>();

        for (Asset asset : allAssets) {
            int modelId = asset.getModelID();
            if (modelId == -1) {
                Log.d(TAG, "Asset " + asset.getTag() + " has no assigned model, skipping");
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

        TypedArray ta = getContext().getResources().obtainTypedArray(R.array.material_colors);
        int[] colors = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            colors[i] = ta.getColor(i, 0);
        }
        ta.recycle();

        Random r = new Random(System.currentTimeMillis());


        for (Map.Entry<Model, Integer> entry : modelCount.entrySet()) {
            Model model = entry.getKey();

            LinearLayout container = null;
            if (gridView.getChildCount() == 0) {
                container = getNewContainer();
                gridView.addView(container);
            } else {
                LinearLayout l = (LinearLayout) gridView.getChildAt(gridView.getChildCount() - 1);
                if (l.getChildCount() > 1) {
                    container = getNewContainer();
                    gridView.addView(container);
                } else {
                    container = l;
                }
            }

            String manufacturer = "";
            OverviewCountView overviewCountView = new OverviewCountView(getContext(),
                    model.getName(), entry.getValue());

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
                color = getResources().getColor(R.color.colorAccent);
            }

            int textColor = ColorHelper.getValueTextColorForBackground(getContext(), color);
            overviewCountView.setTextColor(textColor);
            overviewCountView.setColor(color);

            overviewCountView.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 1f));

            container.addView(overviewCountView);
        }
    }

    private LinearLayout getNewContainer() {
        LinearLayout container;
        container = new LinearLayout(getContext());
        container.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT, 2f));
        container.setOrientation(LinearLayout.HORIZONTAL);
        return container;
    }

}
