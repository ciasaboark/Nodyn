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

package io.phobotic.nodyn_app.sync.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.phobotic.nodyn_app.database.model.Action;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.FullDataModel;
import io.phobotic.nodyn_app.database.model.MaintenanceRecord;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.sync.SyncErrorListener;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public interface SyncAdapter {
    FullDataModel fetchFullModel(Context context) throws SyncException;

    void checkoutAssetTo(Context context, int assetID, String assetTag, int userID, @Nullable Long checkout,
                         @Nullable Long expectedCheckin, @Nullable String notes)
            throws Exception;

    void checkinAsset(Context context, int assetID, String assetTag, @Nullable Long checkinDate,
                      @Nullable String notes) throws Exception;

    void syncActionItems(Context context, List<Action> unsyncedActions, SyncErrorListener listener) throws SyncException;

    void markActionItemsSynced(Context context, List<Action> actions);

    List<MaintenanceRecord> getMaintenanceRecords(Context context, Asset asset) throws SyncException,
            SyncNotSupportedException;

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

    List<Asset> getAssets(Context context, User user) throws SyncException,
            SyncNotSupportedException;

    @Nullable
    DialogFragment getConfigurationDialogFragment(Context context);
}
