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

package io.phobotic.nodyn_app.fragment.asset;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.MaintenanceRecord;
import io.phobotic.nodyn_app.sync.SyncManager;
import io.phobotic.nodyn_app.sync.adapter.SyncAdapter;
import io.phobotic.nodyn_app.sync.adapter.SyncException;
import io.phobotic.nodyn_app.sync.adapter.SyncNotSupportedException;
import io.phobotic.nodyn_app.view.CloudErrorView;
import io.phobotic.nodyn_app.view.MaintenanceRecordView;

import static io.phobotic.nodyn_app.helper.AnimationHelper.fadeIn;
import static io.phobotic.nodyn_app.helper.AnimationHelper.fadeOut;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AssetMaintenanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AssetMaintenanceFragment extends Fragment {
    private static final String TAG = AssetMaintenanceFragment.class.getSimpleName();
    private static final String ARG_ASSET = "asset";
    private static Asset asset;
    private View rootView;
    private CloudErrorView error;
    private View loading;
    private LinearLayout holder;
    private View emptyListWarning;

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
        holder = rootView.findViewById(R.id.holder);
        error = rootView.findViewById(R.id.error);
        loading = rootView.findViewById(R.id.loading);
        emptyListWarning = rootView.findViewById(R.id.empty_list_warning);

        emptyListWarning.setVisibility(View.GONE);
        error.setVisibility(View.GONE);
        holder.setVisibility(View.GONE);
        holder.removeAllViews();
        loading.setVisibility(View.VISIBLE);

        refresh();
    }

    private void refresh() {
        fadeOut(getContext(), error);
        fadeOut(getContext(), holder);
        holder.removeAllViews();

        DownloadMaintenanceTask task = new DownloadMaintenanceTask();
        task.execute(asset);
    }

    private void setTextOrHide(@NotNull View view, @NotNull TextView tv, @Nullable String text) {
        if (text == null || text.equals("")) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
    }

    private void showRemoteError(@Nullable final String reason, @Nullable final String message) {
        Activity a = getActivity();
        if (a != null) {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StringBuilder sb = new StringBuilder();
                    if (reason != null) sb.append(reason);
                    if (reason != null && message != null) sb.append(": ");
                    if (message != null) sb.append(message);
                    error.setMessage(sb.toString());
                    final Snackbar snackbar = Snackbar.make(rootView, sb.toString(), Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction(R.string.close, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.setActionTextColor(getResources().getColor(R.color.warning_strong));
                    snackbar.show();
                    //in addition to the error also show the empty list warning
                    fadeIn(getContext(), emptyListWarning);
                    fadeOut(getContext(), loading);
                    fadeOut(getContext(), holder);
                }
            });
        }
    }

    private void showMainteanceRecords(final List<MaintenanceRecord> records) {
        Activity a = getActivity();
        if (a != null) {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fadeOut(getContext(), loading);
                    fadeOut(getContext(), error);

                    if (records.isEmpty()) {
                        fadeIn(getContext(), emptyListWarning);
                    } else {
                        fadeIn(getContext(), holder);
                        for (MaintenanceRecord record : records) {
                            MaintenanceRecordView v = new MaintenanceRecordView(getContext(), null, record);
                            holder.addView(v);
                        }
                    }

//                    holder.invalidate();
                }
            });
        }
    }


    private class DownloadMaintenanceTask extends AsyncTask<Asset, Void, Void> {

        @Override
        protected Void doInBackground(Asset... params) {
            Asset asset = params[0];

            try {
                List<MaintenanceRecord> maintenanceRecords = tryPullMaintenanceRecords(asset);
                showMainteanceRecords(maintenanceRecords);
            } catch (SyncException e) {
                e.printStackTrace();
                showRemoteError(null, e.getMessage());
            } catch (SyncNotSupportedException e) {
                //if this backend does not support showing maintenance records then show a generic
                //+ error message
                String name = SyncManager.getPrefferedSyncAdapter(getContext()).getAdapterName();
                showRemoteError("Backend error", String.format("%s does not support fetching maintenace records", name));
            } catch (Exception e) {
                //we should never receive anything other than a SyncException or a SyncNotSupportedException
                Crashlytics.logException(e);
                showRemoteError(null, "Unknown error");
            }

            return null;
        }

        private
        @NotNull
        List<MaintenanceRecord> tryPullMaintenanceRecords(Asset asset) throws Exception {
            SyncAdapter adapter = SyncManager.getPrefferedSyncAdapter(getActivity());
            return adapter.getMaintenanceRecords(getActivity(), asset);
        }
    }

}
