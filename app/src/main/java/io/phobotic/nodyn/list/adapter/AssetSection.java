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

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.Status;
import io.phobotic.nodyn.fragment.SimplifiedAsset;
import io.phobotic.nodyn.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn.list.viewholder.AssetHeaderViewHolder;
import io.phobotic.nodyn.list.viewholder.AssetViewHolder;
import io.phobotic.nodyn.view.AssetView;

/**
 * Created by Jonathan Nelson on 9/9/17.
 */

public class AssetSection extends StatelessSection {
    private final List<SimplifiedAsset> items;
    private final OnListFragmentInteractionListener listener;
    private final Context context;
    private final String manufacturer;
    private final String model;
    private final Database db;
    private Map<Integer, Integer> colorMap;
    private Set<String> archivedSet;

    public AssetSection(Context context, String manufacturer, String model, List<SimplifiedAsset> items,
                        OnListFragmentInteractionListener listener) {
        // call constructor with layout resources for this Section header and items
        super(new SectionParameters.Builder(R.layout.view_asset_wrapper)
                .headerResourceId(R.layout.view_asset_header)
                .build());

        this.context = context;
        this.db = Database.getInstance(context);
        this.manufacturer = manufacturer;
        this.model = model;
        if (items == null) items = new ArrayList<>();
        this.items = items;
        this.listener = listener;

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
    public int getContentItemsTotal() {
        return items.size(); // number of items of this section
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new AssetHeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        AssetHeaderViewHolder headerHolder = (AssetHeaderViewHolder) holder;

        headerHolder.manufacturer.setText(manufacturer);
        headerHolder.model.setText(model);
        headerHolder.countAvailable.setText(String.format(context.getResources()
                .getString(R.string.asset_available_count), getAvailableCount()));
        headerHolder.countAssigned.setText(String.format(context.getResources()
                .getString(R.string.asset_assigned_count), getAssignedCount()));
    }

    /**
     * Returns the count of assets that have no current assignment.
     * Note that this count does not take into account the asset's Status
     */
    private int getAvailableCount() {
        int available = 0;

        for (Asset asset : items) {
            if (asset.getAssignedToID() == -1) {
                available++;
            }
        }

        return available;
    }

    /**
     * Returns the count of assets that have been assigned to a user.
     * Note that this count does not take into account the asset's Status
     */
    private int getAssignedCount() {
        int assigned = 0;

        for (Asset asset : items) {
            if (asset.getAssignedToID() != -1) {
                assigned++;
            }
        }

        return assigned;
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        // return a custom instance of ViewHolder for the items of this section
        return new AssetViewHolder((AssetView) view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        final AssetViewHolder assetViewHolder = (AssetViewHolder) holder;
        SimplifiedAsset asset = items.get(position);
        assetViewHolder.item = asset;
        assetViewHolder.view.setAsset(asset);
        Integer statusColor = colorMap.get(asset.getStatusID());
        assetViewHolder.view.setHighlightColor(statusColor);

        assetViewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.

                    Pair<View, String> p1 = new Pair<View, String>(assetViewHolder.view.getImage(), "image");
                    Pair<View, String> p2 = new Pair<View, String>(assetViewHolder.view.getCard(), "card");

                    Activity activity = (Activity) assetViewHolder.view.getContext();

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

                    listener.onListFragmentInteraction(assetViewHolder.item, p1, p2, p3, p4);
                }
            }
        });
    }
}