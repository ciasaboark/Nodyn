package io.phobotic.nodyn.sync;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.phobotic.nodyn.database.model.Model;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public class ModelResponse {
    private int total;

    @SerializedName("rows")
    private List<Model> models;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Model> getModels() {
        return models;
    }

    public void setModels(List<Model> models) {
        this.models = models;
    }
}
