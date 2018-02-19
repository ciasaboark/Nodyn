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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Action;
import io.phobotic.nodyn_app.list.VerticalSpaceItemDecoration;
import io.phobotic.nodyn_app.list.adapter.ActionRecyclerViewAdapter;
import io.phobotic.nodyn_app.service.SyncService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ActionsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActionsListFragment extends Fragment {
    private static final String TAG = ActionsListFragment.class.getSimpleName();
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_MAX_RECORDS = "max-records";

    private int columnCount;
    private View rootView;
    private RecyclerView recyclerView;
    private View errEmptyList;
    private BroadcastReceiver br;
    private VerticalSpaceItemDecoration decoration;
    private boolean isLoading = false;

    private List<Action> actions = new ArrayList<>();
    private long oldestTimestamp = Long.MAX_VALUE;
    private boolean fetchOlderRecords = true;
    private int MAX_RECORDS = 10;

    public static ActionsListFragment newInstance(int columnCount, int maxRecords) {
        ActionsListFragment fragment = new ActionsListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_MAX_RECORDS, maxRecords);
        fragment.setArguments(args);
        return fragment;
    }

    public ActionsListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Check In/Check Out History");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            columnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case SyncService.BROADCAST_SYNC_FINISH:
                    case SyncService.BROADCAST_SYNC_FAIL:
                    case Database.BROADCAST_ASSET_CHECKOUT:
                    case Database.BROADCASE_ASSET_CHECKIN:
                        showProperView();
                        break;
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_actions_list, container, false);
        init();

        return rootView;
    }

    private void init() {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        errEmptyList = rootView.findViewById(R.id.empty_list);
        final float spacingTop = getResources().getDimension(R.dimen.list_item_spacing_top);
        final float spacingBottom = getResources().getDimension(R.dimen.list_item_spacing_bottom);
        decoration = new VerticalSpaceItemDecoration(spacingTop, spacingBottom);
        initList();
    }

    private void initList() {
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());

        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addItemDecoration(decoration);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction((SyncService.BROADCAST_SYNC_FINISH));
        filter.addAction(SyncService.BROADCAST_SYNC_FAIL);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(br, filter);

        showProperView();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(br);
    }

    private void showProperView() {
        Database db = Database.getInstance(getContext());
        //since this is the initial showing of the list we will pull records with the farthest out
        //+ timestamps backwards, maximum of
        long maxTimestamp = Long.MAX_VALUE;
        actions = db.getActions(maxTimestamp, MAX_RECORDS);

        if (actions.isEmpty()) {
            showEmptyListError();
        } else {
            //record the furthest back timestamp so we can use it as a reference point to pull
            //+ older records later
            Action oldestAction = actions.get(actions.size() - 1);
            this.oldestTimestamp = oldestAction.getTimestamp();

            showList();
        }
    }

    private void showEmptyListError() {
        recyclerView.setVisibility(View.GONE);
        errEmptyList.setVisibility(View.VISIBLE);
    }

    private void showList() {
        oldestTimestamp = Long.MAX_VALUE;
        fetchOlderRecords = true;
        recyclerView.setVisibility(View.VISIBLE);
        errEmptyList.setVisibility(View.GONE);

        isLoading = true;

        recyclerView.setAdapter(new ActionRecyclerViewAdapter(actions, null));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
                if (isLoading) {
                    Log.d(TAG, "List loading, skipping fetching older actions");
                    return;
                }

                if (!recyclerView.canScrollVertically(1)) {
                    //we have scrolled to the bottom
                    if (!fetchOlderRecords) {
                        Log.d(TAG, "Will not fetch older items");
                    } else {
                        Log.d(TAG, "Scrollview has reached bottom");
                        Database db = Database.getInstance(getContext());
                        List<Action> actionsToAdd = db.getActions(oldestTimestamp, MAX_RECORDS);
                        if (actionsToAdd == null || actionsToAdd.isEmpty()) {
                            DateFormat df = DateFormat.getDateTimeInstance();
                            Date d = new Date(oldestTimestamp);
                            String dateString = df.format(d);
                            Log.d(TAG, "No action records older than " + dateString);
                            fetchOlderRecords = false;
                        } else {
                            Log.d(TAG, "Found " + actionsToAdd.size() + " more actions, adding to adapter");
                            isLoading = true;
                            //add the items in one at a time so we get proper animations
                            for (Action a : actionsToAdd) {
                                actions.add(a);
                                oldestTimestamp = a.getTimestamp();
                                //this has to run on a separate thread
                                recyclerView.post(new Runnable() {
                                    public void run() {
                                        recyclerView.getAdapter().notifyItemInserted(actions.size() - 1);
                                    }
                                });
                            }
                            isLoading = false;
                        }
                    }
                }

            }
        });

        isLoading = false;
    }

    public ActionsListFragment setColumnCount(int columnCount) {
        this.columnCount = columnCount;
        initList();
        refresh();
        return this;
    }

    public void refresh() {
        showProperView();
    }
}
