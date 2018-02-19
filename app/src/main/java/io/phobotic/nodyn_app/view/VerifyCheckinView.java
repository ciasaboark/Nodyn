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
import android.widget.LinearLayout;

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
        rootView = inflate(context, R.layout.view_markdown_wrapper, this);

        markdownView = (MarkdownView) rootView.findViewById(R.id.markdown);

        initMarkdown();
    }

    private void initMarkdown() {
        if (!isInEditMode()) {
            markdownView.loadMarkdownFile("file:///android_asset/check_in_verification.md");
        }
    }
}
