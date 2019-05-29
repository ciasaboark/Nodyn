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

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.github.captain_miao.optroundcardview.OptRoundCardView;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn_app.view.ScannedAssetView;

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
    private final OnListFragmentInteractionListener listFragmentInteractionListener;
    private final boolean assetsRemoveable;
    private final Context context;
    private final Database db;
    private OnAssetListChangeListener assetListChangeListener;
    private int lastPosition = -1;
    private Map<Integer, String> modelMap = new HashMap<>();

    public ScannedAssetRecyclerViewAdapter(Context context, List<Asset> items, @Nullable
            OnListFragmentInteractionListener listFragmentInteractionListener,
                                           boolean assetsRemovable) {
        this.context = context;
        this.db = Database.getInstance(context);
        this.items = items;
        this.listFragmentInteractionListener = listFragmentInteractionListener;
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

        View card = holder.view.findViewById(R.id.card);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );

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

        if (card instanceof OptRoundCardView) {
            ((OptRoundCardView) card).showEdgeShadow(false, false, false, false);
        }
        card.setElevation(2.0f);

        int modelID = holder.item.getModelID();
        String modelName = modelMap.get(holder.item.getModelID());
        if (modelName == null) {
            try {
                Model m = db.findModelByID(modelID);
                modelName = m.getName();
            } catch (ModelNotFoundException e) {
            }
        }
        modelMap.put(modelID, modelName);
        ((ScannedAssetView) holder.view).setModelName(modelName);

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listFragmentInteractionListener != null) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    listFragmentInteractionListener.onListFragmentInteraction(holder.item, null);
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

        if (assetListChangeListener != null) {
            assetListChangeListener.onAssetListChange(items);
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    public ScannedAssetRecyclerViewAdapter setAssetListChangeListener(OnAssetListChangeListener assetListChangeListener) {
        this.assetListChangeListener = assetListChangeListener;
        return this;
    }

    public interface OnAssetListChangeListener {
        void onAssetListChange(@NotNull List<Asset> assets);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final ImageButton deleteButton;
        public int position;
        public Asset item;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.deleteButton = view.findViewById(R.id.delete_button);
        }
    }
}
