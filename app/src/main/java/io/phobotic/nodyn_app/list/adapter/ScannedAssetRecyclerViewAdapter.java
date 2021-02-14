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
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.google.android.material.card.MaterialCardView;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn_app.helper.ExecutorHelper;
import io.phobotic.nodyn_app.sync.SyncManager;
import io.phobotic.nodyn_app.view.AssetScannerView;
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

    private final List<AssetScannerView.ScannedAsset> items;
    private final OnListFragmentInteractionListener listFragmentInteractionListener;
    private final boolean isAssetsRemoveable;
    private final boolean isCheckAssetAvailability;
    private final Context context;
    private final Database db;
    private OnAssetListChangeListener assetListChangeListener;
    private int lastPosition = -1;
    private Map<Integer, String> modelMap = new HashMap<>();
    private @ColorInt
    int normalBackground;
    private @ColorInt int availabeColor;
    private @ColorInt int unavailableColor;
    private @ColorInt int unknownColor;
    private @ColorInt int checkingColor;

    public ScannedAssetRecyclerViewAdapter(Context context, List<AssetScannerView.ScannedAsset> items, @Nullable
            OnListFragmentInteractionListener listFragmentInteractionListener,
                                           boolean isAssetsRemoveable,
                                           boolean isCheckAssetAvailability) {
        this.context = context;
        this.db = Database.getInstance(context);
        this.items = items;
        this.listFragmentInteractionListener = listFragmentInteractionListener;
        this.isAssetsRemoveable = isAssetsRemoveable;
        this.isCheckAssetAvailability = isCheckAssetAvailability;
        initializeColors();
    }

    private void initializeColors() {
        //use the default card background color if the status is UNDEFINED
        TypedValue tv = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.card_background_color, tv, true);
        this.normalBackground = tv.data;

        this.availabeColor = context.getResources().getColor(R.color.asset_status_available);
        this.unavailableColor = context.getResources().getColor(R.color.asset_status_unavailable);
        this.unknownColor = context.getResources().getColor(R.color.asset_status_unknown);
        this.checkingColor = context.getResources().getColor(R.color.asset_status_checking);
    }

    private @ColorInt int getStatusColor(AssetScannerView.ScannedAsset scannedAsset) {
        int statusColor = normalBackground;

        switch (scannedAsset.getAvailability()) {
            case AVAILABLE:
                statusColor = availabeColor;
                break;
            case NOT_AVAILABLE:
                statusColor = unavailableColor;
                break;
            case UNKNOWN:
                statusColor = unknownColor;
                break;
            case CHECKING:
                statusColor = checkingColor;
                break;
            case UNDEFINED:
                statusColor = normalBackground;
                break;
        }

        return statusColor;
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
        ((ScannedAssetView) holder.view).setAssetRemovable(isAssetsRemoveable);

        int modelID = holder.item.getAsset().getModelID();
        String modelName = modelMap.get(modelID);
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
                    listFragmentInteractionListener.onListFragmentInteraction(holder.item.getAsset(), null);
                }
            }
        });

        View availabliltyBox = holder.view.findViewById(R.id.availability_box);
        if (isCheckAssetAvailability) {
            availabliltyBox.setVisibility(View.VISIBLE);
            AssetScannerView.AVAILABILITY availability = holder.item.getAvailability();
            ImageView availabilityIV = holder.view.findViewById(R.id.availability_icon);
            availabilityIV.setVisibility(View.GONE);

            String statusString = null;
            switch (availability) {
                case UNKNOWN:
                    statusString = "Unknown status";
                    availabilityIV.setVisibility(View.VISIBLE);
                    availabilityIV.setImageDrawable(context.getDrawable(R.drawable.cloud_question));
                    availabilityIV.setColorFilter(context.getResources().getColor(R.color.white));
                    break;
                case AVAILABLE:
                    statusString = "Confirmed available!";
                    availabilityIV.setVisibility(View.VISIBLE);
                    availabilityIV.setImageDrawable(context.getDrawable(R.drawable.check));
                    availabilityIV.setColorFilter(context.getResources().getColor(R.color.success));
                    break;
                case CHECKING:
                    statusString = "Checking availablility...";
                    break;
                case NOT_AVAILABLE:
                    availabilityIV.setVisibility(View.VISIBLE);
                    availabilityIV.setImageDrawable(context.getDrawable(R.drawable.close));
                    availabilityIV.setColorFilter(context.getResources().getColor(R.color.warning_strong));
                    statusString = "Asset not available!";
                    break;
            }
            TextView availabilityText = holder.view.findViewById(R.id.availability);
            availabilityText.setText(statusString);

            TextView optionalMessage = holder.view.findViewById(R.id.message);
            String message = holder.item.getMessage();
            if (message == null) {
                optionalMessage.setVisibility(View.GONE);
            } else {
                optionalMessage.setVisibility(View.VISIBLE);
                optionalMessage.setText(message);
            }

            View progress = holder.view.findViewById(R.id.progress);
            if (holder.item.isCheckInProgress()) {
                progress.setVisibility(View.VISIBLE);
            } else {
                progress.setVisibility(View.GONE);
            }
        } else {
            availabliltyBox.setVisibility(View.GONE);
        }

        if (holder.deleteButton != null) {
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeAt(holder.position);
                }
            });
        }

        @ColorInt int statusColor = getStatusColor(holder.item);
        MaterialCardView card = holder.view.findViewById(R.id.card);
        card.setStrokeColor(statusColor);


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
        void onAssetListChange(@NotNull List<AssetScannerView.ScannedAsset> assets);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final ImageButton deleteButton;
        public int position;
        public AssetScannerView.ScannedAsset item;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            this.deleteButton = view.findViewById(R.id.delete_button);
        }
    }
}
