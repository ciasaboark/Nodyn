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

import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 3/19/18.
 */

public abstract class AvatarProvider {
    public abstract String fetchUserAvatar(@NotNull Context context, @NotNull User user, int size);

    public abstract String getRequiredField();

    public abstract boolean isUniversal();

    public abstract @Nullable
    Drawable getIconDrawable(@NotNull Context context);

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AvatarProvider) {
            return getName().equals(((AvatarProvider) obj).getName());
        } else {
            return false;
        }
    }

    public abstract @NotNull
    String getName();

    public abstract
    @Nullable
    DialogFragment getConfigurationDialogFragment(Context context);
}
