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

package io.phobotic.nodyn_app.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.activity.OnSetupCompleteListener;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.helper.SettingsHelper;
import io.phobotic.nodyn_app.service.SyncService;

import static io.phobotic.nodyn_app.service.SyncService.BROADCAST_SYNC_DEBUG;
import static io.phobotic.nodyn_app.service.SyncService.BROADCAST_SYNC_FAIL;
import static io.phobotic.nodyn_app.service.SyncService.BROADCAST_SYNC_FINISH;
import static io.phobotic.nodyn_app.service.SyncService.BROADCAST_SYNC_MESSAGE;
import static io.phobotic.nodyn_app.service.SyncService.BROADCAST_SYNC_PROGRESS_MAIN;
import static io.phobotic.nodyn_app.service.SyncService.BROADCAST_SYNC_PROGRESS_SUB;
import static io.phobotic.nodyn_app.service.SyncService.BROADCAST_SYNC_PROGRESS_SUB_KEY;
import static io.phobotic.nodyn_app.service.SyncService.BROADCAST_SYNC_START;
import static io.phobotic.nodyn_app.service.SyncService.BROADCAST_SYNC_UPDATE;

/**
 * Created by Jonathan Nelson on 2/19/18.
 */

public class FirstSyncErrorFragment extends Fragment {
    private View rootView;
    private FloatingActionButton syncFab;
    private BroadcastReceiver br;
    private TextView debugText;
    private TransitionDrawable td;
    private ImageView iv;
    private Button settingsButton;
    private View circle;
    private View syncStatusCard;
    private View errorBox;
    private View syncNeededCard;
    private View syncSuccessCard;
    private FloatingActionButton nextFab;
    private ProgressBar syncProgress;
    private OnSetupCompleteListener listener;

    public static FirstSyncErrorFragment newInstance() {
        FirstSyncErrorFragment fragment = new FirstSyncErrorFragment();
        return fragment;
    }

    public FirstSyncErrorFragment() {
        // Required empty public constructor
    }

    public void setOnSetupCompleteListener(OnSetupCompleteListener listener) {
        this.listener = listener;
    }

