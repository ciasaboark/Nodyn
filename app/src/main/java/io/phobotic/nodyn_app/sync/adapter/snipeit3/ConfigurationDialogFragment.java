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

package io.phobotic.nodyn_app.sync.adapter.snipeit3;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import io.phobotic.nodyn_app.R;

/**
 * Created by Jonathan Nelson on 7/27/17.
 */

public class ConfigurationDialogFragment extends DialogFragment {
    public static ConfigurationDialogFragment newInstance() {
        ConfigurationDialogFragment frag = new ConfigurationDialogFragment();
        return frag;
    }

    public ConfigurationDialogFragment() {
        //required empty constructor
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_snipe_it_3, null);

        AlertDialog.Builder b = new MaterialAlertDialogBuilder(getContext(), R.style.Widgets_Dialog)
                .setIcon(R.drawable.ic_sync_black_24dp)
                .setTitle("SnipeIt version 3.x")
                .setView(rootView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        return b.create();
    }
}
