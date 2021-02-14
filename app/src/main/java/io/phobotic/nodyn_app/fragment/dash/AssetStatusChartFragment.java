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


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.helper.ColorHelper;

public class AssetStatusChartFragment extends Fragment {
    public static final String TAG = AssetStatusChartFragment.class.getSimpleName();
    private View rootView;
    private PieChart chart;
    private Database db;
    private View error;
    private View progress;

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
        chart = rootView.findViewById(R.id.chart_detail);
        progress = rootView.findViewById(R.id.progress);
        error = rootView.findViewById(R.id.error);
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
        chart.setVisibility(View.INVISIBLE);
        error.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);

        ChartDataLoader chartDataLoader = new ChartDataLoader();
        chartDataLoader.execute();
    }

    private void showError() {
        if (chart != null) {
            Activity a = getActivity();
            if (a != null) {
                a.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        animateOut(progress);
                        animateOut(chart);
                        animateIn(error);
                    }
                });
            }
        }
    }

    private void animateOut(final View view) {
        Activity a = getActivity();
        if (a != null && view != null) {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Animation fadeOut = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            view.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    view.startAnimation(fadeOut);
                }
            });
        }
    }

    private void animateIn(final View view) {
        Activity a = getActivity();
        if (a != null && view != null) {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
                    fadeIn.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            view.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });

                    view.startAnimation(fadeIn);
                }
            });
        }

    }

    private void setChartData(final PieData data) {
        if (chart != null && data != null) {
            Activity a = getActivity();
            if (a != null) {
                a.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chart.setData(data);
                        chart.setExtraOffsets(20, 20, 20, 20);

                        chart.animateY(1000, Easing.EasingOption.EaseInOutQuad);

                        // undo all highlights
                        chart.highlightValues(null);

                        chart.invalidate();

                        animateIn(chart);
                        animateOut(progress);
                    }
                });
            }
        }
    }

    private class ChartDataLoader extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
            List<Asset> allAssets = db.getAssets();

            //filter the list to only include models that this tablet can work with
            List<Asset> filteredList = new ArrayList<>();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            boolean allowAllModels = prefs.getBoolean(getString(R.string.pref_key_check_out_all_models), false);
            if (allowAllModels) {
                filteredList = allAssets;
            } else {
                Set<String> allowedModels = prefs.getStringSet(getString(R.string.pref_key_check_out_models), new HashSet<String>());

                for (Asset a : allAssets) {
                    int m = a.getModelID();
                    if (allowedModels.contains(String.valueOf(m))) {
                        filteredList.add(a);
                    }
                }
            }

            if (filteredList == null || filteredList.isEmpty()) {
                showError();
            } else {
                List<io.phobotic.nodyn_app.database.model.Status> allStatuses = db.getStatuses();
                //slight optimization if we have a large number of assets to look at
                Map<Integer, io.phobotic.nodyn_app.database.model.Status> statusIDMap = new LinkedHashMap<>();
                for (io.phobotic.nodyn_app.database.model.Status s : allStatuses) {
                    statusIDMap.put(s.getId(), s);
                }

                Map<io.phobotic.nodyn_app.database.model.Status, Integer> statusCountMap = new LinkedHashMap<>();


                //skip setting a zero value for the fallback status.  This will keep it from showing
                //+ in the graph unless we have an asset with a bad status ID
                statusIDMap.put(-1, new io.phobotic.nodyn_app.database.model.Status("Unknown Status", null, null));


                Activity activity = getActivity();

                //its possible the user navigated away from the activity holding this fragment while the data was loading.
                //if the activity becomes null during processing we can just abort
                for (Asset asset : filteredList) {
                    if (activity == null) {
                        return null;
                    }

                    //treat assets assigned to an associate as their own virtual status
                    io.phobotic.nodyn_app.database.model.Status s;
                    if (asset.getAssignedToID() != -1) {
                        s = new io.phobotic.nodyn_app.database.model.Status(getString(R.string.asset_status_assigned),
                                getString(R.string.asset_status_assigned_type), null);
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

                //once we have our map showing the counts per status we need to get them into a sorted list
                List<StatusCount> statusCountList = new ArrayList<>();
                for (Map.Entry<io.phobotic.nodyn_app.database.model.Status, Integer> entry : statusCountMap.entrySet()) {
                    StatusCount statusCount = new StatusCount()
                            .setCount(entry.getValue())
                            .setStatus(entry.getKey());
                    statusCountList.add(statusCount);
                }

                //sort the list from largest to smallest
                Collections.sort(statusCountList, new Comparator<StatusCount>() {
                    @Override
                    public int compare(StatusCount o1, StatusCount o2) {
                        return -((Integer) o1.getCount()).compareTo(o2.getCount());
                    }
                });

                //shuffle sort the list so the largest value is followed by the smallest
                statusCountList = shuffleSort(statusCountList);

                for (StatusCount statusCount : statusCountList) {
                    PieEntry e = new PieEntry(statusCount.getCount(), statusCount.getStatus().getName());
                    entries.add(e);
                }

                if (activity == null) {
                    return null;
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

                if (activity == null) {
                    return null;
                }

                //try to use the status color if possible
                for (io.phobotic.nodyn_app.database.model.Status s : statusCountMap.keySet()) {
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

                if (activity == null) {
                    return null;
                }

                setChartData(data);
            }

            return null;
        }

        private List<StatusCount> shuffleSort(List<StatusCount> statusCountList) {
            if (statusCountList == null) {
                return new ArrayList<>();
            } else {
                boolean pullFront = true;
                List<StatusCount> sortedList = new ArrayList<>();

                while (!statusCountList.isEmpty()) {
                    int index;
                    if (pullFront) {
                        index = 0;
                    } else {
                        index = statusCountList.size() - 1;
                    }

                    StatusCount statusCount = statusCountList.remove(index);
                    sortedList.add(statusCount);

                    pullFront = !pullFront;
                }

                return sortedList;
            }
        }
    }

    private class StatusCount {
        private Status status;
        private int count;

        public Status getStatus() {
            return status;
        }

        public StatusCount setStatus(Status status) {
            this.status = status;
            return this;
        }

        public int getCount() {
            return count;
        }

        public StatusCount setCount(int count) {
            this.count = count;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (status != null) {
                sb.append(status.getName());
            } else {
                sb.append("No status");
            }

            sb.append(" " + count);
            return sb.toString();
        }
    }

}