    private void animateCards() {
        Animation moveDown = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_down);
        moveDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                syncNeededCard.setVisibility(View.GONE);
                final Animation moveUp = AnimationUtils.loadAnimation(getContext(), R.anim.bottom_up);
                moveUp.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        syncSuccessCard.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                syncSuccessCard.startAnimation(moveUp);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        syncNeededCard.startAnimation(moveDown);
    }


    private void animateCircle(final Drawable d, final int tintColor) {
        float startRotation = circle.getRotationY();
        ObjectAnimator firstHalf = ObjectAnimator.ofFloat(circle, "rotationY", 0, 90)
                .setDuration(300);
        firstHalf.setInterpolator(new AccelerateDecelerateInterpolator());

        final ObjectAnimator secondHalf = ObjectAnimator.ofFloat(circle, "rotationY", -90, 0)
                .setDuration(300);
        secondHalf.setInterpolator(new AccelerateInterpolator());

        firstHalf.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                iv.setImageDrawable(d);
                circle.setBackgroundTintList(ColorStateList.valueOf(tintColor));
                secondHalf.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        firstHalf.start();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case BROADCAST_SYNC_START:
                        handleSyncStart();
                        break;
                    case BROADCAST_SYNC_FINISH:
                        handleSyncFinish(context);
                        break;
                    case BROADCAST_SYNC_DEBUG:
                        handleSyncDebugMessage(intent);
                        break;
                    case BROADCAST_SYNC_FAIL:
                        handleSyncFail(intent);
                        break;
                    case BROADCAST_SYNC_UPDATE:
                        handleSyncUpdateMessage(intent);
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_first_sync_error, container, false);
        init();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BROADCAST_SYNC_START);
        filter.addAction((BROADCAST_SYNC_FINISH));
        filter.addAction(BROADCAST_SYNC_FAIL);
        filter.addAction(BROADCAST_SYNC_UPDATE);
        filter.addAction(BROADCAST_SYNC_DEBUG);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(br, filter);
        Intent i = new Intent(SyncNotificationFragment.BROADCAST_SYNC_DETAILS_SHOWN);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(i);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(br);
        Intent i = new Intent(SyncNotificationFragment.BROADCAST_SYNC_DETAILS_HIDDEN);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(i);
    }

    private void init() {
        findViews();
        initButtons();
        initTextViews();
        initCards();
    }

    private void findViews() {
        syncNeededCard = rootView.findViewById(R.id.card_need_sync);
        syncSuccessCard = rootView.findViewById(R.id.card_success);
        syncFab = (FloatingActionButton) rootView.findViewById(R.id.sync_button);
        nextFab = (FloatingActionButton) rootView.findViewById(R.id.next_button);
        debugText = (TextView) rootView.findViewById(R.id.sync_status_text);
        circle = rootView.findViewById(R.id.circle);
        syncStatusCard = rootView.findViewById(R.id.sync_status_card);
        iv = (ImageView) rootView.findViewById(R.id.image);
        errorBox = rootView.findViewById(R.id.sync_error);
        settingsButton = (Button) rootView.findViewById(R.id.settings_button);
        syncProgress = (ProgressBar) rootView.findViewById(R.id.sync_progress);
    }

    private void initButtons() {
        syncFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                debugText.setText("");
                AnimationHelper.expand(syncStatusCard);
                syncFab.hide();
                AnimationHelper.collapse(errorBox);
                AnimationHelper.fadeIn(getContext(), syncProgress);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Context context = getContext();
                        if (context != null) {
                            Intent i = new Intent(getContext(), SyncService.class);
                            context.startService(i);
                        }
                    }
                }, 1000);
            }
        });
        syncFab.setVisibility(View.GONE);


        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsHelper.loadKioskSettings(getContext(), null);
            }
        });

        syncFab.show();

        nextFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onSetupComplete();
                }
            }
        });
    }

    private void initTextViews() {
        debugText.setMovementMethod(new ScrollingMovementMethod());

    }

    private void initCards() {
        syncSuccessCard.setVisibility(View.GONE);
        syncNeededCard.setVisibility(View.VISIBLE);
        errorBox.setVisibility(View.GONE);
        syncProgress.setVisibility(View.GONE);
        syncStatusCard.setVisibility(View.GONE);
        syncFab.setVisibility(View.VISIBLE);
    }

    private void handleSyncFail(Intent intent) {
        String errorMsg = intent.getExtras().getString(
                BROADCAST_SYNC_MESSAGE);
        syncFab.setEnabled(true);
        syncFab.show();

        Drawable d = getResources().getDrawable(R.drawable.cloud_off_outline);
        int color = getResources().getColor(R.color.warning_strong);
        animateCircle(d, color);
        addLine("Sync process failed: " + errorMsg);

        //show the error box below
        AnimationHelper.expand(errorBox);
    }

    private void handleSyncUpdateMessage(Intent intent) {
        try {
            String updateMessage = intent.getStringExtra(BROADCAST_SYNC_MESSAGE);
            String progressString = intent.getStringExtra(BROADCAST_SYNC_PROGRESS_MAIN);
            int progress;
            if (progressString == null) {
                progress = -1;
            } else {
                progress = Integer.parseInt(progressString);
            }

            String subProgressString = intent.getStringExtra(BROADCAST_SYNC_PROGRESS_SUB);
            int subProgress;
            if (subProgressString == null) {
                subProgress = -1;
            } else {
                subProgress = Integer.parseInt(subProgressString);
            }


            String subProgressKey = intent.getStringExtra(BROADCAST_SYNC_PROGRESS_SUB_KEY);
            if (progress == -1) {
                addLine("Sync in progress: " + updateMessage);
            } else {
                addLine("Sync in progress: ", updateMessage, progress, subProgressKey, subProgress);
            }
        } catch (Exception e) {
        }
    }

    private void handleSyncDebugMessage(Intent intent) {
        String message = intent.getExtras().getString(BROADCAST_SYNC_MESSAGE);
        addLine("Debug: " + message);
    }

    private void handleSyncFinish(Context context) {
        addLine("Sync process finished");
        syncFab.setVisibility(View.GONE);

        animateCards();
        Drawable d = getResources().getDrawable(R.drawable.check);
        int color = getResources().getColor(R.color.success);
        animateCircle(d, color);

        //go ahead and unregister the broadcast receiver.  This will keep the text
        //+ from scrolling pas the last message
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(br);
    }

    private void handleSyncStart() {
        AnimationHelper.fadeOut(getContext(), syncProgress);
        debugText.setText("");
        AnimationHelper.collapse(errorBox);
        syncFab.hide();
        addLine("Starting sync process");
    }

    private void addLine(String line) {
        addLine(line, null, -1, null, -1);
    }

    private void addLine(String prefix, String mainProgressMessage, int progress,
                         String subProgressMessage, int subProgress) {
        String mainProgressString = "";
        if (mainProgressMessage != null && progress != -1) {
            mainProgressString = String.format("%s (%d)", mainProgressMessage, progress);
        }

        String subProgressString = "";
        if (subProgressMessage != null && subProgress != -1) {
            subProgressString = String.format(" %s (%d)", subProgressMessage, subProgress);
        }

        String line = String.format("%s: %s%s", prefix, mainProgressString, subProgressString);
        SpannableString spannableString = new SpannableString(line);
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, prefix.length(), 0);

        if (mainProgressString.length() > 0) {
            int startIndex = prefix.length() + 2;
            spannableString.setSpan(new ForegroundColorSpan(Color.GREEN), startIndex, startIndex + mainProgressMessage.length(), 0);
        }

        if (subProgressString.length() > 0) {
            int startIndex = prefix.length() + 2 + mainProgressString.length();
            spannableString.setSpan(new ForegroundColorSpan(Color.RED), startIndex, startIndex + subProgressString.length(), 0);
        }

        insertLine(spannableString);
    }

    private void insertLine(SpannableString ss) {
        if (debugText.getText().toString().length() == 0) {
            debugText.append(ss);
        } else {
            debugText.append("\n" + ss);
        }
//        debugText.setText(ss);
    }

}

