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

package io.phobotic.nodyn.sync.adapter.snipeit3.shadow;

import com.google.gson.annotations.SerializedName;

import io.phobotic.nodyn.database.Verifiable;
import io.phobotic.nodyn.sync.HtmlEncoded;

/**
 * Created by Jonathan Nelson on 9/12/17.
 */

public class ShadowUser {
    private int id;

    @Verifiable("Name")
    @HtmlEncoded
    private String name;

    @SerializedName("jobtitle")
    private String jobTitle;

    @Verifiable("Email address")
    @HtmlEncoded
    private String email;

    @Verifiable("Username")
    private String username;

    private String location;
    private String manager;

    @SerializedName("assets")
    private int numAssets;

    @Verifiable("Employee Number")
    @SerializedName("employee_num")
    private String employeeNum;

    @HtmlEncoded
    private String groups;

    @Verifiable("Notes")
    private String notes;

    @SerializedName("companyName")
    private String companyName;

    public int getId() {
        return id;
    }

    public ShadowUser setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ShadowUser setName(String name) {
        this.name = name;
        return this;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public ShadowUser setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public ShadowUser setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public ShadowUser setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public ShadowUser setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getManager() {
        return manager;
    }

    public ShadowUser setManager(String manager) {
        this.manager = manager;
        return this;
    }

    public int getNumAssets() {
        return numAssets;
    }

    public ShadowUser setNumAssets(int numAssets) {
        this.numAssets = numAssets;
        return this;
    }

    public String getEmployeeNum() {
        return employeeNum;
    }

    public ShadowUser setEmployeeNum(String employeeNum) {
        this.employeeNum = employeeNum;
        return this;
    }

    public String getGroups() {
        return groups;
    }

    public ShadowUser setGroups(String groups) {
        this.groups = groups;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public ShadowUser setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public String getCompanyName() {
        return companyName;
    }

    public ShadowUser setCompanyName(String companyName) {
        this.companyName = companyName;
        return this;
    }
}
