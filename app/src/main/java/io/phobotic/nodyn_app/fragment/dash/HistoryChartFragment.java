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

package io.phobotic.nodyn_app.fragment.dash;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;

import com.github.mikephil.charting.charts.LineChart;

import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.charts.HistoryChartBuilder;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.audit.AuditDatabase;
import io.phobotic.nodyn_app.database.audit.model.AuditDetail;
import io.phobotic.nodyn_app.database.audit.model.AuditHeader;
import io.phobotic.nodyn_app.database.statistics.summary.day_activity.DayActivitySummary;
import io.phobotic.nodyn_app.database.statistics.summary.day_activity.DayActivitySummaryDatabase;
import io.phobotic.nodyn_app.service.StatisticsService;


public class HistoryChartFragment extends Fragment {
    private static final String TAG = HistoryChartFragment.class.getSimpleName();
    private View rootView;
    private LineChart chart;
    private Database db;
    private ProgressBar progressBar;
    private BroadcastReceiver br;
    private boolean loading;


    public static HistoryChartFragment newInstance() {
        HistoryChartFragment fragment = new HistoryChartFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public HistoryChartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        db = Database.getInstance(context);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case StatisticsService.BROADCAST_BUILD_STATISTICS_START:
                        Log.d(TAG, "Received notice statistics service started");
                        showSpinner();
                        break;
                    case StatisticsService.BROADCAST_BUILD_STATISTICS_FINISH:
                        Log.d(TAG, "Received notice statistics service finished");
                        refresh();
                        break;
                }
            }
        };
    }

    private void showSpinner() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void init() {
        chart = rootView.findViewById(R.id.chart);
        progressBar = rootView.findViewById(R.id.spinner);
        refresh();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_history_chart, container, false);
        init();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(StatisticsService.BROADCAST_BUILD_STATISTICS_START);
        filter.addAction(StatisticsService.BROADCAST_BUILD_STATISTICS_FINISH);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(br, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(br);
    }

    public void refresh() {
//        AnimationHelper.fadeIn(getContext(), progressBar);
//        AnimationHelper.fadeOut(getContext(), chart);
        chart.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new Handler(getContext().getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                DayActivitySummaryDatabase db = DayActivitySummaryDatabase.getInstance(getContext());
                //this list will already be filtered to 30 days
                List<DayActivitySummary> list = db.dayActivityDao().getAll();

                //filter the list to only show the last 7 days worth of records
                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.DAY_OF_YEAR, -30);
                long cutoff = calendar.getTimeInMillis();

                //also include any audits.  Filter out anything that is too old or any audit headers
                //+ without detail records
                AuditDatabase auditDatabase = AuditDatabase.getInstance(getContext());
                List<AuditHeader> audits = auditDatabase.headerDao().findAll();

                Iterator<AuditHeader> it = audits.iterator();
                while (it.hasNext()) {
                    AuditHeader a = it.next();
                    List<AuditDetail> details = auditDatabase.detailDao().findByAudit(a.getId());
                    if (a.getBegin() < cutoff) {
                        it.remove();
                    } else if (details == null || details.isEmpty()) {
                        it.remove();
                    }
                }

                updateChart(list, audits);
            }
        });
    }

    private void updateChart(@NotNull final List<DayActivitySummary> dayActivitySummaryList, @NotNull final List<AuditHeader> audits) {
        Activity a = getActivity();
        if (a != null) {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    buildChart(dayActivitySummaryList, audits);
                }
            });
        }
    }

    private void buildChart(@NotNull final List<DayActivitySummary> dayActivitySummaryList, @NotNull List<AuditHeader> audits) {
        HistoryChartBuilder builder = new HistoryChartBuilder();
        builder.buildChart(getContext(), chart, dayActivitySummaryList, audits);

        final int visibleRange = 7;
        chart.setVisibleXRangeMaximum(visibleRange);
        chart.moveViewToX(0);
        float minX = chart.getXAxis().getAxisMinimum();
        final float maxX = chart.getXAxis().getAxisMaximum() - visibleRange;
        ValueAnimator animator = ValueAnimator.ofFloat(minX, maxX)
                .setDuration(2000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float val = (float) animation.getAnimatedValue();
                chart.moveViewToX(val);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                chart.moveViewToX(30);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();

        chart.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }



}
