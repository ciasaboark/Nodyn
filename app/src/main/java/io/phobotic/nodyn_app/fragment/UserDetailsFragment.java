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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.avatar.AvatarHelper;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.transformer.ZoomOutPageTransformer;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserDetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserDetailsFragment extends Fragment {
    private static final String USER = "user";

    private User user;
    private View rootView;
    private TextView name;
    private TextView email;
    private View emailBox;

    private ImageView image;
    private TextView username;
    private View usernameBox;
    private ViewPager pager;
    private TabLayout tabs;

    public static UserDetailsFragment newInstance(User user) {
        UserDetailsFragment fragment = new UserDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    public UserDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = (User) getArguments().getSerializable(USER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_user_details, container, false);
        init();
        return rootView;
    }

    private void init() {
        image = (ImageView) rootView.findViewById(R.id.image);
        name = (TextView) rootView.findViewById(R.id.model);

        username = (TextView) rootView.findViewById(R.id.username);
        usernameBox = rootView.findViewById(R.id.username_box);

        email = (TextView) rootView.findViewById(R.id.email);
        emailBox = rootView.findViewById(R.id.email_box);

        FragmentManager fm = getChildFragmentManager();
        PagerAdapter pagerAdapter = new UserPagerAdapter(fm);
        tabs = (TabLayout) rootView.findViewById(R.id.tabs);
        pager = (ViewPager) rootView.findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));

        pager.setPageTransformer(true, new ZoomOutPageTransformer());

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        setFields();
    }

    private void setFields() {
        unHideAllViews();
        if (user != null) {
            setTextOrHide(name, name, user.getName());
            setTextOrHide(usernameBox, username, user.getUsername());
            setTextOrHide(emailBox, email, user.getEmail());
        }
    }

    private void unHideAllViews() {
        name.setVisibility(View.VISIBLE);
        usernameBox.setVisibility(View.VISIBLE);
        emailBox.setVisibility(View.VISIBLE);

    }

    private void setTextOrHide(View view, TextView tv, @Nullable String text) {
        if (text == null || text.equals("")) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AvatarHelper avatarHelper = new AvatarHelper();
        avatarHelper.loadAvater(getContext(), user, image, 400);
    }

    private class UserPagerAdapter extends FragmentStatePagerAdapter {
        public UserPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    UserExtendedDetailsFragment fragment = UserExtendedDetailsFragment.newInstance(user);
                    return fragment;
                case 1:
                    UserAssetsListFragment assetsFragment = UserAssetsListFragment.newInstance(1, user);
                    return assetsFragment;
                case 2:
                    ActionHistoryFragment historyFragment = ActionHistoryFragment.newInstance(user);
                    return historyFragment;
                default:
                    return new Fragment();
            }

        }

        @Override
        public int getCount() {
            return 3;
        }
    }

}
