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

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 3/19/18.
 */

public class TinyGraphAvatarProvider extends AvatarProvider {
    private static final String TAG = TinyGraphAvatarProvider.class.getSimpleName();
    private static final String SOURCE = "http://tinygraphs.com/%s/%s?theme=%s&numcolors=4&size=%d&fmt=jpg";

    @Override
    public String getName() {
        return "Tiny Graph";
    }

    @Override
    public String fetchUserAvatar(User user, int size) {
        //if we could not pick up an avatar from gravitar we can use the users login information
        //+ to generate a random avatar

        String[] themes = {
                "frogideas",
                "sugarsweets",
                "heatwave",
                "daisygarden",
                "seascape",
                "summerwarmth",
                "bythepool",
                "duskfalling",
                "berrypie"
        };

        String[] styles = {
                "squares"
        };

        //we'll want to restrict this to positive numbers to make sure the mod arithmetic works
        int hashCode = Math.abs(user.getUsername().hashCode());
        if (hashCode == 0) hashCode = 1;

        int themeNumber = hashCode % themes.length;
        int styleNumber = hashCode % styles.length;
        String theme = themes[themeNumber];
        String style = styles[styleNumber];

        String source = String.format(SOURCE, style, user.getUsername(), theme, size);
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

    @Override
    public Drawable getIconDrawable(@NotNull Context context) {
        Drawable d = context.getDrawable(R.drawable.tinygraph);
        return d;
    }


}
