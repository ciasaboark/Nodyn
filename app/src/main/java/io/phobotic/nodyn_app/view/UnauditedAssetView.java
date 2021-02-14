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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.Nullable;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.model.Asset;

/**
 * Created by Jonathan Nelson on 1/9/18.
 */

public class UnauditedAssetView extends RelativeLayout {
    private static final String TAG = UnauditedAssetView.class.getSimpleName();
    private final Context context;
    private Asset asset;
    private View rootView;
    private TextView serial;
    private TextView model;
    private TextView tag;
    private ImageView image;
    private String modelName;

    public UnauditedAssetView(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public UnauditedAssetView(@NotNull Context context, AttributeSet attrs, @Nullable Asset asset) {
        super(context, attrs);
        this.context = context;
        this.asset = asset;
        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_unaudited_asset, this);
        tag = rootView.findViewById(R.id.tag);
        serial = rootView.findViewById(R.id.serial);
        model = rootView.findViewById(R.id.model);
        image = findViewById(R.id.image);

        setFields();
    }

    private void setFields() {
        if (!isInEditMode()) {
            if (asset != null) {
                setTextOrHide(tag, tag, asset.getTag());
                setTextOrHide(serial, serial, asset.getSerial());
                setTextOrHide(serial, model, modelName);

                loadImage();
            }
        }
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

//        float borderWidth = getResources().getDimension(R.dimen.picasso_tiny_image_circle_border_width);
//        Transformation transformation = new RoundedTransformationBuilder()
//                .borderColor(getResources().getColor(R.color.circleBorder))
//                .borderWidthDp(borderWidth)
//                .cornerRadiusDp(50)
//                .oval(false)
//                .build();

        Picasso.with(getContext())
                .load(image)
                .placeholder(R.drawable.ic_devices_other_black_48dp)
                .error(R.drawable.ic_important_devices_black_24dp)
                .into(this.image);
    }

    public UnauditedAssetView setModelName(String modelName) {
        this.modelName = modelName;
        setFields();
        return this;
    }

    public Asset getAuditRecord() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
        setFields();
    }
}
