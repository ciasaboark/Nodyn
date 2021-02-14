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

package io.phobotic.nodyn_app.database.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public class FullDataModel {
    private List<Asset> assets;
    private List<Model> models;
    private List<User> users;
    private List<Group> groups;
    private List<Category> categories;
    private List<Status> statuses;
    private List<Manufacturer> manufacturers;
    private List<Company> companies;

    public List<Company> getCompanies() {
        return companies;
    }

    public FullDataModel setCompanies(List<Company> companies) {
        if (companies == null) companies = new ArrayList<>();
        this.companies = companies;
        return this;
    }

    public List<Status> getStatuses() {
        return statuses;
    }

    public FullDataModel setStatuses(List<Status> statuses) {
        if (statuses == null) statuses = new ArrayList<>();
        this.statuses = statuses;
        return this;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public FullDataModel setCategories(List<Category> categories) {
        if (categories == null) categories = new ArrayList<>();
        this.categories = categories;
        return this;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public FullDataModel setGroups(List<Group> groups) {
        if (groups == null) groups = new ArrayList<>();
        this.groups = groups;
        return this;
    }

    public List<Asset> getAssets() {
        return assets;
    }

    public FullDataModel setAssets(List<Asset> assets) {
        if (assets == null) assets = new ArrayList<>();
        this.assets = assets;
        return this;
    }

    public List<Model> getModels() {
        return models;
    }

    public FullDataModel setModels(List<Model> models) {
        if (models == null) models = new ArrayList<>();
        this.models = models;
        return this;
    }

    public List<User> getUsers() {
        return users;
    }

    public FullDataModel setUsers(List<User> users) {
        if (users == null) users = new ArrayList<>();
        this.users = users;
        return this;
    }

    public List<Manufacturer> getManufacturers() {
        return manufacturers;
    }

    public FullDataModel setManufacturers(List<Manufacturer> manufacturers) {
        if (manufacturers == null) manufacturers = new ArrayList<>();
        this.manufacturers = manufacturers;
        return this;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
