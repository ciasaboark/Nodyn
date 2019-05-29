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
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;

import com.github.captain_miao.optroundcardview.OptRoundCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.annotation.ColorInt;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.fragment.SimplifiedAsset;
import io.phobotic.nodyn_app.fragment.asset.AssetStatusListFragment;
import io.phobotic.nodyn_app.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn_app.helper.ColorHelper;
import io.phobotic.nodyn_app.list.viewholder.AssetStatusHeaderViewHolder;
import io.phobotic.nodyn_app.list.viewholder.AssetViewHolder;
import io.phobotic.nodyn_app.view.AssetView;

/**
 * Created by Jonathan Nelson on 9/9/17.
 */

public class AssetStatusSection extends StatelessSection {
    private final List<SimplifiedAsset> items;
    private final OnListFragmentInteractionListener listener;
    private final Context context;
    private final String status;
    private final int total;
    private final Database db;
    private final Integer highlightColor;
    //    private Map<Integer, Integer> colorMap;
    private Set<String> archivedSet;

    public AssetStatusSection(Context context, String status, int total, List<SimplifiedAsset> items,
                              @ColorInt Integer highlightColor,
                              OnListFragmentInteractionListener listener) {
        // call constructor with layout resources for this Section header and items
        super(new SectionParameters.Builder(R.layout.view_asset_wrapper)
                .headerResourceId(R.layout.view_asset_status_header)
                .build());

        this.context = context;
        this.db = Database.getInstance(context);
        this.status = status;
        this.total = total;
        this.highlightColor = highlightColor;

        if (items == null) items = new ArrayList<>();
        this.items = items;
        this.listener = listener;
    }


    @Override
    public int getContentItemsTotal() {
        return items.size(); // number of items of this section
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new AssetStatusHeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        AssetStatusHeaderViewHolder headerHolder = (AssetStatusHeaderViewHolder) holder;
        Drawable d = null;
        switch (status) {
            case AssetStatusListFragment.STATUS_PAST_DUE:
                d = context.getResources().getDrawable(R.drawable.calendar_clock);
                break;
            case AssetStatusListFragment.STATUS_OUT:
                d = context.getResources().getDrawable(R.drawable.arrow_up_bold_hexagon_outline);
                break;
            case AssetStatusListFragment.STATUS_AVAILABLE:
                d = context.getResources().getDrawable(R.drawable.monitor_cellphone_star);
                break;
        }


        headerHolder.icon.setImageDrawable(d);
        headerHolder.status.setText(status);
        headerHolder.countTotal.setText(String.format("%d total", total));

        if (highlightColor != null) {
            headerHolder.card.setCardBackgroundColor(highlightColor);
            int textColor = ColorHelper.getValueTextColorForBackground(context, highlightColor);
            headerHolder.status.setTextColor(textColor);
            headerHolder.countTotal.setTextColor(textColor);
            d.setTint(textColor);
        }
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

        View card = ((AssetViewHolder) holder).view.findViewById(R.id.card);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

        if (highlightColor != null) {
            assetViewHolder.view.setBackdropColor(highlightColor);
        }

        int horzMargin = context.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        int topMargin = 0;
        int bottomMargin = 0;
        //if this is the only card then show all corners.
        if (items.size() == 1) {
            if (card instanceof OptRoundCardView) {
                ((OptRoundCardView) card).showCorner(true, true, true, true);
                topMargin = horzMargin;
                bottomMargin = horzMargin;
            }
        } else {
            //otherwise show corners only on the top of the first card and the bottom of the last
            if (position == 0) {
                if (card instanceof OptRoundCardView) {
                    ((OptRoundCardView) card).showCorner(true, true, false, false);
                    topMargin = horzMargin;
                }
            } else if (position == items.size() - 1) {
                //if this is the
                if (card instanceof OptRoundCardView) {
                    ((OptRoundCardView) card).showCorner(false, false, true, true);
                    bottomMargin = horzMargin;
                }
            } else {
                if (card instanceof OptRoundCardView) {
                    ((OptRoundCardView) card).showCorner(false, false, false, false);
                }
            }
        }


        params.setMargins(horzMargin, topMargin, horzMargin, bottomMargin);
        card.setLayoutParams(params);

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
}