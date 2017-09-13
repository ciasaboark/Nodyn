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

package io.phobotic.nodyn.sync.adapter.dummy;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn.database.model.Action;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.AssetHistoryRecord;
import io.phobotic.nodyn.database.model.Category;
import io.phobotic.nodyn.database.model.FullDataModel;
import io.phobotic.nodyn.database.model.Group;
import io.phobotic.nodyn.database.model.MaintenanceRecord;
import io.phobotic.nodyn.database.model.Model;
import io.phobotic.nodyn.database.model.Status;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.database.model.UserHistoryRecord;
import io.phobotic.nodyn.sync.CheckinException;
import io.phobotic.nodyn.sync.CheckoutException;
import io.phobotic.nodyn.sync.SyncErrorListener;
import io.phobotic.nodyn.sync.adapter.SyncAdapter;
import io.phobotic.nodyn.sync.adapter.SyncException;
import io.phobotic.nodyn.sync.adapter.SyncNotSupportedException;

/**
 * Created by Jonathan Nelson on 7/27/17.
 */

public class DummyAdapter implements SyncAdapter {
    @Override
    public List<Asset> fetchAssets(Context context) throws SyncException {
        return new ArrayList<>();
    }

    @Override
    public List<Model> fetchModels(Context context) throws SyncException {
        return new ArrayList<>();
    }

    @Override
    public List<User> fetchUsers(Context context) throws SyncException {
        return new ArrayList<>();
    }

    @Override
    public List<Group> fetchGroups(Context context) throws SyncException {
        return new ArrayList<>();
    }

    @Override
    public List<Category> fetchCategories(Context context) throws SyncException {
        return new ArrayList<>();
    }

    @Override
    public List<Status> fetchStatuses(Context context) throws SyncException {
        return new ArrayList<>();
    }

    @Override
    public FullDataModel fetchFullModel(Context context) throws SyncException {
        return new FullDataModel()
                .setModels(fetchModels(context))
                .setUsers(fetchUsers(context))
                .setCategories(fetchCategories(context))
                .setAssets(fetchAssets(context))
                .setGroups(fetchGroups(context));
    }

    @Override
    public void checkoutAssetTo(Context context, int assetID, String assetTAg, int userID,
                                @Nullable Long checkout, @Nullable Long expectedCheckin,
                                @Nullable String notes)
            throws CheckoutException {
        throw new CheckoutException();
    }

    @Override
    public void checkinAsset(Context context, int assetID, String assetTag, @Nullable Long checkinDate,
                             @Nullable String notes) throws CheckinException {
        throw new CheckinException();
    }

    @Override
    public void syncActionItems(Context context, SyncErrorListener listener) throws SyncException {
        //nothing to do here
    }

    @Override
    public void markActionItemsSynced(Context context, List<Action> actions) {
        //nothing to do here
    }

    @Override
    public List<MaintenanceRecord> getMaintenanceRecords(Context context, Asset asset)
            throws SyncException,
            SyncNotSupportedException {
        throw new SyncNotSupportedException("No sync adapter selected",
                "Dummy adapter does not support pulling maintenance records");
    }

    @Override
    public List<AssetHistoryRecord> getHistory(Context context, Asset asset) throws SyncException,
            SyncNotSupportedException {
        throw new SyncNotSupportedException("No sync adapter selected",
                "Dummy adapter does not support pulling asset history records");
    }

    @Override
    public List<UserHistoryRecord> getHistory(Context context, User user) throws SyncException,
            SyncNotSupportedException {
        throw new SyncNotSupportedException("No sync adapter selected",
                "Dummy adapter does not support pulling user history records");
    }

    @Override
    public List<Asset> getAssets(Context context, User user) throws SyncException, SyncNotSupportedException {
        throw new SyncNotSupportedException("No sync adapter selected",
                "Dummy adapter does not support pulling user asset records");
    }

    @Nullable
    @Override
    public DialogFragment getConfigurationDialogFragment(Context context) {
        return null;
    }
}
