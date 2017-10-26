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

package io.phobotic.nodyn.sync.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import java.util.List;

import io.phobotic.nodyn.database.model.Action;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.FullDataModel;
import io.phobotic.nodyn.database.model.MaintenanceRecord;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.sync.SyncErrorListener;

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

    void syncActionItems(Context context, SyncErrorListener listener) throws SyncException;

    void markActionItemsSynced(Context context, List<Action> actions);

    List<MaintenanceRecord> getMaintenanceRecords(Context context, Asset asset) throws SyncException,
            SyncNotSupportedException;

    List<Action> getAssetActivity(Context context, Asset asset) throws SyncException,
            SyncNotSupportedException;

    List<Action> getUserActivity(Context context, User user) throws SyncException,
            SyncNotSupportedException;

    List<Action> getActivity(Context context) throws SyncException, SyncNotSupportedException;

    List<Asset> getAssets(Context context, User user) throws SyncException,
            SyncNotSupportedException;

    @Nullable
    DialogFragment getConfigurationDialogFragment(Context context);
}
