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

package io.phobotic.nodyn_app.avatar;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 3/19/18.
 */

public class GravitarProvider extends AvatarProvider {
    private static String SOURCE = "https://www.gravatar.com/avatar/%s?d=not_viable&s=%d";

    @Override
    public String getName() {
        return "Gravitar";
    }

    @Override
    public String fetchUserAvatar(User user, int size) {
        String email = user.getEmail();
        String source = null;
        if (email != null && !email.equals("")) {
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(StandardCharsets.UTF_8.encode(email));
                String hash = null;
                hash = String.format("%032x", new BigInteger(1, md5.digest()));


                source = String.format(SOURCE, hash, size);
            } catch (Exception e) {
            }
        }

        return source;
    }

    @Override
    public String getRequiredField() {
        return "email address";
    }

    @Override
    public boolean isUniversal() {
        return false;
    }

    @Nullable
    @Override
    public Drawable getIconDrawable(@NotNull Context context) {
        Drawable d = context.getDrawable(R.mipmap.gravatar);
        return d;
    }
}
