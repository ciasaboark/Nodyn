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


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.sync.Action;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.list.adapter.ActionRecyclerViewAdapter;
import io.phobotic.nodyn_app.list.decorator.VerticalSpaceItemDecoration;
import io.phobotic.nodyn_app.sync.SyncManager;
import io.phobotic.nodyn_app.sync.adapter.SyncAdapter;
import io.phobotic.nodyn_app.sync.adapter.SyncException;
import io.phobotic.nodyn_app.sync.adapter.SyncNotSupportedException;

import static java.lang.Thread.sleep;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ActionHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActionHistoryFragment extends Fragment {
    private static final String TAG = ActionHistoryFragment.class.getSimpleName();
    private static final String ASSET = "asset";
    private static final String USER = "user";
    List<Action> actionList = new ArrayList<>();
    private Asset asset;
    private User user;
    private View rootView;
    private View list;
    private View loading;
    private View localUpToDate;
    private View localOnlyWarning;
    private View emptyListWarning;
    private boolean isLoading = true;
    private boolean canFetchMoreRecords = true;
    private int MAX_RECORDS = 30;
    private SwipeRefreshLayout swipeRefresh;
    private TextView remoteError;
    private RecyclerView recyclerView;
    private boolean scrollListenerAttached = false;
    private int curPage = 0;


    public static ActionHistoryFragment newInstance() {
        ActionHistoryFragment fragment = new ActionHistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static ActionHistoryFragment newInstance(Asset asset) {
        ActionHistoryFragment fragment = new ActionHistoryFragment();
        Bundle args = new Bundle();
        args.putSerializable(ASSET, asset);
        fragment.setArguments(args);
        return fragment;
    }

    public static ActionHistoryFragment newInstance(User user) {
        ActionHistoryFragment fragment = new ActionHistoryFragment();
        Bundle args = new Bundle();
        args.putSerializable(USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    public ActionHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            asset = (Asset) getArguments().getSerializable(ASSET);
            user = (User) getArguments().getSerializable(USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_action_history, container, false);

        init();
        return rootView;
    }

    private void init() {
//        swipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
//        swipeRefresh.setColorSchemeResources(R.color.refresh_progress_1);
//        swipeRefresh.setRefreshing(false);
//        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                refresh();
//                swipeRefresh.setRefreshing(false);
//            }
//        });



        list = rootView.findViewById(R.id.list);
        recyclerView = rootView.findViewById(R.id.recyclerview);
        loading = rootView.findViewById(R.id.loading);
        localUpToDate = rootView.findViewById(R.id.sync_up_to_date);
        emptyListWarning = rootView.findViewById(R.id.empty_list_warning);
        localOnlyWarning = rootView.findViewById(R.id.local_only_warning);
        remoteError = rootView.findViewById(R.id.remote_error);

        initList();
        refresh();
    }

    private void initList() {
        final float spacingTop = getResources().getDimension(R.dimen.list_item_spacing_top);
        final float spacingBottom = getResources().getDimension(R.dimen.list_item_spacing_bottom);
        RecyclerView.ItemDecoration decoration = new VerticalSpaceItemDecoration(spacingTop, spacingBottom);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.addItemDecoration(decoration);
        recyclerView.setLayoutManager(layoutManager);

    }

    private void refresh() {
        list.setVisibility(View.GONE);
        localUpToDate.setVisibility(View.GONE);
        localOnlyWarning.setVisibility(View.GONE);
        emptyListWarning.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);
        curPage = 0;
        canFetchMoreRecords = true;
        isLoading = false;

        actionList = new ArrayList<>();
        recyclerView.setAdapter(new ActionRecyclerViewAdapter(actionList, null));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        FetchUnsyncedAsyncTask unsyncedAsyncTask = new FetchUnsyncedAsyncTask();
        if (asset != null) {
            unsyncedAsyncTask.execute(asset);
        } else if (user != null) {
            unsyncedAsyncTask.execute(user);
        } else {
            unsyncedAsyncTask.execute();
        }
    }

    private void showUnsyncedActions(final List<Action> unsyncedActions) {
        //this will be called from the async task, so make sure we are running on the UI thread
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //just add the items to the front of the list for now, then begin pulling
                    //+ remote records
                    actionList.addAll(0, unsyncedActions);
                    if (unsyncedActions.isEmpty()) {
                        showLocalUpToDateMessage();
                    }

                    fetchPage();
                }
            });
        }
    }

    /**
     * Show the local up to date sync message for a few seconds
     */
    private void showLocalUpToDateMessage() {
        Animation enterTop = AnimationUtils.loadAnimation(getContext(), R.anim.enter_from_top);
        enterTop.setInterpolator(new AccelerateInterpolator());
        enterTop.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                localUpToDate.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideLocalUptoDateMessage();
                    }
                }, 5000);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        enterTop.setDuration(1000);
        localUpToDate.startAnimation(enterTop);
    }

    private void hideLocalUptoDateMessage() {
        Context context = getContext();
        if (context != null) {
            Animation exitTop = AnimationUtils.loadAnimation(context, R.anim.exit_top);
            exitTop.setInterpolator(new AccelerateInterpolator());
            exitTop.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    localUpToDate.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            exitTop.setDuration(1000);
            localUpToDate.startAnimation(exitTop);
        }
    }

    private void showRemoteHistory(final List<Action> remoteActions, final int page) {
        //this will be called from the async task, so make sure we are running on the UI thread
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //if this is any page past the first (0) then we need to remove the null object
                    //+ at the end of the list.
                    if (page > 0) {
                        actionList.remove(actionList.size() - 1);
                        RecyclerView.Adapter adapter = recyclerView.getAdapter();
                        adapter.notifyItemRemoved(actionList.size());
                    }

                    for (Action a : remoteActions) {
                        actionList.add(a);
                        recyclerView.getAdapter().notifyItemInserted(actionList.size());
                    }

                    //if we can fetch more records then add a null item to the end of the list
                    //+ this will be replaced with a spinner
                    if (canFetchMoreRecords) {
                        actionList.add(null);
                        recyclerView.getAdapter().notifyItemInserted(actionList.size() - 1);
                    } else {

                    }

                    //if the remote history pull succeeded then show
                    if (actionList.isEmpty()) {
                        showView(emptyListWarning);
                    } else {
                        showList();
                    }

                    loading.setVisibility(View.GONE);
                }
            });
        }

        attachScrollListenerIfNeeded();
    }

    private void fetchPage() {
        FetchRemoteAsyncTask fetchRemoteAsyncTask = new FetchRemoteAsyncTask();
        if (asset != null) {
            fetchRemoteAsyncTask.execute(asset, curPage);
        } else if (user != null) {
            fetchRemoteAsyncTask.execute(user, curPage);
        } else {
            fetchRemoteAsyncTask.execute(null, curPage);
        }
    }

    private void showView(final View v) {
        v.setVisibility(View.VISIBLE);
    }

    private void showList() {
        showView(list);
        //todo notify on each item inserted instead of all at once
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    private void attachScrollListenerIfNeeded() {
        if (!scrollListenerAttached) {
            scrollListenerAttached = true;
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrolled(final RecyclerView recyclerView, int dx, int dy) {
                    //trigger loading the next page before we actually reach the bottom
                    LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
                    int totalItems = recyclerView.getAdapter().getItemCount();
                    int bottomItem = lm.findLastVisibleItemPosition();

//                    if (!recyclerView.canScrollVertically(1)) {
                    if (totalItems - bottomItem <= 10) {
                        //we have scrolled to the bottom
                        if (isLoading) {
                            Log.d(TAG, "List loading, skipping fetching older actions");
                            return;
                        } else if (!canFetchMoreRecords) {
                            Log.d(TAG, "Will not fetch older items");
                        } else {
                            Log.d(TAG, "Scrollview has reached bottom");
                            fetchNextPage();
                        }
                    }

                }
            });
        }
    }

    private void fetchNextPage() {
        curPage++;
        isLoading = true;
        fetchPage();
    }

    private void refetchCurrentPage() {
        isLoading = true;
        fetchPage();
    }

    private void showRemoteError(final String reason, final String message) {
        Activity a = getActivity();
        if (a != null) {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //show the local history
                    FetchSyncedAsyncTask fetchSyncedAsyncTask = new FetchSyncedAsyncTask();
                    if (asset != null) {
                        fetchSyncedAsyncTask.execute(asset, curPage);
                    } else if (user != null) {
                        fetchSyncedAsyncTask.execute(user, curPage);
                    } else {
                        fetchSyncedAsyncTask.execute(null, curPage);
                    }

                    remoteError.setText(message);
                    showView(localOnlyWarning);
                }
            });
        }

    }

    private void showSyncedActions(final List<Action> syncedActions) {
        //this will be called from the async task, so make sure we are running on the UI thread
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //
                    actionList.addAll(syncedActions);

                    //show the actions if ther are any
                    if (actionList.isEmpty()) {
                        showView(emptyListWarning);
                    } else {
                        showList();
                        showView(localOnlyWarning);
                    }

                    hideView(loading);
                }
            });
        }
    }

    private void hideView(final View v) {
        if (v.getVisibility() != View.VISIBLE) {
            return;
        }

        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(fadeOut);
    }

    private List<Action> filterByAsset(Asset asset, List<Action> actions) {
        List<Action> filteredList = new ArrayList<>();

        for (Action action : actions) {
            if (action.getAssetID() == asset.getId()) {
                filteredList.add(action);
            }
        }

        return filteredList;
    }

    private List<Action> filterByUser(User user, List<Action> actions) {
        List<Action> filteredList = new ArrayList<>();

        for (Action action : actions) {
            if (action.getUserID() == user.getId()) {
                filteredList.add(action);
            }
        }


        return filteredList;
    }

    private class FetchUnsyncedAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            try {
                Object obj = null;
                if (params.length > 0) {
                    obj = params[0];
                }

                Database db = Database.getInstance(getContext());
                List<Action> actions = db.getUnsyncedActions();
                if (obj == null) {
                    //pull all records
                } else if (obj instanceof Asset) {
                    //filter by the provided asset
                    actions = filterByAsset((Asset) obj, actions);
                } else if (obj instanceof User) {
                    //filter by the provided user
                    actions = filterByUser((User) obj, actions);
                } else {
                    //what were we passed?
                    actions = new ArrayList<>();
                }

                showUnsyncedActions(actions);
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);

                //make sure we call showUnsyncedActions so we can continue pulling records
                showUnsyncedActions(new ArrayList<Action>());
            }

            return null;
        }
    }

    private class FetchSyncedAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            try {
                Object obj = null;
                if (params.length > 0) {
                    obj = params[0];
                }

                Database db = Database.getInstance(getContext());
                List<Action> actions = db.getSyncedActions();
                if (obj == null) {
                    //keep all the records
                } else if (obj instanceof Asset) {
                    //filter by the provided asset
                    actions = filterByAsset((Asset) obj, actions);
                } else if (obj instanceof User) {
                    //filter by the provided user
                    actions = filterByUser((User) obj, actions);
                } else {
                    //what were we passed?
                    actions = new ArrayList<>();
                }

                showSyncedActions(actions);
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);

                //make sure we call showSyncedActions so the proper view can be shown
                showSyncedActions(new ArrayList<Action>());
            }

            return null;
        }
    }

    private class FetchRemoteAsyncTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            isLoading = true;
            Object obj = null;
            if (params.length > 0) {
                obj = params[0];
            }

            int page = 0;
            if (params.length > 1) {
                if (params[1] instanceof Integer) {
                    page = (int) params[1];
                }
            }

            try {
                List<Action> historyRecords;
                sleep(1000);
                if (obj == null) {
                    //do not filter the list
                    historyRecords = tryPullAllRecordsPage(page);
                } else if (obj instanceof Asset) {
                    historyRecords = tryPullAssetsPage(asset, page);
                } else if (obj instanceof User) {
                    historyRecords = tryPullUserPage(user, page);
                } else {
                    //what were we passed?
                    historyRecords = new ArrayList<>();
                }

                //no mas.  Check the record count before stripping the unknown record types
                if (historyRecords == null || historyRecords.isEmpty()) {
                    canFetchMoreRecords = false;
                }

                // Strip all records with an unknown action type so we only show check-in, check-out records
                // TODO: 10/26/17 add additional views to cover the other action types
                Iterator<Action> it = historyRecords.iterator();
                while (it.hasNext()) {
                    Action a = it.next();
                    if (a.getDirection() != Action.Direction.CHECKIN &&
                            a.getDirection() != Action.Direction.CHECKOUT) {
                        it.remove();
                    }
                }

                showRemoteHistory(historyRecords, page);
            } catch (SyncException e) {
                e.printStackTrace();
                canFetchMoreRecords = false;
                showRemoteError(null, e.getMessage());
            } catch (SyncNotSupportedException e) {
                canFetchMoreRecords = false;
                showRemoteError(e.getReason(), e.getMessage());
            } catch (Exception e) {
                canFetchMoreRecords = false;
                showRemoteError(null, e.getMessage());
            }

            isLoading = false;

            return null;
        }

        private
        @NotNull
        List<Action> tryPullAssetsPage(Asset asset, int page) throws SyncNotSupportedException, SyncException {
            SyncAdapter adapter = SyncManager.getPrefferedSyncAdapter(getActivity());
            return adapter.getAssetActivity(getActivity(), asset, page);
        }

        private
        @NotNull
        List<Action> tryPullUserPage(User user, int page) throws SyncNotSupportedException, SyncException {
            SyncAdapter adapter = SyncManager.getPrefferedSyncAdapter(getActivity());
            return adapter.getUserActivity(getActivity(), user, page);
        }

        private
        @NotNull
        List<Action> tryPullAllRecordsPage(int page) throws SyncNotSupportedException, SyncException {
            SyncAdapter adapter = SyncManager.getPrefferedSyncAdapter(getActivity());
            return adapter.getActivity(getActivity(), page);
        }

    }
}
