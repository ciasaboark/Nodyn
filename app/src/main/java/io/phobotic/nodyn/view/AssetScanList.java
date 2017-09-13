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

package io.phobotic.nodyn.view;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.UserHelper;
import io.phobotic.nodyn.database.exception.AssetNotFoundException;
import io.phobotic.nodyn.database.exception.UserNotFoundException;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.list.adapter.ScannedAssetRecyclerViewAdapter;

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
    private OnInputScanned listener;
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
     * Attempt to process the input string as a value representing a {@link User}.  If no matches
     * are found then attempt to match against an {@link Asset}.  If that fails as well then
     * show an error
     *
     * @param inputString
     */
    private void processInputString(@NotNull String inputString) {
        Database db = Database.getInstance(getContext());

        try {
            User user = UserHelper.getUserByInputString(getContext(), inputString);
            processUserScan(user);
        } catch (UserNotFoundException e) {
            Asset asset = null;
            try {
                asset = db.findAssetByTag(inputString);
                processAssetScan(asset);
            } catch (AssetNotFoundException e1) {
                AlertDialog d = new AlertDialog.Builder(getContext())
                        .setTitle("Unknown asset")
                        .setMessage("Unknown associate or asset <" + inputString + ">")
                        .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //nothing to do here
                            }
                        })
                        .create();
                d.show();
                final MediaPlayer mp = MediaPlayer.create(getContext(), R.raw.correct);
                mp.start();
            }
        }
    }

    @NonNull
    private ScannedAssetRecyclerViewAdapter getAdapter() {
        return new ScannedAssetRecyclerViewAdapter(context, scannedAssetsList, null, assetsRemovable);
    }

    private void showError() {
        error.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showList() {
        error.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void processUserScan(User user) {
        if (listener != null) {
            listener.onUserScanned(user);
        }
    }

    private void processAssetScan(Asset asset) {
        if (listener != null) {
            listener.onAssetScanned(asset);
        }
    }

    public AssetScanList setListener(OnInputScanned listener) {
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

    public void clear() {
        int count = scannedAssetsList.size();
        scannedAssetsList.clear();
        recyclerView.getAdapter().notifyItemRangeRemoved(0, count);
        showErrorOrList();
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            input.requestFocus();
        }
    }

    public interface OnInputScanned {
        void onUserScanned(User user);

        void onAssetScanned(Asset asset);

        void onScanError(String message);
    }
}