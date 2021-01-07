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
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.avatar.AvatarHelper;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 9/6/17.
 */

public class VerifyCheckOutView extends LinearLayout {
    private static final String TAG = VerifyCheckinView.class.getSimpleName();
    private final Context context;
    private ObservableMarkdownView markdownView;
    private View rootView;
    private User user;

    public VerifyCheckOutView(@NotNull User user, @NotNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.user = user;
        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_verify_checkout, this);

        markdownView = rootView.findViewById(R.id.markdown);

        if (!isInEditMode()) {
            initUserDetails();
            initMarkdown();
        }
    }

    private void initUserDetails() {
        ImageView image = rootView.findViewById(R.id.image);
        TextView textView = rootView.findViewById(R.id.username);
        textView.setText(user.getName());
        AvatarHelper helper = new AvatarHelper();
        helper.loadAvater(context, user, image, 90);
    }

    private void initMarkdown() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String eulaText = prefs.getString(getResources().getString(R.string.pref_key_check_out_eula), null);

        //if no EULA text has been set fall back to the default
        if (eulaText == null) {
            eulaText = getResources().getString(R.string.pref_default_check_out_eula);
        } else if (eulaText.length() == 0) {
            //if the EULA has been set to an empty string then don't use the default, just indicate that no
            //+ EULA has been set
            eulaText = getResources().getString(R.string.check_out_no_eula_set);
        }

        markdownView.loadMarkdown(eulaText, "file:///android_asset/markdown.css");
    }

    public ObservableMarkdownView getMarkdownView() {
        return markdownView;
    }
}
