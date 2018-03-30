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

import java.io.Serializable;

import io.phobotic.nodyn_app.database.Verifiable;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public class User implements Serializable {
    private int id;

    @Verifiable("Name")
    private String name;
    @Verifiable("Email address")
    private String email;

    @Verifiable("Username")
    private String username;

    @Verifiable("Employee Number")
    private String employeeNum;

    @Verifiable("Notes")
    private String notes;

    private String jobTitle;
    private int locationID;
    private int managerID;

    private int[] groupsIDs;
    private int companyID;
    private String avatarURL;

    public int getId() {
        return id;
    }

    public User setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getEmployeeNum() {
        return employeeNum;
    }

    public User setEmployeeNum(String employeeNum) {
        this.employeeNum = employeeNum;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public User setNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public User setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
        return this;
    }

    public int getLocationID() {
        return locationID;
    }

    public User setLocationID(int locationID) {
        this.locationID = locationID;
        return this;
    }

    public int getManagerID() {
        return managerID;
    }

    public User setManagerID(int managerID) {
        this.managerID = managerID;
        return this;
    }

    public int[] getGroupsIDs() {
        return groupsIDs;
    }

    public User setGroupsIDs(int[] groupsIDs) {
        this.groupsIDs = groupsIDs;
        return this;
    }

    public int getCompanyID() {
        return companyID;
    }

    public User setCompanyID(int companyID) {
        this.companyID = companyID;
        return this;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public User setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
        return this;
    }

    @Override
    public String toString() {
        return name;
    }

    public class Columns {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String JOB_TITLE = "job";
        public static final String EMAIL = "email";
        public static final String USERNAME = "username";
        public static final String LOCATION_ID = "location";
        public static final String MANAGER_ID = "manager";
        public static final String NUM_ASSETS = "numassets";
        public static final String EMPLOYEE_NUM = "employeeNum";
        public static final String GROUP_IDS = "groups";
        public static final String NOTES = "notes";
        public static final String COMPANY_ID = "company";
        public static final String AVATAR_URL = "avatar_url";
    }
}
