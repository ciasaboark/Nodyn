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

package io.phobotic.nodyn_app.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.audit.AuditDatabase;
import io.phobotic.nodyn_app.database.audit.model.Audit;
import io.phobotic.nodyn_app.database.audit.model.AuditHeader;
import io.phobotic.nodyn_app.database.audit.model.AuditDefinition;
import io.phobotic.nodyn_app.database.model.Group;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.fragment.UserAuthorizationFragment;
import io.phobotic.nodyn_app.fragment.audit.AssetAuditFragment;
import io.phobotic.nodyn_app.fragment.audit.AuditDefinitionSelectorFragment;
import io.phobotic.nodyn_app.fragment.audit.AuditStatusListener;
import io.phobotic.nodyn_app.fragment.audit.OnAuditCreatedListener;
import io.phobotic.nodyn_app.service.AuditEmailService;

public class AuditActivity extends AppCompatActivity implements AuditStatusListener, OnAuditCreatedListener, UserAuthorizationFragment.OnUserAuthorizedListener {

    private static final String TAG = AuditActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audit);
        setupActionBar();

        init();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setFocusable(false);

    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent i = new Intent(getApplicationContext(), AuditEmailService.class);
        startService(i);
    }

    private void init() {
        // TODO: 1/17/18 load the intro fragment first, then go to the authorization stage
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean requireScan = prefs.getBoolean(getString(R.string.pref_key_audit_require_user_scan),
                Boolean.parseBoolean(getString(R.string.pref_default_audit_require_user_scan)));
        if (requireScan) {
            //show the authorization fragment
            Database db = Database.getInstance(this);

            Boolean allowAllGroups = prefs.getBoolean(getString(R.string.pref_key_audit_allow_all_groups),
                    Boolean.parseBoolean(getString(R.string.pref_default_audit_allow_all_groups)));
            ArrayList<Integer> allowedGroups = new ArrayList<>();

            if (allowAllGroups) {
                List<Group> groups = db.getGroups();
                for (Group g : groups) {
                    allowedGroups.add(g.getId());
                }
            } else {
                Set<String> groupSet = prefs.getStringSet(getResources()
                        .getString(R.string.pref_key_audit_allowed_groups), new HashSet<String>());
                for (String s : groupSet) {
                    try {
                        int i = Integer.parseInt(s);
                        allowedGroups.add(i);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "Unable to parse selected group ID '" + s + "' as integer value, skipping");
                    }
                }
            }

            UserAuthorizationFragment fragment = UserAuthorizationFragment.newInstance(UserAuthorizationFragment.Role.AUDIT, allowedGroups, allowAllGroups);
            fragment.setListener(this);
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.frame, fragment).commit();
        } else {
            //skip past the authorization fragment if it is not required
            onUserAuthorized(null);
        }
    }

    @Override
    public void onUserAuthorized(User authorizedUser) {
        FragmentManager fm = getSupportFragmentManager();
        AuditDefinitionSelectorFragment fragment = AuditDefinitionSelectorFragment.newInstance(authorizedUser);
        fragment.setListener(this);
        fm.beginTransaction().replace(R.id.frame, fragment).commit();
    }

    @Override
    public void onDefinedAuditChosen(User user, AuditDefinition auditDefinition) {
        AuditDatabase db = AuditDatabase.getInstance(this);

        AuditHeader audit = AuditHeader.fromDefinition(this, user, auditDefinition);

        AssetAuditFragment fragment = AssetAuditFragment.newInstance(auditDefinition, audit, user);
        fragment.setListener(this);
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.frame, fragment).commit();
    }

    @Override
    public void onAuditCancelled() {

    }

    @Override
    public void onAuditComplete(Audit audit) {
        AuditDatabase db = AuditDatabase.getInstance(this);
        audit.getHeader().setEnd(System.currentTimeMillis());
        db.headerDao().insert(audit.getHeader());
        db.detailDao().insert(audit.getDetails());

        AlertDialog d = new MaterialAlertDialogBuilder(this, R.style.Widgets_Dialog)
                .setTitle("Audit complete")
                .setMessage("Thank you. Audit results have been stored and will be transmitted soon")
                .setPositiveButton(android.R.string.ok, null)
                .create();

        d.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
//                Keyboardhelper.forceHideOSK(this, view);
                finish();
            }
        });

        d.show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            showExitWarning();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void showExitWarning() {
        AlertDialog d = new MaterialAlertDialogBuilder(this, R.style.Widgets_Dialog)
                .setTitle("Cancel Audit?")
                .setMessage("Cancel the current asset audit?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing to do here
                    }
                })
                .create();
        d.show();
    }

    @Override
    public void onBackPressed() {
        showExitWarning();
    }

    @Override
    public void onAuditCreated(AuditDefinition auditDefinition) {

    }

    @Override
    public void onCreateAuditCancelled() {

    }
}
