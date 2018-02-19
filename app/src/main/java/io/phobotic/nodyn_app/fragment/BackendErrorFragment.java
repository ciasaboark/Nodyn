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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.helper.SettingsHelper;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BackendErrorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BackendErrorFragment extends Fragment {
    private View rootView;
    private FloatingActionButton buttonSettings;

    public static BackendErrorFragment newInstance() {
        BackendErrorFragment fragment = new BackendErrorFragment();
        return fragment;
    }

    public BackendErrorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_backend_error, container, false);
        init();

        return rootView;
    }

    private void init() {
        initFab();
        initTextViews();
    }

    private void initFab() {
        buttonSettings = (FloatingActionButton) rootView.findViewById(R.id.settings_button);

        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsHelper.loadKioskSettings(getActivity());
            }
        });
    }

    private void initTextViews() {
//        TextView info2 = (TextView) rootView.findViewById(R.id.info2);
//        String src = getString(R.string.no_backend_selected_info2);
//        SpannableString spannableString = new SpannableString(src);
//
//        Drawable d = getResources().getDrawable(R.drawable.ic_book_open_page_variant_white_24dp);
//        d.setTint(ColorHelper.fetchAccentColor(getContext()));
//        d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
//        ImageSpan imageSpan = new ImageSpan(d, ImageSpan.ALIGN_BOTTOM);
//
//        SpannableString linkSpan = new SpannableString("manual");
//        linkSpan.setSpan(new URLSpan("https://github.com/ciasaboark/Nodyn/wiki"), 0, 6, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//
//        int index = spannableString.toString().indexOf("@");
//        spannableString.setSpan(linkSpan, index, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
////        spannableString.setSpan(imageSpan, spannableString.toString().indexOf("@"),
////                spannableString.toString().indexOf("@") +1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//
//        info2.setText(spannableString);
    }

}
