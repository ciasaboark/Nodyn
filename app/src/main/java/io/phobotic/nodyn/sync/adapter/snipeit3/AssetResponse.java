package io.phobotic.nodyn.sync;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.phobotic.nodyn.database.model.Asset;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public class AssetResponse {
    private int total;

    @SerializedName("rows")
    private List<Asset> assets;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public void setAssets(List<Asset> assets) {
        this.assets = assets;
    }
}
