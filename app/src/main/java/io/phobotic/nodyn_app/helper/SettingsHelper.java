/*
 * Copyright (c) 2019 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.activity.SettingsActivity;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Group;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.view.KioskPasswordView;

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

    public static void loadKioskSettings(final Context context, @Nullable final Bundle bundle) {
        final boolean isSettingsLocked = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                context.getString(R.string.pref_key_general_kiosk_lock_settings), Boolean.parseBoolean(
                        context.getString(R.string.pref_default_general_kiosk_lock_settings)));

        String storedPassword = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_key_general_kiosk_password), null);

        //just in case someone managed to set an empty password
        if (isSettingsLocked && storedPassword != null && storedPassword.length() > 0) {
            final KioskPasswordView kioskView = new KioskPasswordView(context);

            final AlertDialog d = new MaterialAlertDialogBuilder(context, R.style.Widgets_Dialog)
                    .setTitle(context.getString(R.string.pref_settings_password_title))
                    .setView(kioskView)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String passwordInput = kioskView.getPassword();
                            if (passwordInput == null) passwordInput = "";

                            String storedPassword = PreferenceManager.getDefaultSharedPreferences(context)
                                    .getString(context.getString(R.string.pref_key_general_kiosk_password), null);

                            if (passwordInput.equals(storedPassword)) {
                                loadSettingsActivity(context, bundle);
                            } else {
                                showKioskPasswordMismatchDialog(context);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO: 10/1/17 disable kiosk mode
                        }
                    })
                    .create();

            d.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    final Button positveButton = d.getButton(DialogInterface.BUTTON_POSITIVE);
                    kioskView.getInput().setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                positveButton.callOnClick();
                                return true;
                            }
                            return false;
                        }
                    });
                }
            });
            d.show();
        } else {
            loadSettingsActivity(context, bundle);
        }

    }

    private static void loadSettingsActivity(final Context context, @Nullable Bundle bundle) {
        Intent i = new Intent(context, SettingsActivity.class);
        if (bundle != null) {
            i.putExtras(bundle);
        }
        context.startActivity(i);
    }

    private static void showKioskPasswordMismatchDialog(final Context context) {
        AlertDialog d = new MaterialAlertDialogBuilder(context, R.style.Widgets_Dialog)
                .setTitle("Password Mismatch")
                .setMessage("Sorry, the password you entered is not correct")
                .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing to do here
                    }
                })
                .create();
        d.show();
    }
}
