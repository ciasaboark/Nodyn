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

package io.phobotic.nodyn_app.charts;

import android.content.Context;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.ColorInt;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.audit.model.Audit;
import io.phobotic.nodyn_app.database.statistics.day_activity.DayActivity;

/**
 * Created by Jonathan Nelson on 2019-05-12.
 */
public class HistoryChartBuilder {
    private static final String TAG = HistoryChartBuilder.class.getSimpleName();

    public HistoryChartBuilder() {
    }

    public void buildChart(Context context, final LineChart chart,
                           final List<DayActivity> dayActivityList, final List<Audit> audits) {
        initChart(context, chart);
        List<Entry> checkoutEntries = new ArrayList<>();
        List<Entry> checkinEntries = new ArrayList<>();
        List<Entry> auditEntries = new ArrayList<>();

        //keep track of which days corrispond to which gridline.  We will use this to insert audit
        //+ data after the daily activity has been processed
        Map<Long, Integer> dayIndexMap = new HashMap<>();

        for (int i = 0; i < dayActivityList.size(); i++) {
            DayActivity activity = dayActivityList.get(i);
            long timestamp = activity.getTimestamp();
            dayIndexMap.put(timestamp, i);

            DateFormat df = DateFormat.getDateTimeInstance();
            Date d = new Date(timestamp);
            Log.d(TAG, "Check-outs " + df.format(d) + ": " + activity.getTotalCheckouts());
            checkoutEntries.add(new Entry(i, activity.getTotalCheckouts()));

            Log.d(TAG, "Check-ins " + df.format(d) + ": " + activity.getTotalCheckins());
            checkinEntries.add(new Entry(i, activity.getTotalCheckins()));
        }

        //insert the audit data.  We don't really care how many audits were performed on a specific
        //+ day, just that at least one was completed
        Calendar calendar = Calendar.getInstance();
        for (Audit audit : audits) {
            //reset the audit time to the beginning of the day so we can match it to a grid index
            calendar.setTimeInMillis(audit.getBegin());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            Integer index = dayIndexMap.get(calendar.getTimeInMillis());
            if (index != null) {
                //force the audit notation to show at the bottom of the chart
                auditEntries.add(new Entry(index, 0));
            }
        }

        LineData lineData = new LineData();

        if (!auditEntries.isEmpty()) {
            LineDataSet dataSet = new LineDataSet(auditEntries, "Audits");
            int color = context.getResources().getColor(R.color.chart_audit);
            buildAuditDataSet(dataSet, color);
            lineData.addDataSet(dataSet);
        }

        if (!checkoutEntries.isEmpty()) {
            LineDataSet dataSet = new LineDataSet(checkoutEntries, "Check Outs");
            int color = context.getResources().getColor(R.color.chart_out);
            buildDataSet(dataSet, color);
            lineData.addDataSet(dataSet);
        }

        if (!checkinEntries.isEmpty()) {
            LineDataSet dataSet = new LineDataSet(checkinEntries, "Check Ins");
            int color = context.getResources().getColor(R.color.chart_in);
            buildDataSet(dataSet, color);
            lineData.addDataSet(dataSet);
        }

        final DateFormat df = new SimpleDateFormat("d MMM");
        chart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                float[] values = axis.mEntries;
                int index = (int) value;
                DayActivity dayActivity = dayActivityList.get(index);
                long timeStamp = dayActivity.getTimestamp();
                String dateString = df.format(new Date(timeStamp));
                dateString = dateString.toUpperCase();
                return dateString;
            }
        });

        if (lineData.getDataSetCount() == 0) {
            chart.setData(null);
        } else {
            lineData.setDrawValues(false);
            chart.setData(lineData);
        }

        chart.invalidate();


//        AnimationHelper.fadeIn(getContext(), chart);
//        AnimationHelper.fadeOut(getContext(), progressBar);
    }

    private void initChart(Context context, LineChart chart) {
        YAxis leftAxis = chart.getAxis(YAxis.AxisDependency.LEFT);
        YAxis rightAxis = chart.getAxis(YAxis.AxisDependency.RIGHT);
        @ColorInt int textColor = context.getResources().getColor(android.R.color.primary_text_light);
        @ColorInt int labelColor = context.getResources().getColor(android.R.color.secondary_text_light);
        @ColorInt int gridColor = context.getResources().getColor(R.color.chart_grid);
        chart.getLegend().setTextSize(15f);
        chart.getLegend().setTextColor(textColor);
        chart.getAxisLeft().setTextColor(labelColor);
        chart.getAxisRight().setTextColor(labelColor);
        chart.getXAxis().setTextColor(labelColor);
        leftAxis.setTextColor(labelColor);
        rightAxis.setTextColor(labelColor);

        List<LegendEntry> legendEntries = new ArrayList<>();
        legendEntries.add(new LegendEntry("Asset Audit", Legend.LegendForm.DEFAULT,
                10f, 2f, null, context.getResources().getColor(
                R.color.chart_audit)));
        legendEntries.add(new LegendEntry("Check Outs", Legend.LegendForm.DEFAULT,
                10f, 2f, null, context.getResources().getColor(
                R.color.chart_out)));
        legendEntries.add(new LegendEntry("Check Ins", Legend.LegendForm.DEFAULT,
                10f, 2f, null, context.getResources().getColor(
                R.color.chart_in)));

        Legend l = chart.getLegend();
        l.setCustom(legendEntries);

//        chart.getXAxis().setCenterAxisLabels(true);

        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1.0f);
        xAxis.setGridColor(gridColor);
        xAxis.setAxisLineColor(gridColor);
//        chart.setXAxisRenderer(new CustomXAxisRenderer(chart.getViewPortHandler(), xAxis,
//                chart.getTransformer(YAxis.AxisDependency.LEFT)));

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setGranularity(1.0f);
        yAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) Math.floor(value));
            }
        });
        yAxis.setAxisMinimum(0);
        yAxis.enableGridDashedLine(5, 10, 0);
        yAxis.setGridColor(gridColor);
        yAxis.setAxisLineColor(gridColor);
        chart.getAxisRight().setEnabled(false);


        chart.setPinchZoom(false);
        chart.setScaleYEnabled(false);
        chart.setExtraOffsets(20, 20, 20, 20);
        chart.getDescription().setEnabled(false);

    }

    private void buildAuditDataSet(LineDataSet dataSet, @ColorInt int color) {
//        dataSet.setColor(getResources().getColor(android.R.color.transparent));
        dataSet.setCircleColor(color);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(0);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawCircles(true);

        dataSet.setCircleRadius(9f);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setLineWidth(0);
    }

    private void buildDataSet(LineDataSet dataSet, @ColorInt int color) {
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(1);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawCircles(true);

        dataSet.setCircleRadius(5f);
        dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        dataSet.setLineWidth(2);
    }
}
