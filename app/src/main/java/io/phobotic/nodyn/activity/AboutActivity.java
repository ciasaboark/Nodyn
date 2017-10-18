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

package io.phobotic.nodyn.activity;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import io.phobotic.nodyn.MoonCalculation;
import io.phobotic.nodyn.R;
import io.phobotic.nodyn.Versioning;

public class AboutActivity extends AppCompatActivity {

    private static final String TAG = AboutActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        init();
    }

    private void init() {
        final TextView intro = (TextView) findViewById(R.id.about_intro);
        final TextView introContinued = (TextView) findViewById(R.id.about_intro_continued);
        introContinued.setMovementMethod(LinkMovementMethod.getInstance());

        initSourceText();
        initBugsText();
        initCommentsText();
        initVersionTextView();
        initLicenseView();

        initAnimation(intro, introContinued);
    }

    private void initSourceText() {
        TextView sourceText1 = (TextView) findViewById(R.id.about_source_text1);
        sourceText1.setMovementMethod(LinkMovementMethod.getInstance());

        TextView sourceText2 = (TextView) findViewById(R.id.about_source_text2);
        sourceText2.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initBugsText() {
        TextView bugsText1 = (TextView) findViewById(R.id.about_bugs_text1);
        bugsText1.setMovementMethod(LinkMovementMethod.getInstance());

        TextView bugsText2 = (TextView) findViewById(R.id.about_bugs_text2);
        bugsText2.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initCommentsText() {
        TextView commentsText = (TextView) findViewById(R.id.about_comments_text);
        commentsText.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initVersionTextView() {
        TextView versionText = (TextView) findViewById(R.id.about_version_number);
        String formattedVersion = String.format(getString(R.string.about_version),
                Versioning.getVersionCode());
        versionText.setText(formattedVersion);
        //textview using marquee scrolling, but this only works if the textview is selected
        versionText.setSelected(true);
    }

    private void initLicenseView() {
        TextView licenseText1 = (TextView) findViewById(R.id.about_license_text1);
        licenseText1.setMovementMethod(LinkMovementMethod.getInstance());

        TextView licenseText2 = (TextView) findViewById(R.id.about_license_text2);
        licenseText2.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void initAnimation(final TextView intro, final TextView introContinued) {
        final TextView versionText = (TextView) findViewById(R.id.about_version_number);
        final View background = findViewById(R.id.background);

        MoonCalculation moonCalculation = new MoonCalculation();
        Calendar cal = Calendar.getInstance();
        for (int i = 0; i < 30; i++) {
            int year = cal.get(Calendar.YEAR);
            int day = cal.get(Calendar.DAY_OF_MONTH);
            int month = cal.get(Calendar.MONTH) + 1;    //Moon calculation expects Jan = 1
            int phase = moonCalculation.moonPhase(year, month, day);
            String phaseName = MoonCalculation.moon_phase_name[phase];
            DateFormat df = new SimpleDateFormat();
            Log.d(TAG, df.format(new Date(cal.getTimeInMillis())) + ": " + phaseName);

            cal.add(Calendar.DAY_OF_MONTH, 1);
        }


        double[] gps = getGPS();
        Location location = new Location(String.valueOf(gps[0]), String.valueOf(gps[1]));
        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, TimeZone.getDefault());

        Calendar officialSunset = calculator.getOfficialSunsetCalendarForDate(Calendar.getInstance());
        long sunset = officialSunset.getTimeInMillis();
        long now = System.currentTimeMillis();
        if (sunset <= now) {
            animateInShine();
            animateInBackground(background);
            animateLogoChange();
            animateIntroTextColorChange(intro, introContinued, versionText);
        }
    }

    private double[] getGPS() {
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        LocationManager lm = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);

        // TODO: 10/18/17 call requires permission dialog, fix this later
//        lm.requestLocationUpdates(locationProvider, 0, 0, new LocationListener() {
//            @Override
//            public void onLocationChanged(android.location.Location location) {
//
//            }
//
//            @Override
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//
//            }
//
//            @Override
//            public void onProviderEnabled(String provider) {
//
//            }
//
//            @Override
//            public void onProviderDisabled(String provider) {
//
//            }
//        });


        List<String> providers = lm.getProviders(true);

        android.location.Location l = null;

        for (int i = providers.size() - 1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }

        double[] gps = new double[2];
        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }

        return gps;
    }

    private void animateInShine() {
        View shine = findViewById(R.id.shine);
        AnimationDrawable shineAnimationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.about_shine);
        shine.setBackground(shineAnimationDrawable);
        shineAnimationDrawable.setEnterFadeDuration(5000);
        shineAnimationDrawable.setExitFadeDuration(0);
        shineAnimationDrawable.setOneShot(true);
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
        final ImageView logo = (ImageView) findViewById(R.id.logo);
        AnimationDrawable logoDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.about_logo);
        logo.setImageDrawable(logoDrawable);
        logoDrawable.setEnterFadeDuration(1000);
        logoDrawable.setExitFadeDuration(2000);
        logoDrawable.setOneShot(true);
        logoDrawable.start();
    }

    private void animateIntroTextColorChange(final TextView intro, final TextView introContinued, final TextView versionText) {
        animateTextColorChange(intro, introContinued, versionText);
        animateLinkColorChange(intro, introContinued);
    }

    private void animateTextColorChange(final TextView intro, final TextView introContinued, final TextView versionText) {
        final float[] from = new float[3],
                to = new float[3];

        Color.colorToHSV(getResources().getColor(android.R.color.secondary_text_light), from);
        Color.colorToHSV(getResources().getColor(android.R.color.primary_text_dark), to);

        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.setDuration(1500);
        anim.setStartDelay(3000);

        final float[] hsv = new float[3];
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // Transition along each axis of HSV (hue, saturation, value)
                hsv[0] = from[0] + (to[0] - from[0]) * animation.getAnimatedFraction();
                hsv[1] = from[1] + (to[1] - from[1]) * animation.getAnimatedFraction();
                hsv[2] = from[2] + (to[2] - from[2]) * animation.getAnimatedFraction();

                versionText.setTextColor(Color.HSVToColor(hsv));
                intro.setTextColor(Color.HSVToColor(hsv));
                introContinued.setTextColor(Color.HSVToColor(hsv));
            }
        });

        anim.start();
    }

    private void animateLinkColorChange(final TextView intro, final TextView introContinued) {
        final float[] from = new float[3],
                to = new float[3];

        Color.colorToHSV(getResources().getColor(R.color.text_link_color_light), from);
        Color.colorToHSV(getResources().getColor(R.color.text_link_color_dark), to);

        ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
        anim.setDuration(1500);
        anim.setStartDelay(3000);

        final float[] hsv = new float[3];
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                // Transition along each axis of HSV (hue, saturation, value)
                hsv[0] = from[0] + (to[0] - from[0]) * animation.getAnimatedFraction();
                hsv[1] = from[1] + (to[1] - from[1]) * animation.getAnimatedFraction();
                hsv[2] = from[2] + (to[2] - from[2]) * animation.getAnimatedFraction();

                intro.setLinkTextColor(Color.HSVToColor(hsv));
                introContinued.setLinkTextColor(Color.HSVToColor(hsv));
            }
        });

        anim.start();
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
            case R.id.menu_legal:
                Log.d(TAG, "show legal menu here");
                consumed = true;
                break;
        }

        return consumed;
    }
}
