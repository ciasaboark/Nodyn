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

package io.phobotic.nodyn_app.fragment.audit;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.audit.AuditDatabase;
import io.phobotic.nodyn_app.database.audit.model.AuditDefinition;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.helper.SettingsHelper;
import io.phobotic.nodyn_app.view.AuditDefinitionView;

public class AuditDefinitionSelectorFragment extends Fragment implements OnAuditCreatedListener {
    private static final String ARG_USER = "user";
    AuditStatusListener listener;
    private User user;
    private View rootView;
    private Button customAuditButton;
    private Button nextButton;
    private View error;
    private RecyclerView recyclerView;
    private AuditDefinition selectedDefinition;
    private View customAuditWarning;
    private Button settingsButton;

    public static AuditDefinitionSelectorFragment newInstance(User user) {
        AuditDefinitionSelectorFragment fragment = new AuditDefinitionSelectorFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    public AuditDefinitionSelectorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(ARG_USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_audit_definition_selector, container, false);
        init();

        return rootView;
    }

    private void init() {
        findViews();
        initButtons();
        initList();
    }

    private void findViews() {
        error = rootView.findViewById(R.id.error);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview);
        nextButton = (Button) rootView.findViewById(R.id.next_button);
        settingsButton = (Button) rootView.findViewById(R.id.settings_button);
        customAuditButton = (Button) rootView.findViewById(R.id.create_button);
        customAuditWarning = rootView.findViewById(R.id.custom_audit_warning);
    }

    private void initButtons() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean allowCustomAudits = prefs.getBoolean(
                getString(R.string.pref_key_audit_enable_custom_audits), Boolean.parseBoolean(
                        getString(R.string.pref_default_audit_enable_custom_audits)));

        if (allowCustomAudits) {
            customAuditButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentManager fm = getChildFragmentManager();
                    CreateAuditDialogFragment fragment = CreateAuditDialogFragment.createTempAudit();
                    fragment.setListener(AuditDefinitionSelectorFragment.this);
                    fragment.show(fm, "create_audit_fragment");
                }
            });
            customAuditWarning.setVisibility(View.GONE);
        } else {
            customAuditButton.setVisibility(View.GONE);
        }

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsHelper.loadKioskSettings(getContext(), null);
            }
        });


        nextButton.setEnabled(false);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDefinedAuditChosen(user, selectedDefinition);
                }
            }
        });
    }

    private void initList() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        updateList();
    }

    private void updateList() {
        AuditDatabase db = AuditDatabase.getInstance(getContext());
        List<AuditDefinition> definitionList = db.getDefinedAudits();

        if (definitionList.isEmpty()) {
            showError();
        } else {
            showList(definitionList);
        }
    }

    private void showError() {
        error.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showList(List<AuditDefinition> auditDefinitions) {
        recyclerView.setVisibility(View.VISIBLE);
        error.setVisibility(View.GONE);
        List<SelectableAuditDefinition> definitions = new ArrayList<>();
        for (AuditDefinition ad : auditDefinitions) {
            definitions.add(new SelectableAuditDefinition(ad));
        }

        recyclerView.setAdapter(new DefinedAuditRecyclerViewAdapter(definitions));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    public AuditDefinitionSelectorFragment setListener(AuditStatusListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public void onAuditCreated(AuditDefinition auditDefinition) {
        if (listener != null) {
            listener.onDefinedAuditChosen(user, auditDefinition);
        }
    }

    @Override
    public void onCreateAuditCancelled() {
        //nothing to do here, user will just fall back from the dialog
    }

    private class DefinedAuditRecyclerViewAdapter extends RecyclerView.Adapter<AuditDefinitionSelectorFragment.DefinedAuditRecyclerViewAdapter.ViewHolder> {
        private final List<SelectableAuditDefinition> items;

        public DefinedAuditRecyclerViewAdapter(List<SelectableAuditDefinition> items) {
            this.items = items;
        }

        @Override
        public AuditDefinitionSelectorFragment.DefinedAuditRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            AuditDefinitionView view = new AuditDefinitionView(parent.getContext(), null);
            view.setDeletable(false);
            // manually set the CustomView's size
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new AuditDefinitionSelectorFragment.DefinedAuditRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final AuditDefinitionSelectorFragment.DefinedAuditRecyclerViewAdapter.ViewHolder holder, final int position) {
            final SelectableAuditDefinition auditDefinition = items.get(position);
            holder.position = position;
            holder.selectableAuditDefinition = auditDefinition;
            holder.view.setHighlighted(auditDefinition.isSelected());
            holder.view.setAuditDefinition(auditDefinition.getDefinition());
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean selected = !auditDefinition.isSelected();

                    auditDefinition.setSelected(selected);
                    holder.view.setHighlighted(selected);

                    if (selected) {
                        selectedDefinition = auditDefinition.getDefinition();
                        nextButton.setEnabled(true);
                    } else {
                        selectedDefinition = null;
                        nextButton.setEnabled(false);
                    }

                    for (int i = 0; i < items.size(); i++) {
                        if (i != position) {
                            SelectableAuditDefinition d = items.get(i);
                            if (d.isSelected()) {
                                d.setSelected(false);
                                notifyItemChanged(i);
                            }
                        }
                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public int position;
            private AuditDefinitionView view;
            private SelectableAuditDefinition selectableAuditDefinition;


            public ViewHolder(AuditDefinitionView view) {
                super(view);
                this.view = view;
            }

            public void bind(SelectableAuditDefinition selectableAuditDefinition) {
                this.selectableAuditDefinition = selectableAuditDefinition;
            }
        }
    }

    private class SelectableAuditDefinition {
        private boolean selected;
        private AuditDefinition definition;

        public SelectableAuditDefinition(AuditDefinition definition) {
            this.definition = definition;
        }

        public AuditDefinition getDefinition() {
            return definition;
        }

        public boolean isSelected() {
            return selected;
        }

        public SelectableAuditDefinition setSelected(boolean selected) {
            this.selected = selected;
            return this;
        }
    }
}
