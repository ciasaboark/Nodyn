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

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import java.util.List;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn.view.ScannedAssetView;

/**
 * Created by Jonathan Nelson on 8/15/17.
 */


/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class ScannedAssetRecyclerViewAdapter extends
        RecyclerView.Adapter<ScannedAssetRecyclerViewAdapter.ViewHolder> {

    private final List<Asset> items;
    private final OnListFragmentInteractionListener listener;
    private final boolean assetsRemoveable;
    private final Context context;
    private int lastPosition = -1;

    public ScannedAssetRecyclerViewAdapter(Context context, List<Asset> items, @Nullable
            OnListFragmentInteractionListener listener,
                                           boolean assetsRemovable) {
        this.context = context;
        this.items = items;
        this.listener = listener;
        this.assetsRemoveable = assetsRemovable;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ScannedAssetView view = new ScannedAssetView(parent.getContext(), null, null);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.position = position;
        holder.item = items.get(position);
        ((ScannedAssetView) holder.view).setAsset(holder.item);
        ((ScannedAssetView) holder.view).setAssetRemovable(assetsRemoveable);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    listener.onListFragmentInteraction(holder.item, null);
                }
            }
        });

        if (holder.deleteButton != null) {
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeAt(holder.position);
                }
            });
        }

        // Here you apply the animation when the view is bound
        setAnimation(holder.itemView, position);

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void removeAt(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final ImageButton deleteButton;
        public int position;
        public Asset item;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.deleteButton = (ImageButton) view.findViewById(R.id.delete_button);
        }
    }
}
