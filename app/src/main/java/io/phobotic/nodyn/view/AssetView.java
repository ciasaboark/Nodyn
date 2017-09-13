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
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.model.Asset;

/**
 * Created by Jonathan Nelson on 7/14/17.
 */

public class AssetView extends ConstraintLayout {
    private static final String TAG = AssetView.class.getSimpleName();
    private final Context context;
    private Asset asset;
    private View rootView;
    private TextView name;
    private TextView serial;
    private TextView model;
    private TextView tag;
    private TextView status;
    private TextView user;
    private CardView card;
    private View nameBox;
    private View serialBox;
    private View modelBox;
    private View userBox;
    private TextView checkout;
    private View checkoutBox;
    private ImageView image;
    private Integer statusColor = null;
    private boolean isArchived = false;


    public AssetView(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public AssetView(@NotNull Context context, AttributeSet attrs, @Nullable Asset asset) {
        super(context, attrs);
        this.context = context;
        this.asset = asset;
        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_asset, this);
        card = (CardView) rootView.findViewById(R.id.card);

        tag = (TextView) rootView.findViewById(R.id.tag);

        name = (TextView) rootView.findViewById(R.id.name);
        nameBox = rootView.findViewById(R.id.name_box);

        serial = (TextView) rootView.findViewById(R.id.serial);
        serialBox = rootView.findViewById(R.id.serial_box);

        model = (TextView) rootView.findViewById(R.id.model);
        modelBox = rootView.findViewById(R.id.model_box);

        status = (TextView) rootView.findViewById(R.id.status);

        user = (TextView) rootView.findViewById(R.id.user);
        userBox = rootView.findViewById(R.id.user_box);

        checkout = (TextView) rootView.findViewById(R.id.checkout);
        checkoutBox = rootView.findViewById(R.id.checkout_box);

        image = (ImageView) rootView.findViewById(R.id.image);
        setFields();
    }

    private void setFields() {
        if (!isInEditMode()) {
            unHideAllViews();
            if (asset != null) {
                setTextOrHide(tag, tag, asset.getTag());
                setTextOrHide(nameBox, name, asset.getName());
                setTextOrHide(serialBox, serial, asset.getSerial());
                setTextOrHide(modelBox, model, asset.getModel());
                setTextOrHide(status, status, asset.getStatus());
                setTextOrHide(userBox, user, asset.getAssignedTo());
                setTextOrHide(checkoutBox, checkout, asset.getLastCheckout());
                loadImage();
            }
        }
    }

    private void unHideAllViews() {
        tag.setVisibility(View.VISIBLE);
        nameBox.setVisibility(View.VISIBLE);
        serialBox.setVisibility(View.VISIBLE);
        modelBox.setVisibility(View.VISIBLE);
        status.setVisibility(View.VISIBLE);
        userBox.setVisibility(View.VISIBLE);
        checkoutBox.setVisibility(View.VISIBLE);
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

        int circleColor = getResources().getColor(R.color.circleBorder);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useStatusColor = prefs.getBoolean(getResources().getString(
                R.string.pref_key_asset_status_color), Boolean.parseBoolean(
                getResources().getString(R.string.pref_default_asset_status_color)));

        if (statusColor != null && useStatusColor) {
            circleColor = statusColor;
        }

        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(circleColor)
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

    public ImageView getImage() {
        return image;
    }

    public View getCard() {
        return rootView;
    }

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
        setFields();
    }

    public void setStatusColor(Integer statusColor) {
        this.statusColor = statusColor;
        setFields();
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
        setFields();
    }
}
