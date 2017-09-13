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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.database.model.UserHistoryRecord;
import io.phobotic.nodyn.fragment.listener.OnListFragmentInteractionListener;
import io.phobotic.nodyn.sync.SyncManager;
import io.phobotic.nodyn.sync.adapter.SyncAdapter;
import io.phobotic.nodyn.sync.adapter.SyncException;
import io.phobotic.nodyn.sync.adapter.SyncNotSupportedException;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class UserHistoryFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String USER = "user";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    //    private OnListFragmentInteractionListener mListener;
    private View rootView;
    private User user;
    private RecyclerView list;
    private View error;
    private TextView message;
    private TextView reason;

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static UserHistoryFragment newInstance(int columnCount, User user) {
        UserHistoryFragment fragment = new UserHistoryFragment();
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
    public UserHistoryFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnListFragmentInteractionListener) {
//            mListener = (OnListFragmentInteractionListener) context;
//        }
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
        rootView = inflater.inflate(R.layout.fragment_user_history_list, container, false);

        init();

        return rootView;
    }

    private void init() {
        list = (RecyclerView) rootView.findViewById(R.id.list);
        error = rootView.findViewById(R.id.error);
        reason = (TextView) rootView.findViewById(R.id.reason);
        message = (TextView) rootView.findViewById(R.id.message);

        list.setVisibility(View.VISIBLE);
        error.setVisibility(View.VISIBLE);

        SyncAdapter syncAdapter = SyncManager.getPrefferedSyncAdapter(getActivity());

        try {
            List<UserHistoryRecord> records = syncAdapter.getHistory(getActivity(), user);

        } catch (SyncException e) {

        } catch (SyncNotSupportedException e) {
            showError(e.getReason(), e.getMessage());
        }

    }

    private void showError(String reason, String msg) {
        error.setVisibility(View.VISIBLE);
        list.setVisibility(View.GONE);
        this.reason.setText(reason);
        this.message.setText(msg);
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    private void showList() {
        error.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);

        // TODO: 7/27/17 finish user history views and enable recyclerview

//        if (view instanceof RecyclerView) {
//            Context context = view.getContext();
//            RecyclerView recyclerView = (RecyclerView) view;
//            if (mColumnCount <= 1) {
//                recyclerView.setLayoutManager(new LinearLayoutManager(context));
//            } else {
//                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
//            }
//            recyclerView.setAdapter(new MyUserHistoryRecyclerViewAdapter(DummyContent.ITEMS, mListener));
//        }
    }
}
