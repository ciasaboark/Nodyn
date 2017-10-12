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
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.MPPointF;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.model.Action;


public class HistoryChartFragment extends Fragment {
    private static final String TAG = HistoryChartFragment.class.getSimpleName();
    private View rootView;
    private LineChart chart;
    private Database db;
    private String[] timestampMap;

    public static HistoryChartFragment newInstance() {
        HistoryChartFragment fragment = new HistoryChartFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public HistoryChartFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_history_chart, container, false);
        init();

        return rootView;
    }

    private void init() {
        chart = (LineChart) rootView.findViewById(R.id.chart);

        initChart();

        refreshChart();
    }

    private void initChart() {
        Description d = new Description();
        d.setText("30 Day Activity");
        d.setTextSize(15);
        int x = chart.getWidth() - 30;
        int y = chart.getHeight() - 30;
        d.setPosition(x, y);
        chart.setDescription(d);
        MPPointF position = d.getPosition();

        chart.getLegend().setTextSize(15f);
        chart.getLegend().setTextColor(getResources().getColor(android.R.color.secondary_text_light));
//        chart.setBackgroundColor(Color.parseColor("#1DE9B6"));
//        chart.setGridBackgroundColor(Color.parseColor("#B2EBF2"));
    }


    private void refreshChart() {
        List<Action> actionList = db.getActions();
        buildDateStringMap();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        long oneMonthAgo = calendar.getTimeInMillis();

        Map<Long, Integer> checkoutsPerDay = new TreeMap<>();
        Map<Long, Integer> checkinsPerDay = new TreeMap<>();

        //todo we will need to group the actions by day so we can get a better count

        for (Action action : actionList) {
            if (action.getTimestamp() >= oneMonthAgo) {
                calendar.setTimeInMillis(action.getTimestamp());
                calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
                calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
                calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));

                long timestamp = calendar.getTimeInMillis();

                switch (action.getDirection()) {
                    case CHECKIN:
                        Integer checkinCount = checkinsPerDay.get(timestamp);
                        if (checkinCount == null) checkinCount = 0;
                        checkinCount++;
                        checkinsPerDay.put(timestamp, checkinCount);
                        break;
                    case CHECKOUT:
                        Integer checkoutCount = checkinsPerDay.get(timestamp);
                        if (checkoutCount == null) checkoutCount = 0;
                        checkoutCount++;
                        checkoutsPerDay.put(timestamp, checkoutCount);
                }
            }
        }

        //for the graph to be more visually apealing we need to include zero values for days
        //+ that have no data
        List<Entry> checkoutEntries = new ArrayList<>();
        List<Entry> checkinEntries = new ArrayList<>();

        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
        calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));

        //shift back 30 days, and add each day's records in chronological order
        calendar.add(Calendar.DAY_OF_MONTH, -29);
        for (int i = 0; i < 30; i++) {
            long timestamp = calendar.getTimeInMillis();
            Integer checkoutCount = checkoutsPerDay.get(timestamp);
            if (checkoutCount == null) {
                checkoutCount = 0;
            } else {
                Log.d(TAG, "foo");
            }
            checkoutEntries.add(new Entry(i, checkoutCount));


            Integer checkinCount = checkinsPerDay.get(timestamp);
            if (checkinCount == null) {
                checkinCount = 0;
            } else {
                Log.d(TAG, "foo");
            }
            checkinEntries.add(new Entry(i, checkinCount));

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        LineData lineData = new LineData();
        if (!checkoutEntries.isEmpty()) {
            LineDataSet dataSet = new LineDataSet(checkoutEntries, "Asset Check Outs");
            dataSet.setColor(getResources().getColor(R.color.chart_out));
            dataSet.setCircleColor(getResources().getColor(R.color.chart_out));
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(getResources().getColor(R.color.chart_out));
            dataSet.setFillAlpha(100);
            dataSet.setDrawCircleHole(false);
            dataSet.setDrawCircles(false);
            dataSet.setCircleRadius(0f);
            dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            dataSet.setLineWidth(2);
            dataSet.enableDashedLine(7, 1, 0);
            lineData.addDataSet(dataSet);
        }

        if (!checkinEntries.isEmpty()) {
            LineDataSet dataSet = new LineDataSet(checkinEntries, "Asset Check Ins");
            dataSet.setColor(getResources().getColor(R.color.chart_in));
            dataSet.setCircleColor(getResources().getColor(R.color.chart_in));
            dataSet.setDrawFilled(true);
            dataSet.setFillColor(getResources().getColor(R.color.chart_in));
            dataSet.setFillAlpha(100);
            dataSet.setDrawCircleHole(false);
            dataSet.setDrawCircles(false);
            dataSet.setCircleRadius(0f);
            dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            dataSet.setLineWidth(2);
//            dataSet.enableDashedLine(7, 1, 0);
            lineData.addDataSet(dataSet);
        }

        if (lineData.getDataSetCount() == 0) {
            chart.setData(null);
        } else {
            lineData.setDrawValues(false);
            chart.setData(lineData);

        }

        chart.invalidate();

        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1.0f);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int index = (int) value;
                String dateString = timestampMap[index];
                dateString = String.valueOf(dateString);
                return dateString;
            }
        });

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setGranularity(1.0f);
        yAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) Math.floor(value));
            }
        });
        yAxis.setAxisMinimum(0);
        yAxis.enableGridDashedLine(5, 5, 0);
        yAxis.setAxisLineColor(Color.parseColor("#BBDEFB"));
        yAxis.setGridColor(Color.parseColor("#BBDEFB"));
        chart.getAxisRight().setEnabled(false);
        chart.setPinchZoom(false);
        chart.setScaleYEnabled(false);


        float maxYValue = chart.getData().getYMax();
        float minYValue = chart.getData().getYMin();

        if ((maxYValue - minYValue) < 4) {
            float diff = 4 - (maxYValue - minYValue);
            maxYValue = maxYValue + diff;
            chart.getAxisLeft().setAxisMaximum(maxYValue);
            chart.getAxisLeft().setAxisMinimum(minYValue);
        }


        float maxXValue = chart.getData().getXMax();
        float minXValue = chart.getData().getXMin();

        if ((maxXValue - minXValue) < 4) {
            float diff = 4 - (maxXValue - minXValue);
            maxXValue = maxXValue + diff;
            chart.getXAxis().setAxisMaximum(maxXValue);
            chart.getXAxis().setAxisMinimum(minXValue);
        }

        chart.setVisibleXRangeMaximum(7); // allow 20 values to be displayed at once on the x-axis, not more
        chart.moveViewToX(30);
    }

    @NonNull
    private void buildDateStringMap() {
        //build a map of baseline timestamps for the previous 30 days.  These timestamps will be
        //+ be used to group the actions by day
        timestampMap = new String[30];
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -29);

        for (int i = 0; i < 30; i++) {
            calendar.set(Calendar.HOUR_OF_DAY, calendar.getActualMinimum(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, calendar.getActualMinimum(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, calendar.getActualMinimum(Calendar.SECOND));
            calendar.set(Calendar.MILLISECOND, calendar.getActualMinimum(Calendar.MILLISECOND));

            long timestamp = calendar.getTimeInMillis();

            //convert the timestamp into a local
            Locale currentLocale = getResources().getConfiguration().locale;
            DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT,
                    currentLocale);
            Date d = new Date(timestamp);
            String dateString = df.format(d);

            //MPChart does not let us set the X-axis values as string data, so instead we will
            //+ cache the date string and use the timestamp to lookup that value later
            timestampMap[i] = dateString;

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

}