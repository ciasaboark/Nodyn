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

package io.phobotic.nodyn_app.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.helper.MediaHelper;
import io.phobotic.nodyn_app.list.adapter.ScannedAssetRecyclerViewAdapter;

/**
 * Created by Jonathan Nelson on 8/21/17.
 */

public class AssetScanList extends RelativeLayout {
    private static final String TAG = AssetScanList.class.getSimpleName();
    private final Context context;
    private ScanInputView input;
    private View rootView;
    private ArrayList<Asset> scannedAssetsList;
    private RecyclerView recyclerView;
    private View error;
    private OnAssetScannedListener listener;
    private boolean assetsRemovable = true;


    public AssetScanList(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_asset_scan_list, this);
        if (!isInEditMode()) {
            input = (ScanInputView) rootView.findViewById(R.id.input);
            error = rootView.findViewById(R.id.error);
            final TextView preview = (TextView) rootView.findViewById(R.id.preview);

            scannedAssetsList = new ArrayList<>();

            initScanner();
            initList();

            showErrorOrList();
        }
    }

    private void initScanner() {
        input.setListener(new ScanInputView.OnTextInputListener() {
            @Override
            public void onTextInput(String inputString) {
                if (inputString == null || inputString.equals("")) {
                    if (listener != null) {
                        listener.onScanError("Input string empty");
                    }
                } else {
                    processInputString(inputString);
                }
            }
        });

        //restrict the input if we are using kiosk mode
        boolean kioskModeEnabled = isKioskModeEnabled();

        input.setGhostMode(kioskModeEnabled);
        input.setForceScanInput(kioskModeEnabled);
    }

    private void initList() {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(getAdapter());
        recyclerView.setVisibility(View.GONE);
    }

    private void showErrorOrList() {
        if (scannedAssetsList.isEmpty()) {
            showError();
        } else {
            showList();
        }
    }

    /**
     * Attempt to process the input string as an {@link Asset}.  If that fails as well then
     * show an error
     *
     * @param inputString
     */
    private void processInputString(@NotNull final String inputString) {
        Database db = Database.getInstance(getContext());

        Asset asset = null;
        try {
            asset = db.findAssetByTag(inputString);
            processAssetScan(asset);
        } catch (AssetNotFoundException e1) {
            Log.d(TAG, "Unable to find asset matching input '" + inputString + "'");


            final AlertDialog d = new AlertDialog.Builder(getContext())
                    .setTitle(getResources().getString(R.string.asset_scan_list_unknown_asset_title))
                    .setView(R.layout.view_unknown_asset)
                    .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //nothing to do here
                        }
                    })
                    .create();
            d.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    //if kiosk mode is enabled then don't show the text that failed to scan, just show a basic
                    //+ error message
                    String message;
                    if (isKioskModeEnabled()) {
                        message = getResources().getString(R.string.asset_scan_list_unknown_asset_message);
                    } else {
                        message = String.format(getResources().getString(
                                R.string.asset_scan_list_unknown_asset_message_unformatted), inputString);
                    }

                    TextView tv = (TextView) d.findViewById(R.id.message);
                    if (tv != null) {
                        tv.setText(Html.fromHtml(message, null, null));
                    }

                }
            });
            d.show();

            MediaHelper.playSoundEffect(getContext(), R.raw.correct);
        }
    }

    private boolean isKioskModeEnabled() {
        return PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean(getResources().getString(R.string.pref_key_general_kiosk),
                        Boolean.parseBoolean(getResources().getString(
                                R.string.pref_default_general_kiosk)));
    }

    @NonNull
    private ScannedAssetRecyclerViewAdapter getAdapter() {
        ScannedAssetRecyclerViewAdapter adapter = new ScannedAssetRecyclerViewAdapter(context,
                scannedAssetsList, null, assetsRemovable);
        adapter.setAssetListChangeListener(new ScannedAssetRecyclerViewAdapter.OnAssetListChangeListener() {
            @Override
            public void onAssetListChange(@NotNull List<Asset> assets) {
                if (listener != null) {
                    listener.onAssetScanListChanged(assets);
                }

                if (assets.isEmpty()) {
                    showError();
                }
            }
        });
        return adapter;
    }

    private void showError() {
        error.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showList() {
        error.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void processAssetScan(Asset asset) {
        if (listener != null) {
            listener.onAssetScanned(asset);
        }
    }

    public AssetScanList setListener(OnAssetScannedListener listener) {
        this.listener = listener;
        return this;
    }

    public AssetScanList setAssetsRemovable(boolean assetsRemovable) {
        this.assetsRemovable = assetsRemovable;
        return this;
    }

    public List<Asset> getScannedAssets() {
        return scannedAssetsList;
    }

    public void addAsset(Asset asset) {
        scannedAssetsList.add(asset);
        recyclerView.swapAdapter(getAdapter(), false);
        recyclerView.scrollToPosition(scannedAssetsList.size() - 1);
        showErrorOrList();
    }

    public void reset() {
        int count = scannedAssetsList.size();
        scannedAssetsList.clear();
        recyclerView.getAdapter().notifyItemRangeRemoved(0, count);
        input.reset();
        showErrorOrList();
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            input.requestFocus();
        }
    }

    public interface OnAssetScannedListener {
        void onAssetScanned(@NotNull Asset asset);

        void onAssetScanListChanged(@NotNull List<Asset> assets);

        void onScanError(@NotNull String message);
    }
}