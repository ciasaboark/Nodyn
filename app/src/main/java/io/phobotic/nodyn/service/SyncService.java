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
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.model.Action;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.FullDataModel;
import io.phobotic.nodyn.email.ActionHtmlFormatter;
import io.phobotic.nodyn.email.EmailRecipient;
import io.phobotic.nodyn.email.EmailSender;
import io.phobotic.nodyn.email.PastDueAssetHtmlFormatter;
import io.phobotic.nodyn.reporting.CustomEvents;
import io.phobotic.nodyn.schedule.SyncScheduler;
import io.phobotic.nodyn.sync.SyncErrorListener;
import io.phobotic.nodyn.sync.SyncManager;
import io.phobotic.nodyn.sync.adapter.SyncAdapter;
import io.phobotic.nodyn.sync.adapter.SyncException;

/**
 * Created by Jonathan Nelson on 7/9/17.
 */

public class SyncService extends IntentService {
    public static final String BROADCAST_SYNC_START = "sync_start";
    public static final String BROADCAST_SYNC_UPDATE = "sync_update";
    public static final String BROADCAST_SYNC_FINISH = "sync_finish";
    public static final String BROADCAST_SYNC_FAIL = "sync_fail";
    public static final String BROADCAST_SYNC_PROGRESS = "sync_progress";
    public static final String BROADCAST_SYNC_MESSAGE = "sync_message";
    private static final String TAG = SyncService.class.getSimpleName();


    public SyncService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final SyncAdapter syncAdapter = SyncManager.getPrefferedSyncAdapter(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String chosenBackend = prefs.getString(getString(R.string.pref_key_sync_backend), null);

        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        Intent i = new Intent(BROADCAST_SYNC_START);
        broadcastManager.sendBroadcast(i);

        try {
            // TODO: 7/14/17 do we really need three steps here?
            //in order to sync we need to fetch up-to-date data, then try to push our changes,
            //+ then pull up to date data again to make sure we are
            pushLocalActions(syncAdapter);

            Database db = Database.getInstance(this);
            pullRemoteModel(syncAdapter, db);

            i = new Intent(BROADCAST_SYNC_FINISH);
            broadcastManager.sendBroadcast(i);

            Answers.getInstance().logCustom(new CustomEvent(CustomEvents.SYNC_SUCCESS));
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


            //delay searching for past due items until after the local history has been pushed and
            //+ the remote sync is complete
            checkForPastDueAssets();
        }
    }

    private void pushLocalActions(final SyncAdapter syncAdapter) throws SyncException {
        final List<FailedActions> failedActions = new ArrayList();
        syncAdapter.syncActionItems(this, new SyncErrorListener() {
            @Override
            public void onActionSyncError(Action action, Exception e, String message) {
                failedActions.add(new FailedActions(action, e, message));
            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean emailEnabled = preferences.getBoolean("email_enable", false);
        if (emailEnabled && failedActions.size() > 0) {
            Answers.getInstance().logCustom(new CustomEvent(CustomEvents.SYNC_ERROR_ACTION_FAILED));
            StringBuilder sb = new StringBuilder();
            sb.append(ActionHtmlFormatter.getHeader());

            ActionHtmlFormatter formatter = new ActionHtmlFormatter();
            for (FailedActions failedAction : failedActions) {
                sb.append(formatter.formatActionAsHtml(this, failedAction));
            }

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

    private void pullRemoteModel(SyncAdapter syncAdapter, Database db) throws SyncException {
        FullDataModel model = syncAdapter.fetchFullModel(this);
        db.updateModel(model);
    }

    private void checkForPastDueAssets() {
        Database db = Database.getInstance(this);
        List<Asset> allAssets = db.getAssets();
        List<Asset> pastDueAssets = new ArrayList<>();

        // TODO: 10/19/17 should this be restricted to the models that can be checked out, or send notifications for everything?
        long now = System.currentTimeMillis();
        for (Asset asset : allAssets) {
            if (asset.getAssignedToID() != -1) {
                long checkinTimestamp = asset.getExpectedCheckin();
                if (checkinTimestamp != -1) {
                    if (checkinTimestamp < now) {
                        pastDueAssets.add(asset);
                    }
                }
            }
        }

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
    }

    private void sendUpdates() {
        // TODO: 7/9/17
    }
}
