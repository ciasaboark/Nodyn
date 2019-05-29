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

package io.phobotic.nodyn_app.fragment.dash;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.fragment.app.Fragment;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.activity.AssetStatusListActivity;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.view.AssetCountView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CheckoutOverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CheckoutOverviewFragment extends Fragment implements View.OnClickListener {
    private View rootView;
    private AssetCountView availableCard;
    private AssetCountView outCard;
    private AssetCountView pastDueCard;
    private View pastDueWrapper;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CheckoutOverviewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CheckoutOverviewFragment newInstance() {
        CheckoutOverviewFragment fragment = new CheckoutOverviewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CheckoutOverviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_checkout_overview, container, false);

        init();

        return rootView;
    }

    private void init() {
        findViews();
        refresh();
    }

    private void findViews() {
        availableCard = rootView.findViewById(R.id.available_card);
        availableCard.setOnClickListener(this);
        outCard = rootView.findViewById(R.id.out_card);
        outCard.setOnClickListener(this);
        pastDueCard = rootView.findViewById(R.id.past_due_card);
        pastDueCard.setOnClickListener(this);
        pastDueWrapper = rootView.findViewById(R.id.past_due_wrapper);
    }


    public void refresh() {
        Database db = Database.getInstance(getContext());
        List<Asset> assetList = db.getAssets();
        int availableCount = 0;
        int checkedOutCount = 0;
        int pastDueCount = 0;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean checkoutAllModels = prefs.getBoolean(getString(R.string.pref_key_check_out_all_models), false);
        Set<String> availableModels = prefs.getStringSet(getString(R.string.pref_key_check_out_models), new HashSet<String>());

        for (Asset a : assetList) {
            //only look at the models that are allowed to be checked out
            if (checkoutAllModels || availableModels.contains(String.valueOf(a.getModelID()))) {
                if (a.getExpectedCheckin() == -1) {
                    availableCount++;
                } else {
                    checkedOutCount++;

                    //is this model past due?
                    Date expectedCheckin = new Date(a.getExpectedCheckin());
                    Date now = new Date();
                    if (expectedCheckin.before(now)) {
                        pastDueCount++;
                    }
                }
            }

        }

        availableCard.setCount(availableCount);
        outCard.setCount(checkedOutCount);
        pastDueCard.setCount(pastDueCount);

        if (pastDueCount == 0) {
            AnimationHelper.collapse(pastDueWrapper);
        } else {
            AnimationHelper.expand(pastDueWrapper);
        }
    }


    @Override
    public void onClick(View v) {
        Intent i = new Intent(getContext(), AssetStatusListActivity.class);
        getContext().startActivity(i);
    }
}
