package io.phobotic.nodyn.database.model;

import com.google.gson.annotations.SerializedName;

import io.phobotic.nodyn.sync.HtmlEncoded;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public class User {
    private int id;

    @HtmlEncoded
    private String name;

    @SerializedName("jobtitle")
    private String jobTitle;

    @HtmlEncoded
    private String email;

    private String username;
    private String location;
    private String manager;

    private int numAssets;

    @SerializedName("employee_num")
    private String employeeNum;

    @HtmlEncoded
    private String groups;
    private String notes;

    @SerializedName("companyName")
    private String companyName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public int getNumAssets() {
        return numAssets;
    }

    public void setNumAssets(int numAssets) {
        this.numAssets = numAssets;
    }

    public String getEmployeeNum() {
        return employeeNum;
    }

    public void setEmployeeNum(String employeeNum) {
        this.employeeNum = employeeNum;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
