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

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import io.phobotic.nodyn_app.BuildConfig;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.Versioning;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.preference.SettingsPage;
import io.phobotic.nodyn_app.security.EncryptionManager;
import io.phobotic.nodyn_app.view.StepView;

/**
 * Created by Jonathan Nelson on 2/27/19.
 */
public class ShareSettingsFileFragment extends Fragment {
    private static final String TAG = ShareSettingsFileFragment.class.getSimpleName();
    private static final int REQUEST_WRITE_STORAGE = 3;
    private View rootView;
    private CardView card;
    private StepView step1;
    private StepView step2;
    private StepView step3;
    private TextView pass1;
    private TextView pass2;
    private TextView pass3;
    private TextView pass4;
    private String settingsString;
    private String[] otp;
    private Button copyButton;
    private Button shareButton;
    private Button permissionsButton;
    private CardView errorCard;
    private ScrollView scroll;
    private TextView fileMessage;


    public static ShareSettingsFileFragment newInstance() {
        ShareSettingsFileFragment fragment = new ShareSettingsFileFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public static byte[] getSaltedKeyBytes(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //AES requires passwords to be of a specific length.
        byte[] salt = {
                (byte) 0xAA,
                (byte) 0xAA,
                (byte) 0xAA,
                (byte) 0xAA,
                (byte) 0xAA,
                (byte) 0xAA,
                (byte) 0xAA,
                (byte) 0xAA
        };

        KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 65536, 128);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        return f.generateSecret(spec).getEncoded();
    }

    public ShareSettingsFileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.enter_from_bottom);
                    bottomUp.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            scroll.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    scroll.startAnimation(bottomUp);

                    AnimationHelper.fadeOut(getContext(), errorCard);

                    createBackup();
                } else {
                    //the view explaining why permissions are required should already be visible
                }
            }
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
        rootView = inflater.inflate(R.layout.fragment_share_settings_file, container, false);
        init();
        return rootView;
    }

    private void init() {
        findViews();
        card.setVisibility(View.GONE);
        errorCard.setVisibility(View.GONE);
        scroll.setVisibility(View.GONE);

        permissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_STORAGE);
            }
        });


        boolean hasPermission = (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (hasPermission) {
            //if we have permission to write the the SDCard go ahead and generate the backup
            Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.enter_from_bottom);
            bottomUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    scroll.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            scroll.startAnimation(bottomUp);
            createBackup();
        } else {
            // TODO: 3/4/19 show card explaning why access is required
            Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.enter_from_bottom);
            bottomUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    errorCard.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            errorCard.startAnimation(bottomUp);

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);

        }
    }

    private void findViews() {
        card = rootView.findViewById(R.id.card);
        step1 = rootView.findViewById(R.id.step1);
        step2 = rootView.findViewById(R.id.step2);
        step3 = rootView.findViewById(R.id.step3);
        pass1 = rootView.findViewById(R.id.pass1);
        pass2 = rootView.findViewById(R.id.pass2);
        pass3 = rootView.findViewById(R.id.pass3);
        pass4 = rootView.findViewById(R.id.pass4);
        shareButton = card.findViewById(R.id.share_button);
        copyButton = card.findViewById(R.id.copy_button);
        permissionsButton = rootView.findViewById(R.id.permissions_button);
        errorCard = rootView.findViewById(R.id.error_card);
        scroll = rootView.findViewById(R.id.scroll);
        fileMessage = rootView.findViewById(R.id.backup_file);
    }

    private void createBackup() {
        final long minMS = 3500;
        try {
            long start = System.currentTimeMillis();
            step1.start();
            settingsString = readSettings();

            //This came out a bit ugly.  Expand the duration of each step so the user has some visual
            //+ indication that work is being done

            long remaining = minMS - (System.currentTimeMillis() - start);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    step1.complete();
                    long start = System.currentTimeMillis();
                    step2.start();
                    otp = createPassword();

                    long remaining = minMS - (System.currentTimeMillis() - start);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            step2.complete();
                            long start = System.currentTimeMillis();
                            step3.start();
                            File file = null;
                            try {
                                file = createFile(settingsString, otp);
                            } catch (Exception e) {
                                // TODO: 3/3/19
                                Log.e(TAG, "Caught exception creating file: " + e.getMessage());
                                FirebaseCrashlytics.getInstance().recordException(e);
                                step3.complete();
                            }

                            long remaining = minMS - (System.currentTimeMillis() - start);

                            final File finalFile = file;
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    step3.complete();
                                    showCard(otp, finalFile);
                                }
                            }, remaining);
                        }
                    }, remaining);
                }
            }, remaining);


        } catch (Exception e) {
            Log.e(TAG, "Caught exception creating file: " + e.getMessage());
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private String readSettings() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Map<String, ?> allPrefs = prefs.getAll();
        Gson gson = new Gson();
        String json = gson.toJson(allPrefs);

        return json;
    }

    private String[] createPassword() {
        String[] otp = new String[4];
        otp[0] = RandomStringUtils.random(4, true, true);
        otp[1] = RandomStringUtils.random(4, true, true);
        otp[2] = RandomStringUtils.random(4, true, true);
        otp[3] = RandomStringUtils.random(4, true, true);

        return otp;
    }


