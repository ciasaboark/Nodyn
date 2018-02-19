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

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.helper.GzipUtil;

/**
 * TODO: document your custom view class.
 */
public class ShareSettingsQRCodeFragment extends Fragment {
    private static final String TAG = ShareSettingsQRCodeFragment.class.getSimpleName();
    private View rootView;
    private ImageView qrcode;
    private ProgressBar progressBar;
    private View progressBox;

    public static ShareSettingsQRCodeFragment newInstance() {
        ShareSettingsQRCodeFragment fragment = new ShareSettingsQRCodeFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public ShareSettingsQRCodeFragment() {
        // Required empty public constructor
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
        rootView = inflater.inflate(R.layout.fragment_share_settings_qrcode, container, false);
        init();

        return rootView;
    }


    private void init() {
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress);
        progressBar.setIndeterminate(false);
        progressBox = rootView.findViewById(R.id.progress_box);
        progressBox.setVisibility(View.VISIBLE);

        qrcode = (ImageView) rootView.findViewById(R.id.qrcode);
        qrcode.setVisibility(View.INVISIBLE);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Map<String, ?> allPrefs = prefs.getAll();
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            String key = entry.getKey();

        }

        allPrefs.remove("check_out_eula");
//        allPrefs.remove("pref_snipeit_4_api_key");

        Map<String, Object> transferPrefs = new HashMap<>();


        Gson gson = new Gson();
        String json = gson.toJson(allPrefs);
        String compressedJson = "";
        try {
            compressedJson = compress(json);
        } catch (IOException e) {

        }

        QrcodeLoadAsyncTask qrcodeLoadAsyncTask = new QrcodeLoadAsyncTask();
        qrcodeLoadAsyncTask.execute(compressedJson);

    }

    private String compress(String str) throws IOException {
//        str = "Test string";
        Log.d(TAG, "Initial String: " + str);
        byte[] compressedBytes = GzipUtil.compress(str);
        String base64encoded = GzipUtil.base64Encode(compressedBytes);
        Log.d(TAG, "Base64 encoded: " + base64encoded);

        byte[] decompressedBytes = GzipUtil.base64Decode(base64encoded);
        String decodedString = GzipUtil.decompress(decompressedBytes);
        Log.d(TAG, "Base64 decoded: " + decodedString);
        return base64encoded;
    }

    private class QrcodeLoadAsyncTask extends AsyncTask<String, Integer, BitmapDrawable> {


        @Override
        protected BitmapDrawable doInBackground(String... params) {
            String parm = params[0];
            BitmapDrawable drawable = null;
            float progress = 0;
            publishProgress((int) progress);

            try {
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

                //just assume this was 10%
                progress = 10;
                publishProgress((int) progress);

                BitMatrix bitMatrix = qrCodeWriter.encode(parm, BarcodeFormat.QR_CODE, 800, 800, hints);


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
                return drawable;

            } catch (WriterException e) {
                e.printStackTrace();
                publishProgress(100);
                return null;
            }


        }

        @Override
        protected void onPostExecute(BitmapDrawable drawable) {
            super.onPostExecute(drawable);

            if (drawable != null) {
                qrcode.setImageDrawable(drawable);
                qrcode.setVisibility(View.VISIBLE);
                progressBox.setVisibility(View.GONE);
            } else {
                qrcode.setImageResource(R.drawable.email);
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int value = values[0];
            progressBar.setProgress(value);
        }
    }
}
