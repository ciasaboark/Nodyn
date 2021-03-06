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

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import io.phobotic.nodyn_app.MoonCalculation;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.Versioning;

public class AboutActivity extends AppCompatActivity {

    private static final String TAG = AboutActivity.class.getSimpleName();
    private Handler handler;
    private Runnable chirpRunnable;
    private TextView versionText;
    private View background;
    private TextView intro;
    private TextView introContinued;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setupActionBar();
        init();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setTitle(R.string.title_activity_about);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void init() {
        findViews();
        initSourceText();
        initBugsText();
        initCommentsText();
        initVersionTextView();
        initLicenseView();

        final View logo = findViewById(R.id.logo);
        logo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                initAnimation(intro, introContinued);
                logo.setOnLongClickListener(null);
                return true;
            }
        });

        GetLocationAsyncTask getLocationAsync = new GetLocationAsyncTask();
        getLocationAsync.execute();

    }

    private void findViews() {
        versionText = findViewById(R.id.about_version_number);
        background = findViewById(R.id.background);
        intro = findViewById(R.id.about_intro);
        introContinued = findViewById(R.id.about_intro_continued);
        introContinued.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initSourceText() {
        CardView card = findViewById(R.id.card_source);
        animateInCard(card);

        TextView sourceText1 = findViewById(R.id.about_source_text1);
        sourceText1.setMovementMethod(LinkMovementMethod.getInstance());

        TextView sourceText2 = findViewById(R.id.about_source_text2);
        sourceText2.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void animateInCard(final View view) {
        view.setVisibility(View.GONE);

        Animation bottomUp = AnimationUtils.loadAnimation(this, R.anim.enter_from_bottom);
        Random random = new Random(System.currentTimeMillis());
        long startOffset = random.nextInt(200);
        bottomUp.setStartOffset(startOffset);
        bottomUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(bottomUp);
    }

    private void initBugsText() {
        CardView card = findViewById(R.id.card_bugs);
        animateInCard(card);

        TextView bugsText1 = findViewById(R.id.about_bugs_text1);
        bugsText1.setMovementMethod(LinkMovementMethod.getInstance());

        TextView bugsText2 = findViewById(R.id.about_bugs_text2);
        bugsText2.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initCommentsText() {
        CardView card = findViewById(R.id.card_contact);
        animateInCard(card);

        TextView commentsText1 = findViewById(R.id.about_comments_text1);
        commentsText1.setMovementMethod(LinkMovementMethod.getInstance());

        TextView commentsText2 = findViewById(R.id.about_comments_text2);
        commentsText2.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initVersionTextView() {
        TextView versionText = findViewById(R.id.about_version_number);
        String formattedVersion = String.format(getString(R.string.about_version),
                Versioning.getVersionCode());
        versionText.setText(formattedVersion);
    }

    private void initLicenseView() {
        CardView card = findViewById(R.id.card_license);
        animateInCard(card);

        TextView licenseText1 = findViewById(R.id.about_license_text1);
        licenseText1.setMovementMethod(LinkMovementMethod.getInstance());

        TextView licenseText2 = findViewById(R.id.about_license_text2);
        licenseText2.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initAnimation(final TextView intro, final TextView introContinued) {
        animateTransition();

//        MoonCalculation moonCalculation = new MoonCalculation();
//        Calendar cal = Calendar.getInstance();
//        for (int i = 0; i < 30; i++) {
//            int year = cal.get(Calendar.YEAR);
//            int day = cal.get(Calendar.DAY_OF_MONTH);
//            int month = cal.get(Calendar.MONTH) + 1;    //Moon calculation expects Jan = 1
//            int phase = moonCalculation.moonPhase(year, month, day);
//            String phaseName = MoonCalculation.moon_phase_name[phase];
//            DateFormat df = DateFormat.getDateTimeInstance();
//            Log.d(TAG, df.format(new Date(cal.getTimeInMillis())) + ": " + phaseName);
//
//            cal.add(Calendar.DAY_OF_MONTH, 1);
//        }
    }

    private void animateTransition() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                animateInShine();
                animateInBackground(background);
                animateLogoChange();
            }
        });
    }

    private void animateInShine() {
        View shine = findViewById(R.id.shine);
        AnimationDrawable shineAnimationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.about_shine);
        shine.setBackground(shineAnimationDrawable);
        shineAnimationDrawable.setEnterFadeDuration(5000);
        shineAnimationDrawable.setExitFadeDuration(0);
        shineAnimationDrawable.setOneShot(true);
        int delay = 9000;

        handler = new Handler();
        chirpRunnable = new Runnable() {
            @Override
            public void run() {
                MediaPlayer player = MediaPlayer.create(AboutActivity.this, R.raw.cricketchirpsyntheticpure);
                player.start();
            }
        };

        handler.postDelayed(chirpRunnable, delay);
        shineAnimationDrawable.start();
    }

    private void animateInBackground(View background) {
        AnimationDrawable backgroundAnimationDrawable = (AnimationDrawable) background.getBackground();
        backgroundAnimationDrawable.setEnterFadeDuration(1000);
        backgroundAnimationDrawable.setExitFadeDuration(2000);
        backgroundAnimationDrawable.setOneShot(true);
        backgroundAnimationDrawable.start();
    }

    private void animateLogoChange() {
        final ImageView logo = findViewById(R.id.logo);
        AnimationDrawable logoDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.about_logo);
        logo.setImageDrawable(logoDrawable);
        logoDrawable.setEnterFadeDuration(1000);
        logoDrawable.setExitFadeDuration(2000);
        logoDrawable.setOneShot(true);
        logoDrawable.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (handler != null) {
            handler.removeCallbacks(chirpRunnable);
        }
    }

    private double[] getLocation() throws Exception {
        String json = getLocationJson();
        Map<String, Object> retMap = new Gson().fromJson(json,
                new TypeToken<HashMap<String, Object>>() {
                }.getType()
        );
        double lat = (double) retMap.get("latitude");
        double lng = (double) retMap.get("longitude");
        double[] gps = {lat, lng};

        return gps;
    }

    private String getLocationJson() throws Exception {
        String reverseIP = getReverseIP();
        if (reverseIP == null) {
            throw new Exception("Unable to find reverse IP address");
        }

        final String urlString = "https://api.ipdata.co/" + reverseIP;
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // default is GET
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        conn.setUseCaches(false);

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Bad response from server");
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    private String getReverseIP() throws Exception {
        String host = "https://api.ipify.org/?format=json";
        URL url = new URL(host);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // default is GET
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        conn.setUseCaches(false);

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Bad response from server");
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Map<String, Object> retMap = new Gson().fromJson(response.toString(),
                new TypeToken<HashMap<String, Object>>() {
                }.getType()
        );

        String reverseIP = (String) retMap.get("ip");

        return reverseIP;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean consumed = false;

        switch (item.getItemId()) {

        }

        return consumed;
    }

    private class GetLocationAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                double[] gps = getLocation();

                Location location = new Location(String.valueOf(gps[0]), String.valueOf(gps[1]));
                SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, TimeZone.getDefault());

                Calendar cal = Calendar.getInstance();
                Calendar officialSunset = calculator.getOfficialSunsetCalendarForDate(cal);

                cal.add(Calendar.DAY_OF_MONTH, 1);
                Calendar officialSunrise = calculator.getOfficialSunriseCalendarForDate(cal);

                long sunset = officialSunset.getTimeInMillis();
                long sunrise = officialSunrise.getTimeInMillis();
                long now = System.currentTimeMillis();

                //is it a full moon?
                MoonCalculation moonCalculation = new MoonCalculation();
                if (now >= sunset && now <= sunrise) {
                    Log.d(TAG, "past sunset");
                    cal = Calendar.getInstance();
                    int year = cal.get(Calendar.YEAR);
                    int day = cal.get(Calendar.DAY_OF_MONTH);
                    int month = cal.get(Calendar.MONTH) + 1;    //Moon calculation expects Jan = 1
                    int phase = moonCalculation.moonPhase(year, month, day);
                    if (phase == 4) {
                        Log.d(TAG, "MOOOOON!");
                        animateTransition();
                    } else {
                        Log.d(TAG, "moon is in phase: " + MoonCalculation.moon_phase_name[phase]);
                    }

                }
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            return null;
        }
    }
}
