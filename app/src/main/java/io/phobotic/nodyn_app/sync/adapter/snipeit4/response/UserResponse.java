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

package io.phobotic.nodyn_app.sync.adapter.snipeit4.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow.Snipeit4User;


/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public class UserResponse {
    private int total;

    @SerializedName("rows")
    private List<Snipeit4User> users;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<Snipeit4User> getUsers() {
        return users;
    }

    public void setUsers(List<Snipeit4User> users) {
        this.users = users;
    }
}
