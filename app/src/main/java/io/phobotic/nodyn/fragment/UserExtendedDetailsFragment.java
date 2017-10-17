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


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.exception.UserNotFoundException;
import io.phobotic.nodyn.database.helper.GroupTableHelper;
import io.phobotic.nodyn.database.model.User;

public class UserExtendedDetailsFragment extends Fragment {

    private static final String USER = "user";
    private User user;
    private View rootView;
    private TextView location;
    private View locationBox;
    private TextView manager;
    private View managerBox;
    private TextView numAssets;
    private View numAssetsBox;
    private TextView employeeNo;
    private View employeeNoBox;
    private TextView groups;
    private View groupsBox;
    private TextView notes;
    private View notesBox;
    private TextView company;
    private View companyBox;
    private TextView jobTitle;
    private View jobTitleBox;
    private Database db;

    public static UserExtendedDetailsFragment newInstance(User user) {
        UserExtendedDetailsFragment fragment = new UserExtendedDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(USER, user);
        fragment.setArguments(args);
        return fragment;
    }

    public UserExtendedDetailsFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_user_extended_details, container, false);
        db = Database.getInstance(getContext());
        init();
        return rootView;
    }

    private void init() {
        jobTitle = (TextView) rootView.findViewById(R.id.job_title);
        jobTitleBox = rootView.findViewById(R.id.job_title_box);

        location = (TextView) rootView.findViewById(R.id.location);
        locationBox = rootView.findViewById(R.id.location_box);

        manager = (TextView) rootView.findViewById(R.id.manager);
        managerBox = rootView.findViewById(R.id.manager_box);

        numAssets = (TextView) rootView.findViewById(R.id.num_assets);
        numAssetsBox = rootView.findViewById(R.id.num_assets_box);

        employeeNo = (TextView) rootView.findViewById(R.id.employee_no);
        employeeNoBox = rootView.findViewById(R.id.employee_no_box);

        groups = (TextView) rootView.findViewById(R.id.groups);
        groupsBox = rootView.findViewById(R.id.groups_box);

        notes = (TextView) rootView.findViewById(R.id.notes);
        notesBox = rootView.findViewById(R.id.notes_box);

        company = (TextView) rootView.findViewById(R.id.company_name);
        companyBox = rootView.findViewById(R.id.company_name_box);

        setFields();
    }


    private void setFields() {
        unhideAllViews();
        if (user != null) {
            setTextOrHide(jobTitleBox, jobTitle, user.getJobTitle());

            // TODO: 9/14/17 update this to use Location once class has been created
//            String locationName = null;
//            try {
//
//            }
            setTextOrHide(locationBox, location, null);

            String managerName = null;
            try {
                User u = db.findUserByID(user.getManagerID());
                managerName = u.getName();
            } catch (UserNotFoundException e) {
            }

            setTextOrHide(managerBox, manager, managerName);
            setTextOrHide(numAssetsBox, numAssets, String.valueOf(user.getNumAssets()));
            setTextOrHide(employeeNoBox, employeeNo, user.getEmployeeNum());
            setTextOrHide(groupsBox, groups, GroupTableHelper.getGroupString(user, db));
            setTextOrHide(notesBox, notes, user.getNotes());

            // TODO: 9/14/17 update this to use company info once class has been created
            String companyName = null;
            setTextOrHide(companyBox, company, companyName);
        }
    }

    private void unhideAllViews() {
        jobTitleBox.setVisibility(View.VISIBLE);
        locationBox.setVisibility(View.VISIBLE);
        managerBox.setVisibility(View.VISIBLE);
        numAssetsBox.setVisibility(View.VISIBLE);
        employeeNoBox.setVisibility(View.VISIBLE);
        groupsBox.setVisibility(View.VISIBLE);
        notesBox.setVisibility(View.VISIBLE);
        companyBox.setVisibility(View.VISIBLE);
    }

    private void setTextOrHide(View view, TextView tv, @Nullable String text) {
        if (text == null || text.equals("")) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            tv.setText(text);
        }
    }

}
