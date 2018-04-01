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

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.avatar.AvatarProvider;

/**
 * Created by Jonathan Nelson on 3/21/18.
 */

public class AvatarProviderView extends RelativeLayout {
    private static final String TAG = AvatarProviderView.class.getSimpleName();
    private final View rootView;
    private AvatarProvider avatarProvider;
    private TextView title;
    private TextView universal;
    private CheckBox checkBox;
    private ImageView dragHandle;
    private ImageView icon;
    private ImageButton configure;


    public AvatarProviderView(Context context) {
        this(context, null);
    }

    public AvatarProviderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AvatarProviderView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public AvatarProviderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        rootView = inflate(context, R.layout.view_avatar_provider, this);
        init();
    }

    private void init() {
        findViews();
        setFields();
    }

    private void findViews() {
        title = (TextView) rootView.findViewById(R.id.title);
        universal = (TextView) rootView.findViewById(R.id.universal);
        checkBox = (CheckBox) rootView.findViewById(R.id.checkbox);
        dragHandle = (ImageView) rootView.findViewById(R.id.drag_handle);
        icon = (ImageView) rootView.findViewById(R.id.icon);
        configure = (ImageButton) rootView.findViewById(R.id.configure);
    }

    private void setFields() {
        universal.setVisibility(View.GONE);

        if (!isInEditMode() && avatarProvider != null) {
            title.setText(avatarProvider.getName());
            if (avatarProvider.isUniversal()) {
                universal.setVisibility(View.VISIBLE);
            } else {
                universal.setVisibility(View.GONE);
            }
            Drawable d = avatarProvider.getIconDrawable(getContext());
            icon.setImageDrawable(d);
            if (d == null) {
                icon.setVisibility(View.GONE);
            } else {
                icon.setVisibility(View.VISIBLE);
            }

            if (avatarProvider.getConfigurationDialogFragment(getContext()) == null) {
                configure.setVisibility(View.GONE);
            } else {
                configure.setVisibility(View.VISIBLE);
                configure.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Context context = getContext();
                        if (context instanceof FragmentActivity) {
                            FragmentManager fm = ((FragmentActivity) context).getSupportFragmentManager();
                            avatarProvider.getConfigurationDialogFragment(getContext()).show(fm, "avatar-provider-config");
                        }
                    }
                });
            }
        }
    }

    public AvatarProvider getAvatarProvider() {
        return avatarProvider;
    }

    public AvatarProviderView setAvatarProvider(AvatarProvider avatarProvider) {
        this.avatarProvider = avatarProvider;
        setFields();
        return this;
    }
}

