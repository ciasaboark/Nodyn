package io.phobotic.nodyn.sync;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.phobotic.nodyn.database.model.User;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public class UserResponse {
    private int total;

    @SerializedName("rows")
    private List<User> users;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
