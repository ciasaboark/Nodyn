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
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.helper.ColorHelper;

/**
 * Created by Jonathan Nelson on 3/31/19.
 */
public class AssetCountView extends LinearLayout {
    private View rootView;
    private CardView card;
    private TextSwitcher label;
    private TextSwitcher count;
    private String labelText;
    private String countText;
    @ColorInt
    private int defaultColor;
    @ColorInt
    private int cardColor;
    private ImageView icon;


    public AssetCountView(Context context) {
        this(context, null);
    }

    public AssetCountView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        this.rootView = inflate(getContext(), R.layout.view_asset_count, this);
        findViews();
        this.defaultColor = getResources().getColor(R.color.white);
        this.cardColor = defaultColor;


        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.AssetCountView);
            int c = ta.getColor(R.styleable.AssetCountView_card_background_color, -1);
            if (c != -1) {
                this.cardColor = c;
            }

            int drawableRes = ta.getResourceId(R.styleable.AssetCountView_card_icon, -1);
            if (drawableRes != -1) {
                icon.setImageDrawable(getResources().getDrawable(drawableRes, null));
            }


            Integer i = ta.getInt(R.styleable.AssetCountView_asset_count, 0);
            countText = String.valueOf(i);

            String text = ta.getString(R.styleable.AssetCountView_card_label);
            labelText = text;

            ta.recycle();
        }

        initTextSwitchers();

        updateViews();
    }

    private void findViews() {
        card = rootView.findViewById(R.id.card);
        label = rootView.findViewById(R.id.label);
        count = rootView.findViewById(R.id.count);
        icon = rootView.findViewById(R.id.icon);
    }

    private void initTextSwitchers() {
        Animation inAnimation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        Animation outAnimation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);

        label.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(getContext());
                t.setTextAppearance(getContext(), android.R.style.TextAppearance_Material);
                t.setTypeface(null, Typeface.BOLD);
                int textColor = ColorHelper.getValueTextColorForBackground(getContext(), getCardColor());
                t.setTextColor(textColor);
                return t;
            }
        });
        label.setCurrentText(getResources().getString(R.string.blank));
        label.setInAnimation(inAnimation);
        label.setOutAnimation(outAnimation);


        count.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(getContext());
                t.setTextAppearance(getContext(), android.R.style.TextAppearance_Material);
                t.setTextSize(40.0f);
                t.setTypeface(null, Typeface.BOLD);
                int textColor = ColorHelper.getValueTextColorForBackground(getContext(), getCardColor());
                t.setTextColor(textColor);
                return t;
            }
        });
        count.setCurrentText(getResources().getString(R.string.blank));
        count.setInAnimation(inAnimation);
        count.setOutAnimation(outAnimation);
    }

    private void updateViews() {
        label.setText(labelText);
        ((TextView)label.getCurrentView()).setTextColor(cardColor);
        count.setText(countText);
        ((TextView)count.getCurrentView()).setTextColor(cardColor);
        icon.setColorFilter(cardColor);

    }

    private int getCardColor() {
        return cardColor;
    }

    public void setCardColor(@ColorInt int color) {
        this.cardColor = color;
        updateViews();
    }

    public AssetCountView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void setCount(int count) {
        countText = String.valueOf(count);
        updateViews();
    }

    public void setLabel(String label) {
        labelText = label;
        updateViews();
    }

}
