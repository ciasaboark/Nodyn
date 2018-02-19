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

import android.util.Log;

import java.util.List;
import java.util.Map;

import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.helper.URLHelper;

/**
 * Created by Jonathan Nelson on 9/12/17.
 */

public class Snipeit4User {
    private static final String TAG = Snipeit4User.class.getSimpleName();
    private int id;
    private String name;
    private String jobTitle;
    private String email;
    private String username;
    private Snippet location;
    private Snippet manager;
    private int numAssets;
    private String employeeNum;
    private Map<String, Object> groups;
    private String notes;
    private Snippet company;

    public User toUser() {
        User user = new User()
                .setId(id)
                .setName(URLHelper.decode(name))
                .setJobTitle(URLHelper.decode(jobTitle))
                .setEmail(URLHelper.decode(email))
                .setUsername(URLHelper.decode(username))
                .setLocationID(location == null ? -1 : location.getId())
                .setManagerID(manager == null ? -1 : manager.getId())
                .setNumAssets(numAssets)
                .setEmployeeNum(employeeNum)
                .setNotes(URLHelper.decode(notes))
                .setCompanyID(company == null ? -1 : company.getId());

        if (groups != null) {
            try {
                List<Map> l = (List) groups.get("rows");
                int[] groupIDs = new int[l.size()];

                for (int i = 0; i < l.size(); i++) {
                    Map m = l.get(i);
                    groupIDs[i] = (int) Double.parseDouble(String.valueOf(m.get("id")));
                }

                user.setGroupsIDs(groupIDs);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Unable to parse group ids: " + e.getMessage());
            }
        }

        return user;
    }


    public Map<String, Object> getGroups() {
        return groups;
    }
}
