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
import android.support.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 3/27/18.
 * Simple wrapper to redirect requests for user avatars to whatever is provided by the backend
 */

public class BackendAvatarProvider extends AvatarProvider {
    @Override
    public String fetchUserAvatar(User user, int size) {
        return user.getAvatarURL();
    }

    @Override
    public String getRequiredField() {
        return "username";
    }

    @Override
    public boolean isUniversal() {
        return false;
    }

    @Nullable
    @Override
    public Drawable getIconDrawable(@NotNull Context context) {
        Drawable d = context.getDrawable(R.drawable.cloud_sync);
        return d;
    }

    @Override
    public String getName() {
        return "Sync Adapter Avatars";
    }
}
