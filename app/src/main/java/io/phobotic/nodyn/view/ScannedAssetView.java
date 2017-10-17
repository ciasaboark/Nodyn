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

package io.phobotic.nodyn.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.model.Asset;

/**
 * Created by Jonathan Nelson on 8/15/17.
 */

public class ScannedAssetView extends RelativeLayout {
    private static final String TAG = AssetView.class.getSimpleName();
    private final Context context;
    private Asset asset;
    private View rootView;
    private TextView serial;
    private TextView model;
    private TextView tag;
    private View serialBox;
    private View modelBox;
    private ImageView image;
    private ImageButton deleteButton;
    private ImageView checkButton;
    private boolean assetRemovable = true;
    private String modelName;

    public ScannedAssetView(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public ScannedAssetView(@NotNull Context context, AttributeSet attrs, @Nullable Asset asset) {
        super(context, attrs);
        this.context = context;
        this.asset = asset;
        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_scanned_asset, this);
        tag = (TextView) rootView.findViewById(R.id.tag);

        serial = (TextView) rootView.findViewById(R.id.serial);
        serialBox = rootView.findViewById(R.id.serial_box);

        model = (TextView) rootView.findViewById(R.id.model);
        modelBox = rootView.findViewById(R.id.model_box);

        deleteButton = (ImageButton) findViewById(R.id.delete_button);
        checkButton = (ImageView) findViewById(R.id.check_button);
        image = (ImageView) findViewById(R.id.image);

        setFields();
    }

    private void setFields() {
        if (!isInEditMode()) {
            unHideAllViews();
            if (asset != null) {
                setTextOrHide(tag, tag, asset.getTag());
                setTextOrHide(serialBox, serial, asset.getSerial());
                setTextOrHide(modelBox, model, modelName);
                loadImage();

                if (!assetRemovable) {
                    deleteButton.setVisibility(View.GONE);
                    checkButton.setVisibility(View.VISIBLE);
                } else {
                    deleteButton.setVisibility(View.VISIBLE);
                    checkButton.setVisibility(View.GONE);
                }
            }
        }
    }

    private void unHideAllViews() {
        tag.setVisibility(View.VISIBLE);
        serialBox.setVisibility(View.VISIBLE);
        modelBox.setVisibility(View.VISIBLE);
    }

    private void setTextOrHide(View view, TextView tv, @Nullable String text) {
        if (text == null || text.equals("")) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
    }

    private void loadImage() {
        String image = asset.getImage();
        //Picasso requires a non-empty path.  Just rely on the error handling
        if (image == null || image.equals("")) {
            image = "foobar";
        }

        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(getResources().getColor(R.color.circleBorder))
                .borderWidthDp(3)
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

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
        setFields();
    }
}
