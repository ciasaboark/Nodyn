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


import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.ManufacturerNotFoundException;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Manufacturer;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.model.User;

import static io.phobotic.nodyn_app.helper.TextHelper.setTextOrHide;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AssetExtendedDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AssetExtendedDetailsFragment extends Fragment {
    private static final String TAG = AssetExtendedDetailsFragment.class.getSimpleName();
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
    private View nameBox;
    private View companyBox;
    private View serialBox;
    private View manufacturerBox;
    private View modelBox;
    private View modelNoBox;
    private View purchaseDateBox;
    private View purchaseCostBox;
    private View supplierBox;
    private View notesBox;
    private View createdAtBox;
    private View checkoutNameBox;
    private TextView checkoutName;
    private View checkoutDateBox;
    private TextView checkoutDate;
    private View expectedCheckinBox;
    private TextView expectedCheckin;


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
        createdAtBox = rootView.findViewById(R.id.create_date_box);
        createdAt = (TextView) rootView.findViewById(R.id.create_date);

        nameBox = rootView.findViewById(R.id.name_box);
        name = (TextView) rootView.findViewById(R.id.model);

        companyBox = rootView.findViewById(R.id.company_box);
        company = (TextView) rootView.findViewById(R.id.company);

        serialBox = rootView.findViewById(R.id.serial_box);
        serial = (TextView) rootView.findViewById(R.id.serial);
        serial.setPaintFlags(serial.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        manufacturerBox = rootView.findViewById(R.id.manufacturer_box);
        manufacturer = (TextView) rootView.findViewById(R.id.manufacturer);

        modelBox = rootView.findViewById(R.id.model_name_box);
        model = (TextView) rootView.findViewById(R.id.model_name);

        modelNoBox = rootView.findViewById(R.id.model_no_box);
        modelNo = (TextView) rootView.findViewById(R.id.model_no);

        purchaseDateBox = rootView.findViewById(R.id.purchase_date_box);
        purchaseDate = (TextView) rootView.findViewById(R.id.purchase_date);

        purchaseCostBox = rootView.findViewById(R.id.purchase_cost_box);
        purchaseCost = (TextView) rootView.findViewById(R.id.purchase_cost);

        supplierBox = rootView.findViewById(R.id.supplier_box);
        supplier = (TextView) rootView.findViewById(R.id.supplier);

        notesBox = rootView.findViewById(R.id.notes_box);
        notes = (TextView) rootView.findViewById(R.id.notes);

        checkoutNameBox = rootView.findViewById(R.id.checkout_name_box);
        checkoutName = (TextView) rootView.findViewById(R.id.checkout_name);

        checkoutDateBox = rootView.findViewById(R.id.checkout_date_box);
        checkoutDate = (TextView) rootView.findViewById(R.id.checkout_date);

        expectedCheckinBox = rootView.findViewById(R.id.expected_checkin_box);
        expectedCheckin = (TextView) rootView.findViewById(R.id.expected_checkin);

        initFields();
    }

    private void initFields() {
        if (asset != null) {
            setTextOrHide(nameBox, name, asset.getName());

            // TODO: 10/28/17 Companies are not supported in the database, should the asset model be changed to use String value for company?
            setTextOrHide(companyBox, company, null);
            setTextOrHide(modelNoBox, modelNo, null); // TODO: 7/15/17 update asset model to include model number
            setTextOrHide(supplierBox, supplier, null); // TODO: 7/15/17 update asset model to include supplier

            setTextOrHide(serialBox, serial, asset.getSerial());
            setTextOrHide(purchaseDateBox, purchaseDate, asset.getPurchaseDate());
            setTextOrHide(purchaseCostBox, purchaseCost, asset.getPurchaseCost());
            setTextOrHide(notesBox, notes, asset.getNotes());

            //wite the timestamp fields
            DateFormat df = DateFormat.getDateTimeInstance();
            String createdAt = null;
            if (asset.getCreatedAt() != -1) {
                Date d = new Date(asset.getCreatedAt());
                createdAt = df.format(d);
            }
            setTextOrHide(createdAtBox, this.createdAt, createdAt);

            String checkoutDateText = null;
            if (asset.getLastCheckout() != -1) {
                Date d = new Date(asset.getLastCheckout());
                checkoutDateText = df.format(d);
            }
            setTextOrHide(checkoutDateBox, checkoutDate, checkoutDateText);

            String expectedCheckinText = null;
            if (asset.getExpectedCheckin() != -1) {
                Date d = new Date(asset.getExpectedCheckin());
                expectedCheckinText = df.format(d);
            }
            setTextOrHide(expectedCheckinBox, expectedCheckin, expectedCheckinText);


            //the remaining values are stored as IDs into other db tables
            // TODO: 10/28/17 should this be moved to an asynctask?

            Database db = Database.getInstance(getContext());
            String manufacturerText = null;
            try {
                Manufacturer manufacturer = db.findManufacturerByID(asset.getManufacturerID());
                manufacturerText = manufacturer.getName();
            } catch (ManufacturerNotFoundException e) {
                Log.d(TAG, "Unknown manufacturer: " + asset.getManufacturerID());
            }
            setTextOrHide(manufacturerBox, manufacturer, manufacturerText);

            String modelText = null;
            try {
                Model m = db.findModelByID(asset.getModelID());
                modelText = m.getName();
            } catch (ModelNotFoundException e) {
                Log.d(TAG, "Unknown model: " + asset.getModelID());
            }
            setTextOrHide(modelBox, model, modelText);

            String userName = null;
            try {
                User u = db.findUserByID(asset.getAssignedToID());
                userName = u.getName();
            } catch (UserNotFoundException e) {
                Log.d(TAG, "Unknown user: " + asset.getAssignedToID());
            }
            setTextOrHide(checkoutNameBox, checkoutName, userName);
        }
    }
}
