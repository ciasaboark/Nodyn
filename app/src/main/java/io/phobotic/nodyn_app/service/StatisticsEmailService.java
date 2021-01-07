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

package io.phobotic.nodyn_app.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import io.phobotic.nodyn_app.MonthlyStatisticsEmailBuilder;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.StatisticsEmailBuilder;
import io.phobotic.nodyn_app.WeeklyStatisticsEmailBuilder;
import io.phobotic.nodyn_app.email.Attachment;
import io.phobotic.nodyn_app.email.EmailRecipient;
import io.phobotic.nodyn_app.email.EmailSender;
import io.phobotic.nodyn_app.reporting.CustomEvents;

/**
 * Created by Jonathan Nelson on 2019-05-12.
 */
public class StatisticsEmailService extends IntentService {
    public static final String TYPE_WEEKLY = "weekly";
    public static final String TYPE_MONTHLY = "monthly";
    public static final String TYPE_KEY = "key";
    private static final String TAG = StatisticsEmailService.class.getSimpleName();

    public StatisticsEmailService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final Context context = this;
        Bundle bundle = intent.getExtras();
        String type = null;
        if (bundle != null) {
            type = bundle.getString(TYPE_KEY);
        }

        StatisticsEmailBuilder builder = null;
        Calendar calendar = Calendar.getInstance();
        String subject = null;

        if (type == null) {
            Log.e(TAG, "Called without an email type key.  No action will be performed.  " +
                    "Remember to attach a TYPE_KEY to the bundle");
        } else if (TYPE_WEEKLY.equals(type)) {
            DateFormat df = DateFormat.getDateInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            long from = calendar.getTimeInMillis();
            long to = System.currentTimeMillis();

            builder = new WeeklyStatisticsEmailBuilder(from, to);
            subject = String.format("Nodyn Weekly Statistics from %s to %s",
                    df.format(new Date(from)),
                    df.format(new Date(to)));
        } else if (TYPE_WEEKLY.equals(type)) {
            builder = new MonthlyStatisticsEmailBuilder();
            DateFormat df = new SimpleDateFormat("mmm - yyyy");
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            subject = String.format("Nodyn Monthly Statistics for %s",
                    df.format(new Date(calendar.getTimeInMillis())));
        }

        if (builder != null) {
            final StatisticsEmailBuilder finalBuilder = builder;
            final String finalSubject = subject;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<Attachment> attachments = new ArrayList<>();
                        String html = finalBuilder.build(StatisticsEmailService.this, attachments);
                        List<EmailRecipient> recipients = new ArrayList<>();

                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(StatisticsEmailService.this);
                        //include the equipment manager(s) in all past due reminders
                        String equipmentManagers = prefs.getString(
                                context.getString(R.string.pref_key_equipment_managers_addresses),
                                context.getString(R.string.pref_default_equipment_managers_addresses));
                        String[] managers = equipmentManagers.split(",");
                        for (String address : managers) {
                            recipients.add(new EmailRecipient(address));
                        }


                        EmailSender sender = new EmailSender(context)
                                .setBody(html)
                                .setSubject(finalSubject)
                                .setRecipientList(recipients)
                                .withAttachments(attachments)
                                .setFailedListener(new EmailSender.EmailStatusListener() {
                                    @Override
                                    public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
                                        Log.e(TAG, "Statistics email failed with message: " + message);
                                        Answers.getInstance().logCustom(new CustomEvent(CustomEvents.STATISTICS_EMAIL_NOT_SENT));
                                    }
                                }, null)
                                .setSuccessListener(new EmailSender.EmailStatusListener() {
                                    @Override
                                    public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
                                        Log.d(TAG, "Statistics email succeeded with message: " + message);
                                        Answers.getInstance().logCustom(new CustomEvent(CustomEvents.STATISTICS_EMAIL_SENT));
                                    }
                                }, null)
                                .send();

                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        Log.e(TAG, "Caught error while sending statistics email: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
