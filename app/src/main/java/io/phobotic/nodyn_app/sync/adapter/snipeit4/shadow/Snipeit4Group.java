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

package io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow;

import com.google.gson.annotations.SerializedName;

import io.phobotic.nodyn_app.database.model.Group;
import io.phobotic.nodyn_app.helper.URLHelper;

/**
 * Created by Jonathan Nelson on 9/13/17.
 */

public class Snipeit4Group {
    private int id;
    private String name;

    @SerializedName("users_count")
    private int users;


    public Group toGroup() {
        Group group = new Group()
                .setId(id)
                .setName(URLHelper.decode(name))
                .setUserCount(users);

        return group;
    }
}
