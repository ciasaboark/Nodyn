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
import android.support.v4.app.DialogFragment;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 3/19/18.
 */

public class AdorableAvatarProvider extends AvatarProvider {
    private static final String SOURCE = "https://api.adorable.io/avatars/%d/%s.png";

    @Override
    public String fetchUserAvatar(@NotNull Context context, @NotNull User user, int size) {
        String source = String.format(SOURCE, size, user.getUsername());

        return source;
    }

    @Override
    public String getRequiredField() {
        return "username";
    }

    @Override
    public boolean isUniversal() {
        return true;
    }

    @Nullable
    @Override
    public Drawable getIconDrawable(@NotNull Context context) {
        Drawable d = context.getDrawable(R.mipmap.adorable);
        return d;
    }

    @Override
    public String getName() {
        return "Adorable Avatars";
    }

    @Nullable
    @Override
    public DialogFragment getConfigurationDialogFragment(Context context) {
        return null;
    }
}
