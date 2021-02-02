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

package io.phobotic.nodyn_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.cache.EmailImageCache;
import io.phobotic.nodyn_app.charts.AssetUsageChartBuilder;
import io.phobotic.nodyn_app.charts.HistoryChartBuilder;
import io.phobotic.nodyn_app.charts.HourlyUsageChartBuilder;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.audit.AuditDatabase;
import io.phobotic.nodyn_app.database.audit.model.AuditDetail;
import io.phobotic.nodyn_app.database.audit.model.AuditHeader;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.exception.ManufacturerNotFoundException;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Manufacturer;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.statistics.UsageRecord;
import io.phobotic.nodyn_app.database.statistics.summary.assets.AssetStatistics;
import io.phobotic.nodyn_app.database.statistics.summary.assets.AssetStatisticsDatabase;
import io.phobotic.nodyn_app.database.statistics.summary.day_activity.DayActivitySummary;
import io.phobotic.nodyn_app.database.statistics.summary.day_activity.DayActivitySummaryDatabase;
import io.phobotic.nodyn_app.email.Attachment;
import io.phobotic.nodyn_app.helper.AssetHelper;

/**
 * Created by Jonathan Nelson on 2019-05-28.
 */
public class WeeklyStatisticsEmailBuilder implements StatisticsEmailBuilder {
    private static final String TAG = WeeklyStatisticsEmailBuilder.class.getSimpleName();
    private static final String MARKER_IMAGE = "%image%";
    private static final String MARKER_BODY = "%body%";
    private static final String MARKER_HEADER = "%header%";
    private static final String IMAGE_SNIPPIT = "<img width=\"100%\" style=\"max-width: 100%\" src=\"%image%\" />";
    private static final String MODEL_SNIPPIT = "<p>" +
            "<h3 style=\"color: black; font-size: 14pt\">%model_name%</h3>" +
            "<div>Manufacturer: %manufacturer%</div>" +
            "<p>%asset_count% available as of %generation_date%</p>" +
            "<p><img width=\"100%\" style=\"max-width: 100%\" src=\"%image%\" /></p>" +
            "</p>";
    private static final String BODY_SNIPPIT = "<html><bodystyle=\"font-family: sans-serif; padding: 16px\">%header%<div>%body%</div></body></html>";
    private static final String HEADER_SNIPPIT = "<div style=\"background-color: #FF9100; color: white; padding: 16px; margin-bottom: 16pt; border-radius: 5px\"><h1 style=\"margin: 0; padding: 0 0 8pt 0\">Nodyn Weekly Statistics</h1><div>Sent from device: %device_name%</div><div style=\"margin-top: 1em; font-size: 8pt; color: rgba(255, 255, 255, 0.7)\">Generated %generation_timestamp%</div></div><p>These statistics cover dates from %begin_date% through %end_date%.</p><p>During this time there were %total_checkouts% check-out events and %total_checkins% check-in events.</p><p>%total_audits% asset audits were completed</p><p>There are currently %total_asset_count% known assets.</p><div style=\"text-align: center; margin:auto; margin-bottom: 16pt;\"><table style=\"margin: auto; text-align: center\"><tr><td><div style=\"min-width: 15em; max-width: 15em; width: 15em; margin: 4pt; background-color: #95FF48; padding: 16px; border-radius: 5px;\"><div style=\"font-size: 15pt; font-weight: bold; text-align: center; color: black\">Available to Check Out</div><div style=\"font-size: 20pt; font-weight: bold; text-align: center; color: black\">%available_count%</div></div></td><td><div style=\"min-width: 15em; max-width: 15em; width: 15em; margin: 4pt; background-color: #FFCA6C; padding: 16px; border-radius: 5px;\"><div style=\"font-size: 15pt; font-weight: bold; text-align: center; color: black\">Checked Out</div><div style=\"font-size: 20pt; font-weight: bold; text-align: center; color: black\">%checked_out_count%</div></div></td><td><div style=\"min-width: 15em; max-width: 15em; width: 15em; margin: 4pt; background-color: #FF5D82; padding: 16px; border-radius: 5px;\"><div style=\"font-size: 15pt; font-weight: bold; text-align: center; color: black\">Past Due</div><div style=\"font-size: 20pt; font-weight: bold; text-align: center; color: black\">%past_due_count%</div></div></td></tr></table></div>";
    private final long from;
    private final long to;

