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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextSwitcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.fragment.SimplifiedAsset;
import io.phobotic.nodyn_app.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn_app.list.adapter.AssetStatusSection;
import io.phobotic.nodyn_app.service.SyncService;

/**
 * Created by Jonathan Nelson on 4/24/19.
 */
public class AssetStatusListFragment extends Fragment {
    public static final String STATUS_PAST_DUE = "Past Due";
    public static final String STATUS_OUT = "Checked Out";
    public static final String STATUS_AVAILABLE = "Available";
    private static final String TAG = AssetStatusListFragment.class.getSimpleName();
    private static final String ARG_COLUMN_COUNT = "column-count";
    private Database db;
    private int columnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private View rootView;
    private RecyclerView recyclerView;
    private View errEmptyList;


    private BroadcastReceiver br;
    private int scrollPosition = 0;
    private int scrollOffset;
    private List<SimplifiedAsset> assets;

    public static AssetListFragment newInstance(int columnCount) {
        AssetListFragment fragment = new AssetListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AssetStatusListFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        db = Database.getInstance(context);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Assets");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            columnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                switch (action) {
                    case SyncService.BROADCAST_SYNC_FINISH:
                    case SyncService.BROADCAST_SYNC_FAIL:
                        saveScrollPosition();
                        loadData();
                        resumeScrollPosition();
                        break;
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_asset_list, container, false);
        init();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction((SyncService.BROADCAST_SYNC_FINISH));
        filter.addAction(SyncService.BROADCAST_SYNC_FAIL);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(br, filter);

        loadData();

        //loadData() will also resume the scroll position after data has loaded.  This may take
        //+ some time however, so go ahead and resume the last known position now.
        resumeScrollPosition();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(br);
        saveScrollPosition();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void init() {
        recyclerView = rootView.findViewById(R.id.list);
        errEmptyList = rootView.findViewById(R.id.empty_list);

        initRecyclerView();
        loadData();
    }

