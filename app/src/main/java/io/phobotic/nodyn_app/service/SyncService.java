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
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.Action;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.FullDataModel;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.email.ActionHtmlFormatter;
import io.phobotic.nodyn_app.email.EmailRecipient;
import io.phobotic.nodyn_app.email.EmailSender;
import io.phobotic.nodyn_app.reporting.CustomEvents;
import io.phobotic.nodyn_app.schedule.SyncScheduler;
import io.phobotic.nodyn_app.sync.ActionSyncListener;
import io.phobotic.nodyn_app.sync.SyncManager;
import io.phobotic.nodyn_app.sync.adapter.SyncAdapter;
import io.phobotic.nodyn_app.sync.adapter.SyncException;

/**
 * Created by Jonathan Nelson on 7/9/17.
 */

public class SyncService extends IntentService {
    public static final String BROADCAST_SYNC_START = "sync_start";
    public static final String BROADCAST_SYNC_UPDATE = "sync_update";
    public static final String BROADCAST_SYNC_FINISH = "sync_finish";
    public static final String BROADCAST_SYNC_FAIL = "sync_fail";
    public static final String BROADCAST_SYNC_DEBUG = "sync_debug";
    public static final String BROADCAST_SYNC_PROGRESS_MAIN = "sync_progress_main";
    public static final String BROADCAST_SYNC_PROGRESS_SUB = "sync_progress_sub";
    public static final String BROADCAST_SYNC_PROGRESS_SUB_KEY = "sync_progress_sub";
    public static final String BROADCAST_SYNC_MESSAGE = "sync_message";
    private static final String TAG = SyncService.class.getSimpleName();


    public SyncService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final SyncAdapter syncAdapter = SyncManager.getPrefferedSyncAdapter(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        Intent i = new Intent(BROADCAST_SYNC_START);
        broadcastManager.sendBroadcast(i);

        try {
            //push out all local changes
            pushLocalActions(syncAdapter);

            //make sure our local copy matches the backend
            Database db = Database.getInstance(this);
            pullRemoteModel(syncAdapter, db);

            i = new Intent(BROADCAST_SYNC_FINISH);
            broadcastManager.sendBroadcast(i);

            //delete old action records
            db.pruneSyncedActions();

            Answers.getInstance().logCustom(new CustomEvent(CustomEvents.SYNC_SUCCESS));


            prefs.edit().putBoolean(getString(R.string.sync_key_first_sync_completed), true).commit();
        } catch (Exception e) {
            Crashlytics.logException(e);
            Answers.getInstance().logCustom(new CustomEvent(CustomEvents.SYNC_FAILED));
            i = new Intent(BROADCAST_SYNC_FAIL);
            i.putExtra(BROADCAST_SYNC_MESSAGE, e.getMessage());
            broadcastManager.sendBroadcast(i);
            Log.e(TAG, "Aborting sync process.  Unable to fetch full data model from remote host: "
                    + e.getMessage());
        } finally {
            //since the sync process finished (successfully or not) we can be sure that a new
            //+ sync alarm should be scheduled
            Log.d(TAG, "Scheduling new sync");
            SyncScheduler scheduler = new SyncScheduler(this);
            scheduler.forceScheduleSync();


            //start the audit results email sender service
            Intent ai = new Intent(this, AuditEmailService.class);
            startService(ai);
        }

//        Intent i2 = new Intent(this, StatisticsService.class);
//        startService(i2);
    }

