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
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.recyclerview.widget.RecyclerView;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.audit.model.AuditDetail;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.view.AuditedAssetView;

/**
 * Created by Jonathan Nelson on 1/9/18.
 */

public class AuditedAssetRecyclerViewAdapter extends RecyclerView.Adapter<AuditedAssetRecyclerViewAdapter.ViewHolder> {
    private final List<AuditDetail> detailRecords;
    private final Context context;
    private final Database db;
    private OnAuditRemovedListener onAuditRemovedListener;
    private int lastPosition = -1;
    private Map<Integer, String> modelMap = new HashMap<>();

    public AuditedAssetRecyclerViewAdapter(Context context, List<AuditDetail> detailRecords) {
        this.context = context;
        this.db = Database.getInstance(context);
        this.detailRecords = detailRecords;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AuditedAssetView view = new AuditedAssetView(parent.getContext(), null, null);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.position = position;
        AuditDetail record = detailRecords.get(position);
        holder.record = record;
        AuditedAssetView auditedAssetView = (AuditedAssetView) holder.view;
        auditedAssetView.collapse(false);
        auditedAssetView.setDetailRecord(holder.record);

        try {
            Asset a = db.findAssetByID(holder.record.getAssetID());
            int modelID = a.getModelID();
            String modelName = modelMap.get(modelID);
            if (modelName == null) {
                try {
                    Model m = db.findModelByID(modelID);
                    modelName = m.getName();
                    modelMap.put(modelID, modelName);
                } catch (ModelNotFoundException e) {
                }
            }

            ((AuditedAssetView) holder.view).setModelName(modelName);
        } catch (Exception e) {

        }

        if (holder.deleteButton != null) {
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AuditedAssetView) holder.view).collapse(false);
                    removeAt(holder.position);
                }
            });
        }

        // Here you apply the animation when the view is bound
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return detailRecords.size();
    }

    public void removeAt(int position) {
        AuditDetail detailRecord = detailRecords.remove(position);
        notifyItemRemoved(position);
        try {
            Database db = Database.getInstance(context);
            Asset a = db.findAssetByID(detailRecord.getAssetID());
            Toast.makeText(context, "Moved asset " + detailRecord.getAssetID(), Toast.LENGTH_LONG).show();
        } catch (AssetNotFoundException e) {

        }

        if (onAuditRemovedListener != null) {
            onAuditRemovedListener.onAssetRemoved(detailRecord);
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition) {
            ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1, 0, 1);
            scaleAnimation.setDuration(500);
            viewToAnimate.startAnimation(scaleAnimation);
            lastPosition = position;
        }
    }

    public AuditedAssetRecyclerViewAdapter setOnAuditRemovedListener(OnAuditRemovedListener listener) {
        this.onAuditRemovedListener = listener;
        return this;
    }

    public interface OnAuditRemovedListener {
        void onAssetRemoved(@NotNull AuditDetail auditDetailRecord);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final Button deleteButton;
        public int position;
        public AuditDetail record;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.deleteButton = view.findViewById(R.id.delete_button);
        }
    }
}
