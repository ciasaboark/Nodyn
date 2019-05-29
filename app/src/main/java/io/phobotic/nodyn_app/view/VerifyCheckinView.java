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
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import io.phobotic.nodyn_app.R;
import us.feras.mdv.MarkdownView;


/**
 * Created by Jonathan Nelson on 9/1/17.
 */

public class VerifyCheckinView extends LinearLayout {
    private static final String TAG = VerifyCheckinView.class.getSimpleName();
    private final Context context;
    private MarkdownView markdownView;
    private View rootView;

    public VerifyCheckinView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_verify_checkin, this);

        markdownView = rootView.findViewById(R.id.markdown);

        initMarkdown();
    }

    private void initMarkdown() {
        if (!isInEditMode()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String verifcationText = prefs.getString(context.getString(R.string.pref_key_check_in_verification_text), null);

            //if no verification text has been set fall back to the default
            if (verifcationText == null) {
                verifcationText = getResources().getString(R.string.pref_default_check_in_verification_text);
            } else if (verifcationText.length() == 0) {
                //if the verification text has been set to an empty string then don't use the
                //+ default, just indicate that no verification has been set
                verifcationText = getResources().getString(R.string.check_in_no_verification_text);
            }
            markdownView.loadMarkdown(verifcationText);
        }
    }
}
