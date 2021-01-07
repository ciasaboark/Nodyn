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
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.Versioning;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.helper.FilterHelper;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.sync.Action;
import io.phobotic.nodyn_app.database.model.FullDataModel;
import io.phobotic.nodyn_app.database.sync.SyncedAction;
import io.phobotic.nodyn_app.database.sync.SyncAttempt;
import io.phobotic.nodyn_app.database.sync.SyncDatabase;
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
    public static final String BROADCAST_SYNC_PROGRESS_SUB_KEY = "sync_progress_sub_key";
    public static final String BROADCAST_SYNC_MESSAGE = "sync_message";
    public static final String BROADCAST_SYNC_SUB_MESSAGE = "sync_sub_message";
    public static final String SYNC_TYPE_KEY = "sync_type";
    public static final String SYNC_TYPE_FULL = "sync_type_full";
    public static final String SYNC_TYPE_QUICK = "sync_type_quick";
    private static final String TAG = SyncService.class.getSimpleName();


    public SyncService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // TODO: 2019-09-05 determine the type of sync that was requested
        final SyncAdapter syncAdapter = SyncManager.getPrefferedSyncAdapter(this);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);


        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        Intent i = new Intent(BROADCAST_SYNC_START);
        broadcastManager.sendBroadcast(i);

        //default to use the full sync
        String syncType = intent.getStringExtra(SYNC_TYPE_KEY);
        if (syncType == null) syncType = SYNC_TYPE_FULL;

        boolean forceReschedule = false;

        switch (syncType) {
            case SYNC_TYPE_FULL:
                forceReschedule = true;
                performFullSync(syncAdapter, broadcastManager);
                break;
            case SYNC_TYPE_QUICK:
                performQuickSync(syncAdapter, broadcastManager);
                break;
            default:
                String message = String.format("Unknown sync type: %s.  Will not sync.", syncType);
                Log.e(TAG, message);
                Crashlytics.log(message);
        }

        SyncScheduler scheduler = new SyncScheduler(this);
        if (forceReschedule) {
            scheduler.forceScheduleSync();
        } else {
            scheduler.scheduleSyncIfNeeded();
        }
    }

    /**
     * Perform a quick sync with the backend.  Action items are sent out
     */
    private void performQuickSync(SyncAdapter syncAdapter, LocalBroadcastManager broadcastManager) {
        performSync(syncAdapter, broadcastManager, false);
    }

    private void performFullSync(SyncAdapter syncAdapter, LocalBroadcastManager broadcastManager) {
        performSync(syncAdapter, broadcastManager, true);
    }

    private void performSync(SyncAdapter syncAdapter, LocalBroadcastManager broadcastManager, boolean isFullSync) {
        //generate a unique UUID to reference this sync attempt
        String uuid = UUID.randomUUID().toString();

        Log.i(TAG, String.format("Starting %s sync <%s>.", (isFullSync ? "full" : "quick"), uuid));
        List<Action> unsyncedActions = new ArrayList<>();
        //Failed actions are categorized by whether they are recoverable.  Recoverable actions will
        //+ be left unsynced and can be tried again later
        final List<SyncedAction> syncedActions = new ArrayList();
        final List<SyncedAction> recoverableActions = new ArrayList<>();
        long syncStart = System.currentTimeMillis();
        long syncEnd = -1;
        String exceptionClass = null;
        String exceptionMessage = null;
        boolean fullModelFetched = false;
        boolean allActionItemsSynced = false;

        try {
            Database db = Database.getInstance(this);


            //push out all local changes then prune any old records that have synced
            allActionItemsSynced = pushLocalActions(uuid, syncAdapter);
            db.pruneSyncedActions();

            if (isFullSync) {
                Log.d(TAG, "Asking sync adapter to fetch full data model");
                //make sure our local copy matches the backend
                pullRemoteModel(syncAdapter, db);

                fullModelFetched = true;
                Answers.getInstance().logCustom(new CustomEvent(CustomEvents.SYNC_SUCCESS));
            }

            Intent i = new Intent(BROADCAST_SYNC_FINISH);
            broadcastManager.sendBroadcast(i);
        } catch (Exception e) {
            Crashlytics.logException(e);
            Answers.getInstance().logCustom(new CustomEvent(CustomEvents.SYNC_FAILED));
            Intent i = new Intent(BROADCAST_SYNC_FAIL);
            i.putExtra(BROADCAST_SYNC_MESSAGE, e.getMessage());
            broadcastManager.sendBroadcast(i);
            Log.e(TAG, "Aborting sync process.  Unable to fetch full data model from remote host: "
                    + e.getMessage());
            exceptionClass = e.getClass().getSimpleName();
            exceptionMessage = e.getMessage();

        }
        //record this sync attempt
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean emailEnabled = preferences.getBoolean("email_enable", false);
        syncEnd = System.currentTimeMillis();
        SyncAttempt syncAttempt = new SyncAttempt(uuid,
                syncStart,
                syncEnd,
                (isFullSync ? SyncAttempt.SYNC_TYPE_FULL : SyncAttempt.SYNC_TYPE_QUICK),
                fullModelFetched,
                allActionItemsSynced,
                exceptionClass,
                exceptionMessage,
                syncAdapter.getAdapterName(),
                Versioning.getReleaseName(),
                Versioning.getReleaseNumber(),
                (emailEnabled ? 1 : 0),
                false);
        SyncDatabase syncDatabase = SyncDatabase.getInstance(this);
        syncDatabase.syncAttemptDao().insert(syncAttempt);

        //Start the sync email service so any required notifications can be sent out
        Intent ses = new Intent(this, SyncFailureNotificationService.class);
        startService(ses);

        //hold off on sending audit results until a full sync
        if (isFullSync) {
            //start the audit results email sender service.  Audits will be sent immediatly
            //+ after completion.  If the transfer failed this will help ensure they get sent out
            //+ in a timely manner
            Intent ai = new Intent(this, AuditEmailService.class);
            startService(ai);
        }

//        Intent i2 = new Intent(this, StatisticsService.class);
//        startService(i2);
    }

    /**
     * Sync all outstanding Action items that have not synced yet
     * @param syncUUID
     * @param syncAdapter
     * @return true if all action items could be synced, false otherwise
     * @throws SyncException
     */
    private boolean pushLocalActions(final String syncUUID, final SyncAdapter syncAdapter) throws SyncException {
        final boolean[] allActionItemsSynced = {true};
        final Database actionDatabase = Database.getInstance(this);
        List<Action> unsyncedActions = actionDatabase.getUnsyncedActions();
        Log.d(TAG, String.format("Found %d unsycned actions", unsyncedActions.size()));
        final SyncDatabase syncDatabase = SyncDatabase.getInstance(this);

        syncAdapter.syncActionItems(this, unsyncedActions, new ActionSyncListener() {
            @Override
            public void onActionSyncSuccess(Action action) {
                Log.d(TAG, String.format("Action %s synced", action.toString()));

                //mark this action as synced in the action database so we don't try to sync
                //+ it again later
                action.setSynced(true);
                actionDatabase.insertAction(action);

                //add a record to the sync database
                SyncedAction syncedAction = SyncedAction.fromAction(syncUUID, action);
                syncedAction.setSyncSuccess(true);
                syncedAction.setWillRetrySync(false);
                syncDatabase.syncedActionDao().insert(syncedAction);

            }

            @Override
            public void onActionSyncFatalError(Action action, Exception e, String message) {
                allActionItemsSynced[0] = false;
                Log.e(TAG, String.format("Action %s could not be synced.  A fatal error has " +
                        "occurred.  This action will be marked as synced and will be inclued in the " +
                        "sync exception email (if configured)", action.toString()));

                //there is no point in tring to send this action later.  Go ahead and mark it as synced
                action.setSynced(true);
                actionDatabase.insertAction(action);

                //add a record to the sync database
                SyncedAction syncedAction = SyncedAction.fromAction(syncUUID, action);
                syncedAction.setSyncSuccess(false);
                syncedAction.setWillRetrySync(false);
                syncedAction.setExceptionType(e.getClass().getSimpleName());
                syncedAction.setExceptionMessage(e.getMessage());
                syncDatabase.syncedActionDao().insert(syncedAction);
            }

            @Override
            public void onActionSyncRecoverableError(Action action, @Nullable Exception e, @Nullable String message) {
                Log.e(TAG, String.format("Action %s could not be synced.  A recoverable error " +
                        "occurred.  This action will be kept unsynced and will be tried again " +
                        "later. ", action.toString()));
                allActionItemsSynced[0] = false;

                //leave the action unchanged in the action table, but add an entry into the sync table
                SyncedAction syncedAction = SyncedAction.fromAction(syncUUID, action);
                syncedAction.setSyncSuccess(false);
                syncedAction.setWillRetrySync(true);
                syncedAction.setExceptionType(e.getClass().getSimpleName());
                syncedAction.setExceptionMessage(message);
                syncDatabase.syncedActionDao().insert(syncedAction);
            }
        });

        return allActionItemsSynced[0];
    }



    private void pullRemoteModel(SyncAdapter syncAdapter, Database db) throws SyncException {
        FullDataModel model = syncAdapter.fetchFullModel(this);

        //The sync adapter may have already filtered the asset list to the selected models
        //+ and companies, but this behaviour is not guarenteed
        FilterHelper.filterModels(this, model.getAssets());
        FilterHelper.filterCompanies(this, model.getAssets());


        Intent i = new Intent(SyncService.BROADCAST_SYNC_UPDATE);
        i.putExtra(SyncService.BROADCAST_SYNC_MESSAGE, "Updating database");
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        broadcastManager.sendBroadcast(i);

        db.updateModel(model);
    }



}
