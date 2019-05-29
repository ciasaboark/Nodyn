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


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.fragment.app.Fragment;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Asset;

/**
 * A simple {@link Fragment} subclass.
 */
public class StatusOverviewFragment extends Fragment {
    private View rootView;

    public static StatusOverviewFragment newInstance() {
        return new StatusOverviewFragment();
    }


    public StatusOverviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_status_overview, container);
        init();

        return rootView;
    }

    private void init() {
        // TODO: 3/10/19
    }

    @Override
    public void onResume() {
        super.onResume();
        //reflesh the count
        Database db = Database.getInstance(getContext());
        List<Asset> assetList = db.getAssets();
        int totalCheckedIn = 0;
        int totalCheckedOut = 0;
        int totalPastDue = 0;
        long now = System.currentTimeMillis();

        for (Asset asset : assetList) {
            if (asset.getAssignedToID() != -1) {
                totalCheckedOut++;
                long dueDate = asset.getExpectedCheckin();
                if (dueDate >= now) {
                    totalPastDue++;
                }
            } else {
                totalCheckedIn++;
            }
        }


    }

    private void findViews() {
        // TODO: 3/10/19
    }
}
