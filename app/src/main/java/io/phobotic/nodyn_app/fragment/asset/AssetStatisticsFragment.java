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

package io.phobotic.nodyn_app.fragment.asset;

import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;

import androidx.fragment.app.Fragment;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.avatar.AvatarHelper;
import io.phobotic.nodyn_app.charts.AssetUsageChartBuilder;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.database.statistics.assets.AssetStatistics;
import io.phobotic.nodyn_app.database.statistics.assets.AssetStatisticsDatabase;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.view.CloudErrorView;

/**
 * Created by Jonathan Nelson on 2019-05-12.
 */
public class AssetStatisticsFragment extends Fragment {
    private static final String TAG = AssetStatisticsFragment.class.getSimpleName();
    private static final String ASSET = "asset";
    private Asset asset;
    private View rootView;
    private HorizontalBarChart chart;
    private ScrollView holder;
    private CloudErrorView error;
    private View loading;
    private View noStatisticsWarning;
    private View userCard;
    private ImageView userIcon;
    private TextView username;
    private TextView usageText;

    public static AssetStatisticsFragment newInstance(Asset asset) {
        AssetStatisticsFragment f = new AssetStatisticsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ASSET, asset);
        f.setArguments(bundle);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            asset = (Asset) getArguments().getSerializable(ASSET);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_asset_statistics, container, false);
        init();

        return rootView;
    }

    private void init() {
        findViews();
        initChart();
        loadData();
    }

    private void findViews() {
        chart = rootView.findViewById(R.id.chart);
        holder = rootView.findViewById(R.id.holder);
        error = rootView.findViewById(R.id.error);
        loading = rootView.findViewById(R.id.loading);
        noStatisticsWarning = rootView.findViewById(R.id.no_statistics_warning);
        userCard = rootView.findViewById(R.id.user_card);
        userIcon = rootView.findViewById(R.id.user_icon);
        username = rootView.findViewById(R.id.username);
        usageText = rootView.findViewById(R.id.usage_text);

        noStatisticsWarning.setVisibility(View.GONE);
        error.setVisibility(View.GONE);
        holder.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
    }

    private void initChart() {
        chart.getDescription().setEnabled(false);
    }

    private void loadData() {
        final AssetStatisticsDatabase db = AssetStatisticsDatabase.getInstance(getContext());
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                AssetStatistics statistics = db.assetStatisticsDao().getStatisticsForAsset(asset.getId());
                AnimationHelper.fadeOut(getContext(), loading);

                if (statistics == null) {
                    AnimationHelper.fadeIn(getContext(), noStatisticsWarning);
                } else {
                    AnimationHelper.fadeIn(getContext(), holder);
                    showUsageChartData(statistics);
                    showFavoringUser(statistics);
                }
            }
        });
    }

    public void showUsageChartData(AssetStatistics statistics) {
        AssetUsageChartBuilder builder = new AssetUsageChartBuilder();
        builder.buildSevenDayChart(getContext(), chart, statistics);
    }

    private void showFavoringUser(AssetStatistics statistics) {
        int favoringUserID = statistics.getFavoringUser();
        try {
            Database db = Database.getInstance(getContext());
            User u = db.findUserByID(favoringUserID);

            loadImage(u, userIcon);
            username.setText(u.getName());
            userCard.setVisibility(View.VISIBLE);
        } catch (UserNotFoundException e) {
            userCard.setVisibility(View.GONE);
        }
    }

    private void loadImage(User u, ImageView iv) {
        boolean avatarEnabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(
                getContext().getString(R.string.pref_key_users_enable_avatars), false);
        if (avatarEnabled) {
            AvatarHelper avatarHelper = new AvatarHelper();
            avatarHelper.loadAvater(getContext(), u, iv, 90);
        }
    }
}
