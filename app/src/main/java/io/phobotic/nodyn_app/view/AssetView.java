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
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Date;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.StatusNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.fragment.SimplifiedAsset;

/**
 * Created by Jonathan Nelson on 7/14/17.
 */

public class AssetView extends ConstraintLayout {
    private static final String TAG = AssetView.class.getSimpleName();
    private final Context context;
    private Database db;
    private SimplifiedAsset asset;
    private View rootView;
    private TextView name;
    private TextView serial;
    private TextView model;
    private TextView tag;
    private TextView status;
    private TextView user;
    private View nameBox;
    private View serialBox;
    private View modelBox;
    private View userBox;
    private TextView checkout;
    private View checkoutBox;
    private ImageView image;
    private Integer highlightColor = null;
    private boolean isArchived = false;
    private View card;
    private View statusBar;


    public AssetView(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public AssetView(@NotNull Context context, AttributeSet attrs, @Nullable SimplifiedAsset asset) {
        super(context, attrs);
        this.context = context;
        this.db = Database.getInstance(context);
        this.asset = asset;
        init();
    }

    private void init() {
        findViews();
        setFields();
    }

    private void findViews() {
        rootView = inflate(context, R.layout.view_asset, this);
        statusBar = rootView.findViewById(R.id.status_color);
        card = rootView.findViewById(R.id.card);
        tag = rootView.findViewById(R.id.tag);
        name = rootView.findViewById(R.id.name);
        nameBox = rootView.findViewById(R.id.name_box);
        serial = rootView.findViewById(R.id.serial);
        serialBox = rootView.findViewById(R.id.serial_box);
        model = rootView.findViewById(R.id.model_name);
        modelBox = rootView.findViewById(R.id.model_name_box);
        status = rootView.findViewById(R.id.status);
        user = rootView.findViewById(R.id.user);
        userBox = rootView.findViewById(R.id.user_box);
        checkout = rootView.findViewById(R.id.checkout);
        checkoutBox = rootView.findViewById(R.id.checkout_box);
        image = rootView.findViewById(R.id.image);
    }

    public void setBackdropColor(@ColorInt int color) {
        statusBar.setBackgroundColor(color);
        statusBar.setVisibility(View.VISIBLE);
    }

    public void hideBackdropColor() {
        statusBar.setVisibility(View.GONE);
    }

    private void setFields() {
        if (!isInEditMode()) {
            unHideAllViews();
            if (asset != null) {
                // TODO: 9/13/17 update to use names instead of IDs
                setTextOrHide(tag, tag, asset.getTag());
                setTextOrHide(nameBox, name, asset.getName());
                setTextOrHide(serialBox, serial, asset.getSerial());
                setTextOrHide(modelBox, model, asset.getModelName());
                setTextOrHide(status, status, asset.getStatusName());
                setTextOrHide(userBox, user, asset.getAssignedToName());

                String lastCheckout = null;
                if (asset.getLastCheckout() != -1) {
                    Date d = new Date(asset.getLastCheckout());
                    DateFormat df = DateFormat.getDateTimeInstance();
                    lastCheckout = df.format(d);
                }
                setTextOrHide(checkoutBox, checkout, lastCheckout);
                loadImage();
            }
        }
    }

    private void unHideAllViews() {
        if (tag != null) tag.setVisibility(View.VISIBLE);
        if (nameBox != null) nameBox.setVisibility(View.VISIBLE);
        if (serialBox != null) serialBox.setVisibility(View.VISIBLE);
        if (modelBox != null) modelBox.setVisibility(View.VISIBLE);
        if (status != null) status.setVisibility(View.VISIBLE);
        if (userBox != null) userBox.setVisibility(View.VISIBLE);
        if (checkoutBox != null) checkoutBox.setVisibility(View.VISIBLE);
    }

    private void setTextOrHide(View view, TextView tv, @Nullable String text) {
        if (tv == null) {
            return;
        }

        if (text == null || text.equals("")) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
    }

    private void loadImage() {
        String imageURL = asset.getImage();
        //Picasso requires a non-empty path.  Just rely on the error handling
        if (imageURL == null || imageURL.equals("")) {
            imageURL = "foobar";
        }

        int circleColor = getResources().getColor(R.color.circleBorder);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useStatusColor = prefs.getBoolean(getResources().getString(
                R.string.pref_key_asset_status_color), Boolean.parseBoolean(
                getResources().getString(R.string.pref_default_asset_status_color)));

        if (highlightColor != null && useStatusColor) {
            circleColor = highlightColor;
        }

        float borderWidth = getResources().getDimension(R.dimen.picasso_small_image_circle_border_width);
        float cornerRadius = getContext().getResources().getDimension(R.dimen.view_asset_hero_size) / 2;
        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(circleColor)
                .borderWidthDp(borderWidth)
                .cornerRadiusDp(cornerRadius)
                .oval(false)
                .build();

        Drawable d = getResources().getDrawable(R.drawable.monitor_cellphone_star);
        d.setTint(Color.parseColor("#ffffff"));

        Picasso.with(getContext())
                .load(imageURL)
                .transform(new SolidBackgroundTransform())
                .transform(transformation)
                .fit()
                .placeholder(d)
                .into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                        //remove the image tint
                        image.setColorFilter(null);
                    }

                    @Override
                    public void onError() {
                        //nothing to do here
                    }
                });
    }

    private String getHighlightColor() {
        String color = null;
        try {
            Status s = db.findStatusByID(asset.getStatusID());
            color = s.getColor();
        } catch (StatusNotFoundException e) {
        }

        return color;
    }

    public void setHighlightColor(Integer highlightColor) {
        this.highlightColor = highlightColor;
        setFields();
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

    public AssetView setAsset(SimplifiedAsset asset) {
        this.asset = asset;
        setFields();
        return this;
    }

    public void setArchived(boolean archived) {
        isArchived = archived;
        setFields();
    }

    private class SolidBackgroundTransform implements Transformation {

        @Override
        public Bitmap transform(Bitmap source) {
            //overlay the image onto a white background so we don't have to deal
            //+ with transparent pixels

            Bitmap newBitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(),
                    source.getConfig());
            newBitmap.eraseColor(Color.WHITE);
            Canvas canvas = new Canvas(newBitmap);
            canvas.drawBitmap(source, 0f, 0f, null);


            //recycle the old bitmap
            if (!source.isRecycled()) {
                source.recycle();
            }

            return newBitmap;
        }

        @Override
        public String key() {
            return SolidBackgroundTransform.class.getName();
        }
    }
}
