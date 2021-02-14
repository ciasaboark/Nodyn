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
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import androidx.cardview.widget.CardView;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.model.Manufacturer;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.helper.ColorHelper;


/**
 * Created by Jonathan Nelson on 10/8/17.
 */

public class ModelOverviewCountView extends RelativeLayout {
    private final Context context;
    private final int color;
    private View rootView;
    private Model model;
    private Manufacturer manufacturer;
    private int count;
    private View wrapper;
    private CardView card;
    private ImageView image;
    private TextView countText;
    private TextView modelText;
    private TextView manufacturerText;

    public ModelOverviewCountView(Context context, Model model, Manufacturer manufacturer, int count, int color) {
        super(context);
        this.model = model;
        this.manufacturer = manufacturer;
        this.count = count;
        this.color = color;
        this.context = context;
        init();
    }


    private void init() {
        rootView = inflate(context, R.layout.view_model_overview_count, this);
        wrapper = rootView.findViewById(R.id.wrapper);
        countText = rootView.findViewById(R.id.count);
        modelText = rootView.findViewById(R.id.model);
        manufacturerText = rootView.findViewById(R.id.manufacturer);
        image = rootView.findViewById(R.id.image);
        card = rootView.findViewById(R.id.card);

        setCount();
        setManufacturer();
        setModel();
        loadImage();
        setTextColor();
    }

//    private void setColor() {
//        card.setCardBackgroundColor(color);
//    }


    private void loadImage() {
        //image does not exist on the smaller layouts
        if (image != null) {
            String imageURL = model.getImage();
            //Picasso requires a non-empty path.  Just rely on the error handling
            if (imageURL == null || imageURL.equals("")) {
                imageURL = "foobar";
            }

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


            float borderWidth = getResources().getDimension(R.dimen.picasso_small_image_circle_border_width);

            Transformation transformation = new RoundedTransformationBuilder()
                    .borderColor(color)
                    .borderWidthDp(borderWidth)
                    .cornerRadiusDp(50)
                    .oval(false)
                    .build();

            Drawable d = getResources().getDrawable(R.drawable.monitor_cellphone_star);
            d.setTint(Color.parseColor("#ffffff"));

            Picasso.with(getContext())
                    .load(imageURL)
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
    }

    private void setCount() {
        this.countText.setText(String.valueOf(count));
    }

    private void setManufacturer() {
        this.manufacturerText.setText(manufacturer.getName());
    }

    private void setModel() {
        this.modelText.setText(model.getName());
    }

    private void setTextColor() {

//        int textColor = ColorHelper.getValueTextColorForBackground(getContext(), color);
        int textColor = color;
        countText.setTextColor(textColor);
        modelText.setTextColor(textColor);
        manufacturerText.setTextColor(textColor);
    }

}
