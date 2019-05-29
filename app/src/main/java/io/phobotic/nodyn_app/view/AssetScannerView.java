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

package io.phobotic.nodyn_app.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.crashlytics.android.Crashlytics;
import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.phobotic.nodyn_app.DropShadowTransformation;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.AssetNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.helper.MediaHelper;
import io.phobotic.nodyn_app.list.adapter.ScannedAssetRecyclerViewAdapter;
import io.phobotic.nodyn_app.list.decorator.DividerItemDecoration;
import io.phobotic.nodyn_app.transformer.RoundedTransformation;


/**
 * Created by Jonathan Nelson on 8/21/17.
 */

public class AssetScannerView extends RelativeLayout {
    private static final String TAG = AssetScannerView.class.getSimpleName();
    private static final long IMAGE_SWITCH_PERIOD = 8000;
    private static final int IMAGE_SWITCH_DELAY = 2000;
    private final Context context;
    Timer timer;
    TimerTask timerTask;
    private ScanInputView input;
    private View rootView;
    private ArrayList<Asset> scannedAssetsList;
    private RecyclerView recyclerView;
    private ImageSwitcher modelSwitcher;
    private View error;
    private OnAssetScannedListener listener;
    private boolean assetsRemovable = true;
    private boolean isGhostMode;
    private boolean isRestrictedInput;
    private Queue<String> modelImageURLs = new ArrayDeque<>();
    private ImageSwitcher exampleSwitcher;


    public AssetScannerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_asset_scan_list, this);
        if (!isInEditMode()) {
            findViews();
            initImageSwitchers();
            initTimer();

//            final TextView preview = (TextView) rootView.findViewById(R.id.preview);

            scannedAssetsList = new ArrayList<>();

            initScanner();
            initList();

            showIntroOrList();
        }
    }

    private void findViews() {
        input = rootView.findViewById(R.id.input);
        error = rootView.findViewById(R.id.error);
        modelSwitcher = rootView.findViewById(R.id.switcher);
        exampleSwitcher = rootView.findViewById(R.id.example_switcher);

    }

