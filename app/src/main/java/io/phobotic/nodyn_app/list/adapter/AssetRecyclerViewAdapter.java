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

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.fragment.SimplifiedAsset;
import io.phobotic.nodyn_app.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn_app.list.viewholder.AssetViewHolder;
import io.phobotic.nodyn_app.view.AssetView;

public class AssetRecyclerViewAdapter extends RecyclerView.Adapter<AssetViewHolder> {

    private final List<SimplifiedAsset> items;
    private final OnListFragmentInteractionListener listener;
    private final Context context;
    private Map<Integer, Integer> colorMap;

    public AssetRecyclerViewAdapter(Context context, List<SimplifiedAsset> items, OnListFragmentInteractionListener listener) {
        this.items = items;
        this.listener = listener;
        this.context = context;

        buildStatusColorMap();
    }

    private void buildStatusColorMap() {
        this.colorMap = new HashMap<>();

        Database db = Database.getInstance(context);
        List<Status> statuses = db.getStatuses();

        for (Status status : statuses) {
            int statusID = status.getId();
            String colorString = status.getColor();
            Integer colorInt = null;
            try {
                colorInt = Color.parseColor(colorString);
            } catch (Exception e) {
                //if the color parsing fails just move on
            }

            colorMap.put(statusID, colorInt);
        }
    }

    @Override
    public AssetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AssetView view = new AssetView(parent.getContext(), null, null);

        return new AssetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AssetViewHolder holder, int position) {
        SimplifiedAsset asset = items.get(position);
        holder.item = asset;
        holder.view.setAsset(asset);
        Integer statusColor = colorMap.get(asset.getStatusID());
        holder.view.setHighlightColor(statusColor);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.

                    Pair<View, String> p1 = new Pair<View, String>(holder.view.getImage(), "image");
                    Pair<View, String> p2 = new Pair<View, String>(holder.view.getCard(), "card");

                    Activity activity = (Activity) holder.view.getContext();

                    View statusBarBackground = activity.findViewById(android.R.id.statusBarBackground);
                    Pair<View, String> p3 = null;
                    if (statusBarBackground != null) {
                        p3 = Pair.create(statusBarBackground,
                                Window.STATUS_BAR_BACKGROUND_TRANSITION_NAME);
                    }

                    View navBackground = activity.findViewById(android.R.id.navigationBarBackground);
                    Pair<View, String> p4 = null;
                    if (navBackground != null) {
                        p4 = Pair.create(navBackground,
                                Window.NAVIGATION_BAR_BACKGROUND_TRANSITION_NAME);
                    }

                    listener.onListFragmentInteraction(holder.item, p1, p2, p3, p4);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


}
