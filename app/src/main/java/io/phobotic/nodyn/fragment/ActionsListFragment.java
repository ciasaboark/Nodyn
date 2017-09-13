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


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.model.Action;
import io.phobotic.nodyn.list.VerticalSpaceItemDecoration;
import io.phobotic.nodyn.list.adapter.ActionRecyclerViewAdapter;
import io.phobotic.nodyn.service.SyncService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ActionsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActionsListFragment extends Fragment {
    private static final String TAG = ActionsListFragment.class.getSimpleName();
    private static final String ARG_COLUMN_COUNT = "column-count";
    private int columnCount;
    private View rootView;
    private RecyclerView recyclerView;
    private View errEmptyList;
    private BroadcastReceiver br;

    public static ActionsListFragment newInstance(int columnCount) {
        ActionsListFragment fragment = new ActionsListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    public ActionsListFragment() {
        // Required empty public constructor
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

        initList();
    }

    private void initList() {
        if (columnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnCount));
        }

        final float spacingTop = getResources().getDimension(R.dimen.list_item_spacing_top);
        final float spacingBottom = getResources().getDimension(R.dimen.list_item_spacing_bottom);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(spacingTop, spacingBottom));
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
        List<Action> actions = db.getActions();

        //sort the list in reverse chronological order
        Collections.sort(actions, new Comparator<Action>() {
            @Override
            public int compare(Action o1, Action o2) {
                return -(((Long) o1.getTimestamp()).compareTo(o2.getTimestamp()));
            }
        });

        if (actions.isEmpty()) {
            showEmptyListError();
        } else {
            showList(actions);
        }
    }

    private void showEmptyListError() {
        recyclerView.setVisibility(View.GONE);
        errEmptyList.setVisibility(View.VISIBLE);
    }

    private void showList(List<Action> actions) {
        recyclerView.setVisibility(View.VISIBLE);
        errEmptyList.setVisibility(View.GONE);

        recyclerView.setAdapter(new ActionRecyclerViewAdapter(actions, null));
    }
}
