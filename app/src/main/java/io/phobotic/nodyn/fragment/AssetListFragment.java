/*
 * Copyright (c) 2017 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn.fragment;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn.list.VerticalSpaceItemDecoration;
import io.phobotic.nodyn.list.adapter.AssetSection;
import io.phobotic.nodyn.service.SyncService;

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
        List<Asset> assets = db.getAssets();
        if (assets.isEmpty()) {
            showEmptyListError();
        } else {
            showList(assets);
        }
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
    private void showList(List<Asset> assets) {
        assets = applyDefaultFilter(assets);
        showFilteredList(assets);

        if (filterCountShown) {
            animateOutFilterBar();
        }
    }

    //filter the default asset listview to include only the status selected in settings
    private List<Asset> applyDefaultFilter(List<Asset> assets) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean showAll = prefs.getBoolean(getString(R.string.pref_key_asset_status_show_all),
                Boolean.valueOf(getString(R.string.pref_default_asset_status_show_all)));
        if (showAll) {
            return assets;
        } else {
            List<Asset> filteredList = new ArrayList<>();
            Set<String> chosenStatuses = prefs.getStringSet(getString(
                    R.string.pref_key_asset_status_selected_statuses), new HashSet<String>());
            for (Asset asset : assets) {
                if (chosenStatuses.contains(asset.getStatus())) {
                    filteredList.add(asset);
                }
            }

            return filteredList;
        }
    }

    private void showFilteredList(List<Asset> assets) {
        recyclerView.setVisibility(View.VISIBLE);
        errEmptyList.setVisibility(View.GONE);

        SectionedRecyclerViewAdapter sectionedAdapter = new SectionedRecyclerViewAdapter();
        Map<String, List<Asset>> assetMap = getAssetMap(assets);
        for (String key : assetMap.keySet()) {
            List<Asset> sectionAssets = assetMap.get(key);
            String manufacturer = UNKNOWN;
            if (sectionAssets.size() > 0) {
                manufacturer = sectionAssets.get(0).getManufacturer();
            }

            String model = UNKNOWN;
            if (sectionAssets.size() > 0) {
                model = sectionAssets.get(0).getModel();
            }
            sectionedAdapter.addSection(new AssetSection(getContext(), manufacturer, model, sectionAssets,
                    mListener));
        }

        recyclerView.setAdapter(sectionedAdapter);
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

    private Map<String, List<Asset>> getAssetMap(List<Asset> assets) {
        Map<String, List<Asset>> assetMap = new TreeMap<>();
        for (Asset asset : assets) {
            String manufacturer = asset.getManufacturer();
            if (manufacturer == null || manufacturer.length() == 0) {
                manufacturer = UNKNOWN;
            }

            String model = asset.getModel();
            if (model == null || model.length() == 0) {
                model = UNKNOWN;
            }

            String key = manufacturer + "-" + model;

            List<Asset> curAssets = assetMap.get(key);
            if (curAssets == null) {
                curAssets = new ArrayList<>();
            }

            curAssets.add(asset);
            assetMap.put(key, curAssets);
        }

        return assetMap;
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
        List<Asset> assets = db.getAssets();
        assets = filterList(assets, filter);
        String filterText = String.format(getResources().getString(R.string.asset_filter_count),
                assets.size());
        fadeInText(filterCount, filterText);

        if (!filterCountShown) {
            animateInFilterBar();
        }

        showFilteredList(assets);
    }

    private List<Asset> filterList(@NotNull List<Asset> assets, @NotNull String filter) {
        List<Asset> filteredList = new ArrayList<>();

        for (Asset asset : assets) {
            if (assetMatchesFilter(asset, filter)) {
                filteredList.add(asset);
            }
        }

        return filteredList;
    }

    private boolean assetMatchesFilter(@NotNull Asset asset, @NotNull String filter) {
        boolean filterMatches = false;
        //do all the matching case insensitive
        filter = filter.toUpperCase();

        if (asset.getName().toUpperCase().contains(filter)) {
            return true;
        } else if (asset.getStatus().toUpperCase().contains(filter)) {
            return true;
        } else if (asset.getAssignedTo().toUpperCase().contains(filter)) {
            return true;
        } else if (asset.getModel().toUpperCase().contains(filter)) {
            return true;
        } else if (asset.getManufacturer().toUpperCase().contains(filter)) {
            return true;
        } else if (asset.getTag().toUpperCase().contains(filter)) {
            return true;
        } else if (asset.getCategory().toUpperCase().contains(filter)) {
            return true;
        } else if (asset.getCompanyName().toUpperCase().contains(filter)) {
            return true;
        } else if (String.valueOf(asset.getId()).contains(filter)) {
            return true;
        } else if (asset.getSerial().toUpperCase().contains(filter)) {
            return true;
        } else if (asset.getNotes().toUpperCase().contains(filter)) {
            return true;
        }

        return filterMatches;
    }
}
