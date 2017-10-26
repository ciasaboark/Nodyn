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


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.model.Action;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.sync.SyncManager;
import io.phobotic.nodyn.sync.adapter.SyncAdapter;
import io.phobotic.nodyn.sync.adapter.SyncException;
import io.phobotic.nodyn.sync.adapter.SyncNotSupportedException;
import io.phobotic.nodyn.view.ActionView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ActionHistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActionHistoryFragment extends Fragment {
    private static final String ASSET = "asset";
    private static final String USER = "user";
    private Asset asset;
    private User user;

    private View rootView;

    private View remoteBox;
    private View remoteLoading;
    private View remoteError;
    private View remoteSuccess;
    private LinearLayout remoteHolder;

    private View localBox;
    private View localLoading;
    private View localError;
    private View localSuccess;
    private View localList;
    private LinearLayout localHolder;


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
        localBox = rootView.findViewById(R.id.local_box);
        localLoading = rootView.findViewById(R.id.local_loading);
        localSuccess = rootView.findViewById(R.id.local_success);
        localHolder = (LinearLayout) rootView.findViewById(R.id.local_holder);
        localList = rootView.findViewById(R.id.local_list);
        localError = rootView.findViewById(R.id.local_error);

        remoteBox = rootView.findViewById(R.id.remote_box);
        remoteLoading = rootView.findViewById(R.id.remote_loading);
        remoteSuccess = rootView.findViewById(R.id.remote_success);
        remoteHolder = (LinearLayout) rootView.findViewById(R.id.remote_holder);
        remoteError = rootView.findViewById(R.id.remote_error);

        refresh();
    }

    private void refresh() {
        hideView(localError);
        hideView(localSuccess);
        hideView(localList);
        localHolder.removeAllViews();
        showView(localLoading);

        hideView(remoteError);
        hideView(remoteSuccess);
        remoteHolder.removeAllViews();
        showView(remoteLoading);

        DownloadHistoryAsyncTask remoteAsyncTask = new DownloadHistoryAsyncTask();
        FetchUnsyncedAsyncTask unsyncedAsyncTask = new FetchUnsyncedAsyncTask();
        if (asset != null) {
            unsyncedAsyncTask.execute(asset);
            remoteAsyncTask.execute(asset);
        } else {
            unsyncedAsyncTask.execute(user);
            remoteAsyncTask.execute(user);
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

    private void showView(final View v) {
        if (v.getVisibility() == View.VISIBLE) {
            return;
        }

        Animation fadeIn = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        v.startAnimation(fadeIn);
    }

    private void showLocalError() {
        //this will be called from the async task, so we need to make sure we are running on the UI
        //+ thread
        Activity a = getActivity();
        if (a != null) {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideView(localSuccess);
                    hideView(localLoading);
                    showView(localError);
                }
            });
        }
    }

    private void showRemoteError(final String reason, final String message) {
        //this will be called from the async task, so we need to make sure we are running on the UI
        //+ thread
        Activity a = getActivity();
        if (a != null) {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideView(remoteLoading);
                    showView(remoteError);
                }
            });
        }
    }

    private void showLocalHistory(final List<Action> actions) {
        //this will be called from the async task, so make sure we are running on the UI thread
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (actions == null || actions.isEmpty()) {
                        hideView(localLoading);
                        showView(localSuccess);
                    } else {
                        showActions(actions, localLoading, localError, localList, localHolder);
                    }
                }
            });
        }
    }

    private void showActions(List<Action> actions, View loadingView, View errorView, View successView,
                             LinearLayout holder) {
        hideView(loadingView);
        //if we have no actions to display then hide the wrapper view
        if (actions == null || actions.isEmpty()) {
            showView(errorView);
        } else {
            showView(successView);
            showView(holder);
            for (Action a : actions) {
                ActionView v = new ActionView(getContext(), null, a);
                holder.addView(v);
            }
        }
    }

    private void showRemoteHistory(final List<Action> actions) {
        //this will be called from the async task, so make sure we are running on the UI thread
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showActions(actions, remoteLoading, remoteError, remoteSuccess, remoteHolder);
                }
            });
        }
    }

    private class FetchUnsyncedAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            try {
                Object obj = params[0];
                Database db = Database.getInstance(getContext());
                List<Action> actions = db.getUnsyncedActions();
                if (obj instanceof Asset) {
                    actions = filterByAsset((Asset) obj, actions);
                } else if (obj instanceof User) {
                    actions = filterByUser((User) obj, actions);
                } else {
                    actions = new ArrayList<>();
                }

                showLocalHistory(actions);
            } catch (Exception e) {
                showLocalError();
            }

            return null;
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
    }

    private class DownloadHistoryAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            Object obj = params[0];

            try {
                List<Action> historyRecords;

                if (obj instanceof Asset) {
                    historyRecords = tryPullAssetHistoryRecords(asset);
                } else if (obj instanceof User) {
                    historyRecords = tryPullUserHistoryRecords(user);
                } else {
                    throw new Exception("Unknown type.  Can not pull action history for " +
                            obj.getClass().getSimpleName());
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

                showRemoteHistory(historyRecords);
            } catch (SyncException e) {
                e.printStackTrace();
                showRemoteError(null, e.getMessage());
            } catch (SyncNotSupportedException e) {
                showRemoteError(e.getReason(), e.getMessage());
            } catch (Exception e) {
                showRemoteError(null, e.getMessage());
            }

            return null;
        }

        private
        @NotNull
        List<Action> tryPullAssetHistoryRecords(Asset asset) throws SyncNotSupportedException, SyncException {
            SyncAdapter adapter = SyncManager.getPrefferedSyncAdapter(getActivity());
            return adapter.getAssetActivity(getActivity(), asset);
        }

        private
        @NotNull
        List<Action> tryPullUserHistoryRecords(User user) throws SyncNotSupportedException, SyncException {
            SyncAdapter adapter = SyncManager.getPrefferedSyncAdapter(getActivity());
            return adapter.getUserActivity(getActivity(), user);
        }
    }
}
