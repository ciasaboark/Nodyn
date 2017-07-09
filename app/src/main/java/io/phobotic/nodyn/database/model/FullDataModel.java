package io.phobotic.nodyn.database.model;

import java.util.List;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public class FullDataModel {
    private List<Asset> assets;
    private List<Model> models;
    private List<User> users;
    private List<Group> groups;

    public List<Group> getGroups() {
        return groups;
    }

    public FullDataModel setGroups(List<Group> groups) {
        this.groups = groups;
        return this;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public FullDataModel setAssets(List<Asset> assets) {
        this.assets = assets;
        return this;
    }

    public List<Model> getModels() {
        return models;
    }

    public FullDataModel setModels(List<Model> models) {
        this.models = models;
        return this;
    }

    public List<User> getUsers() {
        return users;
    }

    public FullDataModel setUsers(List<User> users) {
        this.users = users;
        return this;
    }
}
