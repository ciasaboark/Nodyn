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

package io.phobotic.nodyn.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.email.ActionHtmlFormatter;
import io.phobotic.nodyn.email.EmailRecipient;
import io.phobotic.nodyn.email.EmailSender;
import io.phobotic.nodyn.email.PastDueAssetHtmlFormatter;
import io.phobotic.nodyn.reporting.CustomEvents;
import io.phobotic.nodyn.schedule.SyncScheduler;

/**
 * Created by Jonathan Nelson on 10/19/17.
 */

public class PastDueAlertService extends IntentService {
    private static final String TAG = PastDueAlertService.class.getSimpleName();
    private Database db;

    public PastDueAlertService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "Waking");
        db = Database.getInstance(this);

        checkForPastDueAssets();
    }

    private void checkForPastDueAssets() {
        List<Asset> pastDueAssets = getPastDueAssets();

        if (pastDueAssets.size() > 0) {
            Answers.getInstance().logCustom(new CustomEvent(CustomEvents.ASSET_PAST_DUE));
            StringBuilder sb = new StringBuilder();
            sb.append(PastDueAssetHtmlFormatter.getHeader());

            PastDueAssetHtmlFormatter formatter = new PastDueAssetHtmlFormatter();
            for (Asset asset : pastDueAssets) {
                sb.append(formatter.formatAssetAsHtml(this, asset));
            }

            sb.append(ActionHtmlFormatter.getFooter());

            List<EmailRecipient> recipients = new ArrayList<>();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            // TODO: 10/19/17 there should probably be a separate list of recipients defined for past due asset reminders
            String addressesString = preferences.getString(
                    getString(R.string.pref_key_email_exceptions_addresses),
                    getString(R.string.pref_default_email_exceptions_addresses));
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
                        DateFormat df = new SimpleDateFormat();
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
}
