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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.LimitLine;
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
import java.util.List;
import java.util.Map;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;
import io.phobotic.nodyn_app.R;

/**
 * Created by Jonathan Nelson on 2019-05-12.
 */
public class HourlyUsageChartBuilder {
    private static final String TAG = HourlyUsageChartBuilder.class.getSimpleName();

    public HourlyUsageChartBuilder() {
    }

    public void buildChart(Context context, final LineChart chart, String modelName,
                           int totalAvailableAssets,
                           Map<Long, Integer> usageMap, long from, long to) {
        int steps = usageMap.size();
        initChart(context, chart, modelName, totalAvailableAssets);

        List<Entry> usageEntries = new ArrayList<>();


        for (Map.Entry<Long, Integer> entry : usageMap.entrySet()) {
            long timestamp = entry.getKey();
            int count = entry.getValue();
            usageEntries.add(new Entry(timestamp, count));
        }

        XAxis xAxis = chart.getXAxis();
        xAxis.setAxisMinimum(from);
        xAxis.setAxisMaximum(to);
        xAxis.setLabelRotationAngle(-30);
        xAxis.setLabelCount(usageMap.size(), true);
//        xAxis.setGranularityEnabled(true);
//        xAxis.setGranularity(24);

        LineData lineData = new LineData();




        if (!usageEntries.isEmpty()) {
            LineDataSet dataSet = new LineDataSet(usageEntries, "Usage");
            int color = context.getResources().getColor(R.color.chart_in_use);
            buildDataSet(dataSet, color);
            lineData.addDataSet(dataSet);
        }

        final DateFormat df = new SimpleDateFormat("d MMM kk:mm");
        chart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float timeStamp, AxisBase axis) {
                //round this value to the nearest hour
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis((long) timeStamp);

                if (calendar.get(Calendar.MINUTE) > 30) {
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                }
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                String label = df.format(new Date(calendar.getTimeInMillis()));
                label = label.toUpperCase();
                return label;
            }
        });

        if (lineData.getDataSetCount() == 0) {
            chart.setData(null);
        } else {
            lineData.setDrawValues(false);
            chart.setData(lineData);
        }

        chart.invalidate();
    }

    private void initChart(Context context, LineChart chart, String modelName, int totalAssets) {
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
        legendEntries.add(new LegendEntry("Hourly Usage", Legend.LegendForm.DEFAULT,
                10f, 2f, null, context.getResources().getColor(
                R.color.chart_in_use)));

        Legend l = chart.getLegend();
        l.setCustom(legendEntries);

        chart.getXAxis().setGridColor(gridColor);
        chart.getXAxis().setAxisLineColor(gridColor);

        //disable the secondary Y axis
        chart.getAxisRight().setEnabled(false);

        YAxis yAxis = chart.getAxisLeft();
        yAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.valueOf((int) Math.floor(value));
            }
        });
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(totalAssets);
        yAxis.enableGridDashedLine(5, 10, 0);
        yAxis.setGridColor(gridColor);
        yAxis.setAxisLineColor(gridColor);

        //add a limit line to show where the maximum allowed usage is (typically 70%)
        float limit = (float)totalAssets * 0.7f;
        LimitLine ll = new LimitLine(limit);
        ll.setLabel("70% Usage");
        ll.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
        ll.setTextSize(12f);

        int color = context.getResources().getColor(R.color.default_accent);
        color = ColorUtils.setAlphaComponent(color, 125);
        ll.setTextColor(color);
        //change the accent color to 50% opacity
        ll.setLineColor(color);
        ll.setLineWidth(2f);
        yAxis.addLimitLine(ll);

//        //MPAndroidchart has a tendency to add duplicate values on the Y axis if our max value is too small.
//        //++ Limit the number of labels to the total number of assets, or to 10
//        int yAxisLabelCount = totalAssets;
//        yAxisLabelCount = Math.min(yAxisLabelCount, 10);
//        yAxis.setLabelCount(yAxisLabelCount, true);
//        chart.getAxisRight().setEnabled(false);

        yAxis.setGranularity(1.0f);
        yAxis.setGranularityEnabled(true);

        chart.setPinchZoom(false);
        chart.setScaleYEnabled(false);
        chart.setExtraOffsets(20, 20, 100, 20);


        Description d = new Description();
        d.setText(modelName);
        d.setYOffset(-35);
        d.setTextSize(20);
        chart.setDescription(d);
        chart.getDescription().setEnabled(true);
    }

    private void buildDataSet(LineDataSet dataSet, @ColorInt int color) {
        dataSet.setColor(color);
        dataSet.setCircleColor(color);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(color);
        dataSet.setFillAlpha(100);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawCircles(false);
        dataSet.setCircleRadius(0f);
        dataSet.setMode(LineDataSet.Mode.STEPPED);
        dataSet.setLineWidth(2);
    }
}
