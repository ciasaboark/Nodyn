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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.fragment.ShareSettingsChooserFragment;

/**
 * Created by Jonathan Nelson on 2/24/19.
 */
public class ShareTypeView extends RelativeLayout {
    private final View rootView;
    private CardView card;
    private ImageView icon;
    private TextView label;

    public ShareTypeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        rootView = inflate(context, R.layout.view_share_type, this);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
//        if (!isInEditMode()) {
        findViews();
        rootView.setOnTouchListener(null);

        rootView.setOnClickListener(null);


        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ShareTypeView);
            int cardColor = ta.getColor(R.styleable.ShareTypeView_card_color, -1);
            if (cardColor != -1) {
                card.setCardBackgroundColor(cardColor);
            }

            Drawable d = ta.getDrawable(R.styleable.ShareTypeView_icon);
            if (d != null) {
                icon.setImageDrawable(d);
            }

            String text = ta.getString(R.styleable.ShareTypeView_label);
            if (text != null) {
                label.setText(text);
            }

            ta.recycle();
        }
//        }
    }

    private void findViews() {
        card = rootView.findViewById(R.id.card);
        icon = rootView.findViewById(R.id.icon);
        label = rootView.findViewById(R.id.label);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        int squareLen = Math.min(width, height);
        int iconDimen = squareLen / 2;
        icon.getLayoutParams().height = iconDimen;
        icon.getLayoutParams().width = iconDimen;
        icon.requestLayout();

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(squareLen, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(squareLen, MeasureSpec.EXACTLY));
    }

    public void setMethod(final ShareSettingsChooserFragment.OnShareMethodChosenListener listener, final ShareSettingsChooserFragment.ShareMethod method) {
        card.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null && method != null) {
                    listener.onMethodChosen(method);
                }
            }
        });
    }
}
