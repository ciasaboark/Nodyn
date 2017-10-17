/*
 * Copyright (c) 2017 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.helper.GroupTableHelper;
import io.phobotic.nodyn.database.model.User;

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

        name = (TextView) rootView.findViewById(R.id.model);

        username = (TextView) rootView.findViewById(R.id.username);
        usernameBox = rootView.findViewById(R.id.username_box);

        groups = (TextView) rootView.findViewById(R.id.groups);
        groupsBox = rootView.findViewById(R.id.groups_box);

        numAssets = (TextView) rootView.findViewById(R.id.num_assets);
        numAssetsBox = rootView.findViewById(R.id.num_assets_box);

        employeeNo = (TextView) rootView.findViewById(R.id.employee_no);
        employeeNoBox = rootView.findViewById(R.id.employee_no_box);

        image = (ImageView) rootView.findViewById(R.id.image);
        setFields();
    }

    private void setFields() {
        if (!isInEditMode()) {
            unHideAllViews();
            if (user != null) {
                setTextOrHide(name, name, user.getName());
                setTextOrHide(usernameBox, username, user.getUsername());
                // TODO: 9/13/17 update this to use names instead of IDs
                setTextOrHide(groupsBox, groups, getGroupString());
                setTextOrHide(numAssetsBox, numAssets, String.valueOf(user.getNumAssets()));
                setTextOrHide(employeeNoBox, employeeNo, user.getEmployeeNum());
                loadImage();
            }
        }
    }

    private void unHideAllViews() {
        name.setVisibility(View.VISIBLE);
        usernameBox.setVisibility(View.VISIBLE);
        numAssetsBox.setVisibility(View.VISIBLE);
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void loadImage() {
        String email = user.getEmail();
        String source = null;

        String hash = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useGravitar = prefs.getBoolean(getResources().getString(
                R.string.pref_key_users_gravitar), false);

        if (email != null && !email.equals("") && useGravitar) {
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(StandardCharsets.UTF_8.encode(email));
                hash = String.format("%032x", new BigInteger(1, md5.digest()));
                source = "https://www.gravatar.com/avatar/" + hash + "?d=not_viable&s=100";
            } catch (Exception e) {

            }
        }

        //Picasso requires a non-empty path.  Just rely on the error handling
        if (source == null || source.equals("")) {
            source = "foobar";
        }

        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(getResources().getColor(R.color.circleBorder))
                .borderWidthDp(3)
                .cornerRadiusDp(50)
                .oval(false)
                .build();

        Picasso.with(getContext())
                .load(source)
                .fit()
                .placeholder(R.drawable.account)
                .error(R.drawable.account)
                .transform(transformation)
                .into(image);
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
