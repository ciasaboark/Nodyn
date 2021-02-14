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

package io.phobotic.nodyn_app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.Nullable;
import io.phobotic.nodyn_app.R;

/**
 * Created by Jonathan Nelson on 8/15/17.
 */

public class ScannedAssetView extends RelativeLayout {
    private static final String TAG = ScannedAssetView.class.getSimpleName();
    private final Context context;
    private AssetScannerView.ScannedAsset scannedAsset;
    private View rootView;
    private TextView serial;
    private TextView model;
    private TextView tag;
    private ImageView image;
    private ImageButton deleteButton;
    private ImageView checkIcon;
    private boolean assetRemovable = true;
    private String modelName;
    private MaterialCardView card;
    private View availabilityBox;
    private ProgressBar progress;
    private TextView availability;
    private ProgressBar progress_large;
    private ImageView errorIcon;

    public ScannedAssetView(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public ScannedAssetView(@NotNull Context context, AttributeSet attrs, @Nullable AssetScannerView.ScannedAsset scannedAsset) {
        super(context, attrs);
        this.context = context;
        this.scannedAsset = scannedAsset;
        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_scanned_asset, this);

        if (!isInEditMode()) {
            findViews();
            setFields();
        }
    }



    private void findViews() {
        card = rootView.findViewById(R.id.card);
        //asset specific views
        tag = rootView.findViewById(R.id.tag);
        serial = rootView.findViewById(R.id.serial);
        model = rootView.findViewById(R.id.model);
        image = findViewById(R.id.image);

        //the small area at the bottom to show the current status
        availabilityBox = rootView.findViewById(R.id.availability_box);
        availabilityBox.setVisibility(View.GONE);
        progress = rootView.findViewById(R.id.progress);
        progress_large = rootView.findViewById(R.id.progress_large);
        availability = rootView.findViewById(R.id.availability);

        //the larger icons to the right
        deleteButton = findViewById(R.id.delete_button);
        deleteButton.setVisibility(View.GONE);
        checkIcon = findViewById(R.id.check_button);
        checkIcon.setVisibility(View.GONE);
        errorIcon = findViewById(R.id.error_icon);
        errorIcon.setVisibility(View.GONE);

    }

    private void setFields() {
        if (scannedAsset != null) {
            tag.setText(scannedAsset.getAsset().getTag());
            serial.setText(scannedAsset.getAsset().getSerial() == null ? "No serial number" :
                    scannedAsset.getAsset().getSerial());
            model.setText(modelName == null ? "No model information" : modelName);
            loadImage();
        }


        if (!assetRemovable) {
            deleteButton.setVisibility(View.GONE);
            checkIcon.setVisibility(View.VISIBLE);
        } else {
            deleteButton.setVisibility(View.VISIBLE);
            checkIcon.setVisibility(View.GONE);
        }


    }

    private void loadImage() {
        String image = scannedAsset.getAsset().getImage();
        //Picasso requires a non-empty path.  Just rely on the error handling
        if (image == null || image.equals("")) {
            image = "foobar";
        }

        float borderWidth = getResources().getDimension(R.dimen.picasso_small_image_circle_border_width);
        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(getResources().getColor(R.color.circleBorder))
                .borderWidthDp(borderWidth)
                .cornerRadiusDp(50)
                .oval(false)
                .build();

        Picasso.with(getContext())
                .load(image)
                .placeholder(R.drawable.ic_important_devices_black_24dp)
                .error(R.drawable.ic_important_devices_black_24dp)
                .fit()
                .transform(transformation)
                .into(this.image);
    }

    public ScannedAssetView setModelName(String modelName) {
        this.modelName = modelName;
        setFields();
        return this;
    }

    public ScannedAssetView setAssetRemovable(boolean assetRemovable) {
        this.assetRemovable = assetRemovable;
        setFields();
        return this;
    }

    public ScannedAssetView showProgress() {
        this.progress_large.setVisibility(View.VISIBLE);
        this.checkIcon.setVisibility(View.GONE);
        this.deleteButton.setVisibility(View.GONE);
        this.errorIcon.setVisibility(View.GONE);
        return this;
    }

    public ScannedAssetView hideProgress() {
        this.progress_large.setVisibility(View.GONE);
        this.checkIcon.setVisibility(View.GONE);
        this.deleteButton.setVisibility(View.GONE);
        this.errorIcon.setVisibility(View.GONE);

        return this;
    }

    public ScannedAssetView showAvailability() {
        availabilityBox.setVisibility(View.VISIBLE);
        return this;
    }

    public ScannedAssetView hideAvailability() {
        availabilityBox.setVisibility(View.GONE);
        return this;
    }

    public ScannedAssetView showCheck() {
        this.progress_large.setVisibility(View.GONE);
        this.availabilityBox.setVisibility(View.GONE);
        this.checkIcon.setVisibility(View.VISIBLE);
        this.deleteButton.setVisibility(View.GONE);
        this.errorIcon.setVisibility(View.GONE);

        return this;
    }

    public ScannedAssetView hideCheck() {
        this.progress_large.setVisibility(View.GONE);
        this.checkIcon.setVisibility(View.GONE);
        this.deleteButton.setVisibility(View.GONE);
        this.errorIcon.setVisibility(View.GONE);

        return this;
    }

    public ScannedAssetView showError() {
        this.progress_large.setVisibility(View.GONE);
        this.checkIcon.setVisibility(View.GONE);
        this.deleteButton.setVisibility(View.GONE);
        this.errorIcon.setVisibility(View.VISIBLE);

        return this;
    }

    public ScannedAssetView hideError() {
        this.progress_large.setVisibility(View.GONE);
        this.checkIcon.setVisibility(View.GONE);
        this.deleteButton.setVisibility(View.GONE);
        this.errorIcon.setVisibility(View.GONE);

        return this;
    }

    public AssetScannerView.ScannedAsset getAsset() {
        return scannedAsset;
    }

    public void setAsset(AssetScannerView.ScannedAsset scannedAsset) {
        this.scannedAsset = scannedAsset;
        setFields();
    }

}
