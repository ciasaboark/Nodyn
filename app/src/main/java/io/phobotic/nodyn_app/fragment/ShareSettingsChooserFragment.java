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

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.view.ShareTypeView;


public class ShareSettingsChooserFragment extends Fragment {
    private OnShareMethodChosenListener listener;
    private View rootView;


    public static ShareSettingsChooserFragment newInstance() {
        ShareSettingsChooserFragment fragment = new ShareSettingsChooserFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public ShareSettingsChooserFragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnShareMethodChosenListener) {
            listener = (OnShareMethodChosenListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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
        rootView = inflater.inflate(R.layout.fragment_share_settings_chooser, container, false);
        init();

        return rootView;
    }

    private void init() {
        ShareTypeView nfcButton = rootView.findViewById(R.id.nfc);
        nfcButton.setMethod(listener, ShareMethod.NFC);

        ShareTypeView qrButton = rootView.findViewById(R.id.qrcode);
        qrButton.setMethod(listener, ShareMethod.QRCODE);

        ShareTypeView fileButton = rootView.findViewById(R.id.file);
        fileButton.setMethod(listener, ShareMethod.FILE_SHARE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public enum ShareMethod {
        NFC,
        FILE_SHARE,
        QRCODE
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnShareMethodChosenListener {
        void onMethodChosen(ShareMethod shareMethod);
    }
}