    private void initRecyclerView() {
        if (columnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnCount));
        }

        final float spacingTop = getResources().getDimension(R.dimen.list_item_spacing_top);
        final float spacingBottom = getResources().getDimension(R.dimen.list_item_spacing_bottom);
    }

    public void saveScrollPosition() {
        scrollPosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                .findFirstVisibleItemPosition();
        View startView = recyclerView.getChildAt(0);
        scrollOffset = (startView == null) ? 0 : (startView.getTop() - recyclerView.getPaddingTop());
    }

    private void loadData() {
        showSpinner();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Database db = Database.getInstance(getContext());
                List<Asset> assetList = db.getAssets();
                SimplifiedAsset.Builder b = new SimplifiedAsset.Builder();
                final List<SimplifiedAsset> simplifiedAssets = new ArrayList<>();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                boolean allowAllModels = prefs.getBoolean(getString(R.string.pref_key_check_out_all_models), false);
                Set<String> allowedModels = prefs.getStringSet(getString(R.string.pref_key_check_out_models), new HashSet<String>());

                boolean allowAllStatuses = prefs.getBoolean(getString(R.string.pref_key_asset_status_allow_all), false);
                Set<String> allowedStatuses = prefs.getStringSet(getString(R.string.pref_key_asset_status_allowed_statuses), new HashSet<String>());

                for (Asset a : assetList) {
                    if (allowAllModels || allowedModels.contains(String.valueOf(a.getModelID()))) {
                        if (allowAllStatuses || allowedStatuses.contains(String.valueOf(a.getStatusID()))) {
                            SimplifiedAsset sa = b.fromAsset(getContext(), a);
                            simplifiedAssets.add(sa);
                        }
                    }
                }

                Activity a = getActivity();
                if (a != null) {
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (simplifiedAssets == null || simplifiedAssets.isEmpty()) {
                                showEmptyListError();
                            } else {
                                showList(simplifiedAssets);
                            }
                        }
                    });
                }

                //adjust the scroll position to be near where we left off.  This will default to the top
                //+ of the list if we have not saved the position first
                resumeScrollPosition();
            }
        });
    }

    public void resumeScrollPosition() {
        ((LinearLayoutManager) recyclerView.getLayoutManager())
                .scrollToPositionWithOffset(scrollPosition, scrollOffset);
    }

    private void showSpinner() {
        View spinner = rootView.findViewById(R.id.busy_spinner);
        spinner.setVisibility(View.VISIBLE);
    }

    private void showEmptyListError() {
        recyclerView.setVisibility(View.GONE);
        errEmptyList.setVisibility(View.VISIBLE);
        hideSpinner();
    }

    private void showList(List<SimplifiedAsset> assets) {
        hideSpinner();
        SectionedRecyclerViewAdapter adapter = buildAdapter(assets);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void hideSpinner() {
        View spinner = rootView.findViewById(R.id.busy_spinner);
        spinner.setVisibility(View.GONE);
    }

    private SectionedRecyclerViewAdapter buildAdapter(List<SimplifiedAsset> assets) {
        Map<String, List<SimplifiedAsset>> assetSectionMap = buildStatusMap(assets);
        SectionedRecyclerViewAdapter sectionedAdapter = new SectionedRecyclerViewAdapter();

        for (Map.Entry<String, List<SimplifiedAsset>> entry : assetSectionMap.entrySet()) {
            String status = entry.getKey();
            List<SimplifiedAsset> sectionAssets = entry.getValue();
            SectionKey key = new SectionKey(status, sectionAssets.size());
            Log.d(TAG, String.format("Adding section %s to adapter with %d assets", key.getStatus(), key.getTotal()));

            Integer highlightColor = null;
            if (status == STATUS_PAST_DUE) {
                highlightColor = getResources().getColor(R.color.asset_past_due_background);
                ;
            } else if (status == STATUS_OUT) {
                highlightColor = getResources().getColor(R.color.asset_checked_out_background);
            } else if (status == STATUS_AVAILABLE) {
                highlightColor = getResources().getColor(R.color.asset_available_background);
            }

            sectionedAdapter.addSection(new AssetStatusSection(getContext(), key.getStatus(),
                    key.getTotal(), sectionAssets, highlightColor, mListener));
        }

        return sectionedAdapter;
    }

    /**
     * Convert a list of Assets into a Map.  The returned Map will be organized by status (past due,
     * assigned, available, other)
     * and model
     *
     * @param assets
     * @return
     */
    private Map<String, List<SimplifiedAsset>> buildStatusMap(List<SimplifiedAsset> assets) {
        LinkedHashMap<String, List<SimplifiedAsset>> assetMap = new LinkedHashMap<>();


        //go ahead and add the section keys early to make sure they retain their ordering
        assetMap.put(STATUS_PAST_DUE, new ArrayList<SimplifiedAsset>());
        assetMap.put(STATUS_OUT, new ArrayList<SimplifiedAsset>());
        assetMap.put(STATUS_AVAILABLE, new ArrayList<SimplifiedAsset>());


        for (SimplifiedAsset asset : assets) {
            int statusID = asset.getStatusID();

            String status;
            if (asset.getAssignedToID() == -1) {
                status = STATUS_AVAILABLE;
            } else {
                status = STATUS_OUT;

                //find out if this asset is past due
                if (asset.getExpectedCheckin() != -1) {
                    Date expectedCheckin = new Date(asset.getExpectedCheckin());
                    Date now = new Date();
                    if (now.after(expectedCheckin)) {
                        status = STATUS_PAST_DUE;
                    }
                }
            }


            List<SimplifiedAsset> curAssets = assetMap.get(status);
            if (curAssets == null) {
                curAssets = new ArrayList<>();
            }

            curAssets.add(asset);
            assetMap.put(status, curAssets);
        }

        //specifically sort the past due list so that the assets that are past due the longest are at the top
        Collections.sort(assetMap.get(STATUS_PAST_DUE), new Comparator<SimplifiedAsset>() {
            @Override
            public int compare(SimplifiedAsset o1, SimplifiedAsset o2) {
                return ((Long) o1.getExpectedCheckin()).compareTo(o2.getExpectedCheckin());
            }
        });


        //sort the other two by asset tag
        Collections.sort(assetMap.get(STATUS_OUT), new Comparator<SimplifiedAsset>() {
            @Override
            public int compare(SimplifiedAsset o1, SimplifiedAsset o2) {
                String t = o1.getTag() == null ? "" : o1.getTag();
                return t.compareTo(o2.getTag());
            }
        });

        Collections.sort(assetMap.get(STATUS_AVAILABLE), new Comparator<SimplifiedAsset>() {
            @Override
            public int compare(SimplifiedAsset o1, SimplifiedAsset o2) {
                String t = o1.getTag() == null ? "" : o1.getTag();
                return t.compareTo(o2.getTag());
            }
        });

        return assetMap;
    }

    private void fadeInText(TextSwitcher view, String newText) {
        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        view.setOutAnimation(out);
        view.setInAnimation(in);
        view.setText(newText);
    }

    public interface MapBuiltListener {
        void onMapBuilt(SectionedRecyclerViewAdapter adapter);
    }

    private class SectionKey implements Comparable<SectionKey> {
        private String status;
        private int total;

        public SectionKey(String status, int total) {
            if (status == null) {
                throw new IllegalArgumentException("status must not be null");
            }
            this.status = status;
            this.total = total;
        }

        public int getTotal() {
            return total;
        }

        public SectionKey setTotal(int total) {
            this.total = total;
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SectionKey)) {
                return false;
            } else {
                return this.status.equals(((SectionKey) obj).getStatus());
            }
        }

        public String getStatus() {
            return status;
        }

        public SectionKey setStatus(String status) {
            this.status = status;
            return this;
        }

        @Override
        public int compareTo(@NonNull SectionKey o) {
            return this.status.compareTo(o.getStatus());
        }
    }
}
