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

package io.phobotic.nodyn;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.model.Group;
import io.phobotic.nodyn.database.model.Status;

/**
 * Created by Jonathan Nelson on 10/24/17.
 */

public class SettingsHelper {
    public static boolean isBackendConfigured(@NotNull Context context) {
        boolean isBackendConfigured = true;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String backend = prefs.getString(context.getString(R.string.pref_key_sync_backend), null);
        String defaultBackend = context.getString(R.string.pref_default_sync_backend);
        if (backend == null || backend.equals(defaultBackend)) {
            isBackendConfigured = false;
        }

        return isBackendConfigured;
    }

    public static boolean isGroupsSynced(@NotNull Context context) {
        boolean isGroupsSynced = true;

        Database db = Database.getInstance(context);
        List<Group> groupList = db.getGroups();
        if (groupList == null || groupList.isEmpty()) {
            isGroupsSynced = false;
        }

        return isGroupsSynced;
    }

    public static boolean isStatusSynced(@NotNull Context context) {
        boolean isStatusSynced = true;

        Database db = Database.getInstance(context);
        List<Status> statuses = db.getStatuses();
        if (statuses == null || statuses.isEmpty()) {
            isStatusSynced = false;
        }

        return isStatusSynced;
    }

    public static boolean isModelsSynced(@NotNull Context context) {
//        boolean isModelsSynced = true;
//
//        Database db = Database.getInstance(context);
//        List<Model> modelList = db.getModels();
//        if (modelList == null || modelList.isEmpty()) {
//            isModelsSynced = false;
//        }
//
//        return isModelsSynced;

        return false;
    }
}
