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

package io.phobotic.nodyn_app.activity;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import java.util.List;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.audit.AuditDatabase;
import io.phobotic.nodyn_app.database.audit.model.AuditDefinition;
import io.phobotic.nodyn_app.fragment.audit.CreateAuditDialogFragment;
import io.phobotic.nodyn_app.fragment.audit.OnAuditCreatedListener;
import io.phobotic.nodyn_app.view.AuditDefinitionView;

public class AuditDefinitionsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View error;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_defined_audits);
        setupActionBar();
        init();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Drawable d = getDrawable(R.drawable.arrow_left);
        d.setTint(getResources().getColor(R.color.white));
        toolbar.setNavigationIcon(d);
//        toolbar.setNavigationIcon(R.drawable.arrow_left);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void init() {
        findViews();
        initFab();
        initList();
    }

    private void initList() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        updateList();
    }

    private void findViews() {
        fab = (FloatingActionButton) findViewById(R.id.fab);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        error = findViewById(R.id.list_error);
    }

    private void updateList() {
        AuditDatabase db = AuditDatabase.getInstance(this);
        List<AuditDefinition> auditDefinitions = db.getDefinedAudits();
        if (auditDefinitions.isEmpty()) {
            showError();
        } else {
            showList(auditDefinitions);
        }
    }

    private void showError() {
        recyclerView.setVisibility(View.GONE);
        error.setVisibility(View.VISIBLE);
    }

    private void showList(List<AuditDefinition> auditDefinitions) {
        recyclerView.setVisibility(View.VISIBLE);
        error.setVisibility(View.GONE);
        recyclerView.setAdapter(new DefinedAuditRecyclerViewAdapter(auditDefinitions));
    }

    private void initFab() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getSupportFragmentManager();
                CreateAuditDialogFragment fragment = CreateAuditDialogFragment.createNamedAudit();
                fragment.setListener(new OnAuditCreatedListener() {
                    @Override
                    public void onAuditCreated(AuditDefinition auditDefinition) {
                        AuditDatabase db = AuditDatabase.getInstance(AuditDefinitionsActivity.this);
                        db.storeDefinedAudit(auditDefinition);
                        updateList();
                    }

                    @Override
                    public void onCreateAuditCancelled() {
                        //nothing to do here
                    }
                });
                fragment.show(fm, "create_audit_fragment");
            }
        });
    }

    private class DefinedAuditRecyclerViewAdapter extends RecyclerView.Adapter<DefinedAuditRecyclerViewAdapter.ViewHolder> {
        private final List<AuditDefinition> items;

        public DefinedAuditRecyclerViewAdapter(List<AuditDefinition> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            AuditDefinitionView view = new AuditDefinitionView(parent.getContext(), null);
            // manually set the CustomView's size
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            AuditDefinition auditDefinition = items.get(position);
            holder.position = position;
            holder.audit = auditDefinition;
            holder.view.setAuditDefinition(auditDefinition);

            if (holder.deleteButton != null) {
                holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeAt(holder.position);
                    }
                });
            }
        }

        public void removeAt(final int position) {
            final AuditDefinition audit = items.remove(position);
            AlertDialog d = new AlertDialog.Builder(AuditDefinitionsActivity.this)
                    .setTitle("Delete Defined Audit?")
                    .setMessage("Are you sure you want to delete this audit?")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AuditDatabase db = AuditDatabase.getInstance(getApplicationContext());
                            db.deleteDefinedAudit(audit.getId());
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, items.size());
                            if (items.isEmpty()) {
                                updateList();
                            }
                        }
                    })
                    .setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //nothing to do here
                        }
                    })
                    .create();
            d.show();
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final ImageButton deleteButton;
            public int position;
            private AuditDefinitionView view;
            private AuditDefinition audit;


            public ViewHolder(AuditDefinitionView view) {
                super(view);
                this.view = view;
                this.deleteButton = (ImageButton) view.findViewById(R.id.delete_button);
            }

            public void bind(AuditDefinition auditDefinition) {
                this.audit = auditDefinition;
            }
        }
    }

}
