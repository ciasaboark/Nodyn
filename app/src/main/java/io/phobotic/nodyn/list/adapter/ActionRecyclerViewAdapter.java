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

package io.phobotic.nodyn.list.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

import io.phobotic.nodyn.database.model.Action;
import io.phobotic.nodyn.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn.view.ActionView;

/**
 * Created by Jonathan Nelson on 8/20/17.
 */

public class ActionRecyclerViewAdapter extends
        RecyclerView.Adapter<ActionRecyclerViewAdapter.ViewHolder> {

    private final List<Action> actions;
    private final OnListFragmentInteractionListener mListener;

    public ActionRecyclerViewAdapter(List<Action> actions, OnListFragmentInteractionListener listener) {
        this.actions = actions;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ActionView view = new ActionView(parent.getContext(), null, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.item = actions.get(position);
        ((ActionView) holder.view).setAction(actions.get(position));
    }

    @Override
    public int getItemCount() {
        return actions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final ActionView view;
        public Action item;

        public ViewHolder(ActionView view) {
            super(view);
            this.view = view;
        }
    }
}
