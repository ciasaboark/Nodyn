/*
 * Copyright (c) 2020 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.fragment.dash;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.sync.SyncAttempt;
import io.phobotic.nodyn_app.helper.ColorHelper;
import io.phobotic.nodyn_app.service.SyncService;
import io.phobotic.nodyn_app.sync.SyncManager;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LastSyncFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LastSyncFragment extends Fragment {
    private static final String TAG = LastSyncFragment.class.getSimpleName();
    private static final int ANIM_DURATION = 1000;
    private static final int ANIM_DELAY = 1000;
    private static final String STATUS_KEY = "status";

    // TODO: Rename and change types of parameters
    private TextSwitcher lastSyncTS;
    private View syncWarning;
    private CardView card;
    private BroadcastReceiver br;
    private View rootView;
    private int status = -1;
    private TextView title;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LastSyncFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LastSyncFragment newInstance() {
        LastSyncFragment fragment = new LastSyncFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public LastSyncFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATUS_KEY, this.status);
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.status = savedInstanceState.getInt(STATUS_KEY, -1);
        }

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.d(TAG, String.format("Saw sync action %s", action));

                String lastSyncTime = getLastSyncString();
                lastSyncTS.setText(lastSyncTime);

                updateStatus();
            }
        };
    }

    private String getLastSyncString() {
        SyncAttempt lastSuccess = SyncManager.getLastSuccessfulSync(getContext());

        String lastSyncTime;
        if (lastSuccess == null) {
            lastSyncTime = "Never";
        } else {
            Date d = new Date(lastSuccess.getEndTime());
            DateFormat df = SimpleDateFormat.getDateTimeInstance();
            lastSyncTime = df.format(d);
        }

        return lastSyncTime;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction((SyncService.BROADCAST_SYNC_FINISH));
        filter.addAction(SyncService.BROADCAST_SYNC_FAIL);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(br, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(br);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_last_sync, container, false);
        init();

        return rootView;
    }

    private void init() {
        findViews();
        initTextSwitchers();
        updateStatus();

        title.setTextColor(ColorHelper.getValueTextColorForBackground(getContext(), getCardColor()));
    }

    private void updateStatus() {
        SyncAttempt lastSuccess = SyncManager.getLastSuccessfulSync(getContext());
        if (lastSuccess == null) {
            this.status = Status.ERROR;
        } else {
            //find out how long ago the last successful sync happened.  If it is within two cycles of
            //+ whatever sync frequency the user has selected then we are OK.  If it is between
            //+ two and four change the color to caution.  If it is older than that change to the error
            //+ color

            long now = System.currentTimeMillis();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            String wakePeriodString = prefs.getString(getContext().getString(R.string.pref_key_sync_frequency),
                    getContext().getString(R.string.pref_default_sync_frequency));
            long syncFrequencyMs = Integer.parseInt(wakePeriodString) * 1000 * 60;

            long cautionEnd = now - (syncFrequencyMs * 2);
            long warningEnd = now - (syncFrequencyMs * 4);

            if (lastSuccess.getEndTime() >= cautionEnd) {
                status = Status.OK;
            } else if (lastSuccess.getEndTime() >= warningEnd) {
                status = Status.CAUTION;
            } else {
                status = Status.ERROR;
            }

        }

        int newTextColor = 0;
        switch (status) {
            case Status.OK:
                newTextColor = getContext().getResources().getColor(R.color.sync_status_ok);
                break;
            case Status.CAUTION:
                newTextColor = getContext().getResources().getColor(R.color.sync_status_caution);
                break;
            case Status.ERROR:
                newTextColor = getContext().getResources().getColor(R.color.sync_status_error);
                break;
        }

//        if (status == Status.ERROR) {
//            syncWarning.setVisibility(View.VISIBLE);
//        } else {
//
//        }

        // TODO: 2/12/2021 is the warning message still needed?
        syncWarning.setVisibility(View.GONE);

//        animateCardColorChange(newCardColor);
        animateTextColorChange(newTextColor);
    }

    private void animateTextColorChange(int newTextColor) {
        int fromColor = title.getCurrentTextColor();
        int toColor = newTextColor;

        final ValueAnimator anim = ValueAnimator.ofArgb(fromColor, toColor);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (int) anim.getAnimatedValue();

                //update the text colors
                title.setTextColor(val);
                ((TextView)lastSyncTS.getCurrentView()).setTextColor(val);
            }
        });

        anim.setDuration(ANIM_DURATION);
        anim.setStartDelay(ANIM_DELAY);
        anim.start();
    }

    private void animateCardColorChange(int newCardColor) {
        int oldColor = card.getCardBackgroundColor().getDefaultColor();
        final ValueAnimator anim = ValueAnimator.ofArgb(oldColor, newCardColor);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (int) anim.getAnimatedValue();
                card.setCardBackgroundColor(val);
            }
        });

        anim.setDuration(ANIM_DURATION);
        anim.setStartDelay(ANIM_DELAY);
        anim.start();
    }

    private void findViews() {
        this.title = (TextView) rootView.findViewById(R.id.title);
        this.lastSyncTS = (TextSwitcher) rootView.findViewById(R.id.last_sync_time);
        this.card = (CardView) rootView.findViewById(R.id.card);
        this.syncWarning = rootView.findViewById(R.id.sync_warning);
    }

    private void initTextSwitchers() {
//        Animation inAnimation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
//        Animation outAnimation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);

        lastSyncTS.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(getContext());
                t.setTextAppearance(getContext(), R.style.Text_Title);
                int textColor = ColorHelper.getValueTextColorForBackground(getContext(), getCardColor());
                t.setTextColor(textColor);
                return t;
            }
        });
        String lastSyncTime = getLastSyncString();

        lastSyncTS.setCurrentText(lastSyncTime);
//        lastSyncTS.setInAnimation(inAnimation);
//        lastSyncTS.setOutAnimation(outAnimation);
    }

    private int getCardColor() {
        int color = card.getCardBackgroundColor().getDefaultColor();
        return color;
    }

    private class Status {
        public static final int OK = 0;
        public static final int CAUTION = 1;
        public static final int ERROR = 2;
    }

}
