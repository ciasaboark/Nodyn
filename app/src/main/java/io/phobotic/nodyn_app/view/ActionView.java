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
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.util.Date;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.avatar.AvatarHelper;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.Action;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.helper.AnimationHelper;

/**
 * Created by Jonathan Nelson on 8/20/17.
 */

public class ActionView extends RelativeLayout {
    private static final String TAG = ActionView.class.getSimpleName();
    private final Context context;
    private Action action;
    private View rootView;
    private TextView direction;
    private TextView user;
    private TextView asset;
    private ImageView assetImage;
    private ImageView userImage;
    private TextView timestamp;
    private ImageView authorizedImage;
    private ImageView verifiedImage;
    private ImageView syncedImage;
    private ImageView icon;
    private View collapse;
    private TextView notes;

    public ActionView(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public ActionView(@NotNull Context context, AttributeSet attrs, @Nullable Action action) {
        super(context, attrs);
        this.context = context;
        this.action = action;
        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_action, this);
        rootView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleCollapse();
            }
        });

        collapse = rootView.findViewById(R.id.collapse);
        icon = (ImageView) rootView.findViewById(R.id.icon);
        direction = (TextView) rootView.findViewById(R.id.direction);
        user = (TextView) rootView.findViewById(R.id.user);
        userImage = (ImageView) rootView.findViewById(R.id.user_image);

        asset = (TextView) rootView.findViewById(R.id.asset);
        assetImage = (ImageView) rootView.findViewById(R.id.asset_image);

        timestamp = (TextView) rootView.findViewById(R.id.time);

        verifiedImage = (ImageView) rootView.findViewById(R.id.verified);
        authorizedImage = (ImageView) rootView.findViewById(R.id.authorized);
        syncedImage = (ImageView) rootView.findViewById(R.id.synced);

        notes = (TextView) rootView.findViewById(R.id.notes);

        setFields();
    }

    private void toggleCollapse() {
        if (collapse.getVisibility() == View.VISIBLE) {
            AnimationHelper.collapse(collapse);
        } else {
            AnimationHelper.expand(collapse);
        }
    }

    private void setFields() {
        if (!isInEditMode() && action != null) {
            direction.setText(action.getDirection().toString());
            switch (action.getDirection()) {
                case CHECKIN:
                    icon.setImageResource(R.drawable.arrow_down_bold_hexagon_outline);
                    icon.setColorFilter(getResources().getColor(R.color.checkIn));
                    break;
                case CHECKOUT:
                    icon.setImageResource(R.drawable.arrow_up_bold_hexagon_outline);
                    icon.setColorFilter(getResources().getColor(R.color.checkOut));
                    break;
            }

            Database db = Database.getInstance(context);
            try {
                User u = db.findUserByID(action.getUserID());
                loadUserImage(u);
                user.setText(u.getName());
            } catch (UserNotFoundException e) {
                user.setText("Unknown user");
            }

            try {
                Asset a = db.findAssetByID(action.getAssetID());
                loadAssetImage(a);
                asset.setText(a.getTag());
            } catch (AssetNotFoundException e) {
                asset.setText("Unknown asset");
            }


            String authorizationString = action.getAuthorization();
            if (authorizationString == null || authorizationString.length() == 0) {
                authorizedImage.setVisibility(View.GONE);
            } else {
                authorizedImage.setVisibility(View.VISIBLE);
            }

            if (!action.isVerified()) {
                verifiedImage.setVisibility(View.GONE);
            } else {
                verifiedImage.setVisibility(View.VISIBLE);
            }

            if (!action.isSynced()) {
                syncedImage.setVisibility(View.GONE);
            } else {
                syncedImage.setVisibility(View.VISIBLE);
            }

            notes.setText(action.getNotes());

            DateFormat df = DateFormat.getDateTimeInstance();
            Date date = new Date(action.getTimestamp());
            String dateString = df.format(date);
            timestamp.setText(dateString);
        }
    }

    private void loadUserImage(User user) {
        AvatarHelper avatarHelper = new AvatarHelper();
        avatarHelper.loadAvater(getContext(), user, userImage, 48);
    }

    private void loadAssetImage(Asset asset) {
        String image = asset.getImage();
        //Picasso requires a non-empty path.  Just rely on the error handling
        if (image == null || image.equals("")) {
            image = "foobar";
        }

        float borderWidth = getResources().getDimension(R.dimen.picasso_small_image_circle_border_width);

        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(getResources().getColor(R.color.circleBorder))
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
                .into(this.assetImage);
    }

    public void fastCollapse() {
        collapse.setVisibility(View.GONE);
    }

    public void setAction(@NotNull Action action) {
        this.action = action;
        setFields();
    }
}