//    private byte[] getEncryptedBytes(String text, String key) throws Exception {
//        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
////        String b64Compressed = StringHelper.compress(text);
//        System.out.println("Original text:" + text);
//
//
//        byte[] input = text.getBytes();
//        byte[] keyBytes = getSaltedKeyBytes(key);
//
//        SecretKeySpec secretKeySpec= new SecretKeySpec(keyBytes, "AES");
//
//        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding");
//
//        // encryption pass
//        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
//
//        byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
//        int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
//        ctLength += cipher.doFinal(cipherText, ctLength);
//
//        System.out.println("Encrypted: " + new String(cipherText));
//        System.out.println("Encrypted length: " + String.valueOf(ctLength));
//
//        return cipherText;
//    }

    private File createFile(String prefs, final String[] otp) throws Exception {
        //wrap the settings in a SettingsPage.  This will allow us to compare version codes
        //+ before importing the settings later

        SettingsPage page = new SettingsPage(BuildConfig.VERSION_CODE, 1, 1,
                prefs, false);
        Gson gson = new Gson();
        String pageJson = gson.toJson(page);

        StringBuilder password = new StringBuilder();
        password.append(otp[0]);
        password.append(otp[1]);
        password.append(otp[2]);
        password.append(otp[3]);


        DateFormat df = new SimpleDateFormat("yyyy_MM_dd");
        Date d = new Date();
        String date = df.format(d);
        final String filename = String.format("nodyn_backup_%s.nodyn", date);

        File sdcard = Environment.getExternalStorageDirectory();
        String filePath = sdcard.getAbsolutePath() + "/" + filename;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            writeEncryptedBytes(fos, pageJson, password.toString());
            String text = String.format(getResources().getString(R.string.share_settings_file_settings_backup_filename), filePath);
            fileMessage.setText(text);
        } catch (Exception e) {
            Exception e1 = new Exception(String.format("Caught exception [%s] writing data to " +
                    "file %s: %s", e.getClass().getSimpleName(), filePath, e.getMessage()));
            FirebaseCrashlytics.getInstance().recordException(e1);
            throw e1;
        }

        return new File(filePath);
    }

    private void showCard(final String[] otp, final File file) {
        pass1.setText(otp[0]);
        pass2.setText(otp[1]);
        pass3.setText(otp[2]);
        pass4.setText(otp[3]);

        Animation bottomUp = AnimationUtils.loadAnimation(getContext(), R.anim.enter_from_bottom);
        bottomUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                card.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        card.startAnimation(bottomUp);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareFile(file);
            }
        });

        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder sb = new StringBuilder();
                sb.append(otp[0] + " ");
                sb.append(otp[1] + " ");
                sb.append(otp[2] + " ");
                sb.append(otp[3] + " ");

                ClipboardManager clipboard = (ClipboardManager) getContext()
                        .getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Nodyn backup password", sb.toString());
                clipboard.setPrimaryClip(clip);

                Snackbar.make(rootView,
                        "One time password was copied to clipboard!", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void writeEncryptedBytes(FileOutputStream fos, String text, String password) throws Exception {
        String b64 = EncryptionManager.encrypt(text, password);
        fos.write(b64.getBytes());
    }

    private void shareFile(File file) {
        DateFormat df = SimpleDateFormat.getDateInstance();
        DateFormat dtf = SimpleDateFormat.getDateTimeInstance();

        Date d = new Date();
        String today = df.format(d);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setType("text/plain");
        StringBuilder sb = new StringBuilder();
        sb.append("Nodyn settings backup\n");
        sb.append("Generated on " + dtf.format(d) + "\n");
        sb.append("Nodyn version " + Versioning.getVersionCode() + ".\n");
        sb.append("Nodyn build version " + BuildConfig.VERSION_CODE + ".\n");
        sb.append("Device : " + Versioning.getDeviceName() + ".\n");

        sb.append("The password for this backup is: <insert here if desired>\n\n");
        sb.append("You can use this backup to restore settings on the same device, or to copy " +
                "settings to a new device.\n");

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Nodyn settings " + today);
        emailIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        Uri fileURI = FileProvider.getUriForFile(
                getContext(),
                getContext().getApplicationContext()
                        .getPackageName() + ".provider", file);

        emailIntent.putExtra(Intent.EXTRA_STREAM, fileURI);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Verify it resolves
        PackageManager packageManager = getContext().getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(emailIntent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start an activity if it's safe
        if (isIntentSafe) {
            startActivity(emailIntent);
        }

    }

    private String getSettingsAsJson() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        Map<String, ?> allPrefs = prefs.getAll();

        Gson gson = new Gson();
        String json = gson.toJson(allPrefs);

        return json;
    }
}

