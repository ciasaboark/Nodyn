/*
 * Copyright (c) 2018 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.email.ActionHtmlFormatter;
import io.phobotic.nodyn_app.email.EmailRecipient;
import io.phobotic.nodyn_app.email.EmailSender;
import io.phobotic.nodyn_app.email.PastDueAssetHtmlFormatter;
import io.phobotic.nodyn_app.reporting.CustomEvents;
import io.phobotic.nodyn_app.schedule.SyncScheduler;

/**
 * Created by Jonathan Nelson on 10/19/17.
 */

public class PastDueAlertService extends IntentService {
    private static final String TAG = PastDueAlertService.class.getSimpleName();
    private Database db;
    private SharedPreferences prefs;

    public PastDueAlertService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "Waking");
        db = Database.getInstance(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Past due notices should only be sent if email alerts and past due reminders have been enabled
        boolean sendEmailAlerts = prefs.getBoolean(getString(R.string.pref_key_email_enable),
                Boolean.parseBoolean(getString(R.string.pref_default_email_enable)));
        boolean sendPastDueAlerts = prefs.getBoolean(getString(R.string.pref_key_past_due_enabled),
                Boolean.parseBoolean(getString(R.string.pref_default_past_due_enabled)));

        if (sendEmailAlerts && sendPastDueAlerts) {
            checkForPastDueAssets();
        } else {
            //reschedule for the next wake time
            SyncScheduler scheduler = new SyncScheduler(this);
            scheduler.schedulePastDueAlertsWake();
        }

    }

    private void checkForPastDueAssets() {
        List<Asset> pastDueAssets = getPastDueAssets();

        if (pastDueAssets.size() > 0) {
            Answers.getInstance().logCustom(new CustomEvent(CustomEvents.ASSET_PAST_DUE));

            //should we also send an alert to the current asset holder
            boolean includeCurrentHolder = prefs.getBoolean(
                    getString(R.string.pref_key_past_due_include_owner),
                    Boolean.parseBoolean(getString(R.string.pref_default_past_due_include_owner)));

            if (includeCurrentHolder) {
                for (Asset a : pastDueAssets) {
                    sendCurrentOwnerReminder(a);
                }
            }


            sendBulkReminder(pastDueAssets);
        }

        SyncScheduler scheduler = new SyncScheduler(this);
        scheduler.schedulePastDueAlertsWake();
    }

    @NonNull
    private List<Asset> getPastDueAssets() {
        List<Asset> allAssets = db.getAssets();
        List<Asset> pastDueAssets = new ArrayList<>();

        // TODO: 10/19/17 should this be restricted to the models that can be checked out, or send notifications for everything?
        // TODO: 10/25/17 this should have its own method in the database to avoid the overhead of looping through all assets
        long now = System.currentTimeMillis();
        for (Asset asset : allAssets) {
            if (asset.getAssignedToID() != -1) {
                long expectedCheckin = asset.getExpectedCheckin();
                if (expectedCheckin != -1) {
                    if (expectedCheckin < now) {
                        Date d = new Date(expectedCheckin);
                        DateFormat df = DateFormat.getDateTimeInstance();
                        String expectedDateString = df.format(d);
                        Log.d(TAG, "Asset " + asset.getTag() + " is past due.  Checkin expected " +
                                "by " + expectedDateString);
                        pastDueAssets.add(asset);
                    }
                }
            }
        }

        return pastDueAssets;
    }

    private void sendCurrentOwnerReminder(Asset asset) {
        //we can only send a personalized reminder if the user that currently holds this asset
        //+ has a valid email address
        try {
            User u = db.findUserByID(asset.getAssignedToID());
            String userEmailAddress = u.getEmail();

            // TODO: 10/31/17 do address validation first, or just let the email fail quietly?
            if (userEmailAddress != null && !userEmailAddress.equals("")) {
                try {
                    StringBuilder sb = new StringBuilder();
                    sb.append(PastDueAssetHtmlFormatter.getHeader());

                    PastDueAssetHtmlFormatter formatter = new PastDueAssetHtmlFormatter();
                    sb.append(formatter.formatAssetAsHtml(this, asset));
                    sb.append(ActionHtmlFormatter.getFooter());

                    List<EmailRecipient> recipients = new ArrayList<>();

                    recipients.add(new EmailRecipient(userEmailAddress));


                    EmailSender sender = new EmailSender(this)
                            .setBody(sb.toString())
                            .setSubject("Past Due Assets")
                            .setRecipientList(recipients)
                            .setFailedListener(new EmailSender.EmailStatusListener() {
                                @Override
                                public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
                                    Log.e(TAG, "Past due assets reminder email failed with message: " + message);
                                    Answers.getInstance().logCustom(new CustomEvent(CustomEvents.PAST_DUE_EMAIL_NOT_SENT));
                                }
                            }, null)
                            .setSuccessListener(new EmailSender.EmailStatusListener() {
                                @Override
                                public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
                                    Log.d(TAG, "Past due assets reminder email succeeded with message: " + message);
                                    Answers.getInstance().logCustom(new CustomEvent(CustomEvents.PAST_DUE_EMAIL_SENT));
                                }
                            }, null)
                            .send();
                } catch (Exception e) {
                    Crashlytics.logException(e);
                    e.printStackTrace();
                    Log.e(TAG, "Caught exception while sending bulk past-due asset " +
                            "reminder email to address <" + userEmailAddress + ">: " + e.getMessage());
                }
            }
        } catch (UserNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "Caught " + e.getClass().getSimpleName() + " for an asset that " +
                    "is assigned.  Was the user deleted before the asset was checked in?");
        }
    }

    private void sendBulkReminder(List<Asset> pastDueAssets) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(PastDueAssetHtmlFormatter.getHeader());

            PastDueAssetHtmlFormatter formatter = new PastDueAssetHtmlFormatter();
            for (Asset asset : pastDueAssets) {
                sb.append(formatter.formatAssetAsHtml(this, asset));
            }

            sb.append(ActionHtmlFormatter.getFooter());

            List<EmailRecipient> recipients = new ArrayList<>();


            String addressesString = prefs.getString(
                    getString(R.string.pref_key_email_past_due_addresses),
                    getString(R.string.pref_default_email_past_due_addresses));
            String[] addresses = addressesString.split(",");
            for (String address : addresses) {
                recipients.add(new EmailRecipient(address));
            }


            EmailSender sender = new EmailSender(this)
                    .setBody(sb.toString())
                    .setSubject("Past Due Assets")
                    .setRecipientList(recipients)
                    .setFailedListener(new EmailSender.EmailStatusListener() {
                        @Override
                        public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
                            Log.e(TAG, "Past due assets reminder email failed with message: " + message);
                            Answers.getInstance().logCustom(new CustomEvent(CustomEvents.PAST_DUE_EMAIL_NOT_SENT));
                        }
                    }, pastDueAssets)
                    .setSuccessListener(new EmailSender.EmailStatusListener() {
                        @Override
                        public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
                            Log.d(TAG, "Past due assets reminder email succeeded with message: " + message);
                            Answers.getInstance().logCustom(new CustomEvent(CustomEvents.PAST_DUE_EMAIL_SENT));
                        }
                    }, pastDueAssets)
                    .send();
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            Log.e(TAG, "Caught exception while sending bulk past-due asset reminder email: " + e.getMessage());
        }
    }
}
