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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.avatar.AvatarHelper;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.helper.GroupTableHelper;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.helper.AnimationHelper;

/**
 * Created by Jonathan Nelson on 8/29/17.
 */

public class UserCardView extends LinearLayout {
    private static final String TAG = UserCardView.class.getSimpleName();
    private final Context context;
    private boolean isMiniView = false;
    private Database db;
    private User user;
    private View rootView;
    private TextView name;
    private TextView username;
    private View usernameBox;
    private TextView groups;
    private View groupsBox;
    private TextView employeeNo;
    private View employeeNoBox;
    private ImageView image;
    private View placeholder;
    private View content;

    public UserCardView(Context context) {
        this(context, null);
    }

    public UserCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.UserCardView,
                0, 0);

        try {
            this.isMiniView = a.getBoolean(R.styleable.UserCardView_useMini, false);
        } finally {
            a.recycle();
        }
        init();
    }

    private void init() {
        if (isMiniView) {
            rootView = inflate(context, R.layout.view_user_card_mini, this);
        } else {
            rootView = inflate(context, R.layout.view_user_card, this);
        }
        if (!isInEditMode()) {
            this.db = Database.getInstance(context);
            name = rootView.findViewById(R.id.model);
            username = rootView.findViewById(R.id.username);
            usernameBox = rootView.findViewById(R.id.username_box);
            groups = rootView.findViewById(R.id.groups);
            groupsBox = rootView.findViewById(R.id.groups_box);
            employeeNo = rootView.findViewById(R.id.employee_no);
            employeeNoBox = rootView.findViewById(R.id.employee_no_box);
            image = rootView.findViewById(R.id.user_image);
            placeholder = rootView.findViewById(R.id.placeholder);
            content = rootView.findViewById(R.id.content);

            setFields();
        }
    }

    private void setFields() {
        if (!isInEditMode()) {
            unHideAllViews();
            if (user != null) {
                setTextOrHide(name, name, user.getName());
                setTextOrHide(usernameBox, username, user.getUsername());
                setTextOrHide(groupsBox, groups, getGroupString());
                setTextOrHide(employeeNoBox, employeeNo, user.getEmployeeNum());
                loadImage();
            } else {
                name.setText(null);
                username.setText(null);
                groups.setText(null);
                employeeNo.setText(null);
                Drawable d = getResources().getDrawable(R.drawable.circle_flat_grey, null);
                image.setImageDrawable(d);
            }
        }
    }

    public void reveal(User user) {
        this.user = user;
        setFields();
        //if we were given a non-null user we will need to fade out the placeholder view and fade in the text
        AnimationHelper.fadeOut(context, placeholder);
        AnimationHelper.fadeIn(context, content);
    }

    public void hideDetails() {
        AnimationHelper.fadeOut(context, content);
        AnimationHelper.fadeIn(context, placeholder);
        Animation anim = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //once the content has faded out we need to unset the content and remove the user image
                user = null;
                setFields();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void unHideAllViews() {
        name.setVisibility(View.VISIBLE);
        usernameBox.setVisibility(View.VISIBLE);
        groupsBox.setVisibility(View.VISIBLE);
        employeeNoBox.setVisibility(View.VISIBLE);
    }

    private void setTextOrHide(View view, TextView tv, @Nullable String text) {
        if (text == null || text.equals("")) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
    }

    private String getGroupString() {
        return GroupTableHelper.getGroupString(user, db);
    }

    private void loadImage() {
        AvatarHelper avatarHelper = new AvatarHelper();
        avatarHelper.loadAvater(getContext(), user, image, 120);
    }

//    public void setUser(User user) {
//        this.user = user;
//        setFields();
//    }

}
