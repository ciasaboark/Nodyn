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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.list.adapter.AssetRecyclerViewAdapter;
import io.phobotic.nodyn_app.list.decorator.VerticalSpaceItemDecoration;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class UserAssetsListFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String USER = "user";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private View rootView;
    private User user;
    private RecyclerView list;
    private View error;
    private TextView message;
    private TextView reason;
    private View loading;
    private VerticalSpaceItemDecoration decoration;

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static UserAssetsListFragment newInstance(int columnCount, User user) {
        UserAssetsListFragment fragment = new UserAssetsListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putSerializable(USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UserAssetsListFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            user = (User) getArguments().getSerializable(USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_user_assets_list, container, false);

        init();
        refresh();

        return rootView;
    }

    private void init() {
        list = rootView.findViewById(R.id.list);
        error = rootView.findViewById(R.id.error);
        reason = rootView.findViewById(R.id.reason);
        message = rootView.findViewById(R.id.message);
        loading = rootView.findViewById(R.id.loading);

        list.setVisibility(View.GONE);
        error.setVisibility(View.GONE);
        loading.setVisibility(View.VISIBLE);

        initList();

        LoadAssetsAsyncTask loadAssetsAsyncTask = new LoadAssetsAsyncTask();
        loadAssetsAsyncTask.execute(user);
    }

    private void refresh() {
        AnimationHelper.fadeOut(getContext(), list);
        AnimationHelper.fadeOut(getContext(), error);
        AnimationHelper.fadeIn(getContext(), loading);
    }

    private void initList() {
        final float spacingTop = getResources().getDimension(R.dimen.list_item_spacing_top);
        final float spacingBottom = getResources().getDimension(R.dimen.list_item_spacing_bottom);
        decoration = new VerticalSpaceItemDecoration(spacingTop, spacingBottom);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        list.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        list.addItemDecoration(decoration);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void showError(String reason, String msg) {
        AnimationHelper.fadeOut(getContext(), list);
        AnimationHelper.fadeOut(getContext(), loading);
        AnimationHelper.fadeIn(getContext(), error);

        this.reason.setText(reason);
        this.message.setText(msg);
    }

    private void showList(List<SimplifiedAsset> assets) {
        AnimationHelper.fadeOut(getContext(), error);
        AnimationHelper.fadeOut(getContext(), loading);
        AnimationHelper.fadeIn(getContext(), list);

        if (list instanceof RecyclerView) {
            if (mColumnCount <= 1) {
                list.setLayoutManager(new LinearLayoutManager(getActivity()));
            } else {
                list.setLayoutManager(new GridLayoutManager(getActivity(), mColumnCount));
            }

            list.setAdapter(new AssetRecyclerViewAdapter(getContext(), assets, null));
        }
    }

    private class LoadAssetsAsyncTask extends AsyncTask<User, Void, Void> {

        @Override
        protected Void doInBackground(User... users) {
            try {
                User user = users[0];

                Database db = Database.getInstance(getContext());
                List<Asset> userAssets = db.findAssetByUserID(user.getId());

                //convert these assets into a list of SimplifiedAssets
                final List<SimplifiedAsset> simplifiedAssets = new ArrayList<>();
                for (Asset a : userAssets) {
                    SimplifiedAsset simplifiedAsset = new SimplifiedAsset.Builder()
                            .fromAsset(getContext(), a);
                    simplifiedAssets.add(simplifiedAsset);
                }

                Activity a = getActivity();
                if (a != null) {
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showList(simplifiedAssets);
                        }
                    });
                }

            } catch (Exception e) {
                showError(e.getClass().getSimpleName(), e.getMessage());
            }


            return null;
        }
    }
}
