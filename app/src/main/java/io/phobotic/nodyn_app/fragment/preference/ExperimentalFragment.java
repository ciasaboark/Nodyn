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

package io.phobotic.nodyn_app.fragment.preference;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.fragment.app.Fragment;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.helper.PastDueEmailHelper;
import io.phobotic.nodyn_app.service.PastDueAlertService;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExperimentalFragment extends Fragment {
    private View rootView;
    private ImageButton testPastDueButton;

    public ExperimentalFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_experimental, container, false);
        init();

        return rootView;
    }

    private void init() {
        testPastDueButton = rootView.findViewById(R.id.test_past_due);
        testPastDueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testPastDueButton.setEnabled(false);
                Database db = Database.getInstance(getContext());
                List<Asset> assetList = db.getAssets();
                boolean randomize = false;
                CheckBox cb = rootView.findViewById(R.id.past_due_random);
                if (cb != null) {
                    randomize = cb.isChecked();
                }

                ProgressBar pb = rootView.findViewById(R.id.past_due_progress);
                AnimationHelper.fadeIn(getContext(), pb);

                if (!randomize) {
                    //if we dont want a random list of assets just start the past due service in
                    //+ the background
                    Intent i = new Intent(getContext(), PastDueAlertService.class);
                    getContext().startService(i);
                    pb.setVisibility(View.GONE);
                    testPastDueButton.setEnabled(true);
                    Toast.makeText(getContext(), "Past due email service started", Toast.LENGTH_LONG).show();
                } else {
                    if (assetList == null || assetList.isEmpty()) {
                        Toast.makeText(getContext(), "No assets to test", Toast.LENGTH_LONG).show();
                        pb.setVisibility(View.GONE);
                        testPastDueButton.setEnabled(true);
                    } else {
                        //get a random list of assets, making sure we have at least one asset in
                        //+ the resulting list
                        List<Asset> randomList = new ArrayList<>();
                        int listSize = (int) (assetList.size() * .10);
                        listSize = Math.max(listSize, 1);
                        Random r = new Random(System.currentTimeMillis());

                        for (int i = 0; i < listSize; i++) {
                            int index = r.nextInt(assetList.size());
                            randomList.add(assetList.remove(index));
                        }

                        PastDueEmailHelper emailHelper = new PastDueEmailHelper(getContext());
                        emailHelper.sendBulkReminder(randomList, null);

                        Toast.makeText(getContext(), "Random list of " + listSize + " assets sent", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

}
