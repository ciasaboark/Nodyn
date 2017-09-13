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
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import io.phobotic.nodyn.ObservableMarkdownView;
import io.phobotic.nodyn.R;

/**
 * Created by Jonathan Nelson on 9/6/17.
 */

public class VerifyCheckOutView extends LinearLayout {
    private static final String TAG = VerifyCheckinView.class.getSimpleName();
    private final Context context;
    private ObservableMarkdownView markdownView;
    private View rootView;

    public VerifyCheckOutView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_markdown_wrapper, this);

        markdownView = (ObservableMarkdownView) rootView.findViewById(R.id.markdown);

        initMarkdown();
    }

    private void initMarkdown() {
        if (!isInEditMode()) {
            markdownView.loadMarkdownFile("file:///android_asset/check_out_verification.md");
        }
    }

    public ObservableMarkdownView getMarkdownView() {
        return markdownView;
    }
}
