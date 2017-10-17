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


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.phobotic.nodyn.ColorHelper;
import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.Status;

public class AssetStatusChartFragment extends Fragment {
    public static final String TAG = AssetStatusChartFragment.class.getSimpleName();
    private View rootView;
    private PieChart chart;
    private Database db;

    public static AssetStatusChartFragment newInstance(String param1, String param2) {
        AssetStatusChartFragment fragment = new AssetStatusChartFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public AssetStatusChartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        db = Database.getInstance(context);
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
        rootView = inflater.inflate(R.layout.fragment_asset_status_chart, container, false);
        init();

        return rootView;
    }

    private void init() {
        chart = (PieChart) rootView.findViewById(R.id.chart_detail);
        initChart();

        refresh();
    }

    private void initChart() {
//        chart.setTransparentCircleColor(Color.WHITE);
//        chart.setUsePercentValues(true);
        chart.setDescription(null);
        chart.setExtraOffsets(10, 10, 10, 10);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setDrawSliceText(true);
        chart.setDrawHoleEnabled(true);
//        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(50);
        chart.setRotationAngle(0);
        chart.setRotationEnabled(false);
        chart.setHoleRadius(30f);
//        chart.setTransparentCircleRadius(1f);
        chart.setCenterTextRadiusPercent(10f);
        chart.setPressed(false);
        chart.setHighlightPerTapEnabled(true);
        chart.setEntryLabelColor(getResources().getColor(android.R.color.primary_text_light));
        chart.getLegend().setTextSize(15f);
        chart.getLegend().setTextColor(getResources().getColor(android.R.color.secondary_text_light));
    }

    public void refresh() {
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
        List<Asset> allAssets = db.getAssets();
        List<Status> allStatuses = db.getStatuses();
        //slight optimization if we have a large number of assets to look at
        Map<Integer, Status> statusIDMap = new LinkedHashMap<>();
        for (Status s : allStatuses) {
            statusIDMap.put(s.getId(), s);
        }

        LinkedHashMap<Status, Integer> statusCountMap = new LinkedHashMap<>();

        //skip setting a zero value for the fallback status.  This will keep it from showing
        //+ in the graph unless we have an asset with a bad status ID
        statusIDMap.put(-1, new Status("Unknown Status", null, null));


        for (Asset asset : allAssets) {
            //treat assets assigned to an associate as their own virtual status
            Status s;
            if (asset.getAssignedToID() != -1) {
                s = new Status("Assigned", "Ready to Deploy", null);
            } else {
                int statusId = asset.getStatusID();
                s = statusIDMap.get(statusId);
                if (s == null) {
                    Log.e(TAG, "Asset " + asset.toString() + " has a status ID of " + statusId + ".  " +
                            "This value does not exist in the status table.");
                    statusId = -1;
                    s = statusIDMap.get(statusId);
                }
            }

            Integer total = statusCountMap.get(s);
            if (total == null) total = 0;

            total++;
            statusCountMap.put(s, total);
        }

        for (Map.Entry<Status, Integer> entry : statusCountMap.entrySet()) {
            PieEntry e = new PieEntry(entry.getValue(), entry.getKey().getName());
            entries.add(e);
        }

        PieDataSet dataSet = new PieDataSet(entries, "Status");
        dataSet.setValueTextSize(15f);
        dataSet.setValueTextColor(getResources().getColor(android.R.color.secondary_text_light));
        dataSet.setValueLineColor(getResources().getColor(android.R.color.secondary_text_light));
        dataSet.setSelectionShift(20f);
        dataSet.setValueLinePart1OffsetPercentage(80.f);
        dataSet.setValueLinePart1Length(0.7f);
        dataSet.setValueLinePart2Length(0.6f);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setYValuePosition(PieDataSet.ValuePosition.INSIDE_SLICE);

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(0f);
        dataSet.setIconsOffset(new MPPointF(0, 40));

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();
        TypedArray ta = getContext().getResources().obtainTypedArray(R.array.vivid_colors);
        int[] fallbackColors = new int[ta.length()];
        for (int i = 0; i < ta.length(); i++) {
            fallbackColors[i] = ta.getColor(i, 0);
        }
        ta.recycle();
        //cycle through the fallback colors if the status has no assigned color
        int mostRecentFallbackColorUsed = 0;

        //try to make sure the value text color is visible regardless of the status color
        List<Integer> valueTextColor = new ArrayList<>();

        //try to use the status color if possible
        for (Status s : statusCountMap.keySet()) {
            int statusColor;
            try {
                statusColor = Color.parseColor(s.getColor());
            } catch (IllegalArgumentException | NullPointerException e) {
                if (mostRecentFallbackColorUsed >= fallbackColors.length) {
                    mostRecentFallbackColorUsed = 0;
                }

                statusColor = fallbackColors[mostRecentFallbackColorUsed++];
            }

            colors.add(statusColor);

            valueTextColor.add(ColorHelper.getValueTextColorForBackground(getContext(), statusColor));
        }


        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new DefaultValueFormatter(0));
        data.setValueTextSize(15f);

        data.setValueTextColors(valueTextColor);
        chart.setData(data);
        chart.setExtraOffsets(20, 20, 20, 20);

        chart.animateY(1000, Easing.EasingOption.EaseInOutQuad);

        // undo all highlights
        chart.highlightValues(null);

        chart.invalidate();

    }

}
