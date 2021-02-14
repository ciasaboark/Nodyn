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
import android.graphics.Color;
import android.os.Handler;
import android.text.Html;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.avatar.AvatarHelper;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.helper.AnimationHelper;

/**
 * Created by Jonathan Nelson on 9/6/17.
 */

public class VerifyCheckOutView extends LinearLayout {
    private static final String TAG = VerifyCheckinView.class.getSimpleName();
    private final Context context;
    private String headerText;
    private String eulaText;
    private ObservableMarkdownView markdownView;
    private View rootView;
    private User user;
    private Timer timer;
    private int scrollY = -1;

    public VerifyCheckOutView(@NotNull User user, @NotNull Context context,
                              @Nullable AttributeSet attrs, String eulaText, String headerText) {
        super(context, attrs);
        this.context = context;
        this.user = user;
        this.eulaText = eulaText;
        this.headerText = headerText;
        init();
    }

    public void setHeaderText(String headerText) {
        this.headerText = headerText;
        initHeader();
    }

    public void setEulaText(String eulaText) {
        this.eulaText = eulaText;
        initMarkdown();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_verify_checkout, this);

        markdownView = rootView.findViewById(R.id.markdown);

        if (!isInEditMode()) {
            initUserDetails();
            initHeader();
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

    private void showOrHideIndicator() {
        if (!isInEditMode()) {
            ImageView indicator = rootView.findViewById(R.id.fade_indicator);
            boolean canScroll = canViewScroll();
            boolean isAtBottom = isScrollAtBottom(scrollY);
            if (!canScroll || isAtBottom) {
                AnimationHelper.fadeOut(getContext(), indicator);
            } else {
                AnimationHelper.fadeIn(getContext(), indicator);
            }
        }
    }

    private boolean isScrollAtBottom(int scrollY) {
        boolean isScrollAtBottom = false;
        int height = (int) Math.floor(markdownView.getContentHeight() * markdownView.getScale());
        int webViewHeight = markdownView.getHeight();
        int cutoff = height - webViewHeight - 10;

        if (scrollY >= cutoff) {
            isScrollAtBottom = true;
        }

        return isScrollAtBottom;
    }

    private boolean canViewScroll() {
        boolean canViewScroll = true;
        int contentHeight = markdownView.getContentHeight();
        int visibleHeight = markdownView.getHeight() - markdownView.getPaddingTop() - markdownView.getPaddingBottom();
        canViewScroll =  visibleHeight < contentHeight;

        return canViewScroll;
    }

    private void initHeader() {
        TextView headerTV = rootView.findViewById(R.id.header);
        if (headerText == null || headerText.length() == 0) {
            headerTV.setVisibility(View.GONE);
        } else {
            headerTV.setVisibility(View.VISIBLE);
            //allow the text to be passed in as HTML formatted text and convert to a spannable
            headerTV.setText(Html.fromHtml(headerText));
        }
    }
    private void initMarkdown() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (eulaText.length() == 0) {
            //if the EULA has been set to an empty string then don't use the default, just indicate that no
            //+ EULA has been set
            eulaText = getResources().getString(R.string.check_out_no_eula_set);
        }

        markdownView.loadMarkdown(eulaText, "file:///android_asset/markdown.css");
        markdownView.setOnScrollChangeListener(new ObservableMarkdownView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(WebView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                VerifyCheckOutView.this.scrollY = scrollY;
                showOrHideIndicator();
            }

            @Override
            public void onPageFinished() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showOrHideIndicator();
                    }
                }, 300);

            }
        });
        markdownView.setBackgroundColor(getContext().getResources().getColor(R.color.dialog_window_background));

        if (this.timer != null) {
            timer.cancel();
        }

        this.timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //must run on the UI thread
                post(new Runnable() {
                    @Override
                    public void run() {
                        showOrHideIndicator();
                    }
                });
            }
        }, 0, 500);
    }



    public ObservableMarkdownView getMarkdownView() {
        return markdownView;
    }
}
