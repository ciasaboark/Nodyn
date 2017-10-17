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


import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.model.Asset;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AssetExtendedDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AssetExtendedDetailsFragment extends Fragment {
    private static final String ASSET = "asset";

    private Asset asset;
    private View rootView;
    private TextView name;
    private TextView company;
    private TextView serial;
    private TextView manufacturer;
    private TextView model;
    private TextView modelNo;
    private TextView purchaseDate;
    private TextView purchaseCost;
    private TextView supplier;
    private TextView notes;
    private TextView createdAt;
    private View name_box;
    private View company_box;
    private View serial_box;
    private View manufacturer_box;
    private View model_box;
    private View modelNo_box;
    private View purchaseDate_box;
    private View purchaseCost_box;
    private View supplier_box;
    private View notes_box;
    private View createdAt_box;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment AssetDetailsFragment.
     */
    public static AssetExtendedDetailsFragment newInstance(Asset asset) {
        AssetExtendedDetailsFragment fragment = new AssetExtendedDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ASSET, asset);

        fragment.setArguments(args);
        return fragment;
    }

    public AssetExtendedDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            asset = (Asset) getArguments().getSerializable(ASSET);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_asset_extended_details, container, false);
        init();

        return rootView;
    }

    private void init() {
        name = (TextView) rootView.findViewById(R.id.model);
        name_box = rootView.findViewById(R.id.name_box);
        company = (TextView) rootView.findViewById(R.id.company);
        company_box = rootView.findViewById(R.id.company_box);
        serial = (TextView) rootView.findViewById(R.id.serial);
        serial_box = rootView.findViewById(R.id.serial_box);
        serial.setPaintFlags(serial.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        manufacturer = (TextView) rootView.findViewById(R.id.manufacturer);
        manufacturer_box = rootView.findViewById(R.id.manufacturer_box);
        model = (TextView) rootView.findViewById(R.id.model_name);
        model_box = rootView.findViewById(R.id.model_name_box);
        modelNo = (TextView) rootView.findViewById(R.id.model_no);
        modelNo_box = rootView.findViewById(R.id.model_no_box);
        purchaseDate = (TextView) rootView.findViewById(R.id.purchase_date);
        purchaseDate_box = rootView.findViewById(R.id.purchase_date_box);
        purchaseCost = (TextView) rootView.findViewById(R.id.purchase_cost);
        purchaseCost_box = rootView.findViewById(R.id.purchase_cost_box);
        supplier = (TextView) rootView.findViewById(R.id.supplier);
        supplier_box = rootView.findViewById(R.id.supplier_box);
        notes = (TextView) rootView.findViewById(R.id.notes);
        notes_box = rootView.findViewById(R.id.notes_box);
        createdAt = (TextView) rootView.findViewById(R.id.created_at);
        createdAt_box = rootView.findViewById(R.id.created_at_box);

        initFields();
    }

    private void initFields() {
        if (asset != null) {
            setTextOrHide(name_box, name, asset.getName());
            // TODO: 9/13/17 change these IDs into names
            setTextOrHide(company_box, company, String.valueOf(asset.getCompanyID()));
            setTextOrHide(serial_box, serial, asset.getSerial());
            setTextOrHide(manufacturer_box, manufacturer, String.valueOf(asset.getManufacturerID()));
            setTextOrHide(model_box, model, String.valueOf(asset.getModelID()));
            setTextOrHide(modelNo_box, modelNo, null); // TODO: 7/15/17 update asset model to include model number
            setTextOrHide(purchaseDate_box, purchaseDate, asset.getPurchaseDate());
            setTextOrHide(purchaseCost_box, purchaseCost, asset.getPurchaseCost());
            setTextOrHide(supplier_box, supplier, null); // TODO: 7/15/17 update asset model to include supplier
            setTextOrHide(notes_box, notes, asset.getNotes());

            String createdAt = null;
            if (asset.getCreatedAt() != -1) {
                Date d = new Date(asset.getCreatedAt());
                DateFormat df = new SimpleDateFormat();
                createdAt = df.format(d);
            }
            setTextOrHide(createdAt_box, this.createdAt, createdAt);
        }
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
