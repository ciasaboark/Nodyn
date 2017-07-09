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

import android.support.annotation.Nullable;

import java.util.List;

import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.Category;
import io.phobotic.nodyn.database.model.FullDataModel;
import io.phobotic.nodyn.database.model.Group;
import io.phobotic.nodyn.database.model.Model;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.sync.CheckinException;
import io.phobotic.nodyn.sync.CheckoutException;
import io.phobotic.nodyn.sync.SyncException;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public abstract class SyncAdapter {
    public abstract List<Asset> fetchAssets() throws SyncException;
    public abstract List<Model> fetchModels() throws SyncException;
    public abstract List<User> fetchUsers() throws SyncException;
    public abstract List<Group> fetchGroups() throws SyncException;
    public abstract List<Category> fetchCategories() throws SyncException;

    public abstract FullDataModel fetchFullModel() throws SyncException;
    public abstract void checkoutAssetTo(int assetID, int userID, @Nullable String checkoutDate,
                                @Nullable String expectedCheckin, @Nullable String notes)
            throws CheckoutException;
    public abstract void checkinAsset(int assetID, @Nullable String checkinDate,
                                      @Nullable String notes) throws CheckinException;
}