    private void pushLocalActions(final SyncAdapter syncAdapter) throws SyncException {
        final List<FailedActions> failedActions = new ArrayList();
        Database db = Database.getInstance(this);
        List<Action> unsyncedActions = db.getUnsyncedActions();

        //go ahead and convert the list of unsynced Actions into something we can attach
        //+ to an email just in case the sync fails
        String syncRecords = convertActionsToString(unsyncedActions);

        syncAdapter.syncActionItems(this, unsyncedActions, new ActionSyncListener() {
            @Override
            public void onActionSyncSuccess(Action action) {

            }

            @Override
            public void onActionSyncError(Action action, Exception e, String message) {
                failedActions.add(new FailedActions(action, e, message));
            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean emailEnabled = preferences.getBoolean("email_enable", false);
        if (failedActions.size() > 0) {
            if (!emailEnabled) {
                //if the user has the email service disabled then we need to go ahead and mark any
                //+ failed actions as 'synced'.  If not these actions will continue to be resent
                //+ (and fail) during each sync
                List<Action> actionList = new ArrayList<>();
                for (FailedActions fa : failedActions) {
                    actionList.add(fa.getAction());
                }
                syncAdapter.markActionItemsSynced(SyncService.this, actionList);
            } else if (emailEnabled) {
                //otherwise we need to be careful about marking a failed action item 'synced'.  This
                //+ should only be done if we are sure that an email with the failed sync actions
                //+ was sent successfully.
                Answers.getInstance().logCustom(new CustomEvent(CustomEvents.SYNC_ERROR_ACTION_FAILED));
                StringBuilder sb = new StringBuilder();
                sb.append(ActionHtmlFormatter.getHeader(this));

                ActionHtmlFormatter formatter = new ActionHtmlFormatter();
                for (FailedActions failedAction : failedActions) {
                    sb.append(formatter.formatActionAsHtml(this, failedAction));
                }


                sb.append("<pre>\n\nSync records that were pushed during this update:\n\n");
                sb.append(syncRecords + "</pre>");

                sb.append(ActionHtmlFormatter.getFooter());

                List<EmailRecipient> recipients = new ArrayList<>();
                String addressesString = preferences.getString(
                        getString(R.string.pref_key_email_exceptions_addresses),
                        getString(R.string.pref_default_email_exceptions_addresses));
                String[] addresses = addressesString.split(",");
                for (String address : addresses) {
                    recipients.add(new EmailRecipient(address));
                }

                EmailSender sender = new EmailSender(this)
                        .setBody(sb.toString())
                        .setSubject("Sync Exceptions")
                        .setRecipientList(recipients)
                        .setFailedListener(new EmailSender.EmailStatusListener() {
                            @Override
                            public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
                                Log.e(TAG, "Sync exception send email failed with message: " + message);
                                Answers.getInstance().logCustom(new CustomEvent(CustomEvents.SYNC_ERROR_EMAIL_NOT_SENT));
                            }
                        }, failedActions)
                        .setSuccessListener(new EmailSender.EmailStatusListener() {
                            @Override
                            public void onEmailSendResult(@Nullable String message, @Nullable Object tag) {
                                Log.d(TAG, "Sync exception send email succeeded with message: " + message);
                                Answers.getInstance().logCustom(new CustomEvent(CustomEvents.SYNC_ERROR_EMAIL_SENT));
                                if (tag instanceof List) {
                                    List<Action> actions = new ArrayList<Action>();
                                    for (FailedActions failedAction : failedActions) {
                                        actions.add(failedAction.getAction());
                                    }
                                    syncAdapter.markActionItemsSynced(SyncService.this, actions);
                                }
                            }
                        }, failedActions)
                        .send();
            }

        }
    }

    private void pullRemoteModel(SyncAdapter syncAdapter, Database db) throws SyncException {
        FullDataModel model = syncAdapter.fetchFullModel(this);
        Intent i = new Intent(SyncService.BROADCAST_SYNC_UPDATE);
        i.putExtra(SyncService.BROADCAST_SYNC_MESSAGE, "Updating database");
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.sendBroadcast(i);

        db.updateModel(model);
    }

    private @NotNull
    String convertActionsToString(@NotNull List<Action> actions) {
        StringBuilder sb = new StringBuilder("");
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
        for (Action a : actions) {
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
            String assetTag = String.valueOf(a.getAssetID() + "<id>");
            if (a.getAssetID() != -1) {
                try {
                    Asset asset = db.findAssetByID(a.getAssetID());
                    assetTag = asset.getTag();
                } catch (AssetNotFoundException e) {
                    //just leave this field as the asset ID number
                }
            }

            String username = String.valueOf(a.getUserID() + "<id>");
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
