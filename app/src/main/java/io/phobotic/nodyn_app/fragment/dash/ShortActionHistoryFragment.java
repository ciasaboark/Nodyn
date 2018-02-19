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

package io.phobotic.nodyn_app.fragment.dash;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Action;
import io.phobotic.nodyn_app.view.ActionView;


public class ShortActionHistoryFragment extends Fragment {
    private static final String ARG_MAX_RECORDS = "max_records";
    private static final String ARG_COLUMN_COUNT = "column_count";
    private static final int MAX_RECORDS = 5;

    private int columnCount = 1;
    private View rootView;
    private GridLayout holder;
    private View progress;
    private View error;


    public static ShortActionHistoryFragment newInstance(int maxRecords, int columnCount) {
        ShortActionHistoryFragment fragment = new ShortActionHistoryFragment();
        Bundle args = new Bundle();
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
            columnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_short_action_history, container, false);
        init();

        return rootView;
    }

    private void init() {
        holder = (GridLayout) rootView.findViewById(R.id.holder);
        holder.removeAllViews();
        holder.setVisibility(View.GONE);
        progress = rootView.findViewById(R.id.progress);
        progress.setVisibility(View.VISIBLE);
        error = rootView.findViewById(R.id.error);
        error.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        holder.removeAllViews();
        refresh();
    }

    public void refresh() {
        holder.removeAllViews();
        holder.setColumnCount(columnCount);
        holder.invalidate();
        progress.setVisibility(View.VISIBLE);
        holder.setVisibility(View.GONE);
        error.setVisibility(View.GONE);

        animateIn(progress);

        DataLoadAsyncTask asyncTask = new DataLoadAsyncTask();
        asyncTask.execute();
    }

    private void animateIn(final View view) {
        Context context = getContext();
        Animation fadeIn = null;
        if (context != null) {
            fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        }

        if (fadeIn == null) {
            view.setVisibility(View.VISIBLE);
        } else {
            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            view.startAnimation(fadeIn);
        }
    }

    private void showActionsOrError(final List<Action> actions) {
        Activity a = getActivity();
        if (a != null) {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    animateOut(progress);
                    if (actions.isEmpty()) {
                        showError();
                    } else {
                        addActions(actions);
                    }
                }
            });
        }
    }

    private void animateOut(final View view) {
        Context context = getContext();
        Animation fadeOut = null;
        if (context != null) {
            fadeOut = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        }

        if (fadeOut == null) {
            view.setVisibility(View.GONE);
        } else {
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            view.startAnimation(fadeOut);
        }
    }

    private void showError() {
        animateIn(error);
    }

    private void addActions(List<Action> actions) {
        animateIn(holder);
        for (Action a : actions) {
            ActionView actionView = new ActionView(getContext(), null, a);
            holder.addView(actionView);
        }
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
        refresh();
    }

    private class DataLoadAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            Database db = Database.getInstance(getContext());
            List<Action> actionList = db.getActions(Long.MAX_VALUE, MAX_RECORDS);

            if (actionList == null) actionList = new ArrayList<>();

            showActionsOrError(actionList);

            return null;
        }
    }
}
