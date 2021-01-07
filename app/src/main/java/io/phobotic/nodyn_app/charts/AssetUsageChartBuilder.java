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

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.ColorInt;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.statistics.UsageRecord;
import io.phobotic.nodyn_app.database.statistics.summary.assets.AssetStatistics;

/**
 * Created by Jonathan Nelson on 2019-05-12.
 */
public class AssetUsageChartBuilder {
    private static final String TAG = AssetUsageChartBuilder.class.getSimpleName();

    public void buildThirtyDayChart(Context context, HorizontalBarChart chart, AssetStatistics statistics) {
        buildFilteredChart(context, chart, statistics, 30);
    }

    private void buildFilteredChart(Context context, HorizontalBarChart chart, AssetStatistics statistics, int range) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -range);
        long cutoff = calendar.getTimeInMillis();
        long now = System.currentTimeMillis();
        DateFormat df = DateFormat.getDateTimeInstance();
        Log.d(TAG, String.format("Looking for records between %s and %s", df.format(cutoff), df.format(now)));

        List<UsageRecord> usageRecords = new ArrayList<>();
        for (UsageRecord r : statistics.getUsageRecords()) {
            long checkout = r.getCheckoutTimestamp();
            long checkin = r.getCheckinTimestamp();
            //zero values indicate a missing checkout record.
            if (checkout == 0) {
                checkout = cutoff;
            }

            //zero values indicate an asset that has not been checked in yet.
            if (checkin == 0) {
                checkin = now;
            }

            //exclude records that have a range completely outisde the range we are looking at, but
            //+ include everything else
            if (checkin < cutoff || checkout > now) {
                //skip these records
            } else {
                usageRecords.add(r);
            }
        }
        buildChart(context, chart, usageRecords, cutoff, now);
    }

    private void buildChart(Context context, HorizontalBarChart chart, List<UsageRecord> usageRecords, long from, long to) {
        initChart(context, chart);

        List<BarEntry> entries = new ArrayList<>();

        List<Float> barValues = new ArrayList<>();
        int recordCount = usageRecords.size();

        Long lastCheckIn = null;


        DateFormat df = SimpleDateFormat.getDateTimeInstance();
        for (UsageRecord record : usageRecords) {
            long recordOut = record.getCheckoutTimestamp();
            long recordIn = record.getCheckinTimestamp();
            Log.d(TAG, String.format("checkout: %s, checkin %s", df.format(recordOut), df.format(recordIn)));
            //its possible the last record.  In that case there will be no ending time.
            if (recordIn == 0) {
                recordIn = System.currentTimeMillis();
            }

            //add an entry representing the time this asset was idle (unless we are looking
            //+ at the first record)
            if (lastCheckIn != null) {
                long idleTimeMs = recordOut - lastCheckIn;
                float idleTimeMinutes = (float) idleTimeMs / 1000 / 60;
                float idleTimeHours = idleTimeMinutes / 60;
                Log.d(TAG, String.format("Asset idle from %s to %s.  A total time of %.2f minutes (%.2f hours)",
                        df.format(new Date(lastCheckIn)),
                        df.format(new Date(record.getCheckoutTimestamp())),
                        idleTimeMinutes,
                        idleTimeHours));

                barValues.add((float) idleTimeMs);
            }

            long usageTimeMs = record.getCheckinTimestamp() - record.getCheckoutTimestamp();
            float useageTimeMinutes = (float) usageTimeMs / 1000 / 60;
            float useageTimeHours = useageTimeMinutes / 60;
            Log.d(TAG, String.format("Asset in use from %s to %s.  A total time of %.2f minutes (%.2f hours)",
                    df.format(new Date(record.getCheckoutTimestamp())),
                    df.format(new Date(record.getCheckinTimestamp())),
                    useageTimeMinutes,
                    useageTimeHours));
            barValues.add((float) usageTimeMs);

            lastCheckIn = record.getCheckinTimestamp();
        }

        //add a placeholder record
        if (lastCheckIn < to) {
            long usage = to - lastCheckIn;
            barValues.add((float) usage);
        }

        Log.d(TAG, String.format("min timestamp: %s, max timestamp %s", df.format(from), df.format(to)));
        YAxis leftAxis = chart.getAxis(YAxis.AxisDependency.LEFT);
        leftAxis.setDrawLabels(true);
        leftAxis.setAxisMinimum(0);
        leftAxis.setAxisMaximum(to - from);
        leftAxis.setGranularity(1);
        chart.setScaleYEnabled(false);
        leftAxis.setGranularityEnabled(true);
//        leftAxis.setLabelCount(8);

        final DateFormat labelDf = new SimpleDateFormat("dd MMM");
        final Long finalMinTimestamp = from;
        leftAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                long timestamp = (long) value; //this value will come in as a delta from the min timestamp
                timestamp += finalMinTimestamp;
                String label = labelDf.format(new Date(timestamp));
                return label;
            }
        });


        int[] colors = new int[]{
                context.getResources().getColor(R.color.chart_in_use),
                context.getResources().getColor(R.color.chart_available)
        };

        float[] vals = new float[barValues.size()];
        for (int i = 0; i < barValues.size(); i++) {
            vals[i] = barValues.get(i);
        }

        entries.add(new BarEntry(0, vals));
        BarDataSet set = new BarDataSet(entries, "Usage");
        set.setColors(colors);
        set.setDrawValues(false);
        BarData data = new BarData(set);
        data.setBarWidth(40f);
        data.setHighlightEnabled(false);

        chart.setData(data);
        chart.invalidate();
    }

    private void initChart(Context context, HorizontalBarChart chart) {
        @ColorInt int gridColor = context.getResources().getColor(R.color.chart_grid);
        @ColorInt int transparent = context.getResources().getColor(android.R.color.transparent);

        //disable the x axis labels.  These would otherwise be drawn on the right edge of the chart
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1.0f);
        xAxis.setGridColor(transparent);
        xAxis.setAxisLineColor(transparent);
        xAxis.setDrawLabels(false);

        //The horizontal chart uses rotated axis.  Disable the axis on the 'right' will disable
        //+ the axis on the bottom
        YAxis rightAxis = chart.getAxis(YAxis.AxisDependency.RIGHT);
        rightAxis.setDrawLabels(false);

        List<LegendEntry> legendEntries = new ArrayList<>();
        legendEntries.add(new LegendEntry("In use", Legend.LegendForm.DEFAULT,
                10f, 2f, null, context.getResources().getColor(
                R.color.chart_in_use)));
        legendEntries.add(new LegendEntry("Available", Legend.LegendForm.DEFAULT,
                10f, 2f, null, context.getResources().getColor(
                R.color.chart_available)));

        Legend l = chart.getLegend();
        l.setCustom(legendEntries);

        chart.getLegend().setTextSize(15f);

        chart.setPinchZoom(true);
        chart.setScaleYEnabled(false);
        chart.setExtraOffsets(20, 20, 20, 20);
    }

    public void buildSevenDayChart(Context context, HorizontalBarChart chart, AssetStatistics statistics) {
        buildFilteredChart(context, chart, statistics, 7);
    }
}
