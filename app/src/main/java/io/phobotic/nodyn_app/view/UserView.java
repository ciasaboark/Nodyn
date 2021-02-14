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
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.Nullable;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.avatar.AvatarHelper;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.helper.GroupTableHelper;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 7/17/17.
 */

public class UserView extends LinearLayout {
    private static final String TAG = UserView.class.getSimpleName();
    private final Context context;
    private final Database db;
    private User user;
    private View rootView;
    private TextView name;
    private ImageView image;
    private TextView username;
    private View usernameBox;
    private TextView groups;
    private View groupsBox;
    private TextView numAssets;
    private View numAssetsBox;
    private TextView employeeNo;
    private View employeeNoBox;

    public UserView(Context context, @Nullable AttributeSet attrs, User user) {
        super(context, attrs);
        this.context = context;
        this.db = Database.getInstance(context);
        this.user = user;
        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_user, this);

        name = rootView.findViewById(R.id.model);
        username = rootView.findViewById(R.id.username);
        groups = rootView.findViewById(R.id.groups);
        numAssets = rootView.findViewById(R.id.num_assets);
        employeeNo = rootView.findViewById(R.id.employee_no);
        image = rootView.findViewById(R.id.image);
        setFields();
    }

    private void setFields() {
        if (!isInEditMode()) {
            if (user != null) {
                setTextOrHide(name, user.getName());
                setTextOrHide(username, user.getUsername());
                // TODO: 9/13/17 update this to use names instead of IDs
                setTextOrHide(groups, getGroupString());
                List<Asset> assetList = db.findAssetByUserID(user.getId());
                setTextOrHide(numAssets, String.valueOf(assetList.size()));
                setTextOrHide(employeeNo, user.getEmployeeNum());
                loadImage();
            }
        }
    }



    private void setTextOrHide(TextView tv, @Nullable String text) {
        tv.setText(text);
    }

    private String getGroupString() {
        return GroupTableHelper.getGroupString(user, db);
    }

    private void loadImage() {
        boolean avatarEnabled = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                context.getString(R.string.pref_key_users_enable_avatars), false);
        if (avatarEnabled) {
            AvatarHelper avatarHelper = new AvatarHelper();
            avatarHelper.loadAvater(getContext(), user, image, 90);
        }
    }


    public ImageView getImage() {
        return image;
    }

    public View getCard() {
        return rootView;
    }

    public void setUser(User user) {
        this.user = user;
        setFields();
    }


    public TextView getName() {
        return name;
    }
}
