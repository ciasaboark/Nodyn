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
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.ManufacturerNotFoundException;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Manufacturer;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn_app.list.AssetListFilterer;
import io.phobotic.nodyn_app.list.VerticalSpaceItemDecoration;
import io.phobotic.nodyn_app.list.adapter.AssetSection;
import io.phobotic.nodyn_app.service.SyncService;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class AssetListFragment extends Fragment {
    private static final String TAG = AssetListFragment.class.getSimpleName();
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String UNKNOWN = "Unknown";

    private Database db;
    private int columnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private View rootView;
    private RecyclerView recyclerView;
    private View errEmptyList;
    private boolean searchBarShown = false;
    private boolean filterCountShown = false;
    private Toolbar searchBar;
    private EditText searchInput;
    private ImageButton clearButton;
    private TextSwitcher filterCount;
    private RelativeLayout filterCountBox;


    private FloatingActionButton buttonSync;
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
    public AssetListFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        db = Database.getInstance(context);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Assets");
        }

        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
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
                        showProperView();
                        break;
                    case SyncService.BROADCAST_SYNC_FAIL:
                        String errorMsg = intent.getExtras().getString(
                                SyncService.BROADCAST_SYNC_MESSAGE);
                        showProperView();
                        break;
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_asset_list, container, false);
        setHasOptionsMenu(true);
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

        if (searchBarShown) {
            filterListAndShow(searchInput.getText().toString());
        } else {
            showProperView();
        }

        ((LinearLayoutManager) recyclerView.getLayoutManager())
                .scrollToPositionWithOffset(scrollPosition, scrollOffset);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(br);
        scrollPosition = ((LinearLayoutManager) recyclerView.getLayoutManager())
                .findFirstVisibleItemPosition();
        View startView = recyclerView.getChildAt(0);
        scrollOffset = (startView == null) ? 0 : (startView.getTop() - recyclerView.getPaddingTop());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_asset_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean consumed = false;

        Log.d(TAG, item.toString());
        switch (item.getItemId()) {
            case R.id.action_filter:
                if (searchBarShown) {
                    searchInput.setEnabled(false);
                    animateOutSearchBar();

                    //reset the list to show all assets
                    searchInput.setText("");
                    showProperView();
                } else {
                    searchInput.setEnabled(true);
                    animateInSearchBar();
                }
                consumed = true;
                break;
        }

        return consumed;
    }

    private void animateOutSearchBar() {
        int height = searchBar.getLayoutParams().height;
        Animator.AnimatorListener listener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                searchBarShown = !searchBarShown;
                if (searchBarShown) {
                    searchInput.requestFocus();
                } else {
                    searchInput.clearFocus();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        animateSearchBar(searchBar, height, 0, listener);
    }

    private void animateInSearchBar() {
        searchBar.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int height = searchBar.getMeasuredHeight();
        Animator.AnimatorListener listener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                searchBarShown = !searchBarShown;
                if (searchBarShown) {
                    searchInput.requestFocus();
                } else {
                    searchInput.clearFocus();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        animateSearchBar(searchBar, 0, height, listener);
    }

    /**
     * Either display the list, the empty list error, or the sync adapter error
     */
    private void showProperView() {
        showSpinner();

        AssetFetcherTask task = new AssetFetcherTask(getContext(), new OnAssetsLoaded() {
            @Override
            public void onAssetsLoaded(final List<SimplifiedAsset> assetList) {
                Activity a = getActivity();
                if (a != null) {
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            assets = assetList;
                            if (assetList.isEmpty()) {
                                showEmptyListError();
                            } else {
                                showDefaultAssetList();
                            }
                        }
                    });
                }
            }
        });

        task.execute();
    }

    private void showSpinner() {
        View spinner = rootView.findViewById(R.id.busy_spinner);
        spinner.setVisibility(View.VISIBLE);
    }

    private void showEmptyListError() {
        recyclerView.setVisibility(View.GONE);
        errEmptyList.setVisibility(View.VISIBLE);
    }

    /**
     * Show all assets except those that are archived or marked lost
     *
     * @param assets
     */
    private void showDefaultAssetList() {
        AssetListFilterer filterer = new AssetListFilterer(getContext(), assets);
        filterer.filterList(null, true, new AssetListFilterer.AssetListFilterListener() {
            @Override
            public void onAssetListBeginFilter() {
                Activity a = getActivity();
                if (a != null) {
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showSpinner();
                        }
                    });
                }
            }

            @Override
            public void onAssetListFinishFilter(final List<SimplifiedAsset> filteredAssets) {
                Activity a = getActivity();
                if (a != null) {
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showFilteredList(filteredAssets);

                            if (filterCountShown) {
                                animateOutFilterBar();
                            }
                        }
                    });
                }
            }
        });
    }

    private void showFilteredList(List<SimplifiedAsset> filteredList) {

        ListMapperTask task = new ListMapperTask(filteredList, new MapBuiltListener() {
            @Override
            public void onMapBuilt(final SectionedRecyclerViewAdapter adapter) {
                Activity a = getActivity();
                if (a != null) {
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            recyclerView.setVisibility(View.VISIBLE);
                            errEmptyList.setVisibility(View.GONE);

                            recyclerView.setAdapter(adapter);

                            hideSpinner();
                        }
                    });
                }
            }
        });
        task.execute();


    }

    private void animateOutFilterBar() {
        int height = filterCountBox.getLayoutParams().height;
        Animator.AnimatorListener listener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                filterCountShown = false;
                fadeInText(filterCount, "");
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        animateSearchBar(filterCountBox, height, 0, listener);
    }

    private void hideSpinner() {
        View spinner = rootView.findViewById(R.id.busy_spinner);
        spinner.setVisibility(View.GONE);
    }

    private void fadeInText(TextSwitcher view, String newText) {
        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        view.setOutAnimation(out);
        view.setInAnimation(in);
        view.setText(newText);
    }

    private void animateSearchBar(final View view, int startHeight, int endHeight, Animator.AnimatorListener listener) {
        ValueAnimator va = ValueAnimator.ofInt(startHeight, endHeight);
        va.setDuration(250);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                view.getLayoutParams().height = value.intValue();
                view.requestLayout();
            }
        });
        if (listener != null) {
            va.addListener(listener);
        }
        va.start();
    }

    private void animateInFilterBar() {
        filterCountBox.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int height = filterCountBox.getMeasuredHeight();
        Animator.AnimatorListener listener = new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                filterCountShown = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        };

        animateSearchBar(filterCountBox, 0, height, listener);
    }

    private void init() {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        buttonSync = (FloatingActionButton) rootView.findViewById(R.id.sync_button);
        errEmptyList = rootView.findViewById(R.id.empty_list);
        filterCountBox = (RelativeLayout) rootView.findViewById(R.id.filter_count_box);
        filterCount = (TextSwitcher) rootView.findViewById(R.id.filter_count);
        filterCount.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(getContext());
                t.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                t.setTextAppearance(getContext(), android.R.style.TextAppearance_Material_Small_Inverse);
                return t;
            }
        });
        filterCount.setCurrentText("");

        searchBar = (Toolbar) rootView.findViewById(R.id.inner_toolbar);
        searchInput = (EditText) rootView.findViewById(R.id.search_input);
        searchInput.setEnabled(false);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {

                } else {

                }

                performSearch();
            }
        });
        searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        clearButton = (ImageButton) rootView.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchInput.setText("");
                performSearch();
            }
        });

        final Context ctx = AssetListFragment.this.getActivity();
        buttonSync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ctx, SyncService.class);
                ctx.startService(i);
            }
        });


        initList();
    }

    private void performSearch() {
        String filter = searchInput.getText().toString();
        if (filter == null || filter.length() == 0) {
            showProperView();
        } else {
            filterListAndShow(filter);
        }
    }

    private void initList() {
        if (columnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), columnCount));
        }

        final float spacingTop = getResources().getDimension(R.dimen.list_item_spacing_top);
        final float spacingBottom = getResources().getDimension(R.dimen.list_item_spacing_bottom);
        recyclerView.addItemDecoration(new VerticalSpaceItemDecoration(spacingTop, spacingBottom));
    }

    private void filterListAndShow(String filter) {
        AssetListFilterer filterer = new AssetListFilterer(getContext(), assets);
        filterer.filterList(filter, false, new AssetListFilterer.AssetListFilterListener() {
            @Override
            public void onAssetListBeginFilter() {
                Activity a = getActivity();
                if (a != null) {
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showSpinner();
                        }
                    });
                }
            }

            @Override
            public void onAssetListFinishFilter(final List<SimplifiedAsset> filteredAssets) {
                Activity a = getActivity();
                if (a != null) {
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String filterText = String.format(getResources()
                                    .getString(R.string.asset_filter_count), filteredAssets.size());
                            fadeInText(filterCount, filterText);

                            if (!filterCountShown) {
                                animateInFilterBar();
                            }

                            showFilteredList(filteredAssets);
                        }
                    });
                }
            }
        });


    }

    private interface OnAssetsLoaded {
        void onAssetsLoaded(List<SimplifiedAsset> assets);
    }

    public interface MapBuiltListener {
        void onMapBuilt(SectionedRecyclerViewAdapter adapter);
    }

    private class AssetFetcherTask extends AsyncTask<Void, Void, Void> {
        private final Context context;
        private final OnAssetsLoaded listener;

        public AssetFetcherTask(Context context, OnAssetsLoaded listener) {
            this.context = context;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Database db = Database.getInstance(context);
            List<Asset> assetList = db.getAssets();
            SimplifiedAsset.Builder b = new SimplifiedAsset.Builder();
            List<SimplifiedAsset> simplifiedAssets = new ArrayList<>();
            for (Asset a : assetList) {
                SimplifiedAsset sa = b.fromAsset(getContext(), a);
                simplifiedAssets.add(sa);
            }

            if (listener != null) {
                listener.onAssetsLoaded(simplifiedAssets);
            }

            return null;
        }
    }

    private class ListMapperTask extends AsyncTask<Void, Void, Void> {
        private final List<SimplifiedAsset> filteredList;
        private final MapBuiltListener listener;


        public ListMapperTask(List<SimplifiedAsset> filteredList, MapBuiltListener listener) {
            this.listener = listener;
            this.filteredList = filteredList;
        }


        @Override
        protected Void doInBackground(Void... params) {
            Map<SectionKey, List<SimplifiedAsset>> assetSectionMap = getAssetSectionMap(filteredList);
            SectionedRecyclerViewAdapter sectionedAdapter = new SectionedRecyclerViewAdapter();

            for (SectionKey key : assetSectionMap.keySet()) {
                List<SimplifiedAsset> sectionAssets = assetSectionMap.get(key);

                sectionedAdapter.addSection(new AssetSection(getContext(), key.getManufacturer(),
                        key.getModel(), sectionAssets, mListener));
            }

            if (listener != null) {
                listener.onMapBuilt(sectionedAdapter);
            }

            return null;
        }

        /**
         * Convert a list of Assets into a Map.  The returned Map will be organized by manufacturer
         * and model
         *
         * @param assets
         * @return
         */
        private Map<SectionKey, List<SimplifiedAsset>> getAssetSectionMap(List<SimplifiedAsset> assets) {
            Map<SectionKey, List<SimplifiedAsset>> assetMap = new TreeMap<>();

            for (SimplifiedAsset asset : assets) {
                int manufacturerID = asset.getManufacturerID();
                int modelID = asset.getModelID();

                String manufacturer = "UNKNOWN";
                try {
                    Manufacturer m = db.findManufacturerByID(manufacturerID);
                    manufacturer = m.getName();
                } catch (ManufacturerNotFoundException e) {
                }

                String model = "UNKNOWN";
                try {
                    Model m = db.findModelByID(modelID);
                    model = m.getName();
                } catch (ModelNotFoundException e) {
                }

                SectionKey key = new SectionKey(manufacturer, model);

                List<SimplifiedAsset> curAssets = assetMap.get(key);
                if (curAssets == null) {
                    curAssets = new ArrayList<>();
                }

                curAssets.add(asset);
                assetMap.put(key, curAssets);
            }

            //sort the individual lists by tag
            for (SectionKey key : assetMap.keySet()) {
                List<SimplifiedAsset> a = assetMap.get(key);
                Collections.sort(a, new Comparator<SimplifiedAsset>() {
                    @Override
                    public int compare(SimplifiedAsset o1, SimplifiedAsset o2) {
                        return o1.getTag().compareTo(o2.getTag());
                    }
                });
            }

            return assetMap;
        }


    }

    private class SectionKey implements Comparable<SectionKey> {
        private String manufacturer;
        private String model;
        private int available;
        private int assigned;

        public SectionKey(String manufacturer, String model) {
            if (manufacturer == null || model == null) {
                throw new IllegalArgumentException("manufacturer and model must not be null");
            }
            this.manufacturer = manufacturer;
            this.model = model;
        }

        public int getAvailable() {
            return available;
        }

        public SectionKey setAvailable(int available) {
            this.available = available;
            return this;
        }

        public int getAssigned() {
            return assigned;
        }

        public SectionKey setAssigned(int assigned) {
            this.assigned = assigned;
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SectionKey)) {
                return false;
            } else {
                return this.manufacturer.equals(((SectionKey) obj).getManufacturer()) &&
                        this.model.equals(((SectionKey) obj).getModel());
            }
        }

        public String getManufacturer() {
            return manufacturer;
        }

        public String getModel() {
            return model;
        }

        @Override
        public int compareTo(@NonNull SectionKey o) {
            String thisKey = manufacturer + model;
            String thatKey = o.getManufacturer() + o.getModel();
            return thisKey.compareTo(thatKey);
        }
    }


}
