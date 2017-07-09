package io.phobotic.nodyn.sync;

import android.support.annotation.Nullable;

import java.util.List;

import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.FullDataModel;
import io.phobotic.nodyn.database.model.Model;
import io.phobotic.nodyn.database.model.User;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public abstract class SyncAdapter {
    public abstract List<Asset> fetchAssets() throws SyncException;
    public abstract List<Model> fetchModels() throws SyncException;
    public abstract List<User> fetchUsers() throws SyncException;

    public abstract FullDataModel fetchFullModel() throws SyncException;
    public abstract void checkoutAssetTo(int assetID, int userID, @Nullable String checkoutDate,
                                @Nullable String expectedCheckin, @Nullable String notes)
            throws CheckoutException;
    public abstract void checkinAsset(int assetID, @Nullable String checkinDate,
                                      @Nullable String notes) throws CheckinException;
}