//    @Override
//    protected void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//        timer.cancel();
//    }
//
//    @Override
//    protected void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        timer.scheduleAtFixedRate(timerTask, IMAGE_SWITCH_DELAY, IMAGE_SWITCH_PERIOD);
//    }

    private void initImageSwitchers() {
        initModelImageSwitcher();
        initExampleImageSwitcher();
    }

    private void initTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                updateImageSwitcher();
            }
        };
    }

    private void initScanner() {
        input.setListener(new ScanInputView.OnTextInputListener() {
            @Override
            public void onTextInputFinished(String inputString) {
                if (inputString == null || inputString.equals("")) {
                    if (listener != null) {
                        listener.onScanError("Input string empty");
                    }
                } else {
                    processInputString(inputString);
                }
            }

            @Override
            public void onTextInputBegin() {
                //nothing to do here
            }
        });
    }

    private void initList() {
        recyclerView = rootView.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(getAdapter());
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), 16.0f, 16.0f));
        recyclerView.setVisibility(View.GONE);
    }

    private void showIntroOrList() {
        if (scannedAssetsList.isEmpty()) {
            showIntro();
        } else {
            showList();
        }
    }

    private void initModelImageSwitcher() {
        Animation out = AnimationUtils.loadAnimation(getContext(), R.anim.exit_left);
        Animation in = AnimationUtils.loadAnimation(getContext(), R.anim.enter_from_right);
        modelSwitcher.setInAnimation(in);
        modelSwitcher.setOutAnimation(out);
        modelSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ImageSwitcher.LayoutParams params = new ImageSwitcher.LayoutParams(
                        ImageSwitcher.LayoutParams.MATCH_PARENT,
                        ImageSwitcher.LayoutParams.MATCH_PARENT);
                imageView.setLayoutParams(params);

                return imageView;
            }
        });

        List<Model> models = new ArrayList<>();
        models = Database.getInstance(getContext()).getModels();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean allModels = prefs.getBoolean(
                getResources().getString(R.string.pref_key_check_out_all_models),
                Boolean.parseBoolean(getResources().getString(R.string.pref_default_check_out_all_models)));

        Set<String> allowedModels = prefs.getStringSet(
                getResources().getString(R.string.pref_key_check_out_models), new HashSet<String>());

        List<String> modelImageList = new ArrayList<>();
        Iterator<Model> it = models.iterator();
        while (it.hasNext()) {
            Model m = it.next();
            if (allModels || allowedModels.contains(String.valueOf(m.getId()))) {
                String imageURL = m.getImage();
                if (imageURL != null && imageURL.length() > 1)
                    modelImageList.add(imageURL);
            }
        }

        Collections.shuffle(modelImageList);
        modelImageURLs.addAll(modelImageList);

        //set the inital image drawable
        modelSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.devices_1));
    }

    private void initExampleImageSwitcher() {
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);

        //a very slow fade between each example
        in.setDuration(1000);
        in.setInterpolator(new LinearInterpolator());
        out.setDuration(1000);
        out.setInterpolator(new LinearInterpolator());

        exampleSwitcher.setInAnimation(in);
        exampleSwitcher.setOutAnimation(out);
        exampleSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ImageSwitcher.LayoutParams params = new ImageSwitcher.LayoutParams(
                        ImageSwitcher.LayoutParams.MATCH_PARENT,
                        ImageSwitcher.LayoutParams.MATCH_PARENT);
                imageView.setLayoutParams(params);

                return imageView;
            }
        });

        //set the inital image drawable

        final Queue<Drawable> exampleDrawables = new ArrayDeque<>();
        exampleDrawables.add(getResources().getDrawable(R.drawable.property_tag_example));
        exampleDrawables.add(getResources().getDrawable(R.drawable.qrcode_label_example));

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final Drawable d = exampleDrawables.poll();
                exampleDrawables.add(d);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        exampleSwitcher.setImageDrawable(d);
                    }
                });

            }
        }, 0, 11000);
    }

    private void updateImageSwitcher() {
        final String nextImageURL = modelImageURLs.poll();
        if (nextImageURL != null) {
            modelImageURLs.add(nextImageURL);

            Transformation backgroundTransformation = new RoundedTransformation();

            float borderWidth = getResources().getDimension(R.dimen.picasso_small_image_circle_border_width);

            Transformation borderTransformation = new RoundedTransformationBuilder()
                    .borderColor(getResources().getColor(R.color.circleBorderLarge))
                    .borderWidthDp(borderWidth)
                    .cornerRadiusDp(300)
                    .oval(false)
                    .build();

            final List<Transformation> transformations = new ArrayList<>();
            transformations.add(backgroundTransformation);
            transformations.add(borderTransformation);
            transformations.add(new DropShadowTransformation(0.1f));

            //Picasso requires running on the main thread
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Picasso.with(getContext())
                                .load(nextImageURL)
                                .placeholder(R.drawable.devices_1)
                                .error(R.drawable.devices_1)
                                .transform(transformations)
                                .resizeDimen(R.dimen.asset_scan_view_carousel_image_width, R.dimen.asset_scan_view_carousel_image_height)
                                .into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        BitmapDrawable bd = new BitmapDrawable(bitmap);
                                        modelSwitcher.setImageDrawable(bd);
                                    }

                                    @Override
                                    public void onBitmapFailed(Drawable errorDrawable) {
                                        //                                switcher.setImageDrawable(errorDrawable);
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                                        //                                switcher.setImageDrawable(placeHolderDrawable);
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    }
                }
            });
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
                    if (isGhostMode) {
                        message = getResources().getString(R.string.asset_scan_list_unknown_asset_message);
                    } else {
                        message = String.format(getResources().getString(
                                R.string.asset_scan_list_unknown_asset_message_unformatted), inputString);
                    }

                    TextView tv = d.findViewById(R.id.message);
                    if (tv != null) {
                        tv.setText(Html.fromHtml(message, null, null));
                    }

                }
            });
            d.show();

            MediaHelper.playSoundEffect(getContext(), R.raw.original_sound__error_bleep_5);
        }
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
                    showIntro();
                }
            }
        });
        return adapter;
    }

    private void showIntro() {
        error.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        try {
            timer.scheduleAtFixedRate(timerTask, IMAGE_SWITCH_DELAY, IMAGE_SWITCH_PERIOD);
        } catch (IllegalStateException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private void showList() {
        error.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        timer.cancel();
    }

    private void processAssetScan(Asset asset) {
        if (listener != null) {
            listener.onAssetScanned(asset);
        }
    }

    public AssetScannerView setListener(OnAssetScannedListener listener) {
        this.listener = listener;
        return this;
    }

    public AssetScannerView setAssetsRemovable(boolean assetsRemovable) {
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
        showIntroOrList();
    }

    public void reset() {
        int count = scannedAssetsList.size();
        scannedAssetsList.clear();
        recyclerView.getAdapter().notifyItemRangeRemoved(0, count);
        input.reset();
        showIntroOrList();
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