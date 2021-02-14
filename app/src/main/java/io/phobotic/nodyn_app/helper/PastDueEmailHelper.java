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

package io.phobotic.nodyn_app.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.Nullable;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.cache.EmailImageCache;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.email.AssetHtmlFormatter;
import io.phobotic.nodyn_app.email.Attachment;
import io.phobotic.nodyn_app.email.EmailRecipient;
import io.phobotic.nodyn_app.email.EmailSender;
import io.phobotic.nodyn_app.reporting.CustomEvents;

/**
 * Created by Jonathan Nelson on 7/3/18.
 */
public class PastDueEmailHelper {
    private static final String TAG = PastDueEmailHelper.class.getSimpleName();
    private static final String MARKER_APP_ICON = "%appicon%";
    private static final String MARKER_PAST_DUE = "%pastdueassets%";
    private static final String MARKER_DUE_SOON = "%duesoonassets%";
    private static final String MARKER_DATETIME_GENERATED = "%datetime%";
    private static final String MARKER_DEVICE_NAME = "%devicename%";
    private static final String MARKER_HEADER_CHEVRON = "%headerchevron%";
    private static final String MARKER_PAST_DUE_CHEVRON = "%pastduechevron%";
    private static final String MARKER_DUE_SOON_CHEVRON = "%duesoonchevron%";
    private static final String MARKER_SMILE = "%smile_image%";

    private static final String PAST_DUE_NO_ASSETS = "<img class=\"smile\" src=\"%smile_image%\" style=\"width: 100px;height: 100px;margin-bottom: .5em;\">\n" +
            "        <div>No items are past due</div>";
    private static final String DUE_SOON_NO_ASSETS = "<img class=\"smile\" src=\"%smile_image%\" style=\"width: 100px;height: 100px;margin-bottom: .5em;\">\n" +
            "        <div>No items are due within the next 24 hours</div>";

    private static final String KEY_ASSET_HEADER_CHEVRON = "header_chevron";
    private static final String KEY_ASSET_PAST_DUE_CHEVRON = "past_due_chevron";
    private static final String KEY_ASSET_DUE_SOON_CHEVRON = "due_soon_chevron";
    private static final String KEY_ASSET_SMILE = "smile";
    private static final String KEY_ASSET_APP_ICON = "app_icon";


    private final Context context;

    public PastDueEmailHelper(Context context) {
        this.context = context;
    }

    public void sendCurrentOwnerReminder(Asset asset) {
//        //we can only send a personalized reminder if the user that currently holds this asset
//        //+ has a valid email address
//        try {
//            Database db = Database.getInstance(context);
//            User u = db.findUserByID(asset.getAssignedToID());
//            String userEmailAddress = u.getEmail();
//
//            // TODO: 10/31/17 do address validation first, or just let the email fail quietly?
//            if (userEmailAddress != null && !userEmailAddress.equals("")) {
//                try {
//                    StringBuilder sb = new StringBuilder();
//                    sb.append(PastDueAssetHtmlFormatter.getHeader());
//
//                    PastDueAssetHtmlFormatter formatter = new PastDueAssetHtmlFormatter();
//                    sb.append(formatter.formatAssetAsHtml(context, asset));
//                    sb.append(ActionHtmlFormatter.getFooter());
//
//                    List<EmailRecipient> recipients = new ArrayList<>();
//
//                    recipients.add(new EmailRecipient(userEmailAddress));
//
//
//                    EmailSender sender = new EmailSender(context)
//                            .setBody(sb.toString())
//                            .setSubject("Past Due Assets")
//                            .setRecipientList(recipients)
//                            .setFailedListener(new EmailSender.EmailStatusListener() {
//                                @Override
//                                public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
//                                    Log.e(TAG, "Past due assets reminder email failed with message: " + message);
//                                    FirebaseAnalytics.getInstance(getApplicationContext()).logEvent(CustomEvents.PAST_DUE_EMAIL_NOT_SENT, null);
//                                }
//                            }, null)
//                            .setSuccessListener(new EmailSender.EmailStatusListener() {
//                                @Override
//                                public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
//                                    Log.d(TAG, "Past due assets reminder email succeeded with message: " + message);
//                                    FirebaseAnalytics.getInstance(getApplicationContext()).logEvent(CustomEvents.PAST_DUE_EMAIL_SENT, null);
//                                }
//                            }, null)
//                            .send();
//                } catch (Exception e) {
//                    FirebaseCrashlytics.getInstance().recordException(e);
//                    e.printStackTrace();
//                    Log.e(TAG, "Caught exception while sending bulk past-due asset " +
//                            "reminder email to address <" + userEmailAddress + ">: " + e.getMessage());
//                }
//            }
//        } catch (UserNotFoundException e) {
//            e.printStackTrace();
//            Log.e(TAG, "Caught " + e.getClass().getSimpleName() + " for an asset that " +
//                    "is assigned.  Was the user deleted before the asset was checked in?");
//        }
    }