    public WeeklyStatisticsEmailBuilder(long from, long to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String build(Context context, List<Attachment> attachments) {
        String html = "";
        try {
            EmailImageCache cache = EmailImageCache.getInstance(context);

            File cacheDir = getCacheImageDir(context);
            StringBuilder imagesPart = new StringBuilder();
            imagesPart.append(buildCheckoutHistoryChart(context, cache, attachments, cacheDir));
            //                    imagesPart.append(buildAssetUsageCharts(context, cache, attachments, cacheDir));
            imagesPart.append(buildModelUsageCharts(context, cache, attachments, cacheDir));

            html = BODY_SNIPPIT;
            String headerHtml = generateHeaderHtml(context);
            html = html.replace(MARKER_HEADER, headerHtml);
            html = html.replaceAll(MARKER_BODY, imagesPart.toString());

            Log.d(TAG, html);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Caught unexpected IOException while building weekly statistics email: " + e.getMessage());
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        return html;
    }

    private String generateHeaderHtml(Context context) {
        String html = HEADER_SNIPPIT;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        DateFormat df = DateFormat.getDateInstance();
        DateFormat dtf = DateFormat.getDateTimeInstance();

        //inject the device name and generation date
        String deviceName = prefs.getString(context.getString(R.string.pref_key_general_id),
                context.getString(R.string.pref_default_general_id));
        html = html.replace("%device_name%", deviceName);
        html = html.replace("%generation_timestamp%", dtf.format(new Date()));

        html = html.replace("%begin_date%", df.format(new Date(from)));
        html = html.replace("%end_date%", df.format(new Date(to)));

        //find out how many check-out/check-in/audit events occurred during this period
        int totalCheckouts = 0;
        int totalCheckins = 0;
        int totalAudits = 0;
        List<DayActivitySummary> daySummaries = getDayActivitySummaries(context);
        for (DayActivitySummary das: daySummaries) {
            totalAudits += das.getTotalAudits();
            totalCheckouts += das.getTotalCheckouts();
            totalCheckins += das.getTotalCheckins();
        }
        html = html.replace("%total_checkouts%", String.valueOf(totalCheckouts));
        html = html.replace("%total_checkins%", String.valueOf(totalCheckins));
        html = html.replace("%total_audits%", String.valueOf(totalAudits));

        //insert the total number of assets that are known
        AssetHelper ah = new AssetHelper();
        Set<Integer> allowedModels = ah.getAllowedModelIDs(context);
        Database db = Database.getInstance(context);
        List<Asset> assets = db.getAssets();
        int totalAssets = assets.size();
        html = html.replace("%total_asset_count%", String.valueOf(totalAssets));

        //fill in the number of assets that are checked out or past due
        boolean checkoutAllModels = prefs.getBoolean(context.getString(R.string.pref_key_check_out_all_models),
                Boolean.parseBoolean(context.getString(R.string.pref_default_check_out_all_models)));
        Set<String> availableModels = prefs.getStringSet(context.getString(R.string.pref_key_check_out_models), new HashSet<String>());
        int availableCount = 0;
        int checkedOutCount = 0;
        int pastDueCount = 0;
        for (Asset a : assets) {
            //only look at the models that are allowed to be checked out
            if (checkoutAllModels || availableModels.contains(String.valueOf(a.getModelID()))) {
                if (a.getExpectedCheckin() == -1) {
                    availableCount++;
                } else {
                    checkedOutCount++;

                    //is this model past due?
                    Date expectedCheckin = new Date(a.getExpectedCheckin());
                    Date now = new Date();
                    if (expectedCheckin.before(now)) {
                        pastDueCount++;
                    }
                }
            }
        }

        html = html.replace("%available_count%", String.valueOf(availableCount));
        html = html.replace("%checked_out_count%", String.valueOf(checkedOutCount));
        html = html.replace("%past_due_count%", String.valueOf(pastDueCount));





        return html;
    }

    private File getCacheImageDir(Context context) throws IOException {
        File cacheDir = new File(context.getCacheDir(), "statistics-images/");
        cacheDir.mkdirs();
        FileUtils.cleanDirectory(cacheDir);
        return cacheDir;
    }

    public String buildCheckoutHistoryChart(Context context, EmailImageCache cache,
                                            List<Attachment> attachments,
                                            File cacheDir) throws IOException {
        String imageSnippit = IMAGE_SNIPPIT;
        List<DayActivitySummary> list = getDayActivitySummaries(context);

        //also include any audits.
        List<AuditHeader> audits = getAuditHeaders(context);

        LineChart chart = new LineChart(context);
        chart.layout(0, 0, 2000, 400);
        HistoryChartBuilder builder = new HistoryChartBuilder();
        builder.buildChart(context, chart, list, audits);


        File imageFile = new File(cacheDir, "chart.png");
        FileOutputStream fos = new FileOutputStream(imageFile);

        int width = chart.getWidth();
        int height = chart.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(bmp);
        chart.draw(canvas);
        bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);

        final String key = "checkout_history_chart";
        cache.cacheImage(key, imageFile, false);

        //insert the static boxes
        String imageSrc = cache.getCachedImage(key);
        addCachedFileAsAttachment(attachments, imageSrc, cache);
        imageSrc = "cid:" + imageSrc;
        imageSnippit = imageSnippit.replaceAll(MARKER_IMAGE, imageSrc);

        return imageSnippit;
    }

    /**
     * Returns a list of Audit headers between the from and to timestamps
     * @param context
     * @return
     */
    private List<AuditHeader> getAuditHeaders(Context context) {
        AuditDatabase auditDatabase = AuditDatabase.getInstance(context);
        List<AuditHeader> audits = auditDatabase.headerDao().findAll();
        Iterator<AuditHeader> it = audits.iterator();
        while (it.hasNext()) {
            AuditHeader a = it.next();
            List<AuditDetail> details = auditDatabase.detailDao().findByAudit(a.getId());
            if (a.getBegin() < from || a.getEnd() > to) {
                it.remove();
            } else if (details == null || details.isEmpty()) {
                it.remove();
            }
        }
        return audits;
    }

    /**
     * Returns a list of DayActivitySummry that are between the from and to timestamps
     * @param context
     * @return
     */
    private List<DayActivitySummary> getDayActivitySummaries(Context context) {
        DayActivitySummaryDatabase db = DayActivitySummaryDatabase.getInstance(context);
        return db.dayActivityDao().getActivityBetween(from, to);
    }

    public String buildModelUsageCharts(Context context, EmailImageCache cache,
                                        List<Attachment> attachments,
                                        File cacheDir) throws IOException {


        StringBuilder sb = new StringBuilder();

        //add placeholder records in case there are no asset statistics for a model that could have
        //+ been checked out
        Map<Integer, List<AssetStatistics>> modelMap = new HashMap<>();
        AssetHelper helper = new AssetHelper();
        Set<Integer> allowedModelIDs = helper.getAllowedModelIDs(context);
        for (Integer i : allowedModelIDs) {
            modelMap.put(i, new ArrayList<AssetStatistics>());
        }

        List<AssetStatistics> assetStatistics = getAssetStatistics(context);
        Database db = Database.getInstance(context);
        for (AssetStatistics statistics : assetStatistics) {
            try {
                Asset a = db.findAssetByID(statistics.getId());
                List<AssetStatistics> l = modelMap.get(a.getModelID());
                if (l == null) l = new ArrayList<>();
                l.add(statistics);
                modelMap.put(a.getModelID(), l);
            } catch (AssetNotFoundException e) {
                Log.w(TAG, String.format("Unable to find asset with id %d.  This asset will " +
                        "be skipped while building model hourly usage charts", statistics.getId()));
            }
        }


        for (Map.Entry<Integer, List<AssetStatistics>> entry : modelMap.entrySet()) {
            int modelID = entry.getKey();
            if (helper.modelCanBeCheckedOut(context, modelID)) {
                Log.d(TAG, String.format("Building model hourly usage chart for model id %d", modelID));
                List<AssetStatistics> l = entry.getValue();
                sb.append(buildModelUsageChart(context, cache, attachments, modelID, l, cacheDir));
            } else {
                Log.d(TAG, String.format("Skipping model ID %d.  This model can not be checked " +
                        "out from this device", modelID));
            }
        }


        return sb.toString();
    }

    private List<AssetStatistics> getAssetStatistics(Context context) {
        AssetStatisticsDatabase assetStatisticsDatabase = AssetStatisticsDatabase.getInstance(context);
        List<AssetStatistics> assetStatistics = assetStatisticsDatabase.assetStatisticsDao().getAll();

        return assetStatistics;
    }

    private void addCachedFileAsAttachment(List<Attachment> attachments, String filename,
                                           EmailImageCache cache) {
        File f = cache.getFileForFilename(filename);
        Attachment attachment = new Attachment(f, filename + ".png", filename);
        attachment.setInline(true);
        attachment.setContentType("image/png");
        attachments.add(attachment);
    }

    private String buildModelUsageChart(Context context, EmailImageCache cache,
                                        List<Attachment> attachments, int modelID,
                                        List<AssetStatistics> assetStatistics,
                                        File cacheDir) throws IOException {
        String modelSnippit = MODEL_SNIPPIT;
        AssetHelper helper = new AssetHelper();

        try {
            Calendar calendar = Calendar.getInstance();
            //jump back seven days, then add a placeholder timestamp to the usage map for every hour
            //+ up until now

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.add(Calendar.HOUR_OF_DAY, -1);
            long to = calendar.getTimeInMillis();

            calendar.add(Calendar.DAY_OF_YEAR, -7);
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            long from = calendar.getTimeInMillis();

            DateFormat df = DateFormat.getDateTimeInstance();
            Log.d(TAG, String.format("Building usage graph from %s to %s",
                    df.format(new Date(from)),
                    df.format(new Date(to))));

            Map<Long, Integer> usageMap = buildHourlyMap(from, to);
            String modelName = String.valueOf(modelID);
            Database db = Database.getInstance(context);
            Model m = db.findModelByID(modelID);
            modelName = m.getName();

            modelSnippit = modelSnippit.replace("%model_name%", modelName);

            Manufacturer manufacturer = db.findManufacturerByID(m.getManufacturerID());
            modelSnippit = modelSnippit.replace("%manufacturer%", manufacturer.getName());

            //filter the list down to only the assets that could possibly be checked out
            List<Asset> allModelAssets = db.findAssetsByModelID(modelID);
            Iterator<Asset> it = allModelAssets.iterator();

            while (it.hasNext()) {
                Asset a = it.next();
                if (!helper.isAssetStatusValid(context, a)) {
                    it.remove();
                }
            }

            //keep track of all the assets we have usage records for. If an asset has no usage
            //+ records we will need to check if it was previously checked out and add a placeholder
            //+ usage record
            Set<Integer> assetsWithRecords = new HashSet<>();
            int totalAssets = allModelAssets.size();

            modelSnippit = modelSnippit.replace("%asset_count%", String.valueOf(totalAssets));
            modelSnippit = modelSnippit.replace("%generation_date%", df.format(new Date()));

            //once the placeholders are in all we need to do is loop through each set of statistics
            //+ and check if the asset checkout range includes the hour
            // TODO: 2019-05-15 three deep nested loop?  find a better way
            for (Map.Entry<Long, Integer> entry : usageMap.entrySet()) {
                long timestamp = entry.getKey();
                int count = entry.getValue();

                for (AssetStatistics statistics : assetStatistics) {
                    assetsWithRecords.add(statistics.getId());
                    List<UsageRecord> usageRecords = statistics.getUsageRecords();
                    if (usageRecords != null) {
                        for (UsageRecord record : usageRecords) {

                            long checkout = record.getCheckoutTimestamp();
                            long checkin = record.getCheckinTimestamp();
                            if (checkout == 0) {
                                checkout = from;
                            }
                            if (checkin == 0) {
                                checkin = to;
                            }

                            //skip over usage records that are completely outside the range we
                            //+ are looking at
                            if (checkin < from || checkout > to) {
                                continue;
                            } else if (timestamp >= checkout && timestamp <= checkin) {
                                count++;
                            }
                        }
                    }
                }

                Log.d(TAG, String.format("Model %d total items checked out at %s: %d", modelID,
                        df.format(new Date(timestamp)), count));

                usageMap.put(timestamp, count);
            }

            int bump = 0;
            for (Asset a : allModelAssets) {
                //if this asset is currently checked but was not accounted for in the usage records
                //+ bump up the usage count on each of the hourly usage counts
                if (!assetsWithRecords.contains(a.getId())) {
                    if (a.getLastCheckout() != -1) {
                        Log.d(TAG, String.format("Asset %s with ID %d did not have any usage " +
                                        "records but was checked out during this period (%s)", a.getTag(),
                                a.getId(), df.format(a.getLastCheckout())));
                        bump++;
                    }
                }
            }

            for (Map.Entry<Long, Integer> entry : usageMap.entrySet()) {
                long key = entry.getKey();
                int count = entry.getValue();
                count += bump;
                usageMap.put(key, count);
            }

            //build the chart
            LineChart chart = new LineChart(context);
            chart.layout(0, 0, 2000, 400);
            HourlyUsageChartBuilder builder = new HourlyUsageChartBuilder();
            builder.buildChart(context, chart, modelName, totalAssets, usageMap, from, to);


            File imageFile = new File(cacheDir, String.format("hourly-usage-%d.png", modelID));
            FileOutputStream fos = new FileOutputStream(imageFile);

            int width = chart.getWidth();
            int height = chart.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bmp.eraseColor(Color.WHITE);
            Canvas canvas = new Canvas(bmp);
            chart.draw(canvas);
            bmp = addBorder(context, bmp);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);

            final String key = String.format("hourly-usage-%d", modelID);
            cache.cacheImage(key, imageFile, false);

            //insert the static boxes
            String imageSrc = cache.getCachedImage(key);
            addCachedFileAsAttachment(attachments, imageSrc, cache);
            imageSrc = "cid:" + imageSrc;
            modelSnippit = modelSnippit.replaceAll(MARKER_IMAGE, imageSrc);
        } catch (ModelNotFoundException | ManufacturerNotFoundException e) {
            modelSnippit = String.format("<div>Model id %d could not be found. Hourly statistics could " +
                    "not be built for this model</div>", modelID);
        }

        return modelSnippit;
    }

