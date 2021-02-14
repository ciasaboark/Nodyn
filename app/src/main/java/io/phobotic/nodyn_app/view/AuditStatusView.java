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

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Random;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.fragment.audit.AuditSelectStatusFragment.SelectableStatus;

/**
 * Created by Jonathan Nelson on 1/27/18.
 */

public class AuditStatusView extends RelativeLayout {
    private final View rootView;
    private TextView statusTextView;
    private ImageView colorStripe;
    private CheckBox checkbox;
    private SelectableStatus selectableStatus;

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

    public AuditStatusView(Context context) {
        this(context, null);
    }

    public AuditStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);

        rootView = inflate(context, R.layout.view_audit_status, this);
        init();
    }

    private void init() {
        statusTextView = (TextView) rootView.findViewById(R.id.status);
        colorStripe = (ImageView) rootView.findViewById(R.id.color);
        colorStripe.setImageTintList(null);
        colorStripe.setVisibility(View.VISIBLE);
        checkbox = (CheckBox) rootView.findViewById(R.id.check);
    }

    public void setStatus(SelectableStatus selectableStatus) {
        if (selectableStatus == null) {
            throw new IllegalArgumentException("Status can not be null");
        }

        this.selectableStatus = selectableStatus;
        setViewAndChildrenEnabled(rootView, selectableStatus.isEnabled());
        checkbox.setChecked(selectableStatus.isChecked());

        statusTextView.setText(selectableStatus.getStatus().getName());

        if (selectableStatus.getStatus().getColor() != null) {
            try {
                String colorString = selectableStatus.getStatus().getColor();
                int color = Color.parseColor(colorString);
                colorStripe.setVisibility(View.VISIBLE);
                setStatusColor(color);
            } catch (IllegalArgumentException e) {
                //just skip setting the color if it can't be parsed
                colorStripe.setVisibility(View.GONE);
            }
        } else {
            colorStripe.setVisibility(View.GONE);
        }
    }


    private void setStatusColor(int color) {
        colorStripe.getDrawable().setTintMode(PorterDuff.Mode.MULTIPLY);
        colorStripe.getDrawable().setTint(color);
    }
}
