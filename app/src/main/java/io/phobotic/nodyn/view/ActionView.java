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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.exception.AssetNotFoundException;
import io.phobotic.nodyn.database.exception.UserNotFoundException;
import io.phobotic.nodyn.database.model.Action;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.User;

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
    private ImageView arrow;
    private ImageView assetImage;
    private ImageView userImage;
    private TextView timestamp;
    private ImageView authorizedImage;
    private ImageView verifiedImage;
    private ImageView syncedImage;
    private ImageView icon;

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
        icon = (ImageView) rootView.findViewById(R.id.icon);
        direction = (TextView) rootView.findViewById(R.id.direction);
        user = (TextView) rootView.findViewById(R.id.user);
        userImage = (ImageView) rootView.findViewById(R.id.user_image);

        asset = (TextView) rootView.findViewById(R.id.asset);
        assetImage = (ImageView) rootView.findViewById(R.id.asset_image);

        timestamp = (TextView) rootView.findViewById(R.id.time);
        arrow = (ImageView) rootView.findViewById(R.id.arrow);

        verifiedImage = (ImageView) rootView.findViewById(R.id.verified);
        authorizedImage = (ImageView) rootView.findViewById(R.id.authorized);
        syncedImage = (ImageView) rootView.findViewById(R.id.synced);

        setFields();
    }

    private void setFields() {
        if (!isInEditMode() && action != null) {
            direction.setText(action.getDirection().toString());
            switch (action.getDirection()) {
                case CHECKIN:
                    arrow.setImageResource(R.drawable.ic_arrow_left_grey600_24dp);
                    icon.setImageResource(R.drawable.ic_arrow_down_bold_hexagon_outline_black_24dp);
                    icon.setColorFilter(getResources().getColor(R.color.checkIn));
                    break;
                case CHECKOUT:
                    arrow.setImageResource(R.drawable.ic_arrow_right_grey600_24dp);
                    icon.setImageResource(R.drawable.ic_arrow_up_bold_hexagon_outline_black_24dp);
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

            DateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
            Date date = new Date(action.getTimestamp());
            String dateString = df.format(date);
            timestamp.setText(dateString);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void loadUserImage(User user) {
        String email = user.getEmail();
        String source = null;

        String hash = null;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean useGravitar = prefs.getBoolean("pref_gravitar", false);

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
                .into(userImage);
    }

    private void loadAssetImage(Asset asset) {
        String image = asset.getImage();
        //Picasso requires a non-empty path.  Just rely on the error handling
        if (image == null || image.equals("")) {
            image = "foobar";
        }

        Transformation transformation = new RoundedTransformationBuilder()
                .borderColor(getResources().getColor(R.color.circleBorder))
                .borderWidthDp(3)
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

    public void setAction(@NotNull Action action) {
        this.action = action;
        setFields();
    }
}