    private Map<Long, Integer> buildHourlyMap(long from, long to) {
        Map<Long, Integer> usageMap = new LinkedHashMap<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(from);
        long timestamp = calendar.getTimeInMillis();
        while (timestamp <= to) {
            usageMap.put(timestamp, 0);
            calendar.add(Calendar.HOUR_OF_DAY, 1);
            timestamp = calendar.getTimeInMillis();
        }
        return usageMap;
    }

    private Bitmap addBorder(Context context, Bitmap bmp) {
        final int borderSize = 2;
        Bitmap resizedBitmap = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(resizedBitmap);
        @ColorInt int borderColor = context.getResources().getColor(R.color.chart_border);
        canvas.drawColor(borderColor);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        return resizedBitmap;
    }

    private String buildAssetUsageCharts(Context context, EmailImageCache cache,
                                         List<Attachment> attachments,
                                         File cacheDir) throws Exception {
        AssetStatisticsDatabase assetStatisticsDatabase = AssetStatisticsDatabase.getInstance(context);
        List<AssetStatistics> list = assetStatisticsDatabase.assetStatisticsDao().getAll();
        HorizontalBarChart chart = new HorizontalBarChart(context);
        chart.layout(0, 0, 800, 150);
        chart.setFitBars(true);
        Database db = Database.getInstance(context);
        AssetHelper helper = new AssetHelper();

        //sort the assets so they are in order by

        List<Pair> assetPairs = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (AssetStatistics statistics : list) {
            try {
                Asset asset = db.findAssetByID(statistics.getId());
                if (helper.isAssetUsable(context, asset)) {
                    Pair p = new Pair(asset, statistics);
                    assetPairs.add(p);
                }
            } catch (AssetNotFoundException e) {
                Log.d(TAG, String.format("Unable to find asset with ID %d.  Skipping this asset.",
                        statistics.getId()));
            }
        }

        Collections.sort(assetPairs, new Comparator<Pair>() {
            @Override
            public int compare(Pair o1, Pair o2) {
                int result = Integer.compare(o1.getAsset().getManufacturerID(), o2.getAsset().getManufacturerID());
                if (result == 0) {
                    result = Integer.compare(o1.getAsset().getModelID(), o2.getAsset().getModelID());

                    if (result == 0) {
                        result = o1.getAsset().getTag().compareTo(o2.getAsset().getTag());
                    }
                }

                return result;
            }
        });

        for (Pair p : assetPairs) {
            sb.append(buildAssetUsageChart(context, cache, cacheDir, attachments, p.getAsset(),
                    p.getStatistics(), chart));
        }

        return sb.toString();
    }

