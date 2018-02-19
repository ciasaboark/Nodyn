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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.view.AuditStatusView;

public class AuditSelectStatusFragment extends Fragment {
    private static final String TAG = AuditSelectStatusFragment.class.getSimpleName();
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int columnCount = 1;
    private View rootView;
    private List<SelectableStatus> selectableStatuses = new ArrayList<>();
    private RecyclerView recyclerView;
    private CheckBox auditAllCheckbox;
    private AuditCreationListener listener;
    private Spinner spinner;


    public static AuditSelectStatusFragment newInstance(int columnCount) {
        AuditSelectStatusFragment fragment = new AuditSelectStatusFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AuditSelectStatusFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            columnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_audit_select_status, container, false);

        init();

        return rootView;
    }

    private void init() {
        findViews();
        initCheckbox();
        initStatusList();
        initSpinner();
    }

    private void findViews() {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        auditAllCheckbox = (CheckBox) rootView.findViewById(R.id.audit_all);
        spinner = (Spinner) rootView.findViewById(R.id.spinner);
    }

    private void initCheckbox() {
        auditAllCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                recyclerView.setEnabled(false);
                statusSelectionChanged();
            }
        });
    }

    private void initStatusList() {
        Database db = Database.getInstance(getContext());
        List<Status> allStatuses = db.getStatuses();
        selectableStatuses = new ArrayList<>();
        for (Status s : allStatuses) {
            SelectableStatus ss = new SelectableStatus(s);
            selectableStatuses.add(ss);
        }

        initList();
    }

    private void initSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.audit_status_meta, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                statusSelectionChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                statusSelectionChanged();
            }
        });
    }

    private void statusSelectionChanged() {
        if (listener != null) {
            List<Status> selectedStatuses = new ArrayList<>();
            for (SelectableStatus ss : selectableStatuses) {
                if (ss.isChecked()) {
                    selectedStatuses.add(ss.getStatus());
                }
            }

            String[] metaValues = getResources().getStringArray(R.array.audit_status_meta_values);
            int pos = spinner.getSelectedItemPosition();
            String metaStatus = metaValues[pos];

            listener.onStatusesSelected(metaStatus, selectedStatuses, auditAllCheckbox.isChecked());
        }
    }

    private void initList() {
        Context context = rootView.getContext();
        if (columnCount <= 1) {
            LinearLayoutManager lm = new LinearLayoutManager(context);
            lm.setOrientation(LinearLayoutManager.VERTICAL);
            recyclerView.setLayoutManager(lm);

        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, columnCount));
        }

        recyclerView.setAdapter(new AuditStatusRecyclerViewAdapter(selectableStatuses));
    }

    public AuditSelectStatusFragment setListener(AuditCreationListener listener) {
        this.listener = listener;
        return this;
    }

    private class AuditStatusRecyclerViewAdapter extends RecyclerView.Adapter<AuditStatusRecyclerViewAdapter.ViewHolder> {
        private final List<SelectableStatus> items;

        public AuditStatusRecyclerViewAdapter(List<SelectableStatus> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            AuditStatusView view = new AuditStatusView(parent.getContext(), null);
            // manually set the CustomView's size
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            SelectableStatus status = items.get(position);
            holder.status = status;
            holder.view.setStatus(status);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.status.setChecked(!holder.status.isChecked());
                    holder.checkbox.setChecked(holder.status.isChecked());
//                    initList();
                    statusSelectionChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
            private CheckBox checkbox;
            private AuditStatusView view;
            private SelectableStatus status;


            public ViewHolder(AuditStatusView view) {
                super(view);
                this.view = view;
                this.checkbox = (CheckBox) view.findViewById(R.id.check);
            }

            public void bind(SelectableStatus selectableStatus) {
                this.status = selectableStatus;
                this.checkbox.setChecked(selectableStatus.isChecked());
                this.checkbox.setOnCheckedChangeListener(this);
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                this.status.setChecked(isChecked);
                statusSelectionChanged();
            }
        }
    }

    public class SelectableStatus {
        private Status status;
        private boolean checked;
        private boolean enabled = true;

        public SelectableStatus(Status status) {
            this.status = status;
        }

        public boolean isChecked() {
            return checked;
        }

        public SelectableStatus setChecked(boolean checked) {
            this.checked = checked;
            return this;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public SelectableStatus setEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Status getStatus() {
            return status;
        }
    }
}
