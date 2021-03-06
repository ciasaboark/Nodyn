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

package io.phobotic.nodyn_app.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.service.SyncService;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SyncNotificationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SyncNotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SyncNotificationFragment extends Fragment {
    public static final String BROADCAST_SYNC_DETAILS_SHOWN = "sync details shown";
    public static final String BROADCAST_SYNC_DETAILS_HIDDEN = "sync details hidden";
    private View rootView;
    private ProgressBar progressBar;
    private TextView message;
    private BroadcastReceiver br;
    private TextView error;
    private boolean visible = false;
    private DonutProgress subProgressBar;
    private String lastSubKey = "unknown";

    //if set to false we will skip showing the sync popup
    private boolean hidePopup = false;

    public static SyncNotificationFragment newInstance() {
        SyncNotificationFragment fragment = new SyncNotificationFragment();
        return fragment;
    }

    public SyncNotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case BROADCAST_SYNC_DETAILS_SHOWN:
                        hidePopup = true;
                        break;
                    case BROADCAST_SYNC_DETAILS_HIDDEN:
                        hidePopup = false;
                        break;
                    case SyncService.BROADCAST_SYNC_START:
                        showActiveMessage("Starting sync process", null);
                        break;
                    case SyncService.BROADCAST_SYNC_FINISH:
                        showStoppedMessage("Sync process finished", null);
                        break;
                    case SyncService.BROADCAST_SYNC_FAIL:
                        String errorMsg = intent.getExtras().getString(
                                SyncService.BROADCAST_SYNC_MESSAGE);
                        showStoppedMessage("Sync process failed", errorMsg);
                        break;
                    case SyncService.BROADCAST_SYNC_UPDATE:
                        String updateMessage = intent.getStringExtra(SyncService.BROADCAST_SYNC_MESSAGE);
                        int progress = intent.getIntExtra(SyncService.BROADCAST_SYNC_PROGRESS_MAIN, -1);
                        int subProgress = intent.getIntExtra(SyncService.BROADCAST_SYNC_PROGRESS_SUB, -1);
                        String subMessage = intent.getStringExtra(SyncService.BROADCAST_SYNC_SUB_MESSAGE);
                        String subProgressKey = intent.getStringExtra(SyncService.BROADCAST_SYNC_PROGRESS_SUB_KEY);

                        if (progress == -1) {
                            showActiveMessage(updateMessage, subMessage);
                        } else {
                            showActiveMessage(updateMessage, subMessage, progress, subProgress, subProgressKey);
                        }
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_sync_notification, container, false);
        init();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(SyncService.BROADCAST_SYNC_START);
        filter.addAction((SyncService.BROADCAST_SYNC_FINISH));
        filter.addAction(SyncService.BROADCAST_SYNC_FAIL);
        filter.addAction(SyncService.BROADCAST_SYNC_UPDATE);
        filter.addAction(BROADCAST_SYNC_DETAILS_SHOWN);
        filter.addAction(BROADCAST_SYNC_DETAILS_HIDDEN);
//        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(br, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(br);
    }

    private void init() {
        progressBar = rootView.findViewById(R.id.horz_progress);
        message = rootView.findViewById(R.id.message);
        error = rootView.findViewById(R.id.sub_message);
        subProgressBar = rootView.findViewById(R.id.donut_progress);
        subProgressBar.setVisibility(View.GONE);

        rootView.setVisibility(View.GONE);
    }

    private void animateIn() {
        if (isVisible()) {
            rootView.setTranslationY(0);
            rootView.setVisibility(View.VISIBLE);
        } else {
            rootView.setVisibility(View.VISIBLE);
            // Start the animation
            rootView.animate()
                    .translationY(0)
                    .setDuration(2000)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            //do nothing
                        }
                    });
        }

        visible = true;
    }

    private void animateOut() {
        rootView.animate()
                .translationY(rootView.getHeight())
                .setDuration(2000)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        visible = false;
                    }
                });
    }

    private void showActiveMessage(@NotNull String message, @Nullable String subMessage) {
        showActiveMessage(message, subMessage, null, null, null);
    }

    private void showActiveMessage(@NotNull String message, @Nullable String subMessage,
                                   @Nullable Integer progress, @Nullable Integer subProgress,
                                   @Nullable String subProgressKey) {
        if (hidePopup) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        this.message.setVisibility(View.VISIBLE);
        error.setVisibility(subMessage == null ? View.INVISIBLE : View.VISIBLE);

        if (progress != null) {
            progressBar.setProgress(progress);
            progressBar.setIndeterminate(false);
        }

        if (subProgressKey != lastSubKey) {
            subProgressBar.setProgress(0);
            subProgressBar.setVisibility(View.GONE);
        }
        lastSubKey = subProgressKey;

        if (subProgress != null) {
            subProgressBar.setVisibility(View.VISIBLE);
            subProgressBar.setProgress(subProgress);
        } else {
            subProgressBar.setProgress(0);
        }

        this.message.setText(message);
        if (subMessage != null) {
            error.setText(subMessage);
        }

        animateIn();
    }

    private void showStoppedMessage(String msg, String err) {
        if (hidePopup) {
            return;
        }

        progressBar.setVisibility(View.GONE);
        message.setVisibility(View.VISIBLE);
        error.setVisibility(err == null ? View.GONE : View.VISIBLE);

        message.setText(msg);
        if (err != null) {
            error.setText(err);
        }

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateOut();
            }
        }, 10000);
    }
}
