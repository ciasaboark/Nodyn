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
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import org.jetbrains.annotations.NotNull;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.UserHelper;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 9/22/17.
 */

public class BadgeScanView extends RelativeLayout {
    private static final String TAG = BadgeScanView.class.getSimpleName();
    private final Context context;
    private OnUserScannedListener onUserScannedListener;
    private ScanInputView input;
    private View rootView;
    private TextSwitcher message;
    private boolean kioskModeEnabled = false;
    private ViewSwitcher.ViewFactory warningFactory;
    private ViewSwitcher.ViewFactory normalFactory;
    private int normalTextColor;

    public BadgeScanView(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public BadgeScanView(@NotNull Context context, AttributeSet attrs, @Nullable OnUserScannedListener onUserScannedListener) {
        super(context, attrs);
        this.context = context;
        this.onUserScannedListener = onUserScannedListener;
        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_badge_scan, this);
        if (!isInEditMode()) {
            input = (ScanInputView) rootView.findViewById(R.id.input);
            input.setListener(new ScanInputView.OnTextInputListener() {
                @Override
                public void onTextInput(String inputString) {
                    if (inputString == null || inputString.equals("")) {
                        if (onUserScannedListener != null) {
                            onUserScannedListener.onUserScanError("Input string empty");
                        }
                    } else {
                        processInputString(inputString);
                    }
                }
            });

            //restrict the input if we are using kiosk mode
            kioskModeEnabled = PreferenceManager.getDefaultSharedPreferences(getContext())
                    .getBoolean(getResources().getString(R.string.pref_key_general_kiosk),
                            Boolean.parseBoolean(getResources().getString(
                                    R.string.pref_default_general_kiosk)));

            input.setGhostMode(kioskModeEnabled);
            input.setForceScanInput(kioskModeEnabled);
            input.clearFocus();
            input.requestFocus();

            message = (TextSwitcher) rootView.findViewById(R.id.error);
            normalFactory = new ViewSwitcher.ViewFactory() {
                @Override
                public View makeView() {
                    TextView t = new TextView(getContext());
                    t.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                    t.setTextAppearance(getContext(), android.R.style.TextAppearance_Material_Subhead);
                    return t;
                }
            };

            message.setFactory(normalFactory);
            normalTextColor = ((TextView) message.getNextView()).getCurrentTextColor();

            reset();
        }
    }

    private void processInputString(@NotNull String inputString) {
        ((TextView) message.getNextView()).setTextColor(normalTextColor);
        Database db = Database.getInstance(getContext());

        try {
            User user = UserHelper.getUserByInputString(getContext(), inputString);
            processUserScan(user);
        } catch (UserNotFoundException e) {
            processUserScanError(inputString);
        }
    }

    public void reset() {
        message.setCurrentText(getResources().getString(R.string.badge_scan_scan_now));
        input.reset();
    }

    private void processUserScan(User user) {
        String message = getResources().getString(R.string.badge_scan_found_user);
        this.message.setText(String.format(message, user.getName()));
        if (onUserScannedListener != null) {
            onUserScannedListener.onUserScanned(user);
        }
    }

    private void processUserScanError(String inputString) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(R.attr.colorAccent, typedValue, true);
        @ColorInt int color = typedValue.data;
        ((TextView) message.getNextView()).setTextColor(color);

        //if kiosk mode is enabled we need to be careful not to show the scanned input that failed.
        String message;
        if (kioskModeEnabled) {
            message = getResources().getString(R.string.badge_scan_unknown_user);
        } else {
            message = getResources().getString(R.string.badge_scan_unknown_user_unformatted);
            message = String.format(message, inputString);
        }
        this.message.setText(message);
        if (onUserScannedListener != null) {
            onUserScannedListener.onUserScanError(message);
        }
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            input.requestFocus();
        }
    }

    public BadgeScanView setOnUserScannedListener(OnUserScannedListener onUserScannedListener) {
        this.onUserScannedListener = onUserScannedListener;
        return this;
    }

    public interface OnUserScannedListener {
        void onUserScanned(@NotNull User user);

        void onUserScanError(@NotNull String message);
    }
}
