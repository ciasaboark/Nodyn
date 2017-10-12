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

package io.phobotic.nodyn.fragment.dash;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.model.Action;
import io.phobotic.nodyn.view.ActionView;


public class ShortActionHistoryFragment extends Fragment {
    private static final String ARG_MAX_RECORDS = "max_records";
    private static final String ARG_COLUMN_COUNT = "column_count";

    private int maxRecords = -1;
    private int columnCount = 1;
    private View rootView;
    private GridLayout holder;


    public static ShortActionHistoryFragment newInstance(int maxRecords, int columnCount) {
        ShortActionHistoryFragment fragment = new ShortActionHistoryFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MAX_RECORDS, maxRecords);
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    public ShortActionHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            maxRecords = getArguments().getInt(ARG_MAX_RECORDS);
            columnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_short_action_history, container, false);
        holder = (GridLayout) rootView.findViewById(R.id.holder);
        init();

        return rootView;
    }

    private void init() {
        holder.removeAllViews();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void setMaxRecords(int maxRecords) {
        this.maxRecords = maxRecords;
        refresh();
    }

    public void refresh() {
        holder.removeAllViews();
        holder.setColumnCount(columnCount);
        holder.invalidate();

        Database db = Database.getInstance(getContext());
        List<Action> actionList = db.getActions();
        if (actionList == null) actionList = new ArrayList<>();

        if (maxRecords > 0 && actionList.size() > maxRecords) {
            List<Action> copy = new ArrayList<>();
            for (int i = 0; i < maxRecords; i++) {
                Action a = actionList.get(i);
                copy.add(a);
            }

            actionList = copy;
        }

        addActions(actionList);
    }

    private void addActions(List<Action> actions) {
        for (Action a : actions) {
            ActionView actionView = new ActionView(getContext(), null, a);
            holder.addView(actionView);
        }
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
        refresh();
    }
}
