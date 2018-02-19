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

package io.phobotic.nodyn_app.list.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.view.UnauditedAssetView;

/**
 * Created by Jonathan Nelson on 1/11/18.
 */

public class UnauditedAssetRecyclerViewAdapter extends RecyclerView.Adapter<UnauditedAssetRecyclerViewAdapter.ViewHolder> {
    private final List<Asset> items;
    private final Context context;
    private final Database db;
    private OnAssetRemovedListener onAssetRemovedListener;
    private int lastPosition = -1;
    private Map<Integer, String> modelMap = new HashMap<>();

    public UnauditedAssetRecyclerViewAdapter(Context context, List<Asset> items) {
        this.context = context;
        this.db = Database.getInstance(context);
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        UnauditedAssetView view = new UnauditedAssetView(parent.getContext(), null, null);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.position = position;
        holder.item = items.get(position);
        ((UnauditedAssetView) holder.view).setAsset(holder.item);

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
        ((UnauditedAssetView) holder.view).setModelName(modelName);

        // Here you apply the animation when the view is bound
        setAnimation(holder.itemView, position);


    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
//            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
//            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public UnauditedAssetRecyclerViewAdapter setOnAssetRemovedListener(OnAssetRemovedListener listener) {
        this.onAssetRemovedListener = listener;
        return this;
    }

    public interface OnAssetRemovedListener {
        void onAssetRemoved(@NotNull Asset asset);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public int position;
        public Asset item;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
        }
    }
}
