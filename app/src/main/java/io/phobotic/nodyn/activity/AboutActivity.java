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

        TextView sourceText = (TextView) findViewById(R.id.about_source_text);
        sourceText.setMovementMethod(LinkMovementMethod.getInstance());

        TextView bugsText = (TextView) findViewById(R.id.about_bugs_text);
        bugsText.setMovementMethod(LinkMovementMethod.getInstance());

        TextView commentsText = (TextView) findViewById(R.id.about_comments_text);
        commentsText.setMovementMethod(LinkMovementMethod.getInstance());

        final TextView versionText = (TextView) findViewById(R.id.about_version_number);
        String formattedVersion = String.format(getString(R.string.about_version),
                Versioning.getVersionCode());
        versionText.setText(formattedVersion);
        //textview using marquee scrolling, but this only works if the textview is selected
        versionText.setSelected(true);

        final View background = findViewById(R.id.background);
//        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
//        fadeInAnimation.setDuration(10000);
//        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//                background.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//        background.startAnimation(fadeInAnimation);

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
            View shine = findViewById(R.id.shine);
            AnimationDrawable shineAnimationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.about_shine);
            shine.setBackground(shineAnimationDrawable);
            shineAnimationDrawable.setEnterFadeDuration(5000);
            shineAnimationDrawable.setExitFadeDuration(0);
            shineAnimationDrawable.setOneShot(true);
            shineAnimationDrawable.start();

            AnimationDrawable backgroundAnimationDrawable = (AnimationDrawable) background.getBackground();
            backgroundAnimationDrawable.setEnterFadeDuration(1000);
            backgroundAnimationDrawable.setExitFadeDuration(2000);
            backgroundAnimationDrawable.setOneShot(true);
            backgroundAnimationDrawable.start();

            final ImageView logo = (ImageView) findViewById(R.id.logo);
            AnimationDrawable logoDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.about_logo);
            logo.setImageDrawable(logoDrawable);
            logoDrawable.setEnterFadeDuration(1000);
            logoDrawable.setExitFadeDuration(2000);
            logoDrawable.setOneShot(true);
            logoDrawable.start();

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

//        logo.setImageDrawable(getDrawable(R.drawable.app_icon_196_light));
//        Animation slideDownAnimation = AnimationUtils.loadAnimation(this, R.anim.top_down);
//        slideDownAnimation.setInterpolator(new AccelerateDecelerateInterpolator(this, null));
//        slideDownAnimation.setDuration(2000);
//        slideDownAnimation.setStartOffset(3000);
//        slideDownAnimation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//                logo.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//        logo.startAnimation(slideDownAnimation);

    }

    private double[] getGPS() {
        LocationManager lm = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);
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
}
