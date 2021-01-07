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
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.RoomDBWrapper;
import io.phobotic.nodyn_app.database.helper.UserHelper;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.database.scan.ScanRecord;
import io.phobotic.nodyn_app.database.scan.ScanRecordDatabase;

/**
 * Created by Jonathan Nelson on 9/22/17.
 */

public class BadgeScanView extends RelativeLayout {
    private static final String TAG = BadgeScanView.class.getSimpleName();
    private static final String SCAN_TYPE = "BadgeScanView";
    private final Context context;
    private boolean isMiniView = false;
    private OnUserScannedListener onUserScannedListener;
    private ScanInputView input;
    private View rootView;
    //    private TextSwitcher message;
    private boolean isDeauthenticateAllowed = false;
    //    private int normalTextColor;
    private UserCardView card;
    private boolean isGhostMode;
    private boolean isRestrictedInput;

    public BadgeScanView(Context context, AttributeSet attrs) {
        this(context, attrs, false, null);
    }

    public BadgeScanView(@NotNull Context context, AttributeSet attrs, boolean isMiniView,
                         @Nullable OnUserScannedListener onUserScannedListener) {
        super(context, attrs);

        this.isMiniView = isMiniView;
        this.context = context;
        this.onUserScannedListener = onUserScannedListener;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.BadgeScanView);
            isMiniView = ta.getBoolean(R.styleable.BadgeScanView_use_mini, false);
            ta.recycle();
        }


        if (isMiniView) {
            rootView = inflate(context, R.layout.view_badge_scan_mini, this);
        } else {
            rootView = inflate(context, R.layout.view_badge_scan, this);
        }

        if (!isInEditMode()) {
            card = rootView.findViewById(R.id.user_card);
            input = rootView.findViewById(R.id.input);
            input.setListener(new ScanInputView.OnTextInputListener() {
                @Override
                public void onTextInputFinished(String inputString) {
                    if (inputString == null || inputString.equals("")) {
                        if (onUserScannedListener != null) {
                            onUserScannedListener.onUserScanError("Input string empty");
                        }
                    } else {
                        processInputString(inputString);
                    }
                }

                @Override
                public void onTextInputBegin() {
                    onUserScannedListener.onInputBegin();
                }
            });

            input.clearFocus();
            input.requestFocus();

            reset();
        }
    }

    public void reset() {
        card.hideDetails();
        enableInput();
        input.reset();
    }

    public void enableInput() {
        input.setVisibility(View.VISIBLE);
        input.requestFocus();
    }

    private void processInputString(@NotNull String inputString) {
        Database db = Database.getInstance(getContext());

        try {
            User user = UserHelper.getUserByInputString(getContext(), inputString);
            recordGoodScan(inputString, user);
            processUserScan(user);
        } catch (UserNotFoundException e) {
            recordBadScan(inputString);
            processUserScanError(inputString);
        }
    }

    private void recordGoodScan(String scannedData, User user) {
        recordScan(scannedData, "Data recognized as user " + user.toString(),
                true);
    }

    private void recordBadScan(String scannedData) {
        recordScan(scannedData, "User not recognized", false);
    }

    private void recordScan(String data, String reason, boolean isAccepted) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String validationField = prefs.getString(context.getString(
                R.string.pref_key_user_scan_field), null);

        ScanRecordDatabase scanLogDb = RoomDBWrapper.getInstance(getContext()).getScanRecordDatabase();
        scanLogDb.scanRecordDao().upsertAll(new ScanRecord(SCAN_TYPE, System.currentTimeMillis(),
                data, isAccepted, this.getClass().getSimpleName(),
                String.format("[validation field: %s] %s", validationField, reason)));
    }

    public void disableInput() {
        input.setVisibility(View.GONE);
    }

    private void processUserScan(User user) {
        card.reveal(user);

        if (onUserScannedListener != null) {
            onUserScannedListener.onUserScanned(user);
        }
    }

    private void processUserScanError(String inputString) {
        //if kiosk mode is enabled we need to be careful not to show the scanned input that failed.
        String message;
        if (isGhostMode) {
            message = getResources().getString(R.string.badge_scan_unknown_user);
        } else {
            message = getResources().getString(R.string.badge_scan_unknown_user_unformatted);
            message = String.format(message, inputString);
        }

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

        void onInputBegin();
    }
}
