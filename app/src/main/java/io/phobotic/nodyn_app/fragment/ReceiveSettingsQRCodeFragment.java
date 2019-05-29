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


import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import androidx.cardview.widget.CardView;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.helper.GzipUtil;
import io.phobotic.nodyn_app.preference.SettingsPage;
import io.phobotic.nodyn_app.view.QRCodePageView;

public class ReceiveSettingsQRCodeFragment extends PreferenceReaderFragment {
    private static final String TAG = ReceiveSettingsQRCodeFragment.class.getSimpleName();
    private View rootView;
    private DecoratedBarcodeView decoder;
    private TextSwitcher title;
    private int totalPages = 0;
    private String[] parts = null;
    private Boolean isCompressed = null;
    private LinearLayout list;
    private HorizontalScrollView scrollView;
    private int remainingPages;
    private int versionCode = -1;
    private TextView info1;
    private TextView info2;
    private CardView card;
    private int titleGravity = Gravity.START;

    public static ReceiveSettingsQRCodeFragment newInstance() {
        ReceiveSettingsQRCodeFragment fragment = new ReceiveSettingsQRCodeFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public ReceiveSettingsQRCodeFragment() {
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
        rootView = inflater.inflate(R.layout.fragment_receive_settings_qrcode, container, false);
        init();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        decoder.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        decoder.pauseAndWait();
    }

    private void init() {
        findViews();
        title.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                TextView t = new TextView(getContext());
                t.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_START);
                t.setGravity(titleGravity);
                t.setTextAppearance(getContext(), android.R.style.TextAppearance_Material_Large);
                t.setTextColor(getResources().getColor(R.color.section_title));
                return t;
            }
        });
        Animation in = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        title.setInAnimation(in);
        title.setOutAnimation(out);
        title.setCurrentText("QR Code");

        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE);
        decoder.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats, null, null, false));
        decoder.setStatusText("Scan the first QR code from the target device");
        final BeepManager beepManager = new BeepManager(getActivity());
        decoder.decodeContinuous(new BarcodeCallback() {

            @Override
            public void barcodeResult(BarcodeResult result) {
                String data = result.getText();
                Log.d(TAG, String.format("Read barcode data: <%s>", data));

                Gson gson = new Gson();
                try {
                    SettingsPage page = gson.fromJson(data, SettingsPage.class);
                    //basic sanity check
                    boolean continueProcessing = true;
                    if (page.getCurPage() < 1) {
                        Log.e(TAG, "QR code page value was less than 1, this is not a valid value, skipping this code");
                        continueProcessing = false;
                    } else if (page.getJsonFragment() == null) {
                        Log.e(TAG, "QR code json fragment was null, skipping this code");
                        continueProcessing = false;
                    } else if (totalPages != 0 && page.getTotalPages() != totalPages) {
                        //each QR code is expected to have the same totalpages value
                        Log.e(TAG, "QR code total pages count does not match previous value, skipping this code");
                        continueProcessing = false;
                    } else if (isCompressed != null && page.isCompressed() != isCompressed) {
                        Log.e(TAG, "QR code compression value does not match previous value, skipping this code");
                        continueProcessing = false;
                    } else if (versionCode != -1 && versionCode != page.getVersionCode()) {
                        Log.e(TAG, "QR code has a version code that does not match previous value, skipping this code");
                        continueProcessing = false;
                    }

                    if (continueProcessing) {
                        processPage(page);
                    }


                } catch (Exception e) {
                    Toast.makeText(getContext(), "Unable to decode settings from codes.", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            }

            private void processPage(SettingsPage page) {
                //if this was the first code scanned then we need to initialize the data buffer and our list of cards
                if (totalPages == 0) {
                    versionCode = page.getVersionCode();
                    totalPages = page.getTotalPages();
                    remainingPages = totalPages;
                    isCompressed = page.isCompressed();
                    parts = new String[totalPages];

                    for (int i = 0; i < totalPages; i++) {
                        QRCodePageView pv = new QRCodePageView(getContext(), null);
                        pv.setPageNumber(i + 1);
                        pv.setScanned(false);
                        list.addView(pv);
                    }

                    //animate the changes to the info card
                    animateCardTransition();
                }

                decoder.setStatusText("Continue scanning QR codes");

                if (parts[page.getCurPage() - 1] == null) {
                    beepManager.playBeepSoundAndVibrate();
                    remainingPages--;
                    parts[page.getCurPage() - 1] = page.getJsonFragment();
                    if (remainingPages == 0) {
                        title.setText("Finished scanning codes!");
                        decoder.pauseAndWait();
                    } else {
                        title.setText(String.format("%d codes remain to be scanned", remainingPages));
                    }
                    //update the page view in the list
                    updatePageView(page);
                }


                //if all the codes have been scanned then we can apply the settings
                boolean allCodesScanned = true;
                for (String part : parts) {
                    if (part == null) {
                        allCodesScanned = false;
                        break;
                    }
                }

                if (allCodesScanned) {
                    applySettings();
                }
            }

            private void applySettings() {
                try {
                    StringBuilder sb = new StringBuilder();
                    for (String part : parts) {
                        sb.append(part);
                    }

                    String normalized = sb.toString();
                    if (isCompressed) {
                        byte[] compressedBytes = GzipUtil.base64Decode(normalized);
                        normalized = GzipUtil.decompress(compressedBytes);
                    }

                    listener.onPreferencesRead(versionCode, normalized);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Unable to decode settings from codes.", Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {

            }
        });
    }

    private void findViews() {
        scrollView = rootView.findViewById(R.id.scrollview);
        list = rootView.findViewById(R.id.list);
        title = rootView.findViewById(R.id.title);
        info1 = rootView.findViewById(R.id.info1);
        info2 = rootView.findViewById(R.id.info2);
        decoder = rootView.findViewById(R.id.scanner);
        card = rootView.findViewById(R.id.card);
    }

    private void animateCardTransition() {
        titleGravity = Gravity.CENTER_HORIZONTAL;
        title.setText("Continue scanning all codes");
        AnimationHelper.expandAndFadeIn(getContext(), scrollView);
        AnimationHelper.collapseAndFadeOut(getContext(), info1);
        AnimationHelper.collapseAndFadeOut(getContext(), info2);

    }

    private void updatePageView(SettingsPage page) {
        int count = list.getChildCount();
        for (int i = 0; i < count; i++) {
            final View v = list.getChildAt(i);
            if (v instanceof QRCodePageView) {
                if (((QRCodePageView) v).getPageNumber() == page.getCurPage()) {
                    ((QRCodePageView) v).setScanned(true);
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            int x = v.getLeft();
                            int curX = scrollView.getScrollX();
                            scrollView.smoothScrollTo(x, 0);
                        }
                    });
                    break;
                }
            }
        }
    }


}
