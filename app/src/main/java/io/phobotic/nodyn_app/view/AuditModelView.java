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

package io.phobotic.nodyn_app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.ManufacturerNotFoundException;
import io.phobotic.nodyn_app.database.model.Manufacturer;
import io.phobotic.nodyn_app.fragment.audit.AuditSelectModelsFragment.SelectableModel;

/**
 * Created by Jonathan Nelson on 1/25/18.
 */

public class AuditModelView extends RelativeLayout {
    private static final String TAG = AuditModelView.class.getSimpleName();
    private SelectableModel model;
    private View rootView;
    private CheckBox checkbox;
    private TextView modelTextView;
    private TextView manufacturerTextView;
    private ImageView image;

    private static void setViewAndChildrenEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, enabled);
            }
        }
    }

    public AuditModelView(Context context) {
        this(context, null);
    }

    public AuditModelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        rootView = inflate(getContext(), R.layout.view_audit_model, this);
        init();
    }

    private void init() {
        findViews();
    }

    private void findViews() {
        modelTextView = (TextView) rootView.findViewById(R.id.model);
        manufacturerTextView = (TextView) rootView.findViewById(R.id.manufacturer);
        checkbox = (CheckBox) rootView.findViewById(R.id.check);
        image = (ImageView) rootView.findViewById(R.id.image);
    }

    private void setModelText() {
        modelTextView.setText(model.getModel().getName());
        try {
            Database db = Database.getInstance(getContext());
            Manufacturer manufacturer = db.findManufacturerByID(model.getModel().getManufacturerID());
            manufacturerTextView.setText(manufacturer.getName());
        } catch (ManufacturerNotFoundException e) {
            //nothing to do here
        }
    }

    public void reset() {
        modelTextView.setText(null);
        manufacturerTextView.setText(null);
        checkbox.setChecked(false);
    }

    public void setModel(SelectableModel selectableModel) {
        if (selectableModel == null) {
            throw new IllegalArgumentException("Model can not be null");
        }

        this.model = selectableModel;
        setViewAndChildrenEnabled(rootView, selectableModel.isEnabled());
        checkbox.setChecked(selectableModel.isChecked());
        setModelText();
        loadImage();
    }

    private void loadImage() {
        String image = model.getModel().getImage();
        //Picasso requires a non-empty path.  Just rely on the error handling
        if (image == null || image.equals("")) {
            image = "foobar";
        }

        int circleColor = getResources().getColor(R.color.circleBorder);

        float borderWidth = getResources().getDimension(R.dimen.picasso_tiny_image_circle_border_width);

        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(circleColor)
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

}
