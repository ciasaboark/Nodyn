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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.audit.model.AuditDefinition;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.model.Status;

public class CreateAuditDialogFragment extends DialogFragment implements AuditCreationListener {
    private static final String TAG = CreateAuditDialogFragment.class.getSimpleName();
    private static final String ARG_NAME_REQUIRED = "name_required";
    private OnAuditCreatedListener listener;
    private View rootView;
    private FrameLayout frame;
    private View intro;
    private boolean nameRequired;
    private AuditDefinition createdAudit;
    private Button finishButton;
    private Button cancelButton;


    public static CreateAuditDialogFragment createNamedAudit() {
        CreateAuditDialogFragment fragment = new CreateAuditDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_NAME_REQUIRED, true);
        fragment.setArguments(args);
        return fragment;
    }

    public static CreateAuditDialogFragment createTempAudit() {
        CreateAuditDialogFragment fragment = new CreateAuditDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_NAME_REQUIRED, false);
        fragment.setArguments(args);
        return fragment;
    }

    public CreateAuditDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_audit, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;

        if (getArguments() != null) {
            nameRequired = getArguments().getBoolean(ARG_NAME_REQUIRED);
        }

        init();
    }

    private void init() {
        findViews();
        initButtons();
        showIntro();
    }

    private void findViews() {
        frame = rootView.findViewById(R.id.frame);
        intro = rootView.findViewById(R.id.intro);
        finishButton = rootView.findViewById(R.id.finish_button);
        cancelButton = rootView.findViewById(R.id.cancel_button);
    }

    private void initButtons() {
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCreateAuditCancelled();
                }
                dismiss();
            }
        });

        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onIntroFinished();
            }
        });
    }

    private void showIntro() {
        frame.setVisibility(View.GONE);
        intro.setVisibility(View.VISIBLE);
    }

    @Override
    public void onIntroFinished() {
        intro.setVisibility(View.GONE);
        frame.setVisibility(View.VISIBLE);

        FragmentManager fm = getChildFragmentManager();
        AuditSelectModelsFragment modelsFragment = AuditSelectModelsFragment.newInstance(1);
        modelsFragment.setListener(CreateAuditDialogFragment.this);
        fm.beginTransaction().replace(R.id.frame, modelsFragment).commit();
        this.createdAudit = new AuditDefinition();

        finishButton.setEnabled(false);
    }

    @Override
    public void onModelsSelected(List<Model> modelsList, boolean auditAllModels) {
        boolean enableNext = auditAllModels || !modelsList.isEmpty();
        finishButton.setEnabled(enableNext);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishButton.setEnabled(false);
                FragmentManager fm = getChildFragmentManager();
                AuditSelectStatusFragment statusesFragment = AuditSelectStatusFragment.newInstance(1);
                statusesFragment.setListener(CreateAuditDialogFragment.this);
                fm.beginTransaction().replace(R.id.frame, statusesFragment).commit();
            }
        });
        List<Integer> modelIDs = new ArrayList<>();
        if (modelsList != null) {
            for (Model m : modelsList) {
                modelIDs.add(m.getId());
            }
        }
        createdAudit.setRequiredModelIDs(modelIDs)
                .setAuditAllModels(auditAllModels);
    }

    @Override
    public void onStatusesSelected(String metaStatus, List<Status> statuses, boolean auditAllStatuses) {
        if (metaStatus == null) {
            String[] metaValues = getResources().getStringArray(R.array.audit_status_meta_values);
            metaStatus = metaValues[0];
        }

        boolean enableNext = auditAllStatuses || !statuses.isEmpty();

        //if this will be an unnamed audit then we can stop after getting a list of statuses
        if (nameRequired) {
            finishButton.setEnabled(enableNext);
            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finishButton.setEnabled(false);
                    FragmentManager fm = getChildFragmentManager();
                    AuditCreateDetailsFragment fragment = AuditCreateDetailsFragment.newInstance();
                    fragment.setListener(CreateAuditDialogFragment.this);
                    fm.beginTransaction().replace(R.id.frame, fragment).commit();
                    finishButton.setText(R.string.finish);
                }
            });
        } else {
            finishButton.setText(R.string.finish);
            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    completeAudit();
                }
            });
            finishButton.setEnabled(enableNext);
        }

        List<Integer> statusIDs = new ArrayList<>();
        if (statuses != null) {
            for (Status s : statuses) {
                statusIDs.add(s.getId());
            }
        }
        createdAudit.setRequiredStatusIDs(statusIDs)
                .setAuditAllStatuses(auditAllStatuses)
                .setMetaStatus(metaStatus);
    }

    /**
     * It is expected that this will be called every time the name or description field is updated.
     *
     * @param name
     * @param description
     */
    @Override
    public void onAuditDetailsEntered(String name, String description, boolean isBlindAudit) {
        createdAudit.setName(name)
                .setDetails(description)
                .setBlindAudit(isBlindAudit);

        finishButton.setText(R.string.finish);
        finishButton.setEnabled(name != null && name.length() > 0);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                completeAudit();
            }
        });
    }

    private void completeAudit() {
        createdAudit.setCreateTimestamp(System.currentTimeMillis());
        if (listener != null) {
            listener.onAuditCreated(createdAudit);
        }
        dismiss();
    }

    public CreateAuditDialogFragment setListener(OnAuditCreatedListener listener) {
        this.listener = listener;
        return this;
    }
}
