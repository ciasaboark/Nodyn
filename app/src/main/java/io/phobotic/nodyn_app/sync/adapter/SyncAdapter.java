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

package io.phobotic.nodyn_app.sync.adapter;

import android.content.Context;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import io.phobotic.nodyn_app.database.audit.model.Audit;
import io.phobotic.nodyn_app.database.audit.model.AuditHeader;
import io.phobotic.nodyn_app.database.sync.Action;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.FullDataModel;
import io.phobotic.nodyn_app.database.model.MaintenanceRecord;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.sync.ActionSyncListener;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public interface SyncAdapter {
    String getAdapterName();

    FullDataModel fetchFullModel(Context context) throws SyncException;

    void checkoutAssetTo(Context context, int assetID, String assetTag, int userID, @Nullable Long checkout,
                         @Nullable Long expectedCheckin, @Nullable String notes)
            throws Exception;

    void checkinAsset(Context context, int assetID, String assetTag, @Nullable Long checkinDate,
                      @Nullable String notes) throws Exception;

    void syncActionItems(Context context, List<Action> unsyncedActions, ActionSyncListener listener) throws SyncException;

    List<MaintenanceRecord> getMaintenanceRecords(Context context, Asset asset) throws SyncException,
            SyncNotSupportedException;

    /**
     * Fetches up-to-date information about the specified asset.
     * @param context
     * @param asset
     * @return
     * @throws SyncNotSupportedException if the backend does not support fetching information about
     * individual assets
     * @throws SyncException if any exception is encountered while fetching asset information
     */
    @NotNull Asset getAsset(@NotNull Context context, @NotNull Asset asset) throws SyncNotSupportedException, SyncException;

    /**
     * Fetch all activity records for the specified Asset.  Returned records should not be limited
     * to a specific time period.  All available records for the Asset should be returned, up to
     * the maximum allowed per page by the backend.  If the backend does not support paging then
     * all records should be returned.
     *
     * @param context
     * @param asset
     * @return
     * @throws SyncException
     * @throws SyncNotSupportedException
     */
    List<Action> getAssetActivity(Context context, @NotNull Asset asset, int page) throws SyncException,
            SyncNotSupportedException;

    /**
     * Fetch all activity records for the specified User.  Returned records should not be limited
     * to a specific time period.  All available records for the User should be returned, up to
     * the maximum allowed per page by the backend.  If the backend does not support paging then
     * all records should be returned.
     *
     * @param context
     * @param user
     * @return
     * @throws SyncException             if the SyncAdapter supports fetching activity records, but an error
     *                                   was encountered
     * @throws SyncNotSupportedException if the SyncAdapter does not support fetching activity
     *                                   records
     */
    List<Action> getUserActivity(@NotNull Context context, @NotNull User user, int page) throws SyncException,
            SyncNotSupportedException;

    /**
     * Fetch all activity records from the remote host.  Records should be fetched up to the maximum
     * number of days in the past.  If maxDays is omitted then all records up to the maximum allowed
     * by the backend should be returned
     *
     * @param context
     * @param page    the page offset to begin fetching records at
     * @return
     * @throws SyncException             if the SyncAdapter supports fetching activity records, but an error
     *                                   was encountered
     * @throws SyncNotSupportedException if the backend system has no ability to fetch actions
     */
    List<Action> getActivity(@NotNull Context context, int page) throws SyncException, SyncNotSupportedException;


    /**
     * Fetch all activity records from now back to the UTC epoch cutoff
     * @param context
     * @param cutoff
     * @return
     */
    List<Action> getActivity(@NotNull Context context, long cutoff) throws SyncException, SyncNotSupportedException;

    /**
     * Fetch all activity records for the previous 30 days.
     *
     * @param context
     * @return
     * @throws SyncException             if the SyncAdapter supports fetching action records but an
     *                                   unrecoverable Exception occurred while fetching action records
     * @throws SyncNotSupportedException if the SyncAdapter does not support pulling action records
     */
    List<Action> getThirtyDayActivity(@NotNull Context context) throws SyncException, SyncNotSupportedException;

    List<Asset> getAssets(Context context, User user) throws SyncException,
            SyncNotSupportedException;

    @Nullable
    DialogFragment getConfigurationDialogFragment(Context context);

    /**
     * Push the results of an audit to the backend service.  This function is non critical, so
     * adapters that do not support recording audits should only implement a stub method
     * @param context
     * @param audit
     */
    void recordAudit(@NotNull Context context, @NotNull Audit audit);
}
