package io.phobotic.nodyn.database.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jonathan Nelson on 7/8/17.
 */

public class Group {
    private int id;
    private String name;
    private int users;

    @SerializedName("created_at")
    private String createdAt;

    public int getId() {
        return id;
    }

    public Group setId(int id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public Group setName(String name) {
        this.name = name;
        return this;
    }

    public int getUsers() {
        return users;
    }

    public Group setUsers(int users) {
        this.users = users;
        return this;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public Group setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