    private String buildAssetUsageChart(Context context, EmailImageCache cache, File cacheDir,
                                        List<Attachment> attachments,
                                        Asset asset, AssetStatistics statistics,
                                        HorizontalBarChart chart) throws Exception {
        String imageSnippit = IMAGE_SNIPPIT;
        Description d = new Description();
        d.setText(asset.getTag());
        d.setYOffset(-35);
        d.setTextSize(20);
        chart.setDescription(d);

        AssetUsageChartBuilder builder = new AssetUsageChartBuilder();
        builder.buildThirtyDayChart(context, chart, statistics);

        final String key = String.format("%d-%d-%s", asset.getModelID(), asset.getId(),
                asset.getTag());
        File imageFile = new File(cacheDir, String.format("%s.jpg", key));
        FileOutputStream fos = new FileOutputStream(imageFile);

        int width = chart.getWidth();
        int height = chart.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(bmp);
        chart.draw(canvas);
        bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);


        cache.cacheImage(key, imageFile, false);

        //insert the static boxes
        String imageSrc = cache.getCachedImage(key);
        addCachedFileAsAttachment(attachments, imageSrc, cache);
        imageSrc = "cid:" + imageSrc;
        imageSnippit = imageSnippit.replaceAll(MARKER_IMAGE, imageSrc);

        return imageSnippit;
    }

    private class Pair {
        private Asset asset;
        private AssetStatistics statistics;

        public Pair(Asset asset, AssetStatistics statistics) {
            this.asset = asset;
            this.statistics = statistics;
        }

        public Asset getAsset() {
            return asset;
        }

        public AssetStatistics getStatistics() {
            return statistics;
        }
    }
}
