/*
 * Copyright (c) 2020 Jonathan Nelson <ciasaboark@gmail.com>
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
import android.util.Log;


import com.google.firebase.analytics.FirebaseAnalytics;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.database.sync.Action;
import io.phobotic.nodyn_app.database.sync.SyncedAction;
import io.phobotic.nodyn_app.database.sync.SyncAttempt;
import io.phobotic.nodyn_app.database.sync.SyncDatabase;
import io.phobotic.nodyn_app.email.ActionHtmlFormatter;
import io.phobotic.nodyn_app.email.EmailRecipient;
import io.phobotic.nodyn_app.email.EmailSender;
import io.phobotic.nodyn_app.reporting.CustomEvents;
import io.phobotic.nodyn_app.schedule.SyncScheduler;
import io.phobotic.nodyn_app.sync.adapter.SyncAdapter;

/**
 * Created by Jonathan Nelson on 2020-02-13.
 */
public class SyncFailureNotificationService extends IntentService {
    private static final String TAG = SyncFailureNotificationService.class.getSimpleName();

    public SyncFailureNotificationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        //Check the sync attempt database for any sync attemps that failed and
        //+ that need to have an email notice sent out.
        SyncDatabase db = SyncDatabase.getInstance(this);

        List<SyncAttempt> failedSyncAttempts = db.syncAttemptDao().findUnsetFailures();
        for (SyncAttempt syncAttempt: failedSyncAttempts) {
            sendSyncErrorEmail(db, syncAttempt);
        }
    }

    private void sendSyncErrorEmail(final SyncDatabase db, @NotNull final SyncAttempt syncAttempt) {
        String uuid = syncAttempt.getUuid();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean emailEnabled = preferences.getBoolean("email_enable", false);

        if (!emailEnabled) {
            Log.d(TAG, "Email reports have not been enabled on this device.  Sync failure email will not be sent");
            //the email service has been disabled, but was requested when this sync attempt was recorded
            //+  Mark the record as 'notice sent' and ignore
            syncAttempt.setNoticeSent(true);
            db.syncAttemptDao().insert(syncAttempt);
        } else {
            Log.d(TAG, String.format("Email reports have been enabled on this device.  " +
                    "Attempting to send sync exception email for <%s>", uuid));

            String body = getBody(db, syncAttempt);

            List<EmailRecipient> recipients = new ArrayList<>();
            String addressesString = preferences.getString(
                    getString(R.string.pref_key_equipment_managers_addresses),
                    getString(R.string.pref_default_equipment_managers_addresses));
            String[] addresses = addressesString.split(",");
            for (String address : addresses) {
                recipients.add(new EmailRecipient(address));
            }

            EmailSender sender = new EmailSender(this)
                    .setBody(body)
                    .setSubject("Sync Exceptions")
                    .setRecipientList(recipients)
                    .setFailedListener(new EmailSender.EmailStatusListener() {
                        @Override
                        public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
                            //we were unable to send the sync error email.  Leave the sync attempt record unmodified
                            //+ so we can attempt to send an email later
                            Log.e(TAG, "Sync exception send email failed with message: " + message);
                            FirebaseAnalytics.getInstance(getApplicationContext()).logEvent(CustomEvents.SYNC_ERROR_EMAIL_NOT_SENT, null);
                        }
                    }, syncAttempt)
                    .setSuccessListener(new EmailSender.EmailStatusListener() {
                        @Override
                        public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
                            Log.d(TAG, "Sync exception send email succeeded with message: " + message);
                            FirebaseAnalytics.getInstance(getApplicationContext()).logEvent(CustomEvents.SYNC_ERROR_EMAIL_SENT, null);

                            //update the sync attempt database so we do not try sending this error email again
                            syncAttempt.setNoticeSent(true);
                            db.syncAttemptDao().insert(syncAttempt);
                        }
                    }, syncAttempt)
                    .send();
        }

    }

    private String getBody(SyncDatabase db, SyncAttempt attempt) {
        List<SyncedAction> syncedActions = db.syncedActionDao().getActions(attempt.getUuid());
        List<SyncedAction> failedActions = new ArrayList<>();
        List<SyncedAction> retryActions = new ArrayList<>();

        for (SyncedAction sa: syncedActions) {
            if (!sa.isSyncSuccess()) {
                if (sa.isWillRetrySync()) {
                    failedActions.add(sa);
                } else {
                    retryActions.add(sa);
                }
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append(ActionHtmlFormatter.getHeader(this));

        DateFormat df = SimpleDateFormat.getDateTimeInstance();
        Date start = new Date(attempt.getStartTime());
        Date end = new Date(attempt.getEndTime());

        sb.append(String.format("<p>This sync attempt started at %s and completed at %s</p>",
                df.format(start), df.format(end)));
        sb.append(String.format("<p>This device is configured to use the sync adapter: %s</p>", attempt.getBackend()));

        Date d = SyncScheduler.getNextWakeTimestamp(this);
        sb.append(String.format("<p>The device will attempt to perform a full sync again at %s.</p>", df.format(d)));

        sb.append(String.format("<p>Exception Type: <pre>%s</pre></p>", attempt.getExceptionClass()));
        sb.append(String.format("<p>Exception Message: <pre>%s</pre></p>", attempt.getExceptionMessage()));

        if (attempt.getSyncType() == SyncAttempt.SYNC_TYPE_FULL && !attempt.isFullModelFetched()) {
            sb.append("<p>The device was unable to fetch a full data model to update the local database.</p>");

        }

        //if we had any actions that could not be synced then include some detailed information on each
        ActionHtmlFormatter formatter = new ActionHtmlFormatter();
        if (!failedActions.isEmpty()) {
            sb.append("<p>Actions that failed due to an unrecoverable error:</p>");

            for (SyncedAction syncedAction : failedActions) {
                sb.append(formatter.formatActionAsHtml(this, syncedAction));
            }

            sb.append("<p></p>");
        }

        if (!retryActions.isEmpty()) {
            sb.append("<p>Actions that failed due to a recoverable error.  These actions " +
                    "will be synced again during the next update:</p>");

            for (SyncedAction syncedAction : retryActions) {
                sb.append(formatter.formatActionAsHtml(this, syncedAction));
            }

            sb.append("<p></p>");
        }

        //if we tried to push out any actions records in this update then include those in the email
        if (syncedActions.isEmpty()) {
            sb.append("<p>No actions were pushed out during this sync.</p>");
        } else {
            String syncRecordsString = convertActionsToString(syncedActions);
            sb.append("<p>Action records that were pushed during this update:</p>");
            sb.append("<pre>\n\n" + syncRecordsString + "</pre>");
        }

        return sb.toString();
    }

    private @NotNull
    String convertActionsToString(@NotNull List<SyncedAction> actions) {
        StringBuilder sb = new StringBuilder();
        sb.append("<small><table><tr>");
        sb.append("<th>Action ID</th>" +
                "<th>Type</th>" +
                "<th>Asset</th>" +
                "<th>User</th>" +
                "<th>Authorization</th>" +
                "<th>Action Timestamp</th>" +
                "<th>Expected Checkin</th>" +
                "<th>Notes</th>" +
                "</tr>");

        String row = "<tr>" +
                "<td>%d</td>" +
                "<td>%s</td>" +
                "<td>%s</td>" +
                "<td>%s</td>" +
                "<td>%s</td>" +
                "<td>%s</td>" +
                "<td>%s</td>" +
                "<td>%s</td>" +
                "</tr>";
        for (SyncedAction a : actions) {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS z");

            String timestamp = String.valueOf(a.getTimestamp());
            if (a.getTimestamp() > 0) {
                Date d = new Date(a.getTimestamp());
                timestamp = df.format(d);
            }

            String expectedCheckin = String.valueOf(a.getExpectedCheckin());
            if (a.getExpectedCheckin() >= 0) {
                Date d = new Date(a.getExpectedCheckin());
                expectedCheckin = df.format(d);
            }

            String direction = Action.Direction.UNKNOWN.toString();
            if (a.getDirection() != null) {
                direction = a.getDirection().toString();
            }

            Database db = Database.getInstance(this);

            //try to use the asset tag instead of the ID number if possible
            String assetTag = a.getAssetID() + "<id>";
            if (a.getAssetID() != -1) {
                try {
                    Asset asset = db.findAssetByID(a.getAssetID());
                    assetTag = asset.getTag();
                } catch (AssetNotFoundException e) {
                    //just leave this field as the asset ID number
                }
            }

            String username = a.getUserID() + "<id>";
            if (a.getUserID() != -1) {
                try {
                    User user = db.findUserByID(a.getUserID());
                    username = user.getName();
                } catch (UserNotFoundException e) {
                    //just leave this field as the user ID number
                }
            }

            String line = String.format(row, a.getId(), direction,
                    assetTag, username, a.getAuthorization(), timestamp, expectedCheckin,
                    a.getNotes());

            sb.append(line + "\n");
        }

        sb.append("</table></small>");

        return sb.toString();
    }
}
