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

package io.phobotic.nodyn_app.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 8/31/17.
 */

public class UserHelper {
    public static
    @NotNull
    User getUserByInputString(@NotNull Context context, String inputString)
            throws UserNotFoundException {
        Database db = Database.getInstance(context);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String validationField = prefs.getString(context.getString(
                R.string.pref_key_user_scan_field), null);
        User user = null;

        if (validationField == null || validationField.equals("")) {
            user = db.findUserByUsername(inputString);
        } else {
            List<User> allUsers = db.getUsers();

            for (User u : allUsers) {
                boolean userMatches = FieldValidator.isFieldMatch(u, validationField,
                        inputString);
                if (userMatches) {
                    user = u;
                    break;
                }
            }
        }

        if (user == null) {
            throw new UserNotFoundException();
        }

        return user;
    }
}
