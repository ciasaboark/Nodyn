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

package io.phobotic.nodyn_app.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.fragment.dash.AssetStatusChartFragment;
import io.phobotic.nodyn_app.fragment.dash.HistoryChartFragment;
import io.phobotic.nodyn_app.fragment.dash.ModelGridFragment;
import io.phobotic.nodyn_app.service.SyncService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DashboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DashboardFragment extends Fragment {
    private static final String TAG = DashboardFragment.class.getSimpleName();
    private View rootView;
    private boolean isLoading = false;
    private Database db;
    private BroadcastReceiver br;
    private AssetStatusChartFragment assetStatusChartFragment;
    private ModelGridFragment modelGridFragment;
    //    private ShortActionHistoryFragment actionHistoryFragment;
    private HistoryChartFragment historyChartFragment;


    public static DashboardFragment newInstance() {
        DashboardFragment fragment = new DashboardFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        db = Database.getInstance(context);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Dashboard");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        init();

        return rootView;
    }

    private void init() {
        FragmentManager fm = getChildFragmentManager();
        modelGridFragment = (ModelGridFragment) fm.findFragmentById(R.id.overview_grid_fragment);
        assetStatusChartFragment = (AssetStatusChartFragment) fm.findFragmentById(R.id.status_chart_fragment);
        historyChartFragment = (HistoryChartFragment) fm.findFragmentById(R.id.history_chart_fragment);

//        actionHistoryFragment = (ShortActionHistoryFragment) fm.findFragmentById(R.id.action_history_fragment);
//        actionHistoryFragment.setColumnCount(2);

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case SyncService.BROADCAST_SYNC_FINISH:
                        refresh();
                        break;
                    case SyncService.BROADCAST_SYNC_FAIL:
                        refresh();
                        break;
                }
            }
        };
    }

    /**
     * Force the internal fragments to re-load their data
     */
    private void refresh() {
        modelGridFragment.refresh();
        assetStatusChartFragment.refresh();
//        actionHistoryFragment.refresh();
        historyChartFragment.refresh();
        isLoading = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction((SyncService.BROADCAST_SYNC_FINISH));
        filter.addAction(SyncService.BROADCAST_SYNC_FAIL);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(br, filter);

        refresh();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(br);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_dash, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean itemHandled = false;

        switch (item.getItemId()) {
            case R.id.menu_refresh:
                refreshIfNeeded();
                itemHandled = true;
                break;
        }

        return itemHandled;
    }

    /**
     * Re-load data into the internal fragments only if the data is not currently loading
     */
    private void refreshIfNeeded() {
        if (!isLoading) {
            refresh();
        }
    }

}
