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

package io.phobotic.nodyn_app.list.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.sync.Action;
import io.phobotic.nodyn_app.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn_app.list.RecyclerViewFastScroller;
import io.phobotic.nodyn_app.view.ActionView;

/**
 * Created by Jonathan Nelson on 8/20/17.
 */

public class ActionRecyclerViewAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {
    private static final int VIEW_ACTION_ITEM = 1;
    private static final int VIEW_ACTION_FOOTER = 2;

    private final List<Action> actions;
    private final OnListFragmentInteractionListener mListener;

    public ActionRecyclerViewAdapter(List<Action> actions, OnListFragmentInteractionListener listener) {
        this.actions = actions;
        mListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ACTION_ITEM) {
            ActionView view = new ActionView(parent.getContext(), null, null);
            vh = new ActionViewHolder(view);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.view_footer_load_more, parent, false);
            vh = new FooterViewHolder(v);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ActionViewHolder) {
            ActionViewHolder h = (ActionViewHolder) holder;
            h.item = actions.get(position);
            h.view.setAction(actions.get(position));
            h.view.fastCollapse();
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object o = actions.get(position);
        if (o == null) {
            return VIEW_ACTION_FOOTER;
        } else {
            return VIEW_ACTION_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return actions.size();
    }

    @Override
    public String getTextToShowInBubble(final int pos) {
        Object o = actions.get(pos);

        if (o == null) {
            return "";
        } else if (o instanceof Action) {
            Action a = (Action) o;
            if (a.getTimestamp() == -1) {
                return "?";
            } else {
                Date d = new Date(a.getTimestamp());
                DateFormat df = new SimpleDateFormat("MMM d yy");
                return df.format(d);
            }
        } else {
            return "";
        }
    }

    public class ActionViewHolder extends RecyclerView.ViewHolder {
        public final ActionView view;
        public Action item;

        public ActionViewHolder(ActionView view) {
            super(view);
            this.view = view;
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public ProgressBar progressBar;

        public FooterViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.progressBar = itemView.findViewById(R.id.progress);
        }
    }
}
