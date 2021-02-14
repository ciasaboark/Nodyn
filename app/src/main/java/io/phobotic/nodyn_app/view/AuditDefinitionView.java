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
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.audit.model.AuditDefinition;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.exception.StatusNotFoundException;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.model.Status;
import io.phobotic.nodyn_app.helper.TextHelper;

/**
 * Created by Jonathan Nelson on 1/29/18.
 */

public class AuditDefinitionView extends RelativeLayout {
    private final View rootView;
    private AuditDefinition auditDefinition;
    private TextView name;
    private TextView description;
    private ImageButton deleteButton;
    private View background;
    private boolean deletable = true;
    private boolean highlighted = false;
    private TextView statuses;
    private LinearLayout modelHolder;
    private View datesWrapper;
    private TextView lastAuditDate;
    private TextView nextAuditDate;
    private TextView blind;
    private TextView meta;
    private MaterialCardView card;

    public AuditDefinitionView(Context context) {
        this(context, null);
    }

    public AuditDefinitionView(Context context, AttributeSet attrs) {
        super(context, attrs);

        rootView = inflate(context, R.layout.view_audit_definition, this);
        init();
    }

    private void init() {
        findViews();
    }

    private void findViews() {
        card = rootView.findViewById(R.id.card);
        name = (TextView) rootView.findViewById(R.id.name);
        description = (TextView) rootView.findViewById(R.id.description);
        statuses = (TextView) rootView.findViewById(R.id.statuses);
        deleteButton = (ImageButton) rootView.findViewById(R.id.delete_button);
        background = rootView.findViewById(R.id.background);
        modelHolder = (LinearLayout) rootView.findViewById(R.id.models);
        datesWrapper = rootView.findViewById(R.id.dates_wrapper);
        lastAuditDate = (TextView) rootView.findViewById(R.id.last_audit_date);
        nextAuditDate = (TextView) rootView.findViewById(R.id.next_audit_date);
        blind = (TextView) rootView.findViewById(R.id.blind);
        meta = (TextView) rootView.findViewById(R.id.meta);
    }

    public AuditDefinitionView setDeletable(boolean deletable) {
        this.deletable = deletable;
        setFields();
        return this;
    }

    private void setFields() {
        if (auditDefinition != null) {
            name.setText(auditDefinition.getName());
            String metaStatusText = getResources().getString(R.string.audit_name_meta_postfix);
            String metaStatus = auditDefinition.getMetaStatus();
            if (metaStatus == null) metaStatus = "";
            switch (metaStatus) {
                case "ALL":
                    metaStatus = "all assets";
                    break;
                case "ASSIGNED":
                    metaStatus = "only assigned assets";
                    break;
                case "UNASSIGNED":
                    metaStatus = "only unassigned assets";
                    break;
                default:
                    metaStatus = "all assets";
            }
            metaStatusText = String.format(metaStatusText, metaStatus);
            meta.setText(metaStatusText);

            TextHelper.setTextOrHide(description, description, auditDefinition.getDetails());
            showHideBlindLabel();
            showStatusLabels();
            showModelIcons();

            setDateFields();
        }

        if (deletable) {
            deleteButton.setVisibility(View.VISIBLE);
            datesWrapper.setVisibility(View.GONE);
        } else {
            deleteButton.setVisibility(View.GONE);
            datesWrapper.setVisibility(View.VISIBLE);
        }

        if (highlighted) {
            card.setStrokeColor(getResources().getColor(R.color.selected_background));
        } else {
            card.setStrokeColor(getResources().getColor(R.color.generic_card_border_dark));
        }
    }

    private void showHideBlindLabel() {
        if (auditDefinition.isBlindAudit()) {
            blind.setVisibility(View.VISIBLE);
        } else {
            blind.setVisibility(View.GONE);
        }
    }

    private void showStatusLabels() {
        Database db = Database.getInstance(getContext());
        StringBuilder sb = new StringBuilder();
        List<Status> statusList = new ArrayList<>();
        if (auditDefinition.isAuditAllStatuses()) {
            statusList = db.getStatuses();
        } else {
            for (int statusID : auditDefinition.getRequiredStatusIDs()) {
                try {
                    Status s = db.findStatusByID(statusID);
                    statusList.add(s);
                } catch (StatusNotFoundException e) {

                }
            }
        }

        String prefix = "";
        for (Status s : statusList) {
            sb.append(prefix + s.getName());
            prefix = ", ";
        }

        statuses.setText(sb.toString());
    }

    private void showModelIcons() {
        Database db = Database.getInstance(getContext());
        modelHolder.removeAllViews();
        Drawable defaultDrawable = getResources().getDrawable(R.drawable.circle_default_device);

        List<Model> modelList = new ArrayList<>();
        if (auditDefinition.isAuditAllModels()) {
            modelList = db.getModels();
        } else {
            for (int modelID : auditDefinition.getRequiredModelIDs()) {
                try {
                    Model m = db.findModelByID(modelID);
                    modelList.add(m);
                } catch (ModelNotFoundException e) {

                }
            }
        }

        for (Model m : modelList) {
            ImageView iv = new ImageView(getContext());
            iv.setImageDrawable(defaultDrawable);
            LayoutParams lp = new LayoutParams(42, 42);
            lp.setMarginEnd(8);
            iv.setLayoutParams(lp);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            modelHolder.addView(iv);
            loadImage(m, iv);
        }
    }

    private void setDateFields() {
        DateFormat df = SimpleDateFormat.getDateInstance();
        String lastAuditString = "Never";
        if (auditDefinition.getLastAuditTimestamp() != -1) {
            Date d = new Date(auditDefinition.getLastAuditTimestamp());
            lastAuditString = df.format(d);
        }

        lastAuditDate.setText(lastAuditString);

        nextAuditDate.setVisibility(View.GONE);
    }

    private void loadImage(Model m, ImageView iv) {
        String image = m.getImage();
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
                .placeholder(R.drawable.circle_default_device)
                .error(R.drawable.circle_default_device)
                .fit()
                .transform(transformation)
                .into(iv);
    }

    public void setAuditDefinition(AuditDefinition auditDefinition) {
        this.auditDefinition = auditDefinition;
        setFields();
    }

    public AuditDefinitionView setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        setFields();
        return this;
    }


}
