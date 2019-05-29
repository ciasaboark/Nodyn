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

package io.phobotic.nodyn_app.fragment.audit;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.view.AuditModelView;

/**
 * Created by Jonathan Nelson on 1/27/18.
 */

public class AuditSelectModelsFragment extends Fragment {
    private static final String TAG = AuditSelectModelsFragment.class.getSimpleName();
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int columnCount = 1;
    private View rootView;
    private List<SelectableModel> selectableModels = new ArrayList<>();
    private RecyclerView recyclerView;
    private CheckBox auditAllCheckbox;
    private AuditCreationListener listener;


    public static AuditSelectModelsFragment newInstance(int columnCount) {
        AuditSelectModelsFragment fragment = new AuditSelectModelsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AuditSelectModelsFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_audit_select_models, container, false);

        init();

        return rootView;
    }

    private void init() {
        findViews();
        initCheckbox();
        initStatusList();
    }

    private void findViews() {
        recyclerView = rootView.findViewById(R.id.list);
        auditAllCheckbox = rootView.findViewById(R.id.audit_all);
    }

    private void initCheckbox() {
        auditAllCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (SelectableModel sm : selectableModels) {
                    sm.setEnabled(!isChecked);
                }
                initList();
                modelSelectionChanged();
            }
        });
    }

    private void initStatusList() {
        Database db = Database.getInstance(getContext());
        List<Model> allModels = db.getModels();
        selectableModels = new ArrayList<>();
        for (Model m : allModels) {
            SelectableModel sm = new SelectableModel(m);
            selectableModels.add(sm);
        }

        initList();
    }

    private void initList() {
        Context context = rootView.getContext();
        if (columnCount <= 1) {
            LinearLayoutManager lm = new LinearLayoutManager(context);
            lm.setOrientation(RecyclerView.VERTICAL);
            recyclerView.setLayoutManager(lm);

        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, columnCount));
        }

        recyclerView.setAdapter(new AuditStatusRecyclerViewAdapter(selectableModels));
    }

    private void modelSelectionChanged() {
        if (listener != null) {
            List<Model> selectedModels = new ArrayList<>();
            for (SelectableModel sm : selectableModels) {
                if (sm.isChecked()) {
                    selectedModels.add(sm.getModel());
                }
            }

            listener.onModelsSelected(selectedModels, auditAllCheckbox.isChecked());
        }
    }

    public AuditSelectModelsFragment setListener(AuditCreationListener listener) {
        this.listener = listener;
        return this;
    }

    private class AuditStatusRecyclerViewAdapter extends RecyclerView.Adapter<AuditStatusRecyclerViewAdapter.ViewHolder> {
        private final List<SelectableModel> items;

        public AuditStatusRecyclerViewAdapter(List<SelectableModel> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            AuditModelView view = new AuditModelView(parent.getContext(), null);
            // manually set the CustomView's size
            view.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            SelectableModel model = items.get(position);
            holder.model = model;
            holder.view.setModel(model);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.model.setChecked(!holder.model.isChecked());
                    holder.checkbox.setChecked(holder.model.isChecked());
//                    initList();
                    modelSelectionChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {
            private CheckBox checkbox;
            private AuditModelView view;
            private SelectableModel model;


            public ViewHolder(AuditModelView view) {
                super(view);
                this.view = view;
                this.checkbox = view.findViewById(R.id.check);
            }

            public void bind(SelectableModel selectableModel) {
                this.model = selectableModel;
                this.checkbox.setChecked(selectableModel.isChecked());
                this.checkbox.setOnCheckedChangeListener(this);
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                this.model.setChecked(isChecked);
                modelSelectionChanged();
            }
        }


    }


    public class SelectableModel {
        private Model model;
        private boolean checked;
        private boolean enabled = true;

        public SelectableModel(Model model) {
            this.model = model;
        }

        public boolean isChecked() {
            return checked;
        }

        public void setChecked(boolean checked) {
            this.checked = checked;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Model getModel() {
            return model;
        }
    }
}

