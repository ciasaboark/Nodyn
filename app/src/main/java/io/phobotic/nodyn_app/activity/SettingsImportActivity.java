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

package io.phobotic.nodyn_app.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.EditText;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.apache.poi.util.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.helper.AnimationHelper;
import io.phobotic.nodyn_app.preference.OnPreferencesReadListener;
import io.phobotic.nodyn_app.preference.SettingsImporter;
import io.phobotic.nodyn_app.preference.SettingsPage;
import io.phobotic.nodyn_app.security.EncryptionManager;
import io.phobotic.nodyn_app.view.StepView;

public class SettingsImportActivity extends AppCompatActivity {
    private static final String TAG = SettingsImportActivity.class.getSimpleName();
    private static final long MIN_MS = 3500;
    private View rootView;
    private View passwordCard;
    private View errorCard;
    private View decryptCard;
    private EditText pass1;
    private EditText pass2;
    private EditText pass3;
    private EditText pass4;
    private FloatingActionButton decryptFileFab;
    private FloatingActionButton backFab;
    private StepView step1;
    private StepView step2;
    private View decryptError;
    private StepView step3;
    private TextView errorText;
    private TextView decryptErrorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_import);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        String action = i.getAction();
        String type = i.getType();

        init();

        if (type != null && (Intent.ACTION_SEND.equals(action) || Intent.ACTION_VIEW.equals(action))) {
            //show the import card

        } else {
            //show the main view with a warning to open the file directly
        }
    }

    private void init() {
        findViews();
        initViews();
        initPasswordCheck();
        initButtons();
        pass1.requestFocus();
    }

    private void findViews() {
        rootView = findViewById(R.id.root);
        errorCard = findViewById(R.id.error_card);
        passwordCard = findViewById(R.id.password_card);
        decryptCard = findViewById(R.id.decrypt_card);
        pass1 = findViewById(R.id.pass1);
        pass2 = findViewById(R.id.pass2);
        pass3 = findViewById(R.id.pass3);
        pass4 = findViewById(R.id.pass4);
        decryptFileFab = findViewById(R.id.fab_decrypt);
        backFab = findViewById(R.id.fab_back);
        step1 = findViewById(R.id.step1);
        step2 = findViewById(R.id.step2);
        step3 = findViewById(R.id.step3);
        decryptError = findViewById(R.id.decrypt_error);
        errorText = findViewById(R.id.error_text);
        decryptErrorText = findViewById(R.id.decrypt_error_text);
    }

    private void initViews() {
        passwordCard.setVisibility(View.VISIBLE);
        decryptCard.setVisibility(View.GONE);
        decryptError.setVisibility(View.GONE);
        errorCard.setVisibility(View.GONE);
        ((View) decryptFileFab).setVisibility(View.GONE);
    }

    private void initPasswordCheck() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //nothing to do here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //nothing to do here
            }

            @Override
            public void afterTextChanged(Editable s) {
                String part1 = pass1.getText().toString();
                String part2 = pass2.getText().toString();
                String part3 = pass3.getText().toString();
                String part4 = pass4.getText().toString();

                if (part1.length() == 4) {
                    pass2.requestFocus();
                }

                if (part2.length() == 4) {
                    pass3.requestFocus();
                }

                if (part3.length() == 4 || part4.length() == 4) {
                    pass4.requestFocus();
                }

                if (part1.length() == 4 &&
                        part2.length() == 4 &&
                        part3.length() == 4 &&
                        part4.length() == 4) {
                    showFabIfRequired(decryptFileFab);
                } else {
                    hideFabIfRequired(decryptFileFab);
                }
            }
        };

        pass1.addTextChangedListener(watcher);
        pass2.addTextChangedListener(watcher);
        pass3.addTextChangedListener(watcher);
        pass4.addTextChangedListener(watcher);
    }

    private void initButtons() {
        decryptFileFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //attempt to use the user supplied password to decrypt the settings file
                hideFab(decryptFileFab);
                beginFileDecrypt();
            }
        });

        backFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show the password import card again
                Animation animateOut = AnimationUtils.loadAnimation(SettingsImportActivity.this, android.R.anim.fade_out);
                final Animation animateIn = AnimationUtils.loadAnimation(SettingsImportActivity.this, R.anim.enter_from_bottom);

                animateIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        passwordCard.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        pass1.requestFocus();
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                animateOut.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        //reset the decrption card
                        decryptCard.setVisibility(View.GONE);
                        step1.reset();
                        step2.reset();
                        decryptError.setVisibility(View.GONE);
                        decryptErrorText.setText("");

                        //pull up the password imput card.  The password field should be blanked
                        //+ out and an error should be visible
                        pass1.setText(null);
                        pass2.setText(null);
                        pass3.setText(null);
                        pass4.setText(null);

                        passwordCard.startAnimation(animateIn);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        //nothing to do here
                    }
                });

                decryptCard.startAnimation(animateOut);
            }
        });
    }

    private void showFabIfRequired(FloatingActionButton fab) {
        if (fab.getVisibility() == View.GONE) {
            showFab(fab);
        }
    }

    private void hideFabIfRequired(FloatingActionButton fab) {
        if (fab.getVisibility() == View.VISIBLE) {
            hideFab(fab);
        }
    }

    private void hideFab(final FloatingActionButton fab) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(fab, "scaleX", 1, 0);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(fab, "scaleY", 1, 0);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(scaleX, scaleY);
        animSetXY.setInterpolator(new BounceInterpolator());
        animSetXY.setDuration(500);
        animSetXY.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ((View) fab).setVisibility(View.GONE);   //why the warning if fab is treated as a FloatingActionButton?
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animSetXY.start();
    }

    private void beginFileDecrypt() {
        //animate in the decryption card
        final Animation animateOut = AnimationUtils.loadAnimation(SettingsImportActivity.this, android.R.anim.fade_out);
        final Animation animateIn = AnimationUtils.loadAnimation(SettingsImportActivity.this, R.anim.enter_from_bottom);

        animateIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                decryptCard.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //begin the decryption process only after the decypt card has finished animating
                beginImport();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        animateOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                //nothing to do here
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                passwordCard.setVisibility(View.GONE);
                decryptCard.startAnimation(animateIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                //nothing to do here
            }
        });

        passwordCard.startAnimation(animateOut);
    }

    private void showFab(final FloatingActionButton fab) {  // TODO: 3/10/19 fab state can be wrong if text changed while still animating.  find fix
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(fab, "scaleX", 0, 1);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(fab, "scaleY", 0, 1);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(scaleX, scaleY);
        animSetXY.setInterpolator(new BounceInterpolator());
        animSetXY.setDuration(500);
        ((View) fab).setVisibility(View.VISIBLE); //why the warning when fab is treated as a FloatingActionButton
        animSetXY.start();
    }

    private void beginImport() {
        step1.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    String decryptedText = decryptFile();
                    step1.complete();
                    tryParseText(decryptedText);
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);

                    step1.fail();
                    String reason = String.format("Caught %s while decrypting bytes", e.getClass().getSimpleName());
                    decryptErrorText.setText(reason);
                    AnimationHelper.expand(decryptError);
                    Log.e(TAG, reason);
                }
            }
        }, MIN_MS);

    }

    private String decryptFile() throws Exception {
        //todo get access to the file, decrypt, then import settings
        StringBuilder password = new StringBuilder();
        password.append(pass1.getEditableText().toString());
        password.append(pass2.getEditableText().toString());
        password.append(pass3.getEditableText().toString());
        password.append(pass4.getEditableText().toString());

        Intent i = getIntent();
        Uri uri = i.getData();
        if (uri.getAuthority() != null) {
            try (InputStream is = this.getContentResolver().openInputStream(uri)) {
                byte[] bytes = IOUtils.toByteArray(is);
                String b64encrypted = new String(bytes);
                String plaintext = EncryptionManager.decrypt(b64encrypted, password.toString());
                Log.d(TAG, plaintext);
                return plaintext;
            } catch (IOException e) {
                throw e;
            }
        } else {
            throw new Exception("Unknown authority value");
        }
    }

    private void tryParseText(final String plaintext) {
        step2.start();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    SettingsPage settingsPage = decodeSettings(plaintext);
                    step2.complete();
                    importSettingsPage(settingsPage);
                } catch (Exception e) {
                    e.printStackTrace();
                    step2.fail();
                    Crashlytics.logException(e);
                }
            }
        }, MIN_MS);
    }

    private SettingsPage decodeSettings(String settingsString) {
        Gson gson = new Gson();
        SettingsPage page = gson.fromJson(settingsString, SettingsPage.class);
        int versionCode = page.getVersionCode();
        return page;
    }

    private void importSettingsPage(SettingsPage page) {
        step3.start();
        //show the settings import dialog
        SettingsImporter settingsImporter = new SettingsImporter(SettingsImportActivity.this);
        settingsImporter.setListener(new OnPreferencesReadListener() {
            @Override
            public void onPreferencesRead(int versionCode, Map<String, Object> map) {

            }

            @Override
            public void onPreferencesRead(int versionCode, String json) {

            }

            @Override
            public void onPreferenceImportComplete(boolean imported) {
                if (imported) {
                    step3.complete();
                    Snackbar.make(rootView,
                            "Settings imported successfully!", Snackbar.LENGTH_SHORT).show();
                } else {
                    step3.fail();
                    Snackbar.make(rootView,
                            "Settings were not imported!", Snackbar.LENGTH_SHORT).show();

                }

                delayedExit();
            }
        });

        try {
            settingsImporter.importSettings(page.getVersionCode(), page.getJsonFragment());
        } catch (Exception e) {
            step3.fail();
            Snackbar.make(rootView,
                    "Error importing settings!", Snackbar.LENGTH_SHORT).show();
            delayedExit();

        }
    }

    private void delayedExit() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SettingsImportActivity.this.finish();
            }
        }, 2000);
    }


}


