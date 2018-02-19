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
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.audit.model.AuditDetailRecord;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.helper.ColorHelper;

/**
 * Created by Jonathan Nelson on 1/9/18.
 */

public class AuditedAssetView extends RelativeLayout {
    private static final String TAG = AuditedAssetView.class.getSimpleName();
    private final Context context;
    private AuditDetailRecord detailRecord;
    private View rootView;
    private TextView serial;
    private TextView model;
    private TextView tag;
    private View serialBox;
    private View modelBox;
    private ImageView image;
    private Button deleteButton;
    private boolean assetRemovable = true;
    private String modelName;
    private ImageButton editButton;
    private View footer;
    private RadioButton damagedRadioButton;
    private RadioButton undamagedRadioButton;
    private RadioButton unknownRadioButton;
    private RadioButton unexpectedRadioButton;
    private EditText notes;

    public AuditedAssetView(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public AuditedAssetView(@NotNull Context context, AttributeSet attrs, @Nullable AuditDetailRecord detalRecord) {
        super(context, attrs);
        this.context = context;
        this.detailRecord = detalRecord;
        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_audited_asset, this);
        tag = (TextView) rootView.findViewById(R.id.tag);

        serial = (TextView) rootView.findViewById(R.id.serial);
        serialBox = rootView.findViewById(R.id.serial_box);

        model = (TextView) rootView.findViewById(R.id.model);
        modelBox = rootView.findViewById(R.id.model_box);

        deleteButton = (Button) findViewById(R.id.delete_button);
        editButton = (ImageButton) findViewById(R.id.edit_button);
        notes = (EditText) findViewById(R.id.notes);
        footer = findViewById(R.id.footer);
        image = (ImageView) findViewById(R.id.image);

        damagedRadioButton = (RadioButton) findViewById(R.id.radio_state_damaged);
        undamagedRadioButton = (RadioButton) findViewById(R.id.radio_state_undamaged);
        unknownRadioButton = (RadioButton) findViewById(R.id.radio_state_unknown);
        unexpectedRadioButton = (RadioButton) findViewById(R.id.radio_state_unexpected);

        setFields();
        initButtons();
        initTextListener();
    }

    private void setFields() {
        if (!isInEditMode()) {
            unHideAllViews();

            if (detailRecord != null) {
                //make sure to clear out the text field
                notes.setText(detailRecord.getNotes());

                Database db = Database.getInstance(getContext());
                try {
                    Asset a = db.findAssetByID(detailRecord.getAssetID());
                    setTextOrHide(tag, tag, a.getTag());
                    setTextOrHide(serialBox, serial, a.getSerial());
                    setTextOrHide(modelBox, model, modelName);
                    loadImage(a);
                } catch (AssetNotFoundException e) {

                }

                setRadioButtonStatus();

                //restore the expanded/collapsed state of this view
                if (detailRecord.isExpanded()) {
                    expand(false);
                } else {
                    collapse(false);
                }
            }
        }
    }

    private void initButtons() {
        editButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!detailRecord.isExpanded()) {
                    expand(true);
                } else {
                    collapse(true);
                }
            }
        });


        damagedRadioButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(damagedRadioButton);
            }
        });

        undamagedRadioButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(undamagedRadioButton);
            }
        });

        unknownRadioButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(unknownRadioButton);
            }
        });

        unexpectedRadioButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(unexpectedRadioButton);
            }
        });
    }

    private void initTextListener() {
        notes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String auditNotes = s.toString();
                if (detailRecord != null) {
                    detailRecord.setNotes(auditNotes);
                }
            }
        });
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

    private void loadImage(Asset a) {
        String image = a.getImage();
        //Picasso requires a non-empty path.  Just rely on the error handling
        if (image == null || image.equals("")) {
            image = "foobar";
        }

        float borderWidth = getResources().getDimension(R.dimen.picasso_small_image_circle_border_width);
        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(getResources().getColor(android.R.color.white))
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

    private void setRadioButtonStatus() {
        undamagedRadioButton.setChecked(true);
        damagedRadioButton.setChecked(false);
        unknownRadioButton.setChecked(false);

        switch (detailRecord.getStatus()) {
            case DAMAGED:
                damagedRadioButton.setChecked(true);
                break;
            case UNDAMAGED:
                undamagedRadioButton.setChecked(true);
                break;
            case OTHER:
                unknownRadioButton.setChecked(true);
                break;
            case UNEXPECTED:
                unexpectedRadioButton.setChecked(true);
                break;
            default:
                undamagedRadioButton.setChecked(true);
        }
    }

    public void expand(boolean animate) {
        if (animate) {
            AnimationHelper.expand(footer);
            detailRecord.setExpanded(true);
        } else {
            if (footer.getVisibility() != View.VISIBLE) footer.setVisibility(View.VISIBLE);
        }

        editButton.getDrawable().setTint(ColorHelper.fetchAccentColor(getContext()));
    }

    public void collapse(boolean animate) {
        if (animate) {
            AnimationHelper.collapse(footer);
            detailRecord.setExpanded(false);
        } else {
            if (footer.getVisibility() != View.GONE) footer.setVisibility(View.GONE);
        }
        editButton.getDrawable().setTintList(null);
    }

    public void onRadioButtonClicked(RadioButton button) {
        // Is the button now checked?
        boolean checked = button.isChecked();

        // Check which radio button was clicked
        switch (button.getId()) {
            case R.id.radio_state_damaged:
                if (checked)
                    detailRecord.setStatus(AuditDetailRecord.Status.DAMAGED);
                break;
            case R.id.radio_state_undamaged:
                if (checked)
                    detailRecord.setStatus(AuditDetailRecord.Status.UNDAMAGED);
                break;
            case R.id.radio_state_unknown:
                if (checked)
                    detailRecord.setStatus(AuditDetailRecord.Status.OTHER);
                break;
            case R.id.radio_state_unexpected:
                if (checked)
                    detailRecord.setStatus(AuditDetailRecord.Status.UNEXPECTED);
                break;
        }
    }

    public AuditedAssetView setModelName(String modelName) {
        this.modelName = modelName;
        setFields();
        return this;
    }

    public AuditedAssetView setAssetRemovable(boolean assetRemovable) {
        this.assetRemovable = assetRemovable;
        setFields();
        return this;
    }

    public AuditDetailRecord getDetailRecord() {
        return detailRecord;
    }

    public AuditedAssetView setDetailRecord(AuditDetailRecord detailRecord) {
        this.detailRecord = detailRecord;
        setFields();
        return this;
    }
}