    public void sendBulkReminder(List<Asset> pastDueAssets, List<Asset> dueSoonAssets) {
        EmailImageCache imageCache = EmailImageCache.getInstance(context);
        List<Model> modelList = getModelsList(pastDueAssets, dueSoonAssets);

        //go ahead and cache some of the model images
        imageCache.updateModelImageCache(modelList);

        //go ahead and cache some of the static images
        imageCache.cacheAssetImage(KEY_ASSET_HEADER_CHEVRON, "header_chevron.png");
        imageCache.cacheAssetImage(KEY_ASSET_PAST_DUE_CHEVRON, "past_due_chevron.png");
        imageCache.cacheAssetImage(KEY_ASSET_DUE_SOON_CHEVRON, "due_soon_chevron.png");
        imageCache.cacheAssetImage(KEY_ASSET_SMILE, "smile.png");
        imageCache.cacheAssetImage(KEY_ASSET_APP_ICON, "app_icon_96.png");

        try {
            String html = getUnformattedHtml();
            List<Attachment> attachments = new ArrayList<>();
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            html = buildHtml(pastDueAssets, dueSoonAssets, imageCache, html, attachments, prefs);

            List<EmailRecipient> recipients = new ArrayList<>();

            //include the equipment manager(s) in all past due reminders
            String equipmentManagers = prefs.getString(
                    context.getString(R.string.pref_key_equipment_managers_addresses),
                    context.getString(R.string.pref_default_equipment_managers_addresses));
            String[] managers = equipmentManagers.split(",");
            for (String address : managers) {
                recipients.add(new EmailRecipient(address));
            }

            //include any additional addresses that have been marked to receive past due reminders
            String addressesString = prefs.getString(
                    context.getString(R.string.pref_key_email_past_due_addresses),
                    context.getString(R.string.pref_default_email_past_due_addresses));
            String[] addresses = addressesString.split(",");
            for (String address : addresses) {
                recipients.add(new EmailRecipient(address));
            }


            EmailSender sender = new EmailSender(context)
                    .setBody(html)
                    .setSubject("Past Due Assets")
                    .setRecipientList(recipients)
                    .withAttachments(attachments)
                    .setFailedListener(new EmailSender.EmailStatusListener() {
                        @Override
                        public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
                            Log.e(TAG, "Past due assets reminder email failed with message: " + message);
                            FirebaseAnalytics.getInstance(context).logEvent(CustomEvents.PAST_DUE_EMAIL_NOT_SENT, null);
                        }
                    }, pastDueAssets)
                    .setSuccessListener(new EmailSender.EmailStatusListener() {
                        @Override
                        public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
                            Log.d(TAG, "Past due assets reminder email succeeded with message: " + message);
                            FirebaseAnalytics.getInstance(context).logEvent(CustomEvents.PAST_DUE_EMAIL_SENT, null);
                        }
                    }, pastDueAssets)
                    .send();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            e.printStackTrace();
            Log.e(TAG, "Caught exception while sending bulk past-due asset reminder email: " + e.getMessage());
        }
    }

    private List<Model> getModelsList(List<Asset> pastDueAssets, List<Asset> dueSoonAssets) {
        List<Model> models = new ArrayList<>();
        Set<Asset> combinedSet = new HashSet<>();
        combinedSet.addAll(pastDueAssets);
        combinedSet.addAll(dueSoonAssets);

        Database db = Database.getInstance(context);
        for (Asset a : combinedSet) {
            try {
                Model m = db.findModelByID(a.getModelID());
                if (!models.contains(m)) {
                    models.add(m);
                }
            } catch (ModelNotFoundException e) {
                //nothing to do here
            }
        }

        return models;
    }

    private String getUnformattedHtml() throws IOException {
        InputStream is = context.getAssets().open("template_email_past_due");
        int size = is.available();

        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        String htmlString = new String(buffer);
        return htmlString;
    }

    public String buildHtml(List<Asset> pastDueAssets, List<Asset> dueSoonAssets,
                            EmailImageCache imageCache, String html, List<Attachment> attachments,
                            SharedPreferences prefs) throws IOException {
        AssetHtmlFormatter formatter = new AssetHtmlFormatter(context);

        //insert the main app icon
        String appIconSrc = imageCache.getCachedImage(KEY_ASSET_APP_ICON);
        addCachedFileAsAttachment(attachments, appIconSrc, imageCache);
        appIconSrc = "cid:" + appIconSrc;
        html = html.replaceAll(MARKER_APP_ICON, appIconSrc);

        //insert the chevrons
//            String headerChevron = imageCache.getCachedImage(KEY_ASSET_HEADER_CHEVRON);
//            addCachedFileAsAttachment(attachments, headerChevron);
//            headerChevron = "cid:" + headerChevron;
//            html = html.replaceAll(MARKER_HEADER_CHEVRON, headerChevron);
//
//            String dueSoonChevron = imageCache.getCachedImage(KEY_ASSET_DUE_SOON_CHEVRON);
//            addCachedFileAsAttachment(attachments, dueSoonChevron);
//            dueSoonChevron = "cid:" + dueSoonChevron;
//            html = html.replaceAll(MARKER_DUE_SOON_CHEVRON, dueSoonChevron);
//
//            String pastDueChevron = imageCache.getCachedImage(KEY_ASSET_PAST_DUE_CHEVRON);
//            addCachedFileAsAttachment(attachments, pastDueChevron);
//            pastDueChevron = "cid:" + pastDueChevron;
//            html = html.replaceAll(MARKER_PAST_DUE_CHEVRON, pastDueChevron);


        if (pastDueAssets != null && pastDueAssets.size() > 0) {
            sortList(pastDueAssets);

            StringBuilder pastDueAssetHtml = new StringBuilder();
            for (Asset asset : pastDueAssets) {
                pastDueAssetHtml.append(formatter.formatPastDueAssetAsHtml(attachments, imageCache, asset));
            }
            html = html.replaceAll(MARKER_PAST_DUE, pastDueAssetHtml.toString());
        } else {
            html = html.replaceAll(MARKER_PAST_DUE, PAST_DUE_NO_ASSETS);
        }

        if (dueSoonAssets != null && dueSoonAssets.size() > 0) {
            sortList(dueSoonAssets);
            StringBuilder dueSoonAssetHtml = new StringBuilder();
            for (Asset asset : dueSoonAssets) {
                dueSoonAssetHtml.append(formatter.formatDueSoonAssetAsHtml(attachments, imageCache, asset));
            }
            html = html.replaceAll(MARKER_DUE_SOON, dueSoonAssetHtml.toString());
        } else {
            html = html.replaceAll(MARKER_DUE_SOON, DUE_SOON_NO_ASSETS);
        }

        Date now = new Date();
        DateFormat df = SimpleDateFormat.getDateTimeInstance();
        html = html.replaceAll(MARKER_DATETIME_GENERATED, df.format(now));


        String deviceName = prefs.getString(context.getString(R.string.pref_key_general_id),
                context.getString(R.string.pref_default_general_id));
        if (deviceName != null && deviceName.length() > 0) {
            deviceName = "No device name set";
        }
        html = html.replaceAll(MARKER_DEVICE_NAME, deviceName);


        //insert the smile image
        String smileImage = imageCache.getCachedImage(KEY_ASSET_SMILE);
        addCachedFileAsAttachment(attachments, smileImage, imageCache);
        smileImage = "cid:" + smileImage;
        html = html.replaceAll(MARKER_SMILE, smileImage);

        return html;
    }

    private void addCachedFileAsAttachment(List<Attachment> attachments, String filename,
                                           EmailImageCache cache) {
        File f = cache.getFileForFilename(filename);
        Attachment attachment = new Attachment(f, filename + ".png", filename);
        attachment.setInline(true);
        attachment.setContentType("image/png");
        attachments.add(attachment);
    }

    public void sortList(List<Asset> assetList) {
        Collections.sort(assetList, new Comparator<Asset>() {
            @Override
            public int compare(Asset o1, Asset o2) {
                int result = Integer.compare(o1.getManufacturerID(), o2.getManufacturerID());
                if (result == 0) {
                    result = Integer.compare(o1.getModelID(), o2.getModelID());
                }

                if (result == 0) {
                    result = o1.getTag().compareTo(o2.getTag());
                }

                return result;
            }
        });
    }
}
