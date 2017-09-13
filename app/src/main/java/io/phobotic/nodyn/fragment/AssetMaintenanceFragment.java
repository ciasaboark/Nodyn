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

package io.phobotic.nodyn.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.MaintenanceRecord;
import io.phobotic.nodyn.sync.SyncManager;
import io.phobotic.nodyn.sync.adapter.SyncAdapter;
import io.phobotic.nodyn.sync.adapter.SyncException;
import io.phobotic.nodyn.sync.adapter.SyncNotSupportedException;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AssetMaintenanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AssetMaintenanceFragment extends Fragment {
    private static final String ARG_ASSET = "asset";
    private static Asset asset;
    private View rootView;
    private View error;
    private View success;
    private TextView message;
    private TextView reason;

    public static AssetMaintenanceFragment newInstance(Asset asset) {
        AssetMaintenanceFragment fragment = new AssetMaintenanceFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ASSET, asset);
        fragment.setArguments(args);
        return fragment;
    }

    public AssetMaintenanceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            asset = (Asset) getArguments().getSerializable(ARG_ASSET);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_asset_maintenance, container, false);
        init();

        return rootView;
    }

    private void init() {
        success = rootView.findViewById(R.id.success);
        error = rootView.findViewById(R.id.fail);
        reason = (TextView) rootView.findViewById(R.id.reason);
        message = (TextView) rootView.findViewById(R.id.message);


        try {
            List<MaintenanceRecord> maintenanceRecords = tryPullMaintenaceRecords();
            error.setVisibility(View.GONE);
            success.setVisibility(View.VISIBLE);

            // TODO: 7/19/17 create list from the maintenance records
        } catch (SyncException e) {
            e.printStackTrace();

            error.setVisibility(View.VISIBLE);
            success.setVisibility(View.GONE);
            reason.setText(R.string.sync_failed_message);
            message.setText(e.getMessage());
        } catch (SyncNotSupportedException e) {
            error.setVisibility(View.VISIBLE);
            success.setVisibility(View.GONE);
            reason.setText(e.getReason());
            message.setText(e.getMessage());
        }
    }

    private
    @NotNull
    List<MaintenanceRecord> tryPullMaintenaceRecords() throws SyncNotSupportedException, SyncException {
        SyncAdapter adapter = SyncManager.getPrefferedSyncAdapter(getActivity());
        return adapter.getMaintenanceRecords(getActivity(), asset);
    }

    private void setTextOrHide(@NotNull View view, @NotNull TextView tv, @Nullable String text) {
        if (text == null || text.equals("")) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
    }

}
