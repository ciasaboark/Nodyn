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

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

import androidx.fragment.app.Fragment;
import io.phobotic.nodyn_app.BuildConfig;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.preference.SettingsPage;


public class SettingsQRCodeFragment extends Fragment {
    private static final String ARG_CUR_PAGE = "curPage";
    private static final String ARG_TOTAL_PAGES = "totalPages";
    private static final String ARG_CODE_DATA = "codeData";
    private static final String ARG_IS_COMPRESSED = "isCompressed";

    private int curPage;
    private int totalPages;
    private String codeData;
    private View rootView;
    private View wait;
    private View holder;
    private ProgressBar progressBar;
    private ImageView qrcode;
    private boolean isCompressed;
    private TextView label;


    public static SettingsQRCodeFragment newInstance(String data, int curPage, int totalPages,
                                                     boolean isCompressed) {
        if (data == null) {
            throw new IllegalArgumentException("Data string can not be null");
        }

        SettingsQRCodeFragment fragment = new SettingsQRCodeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CUR_PAGE, curPage);
        args.putInt(ARG_TOTAL_PAGES, totalPages);
        args.putString(ARG_CODE_DATA, data);
        args.putBoolean(ARG_IS_COMPRESSED, isCompressed);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsQRCodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            curPage = getArguments().getInt(ARG_CUR_PAGE);
            totalPages = getArguments().getInt(ARG_TOTAL_PAGES);
            codeData = getArguments().getString(ARG_CODE_DATA);
            isCompressed = getArguments().getBoolean(ARG_IS_COMPRESSED);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_settings_qrcode, container, false);
        init();


        Gson gson = new Gson();
        SettingsPage page = new SettingsPage(BuildConfig.VERSION_CODE, curPage, totalPages, codeData, isCompressed);
        String json = gson.toJson(page);

        generateQRCode(json);

        return rootView;
    }

    private void init() {
        wait = rootView.findViewById(R.id.wait);
        progressBar = rootView.findViewById(R.id.progress);
        progressBar.setIndeterminate(false);
        wait.setVisibility(View.VISIBLE);

        qrcode = rootView.findViewById(R.id.qrcode);
        holder = rootView.findViewById(R.id.holder);
        holder.setVisibility(View.GONE);
        label = rootView.findViewById(R.id.label);

        String labelText = getString(R.string.share_settings_qrcode_page);
        label.setText(String.format(labelText, curPage, totalPages));
    }

    private void generateQRCode(final String data) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                BitmapDrawable drawable = null;
                float progress = 0;
                publishProgress((int) progress);

                try {
                    QRCodeWriter qrCodeWriter = new QRCodeWriter();
                    Map<EncodeHintType, Object> hints = new HashMap<>();
                    hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
                    hints.put(EncodeHintType.MARGIN, 4);

                    //just assume this was 10%
                    progress = 10;
                    publishProgress((int) progress);

                    BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 400, 400, hints);


                    int width = bitMatrix.getWidth();
                    int height = bitMatrix.getHeight();

                    Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                    float remainingProgress = 100f - progress;
                    float step = remainingProgress / (float) width;

                    for (int x = 0; x < width; x++) {
                        for (int y = 0; y < height; y++) {
                            bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                        }
                        progress = progress + step;
                        publishProgress((int) progress);
//                    Log.d(TAG, "QRCode progress: " + progress);
                    }

                    drawable = new BitmapDrawable(getResources(), bmp);
                    publishProgress(100);

                } catch (WriterException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                    e.printStackTrace();
                    publishProgress(100);
                }

                if (drawable != null) {
                    qrcode.setImageDrawable(drawable);
                    holder.setVisibility(View.VISIBLE);
                    wait.setVisibility(View.GONE);
                } else {
                    qrcode.setImageResource(R.drawable.alert);
                }
            }
        });
    }

    private void publishProgress(int progress) {
        progressBar.setProgress(progress);
    }
}
