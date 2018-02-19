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

package io.phobotic.nodyn_app.activity;


import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.fragment.preference.AssetPreferenceFragment;
import io.phobotic.nodyn_app.fragment.preference.AuditPreferenceFragment;
import io.phobotic.nodyn_app.fragment.preference.BetaPreferenceFragment;
import io.phobotic.nodyn_app.fragment.preference.CheckInPreferenceFragment;
import io.phobotic.nodyn_app.fragment.preference.CheckOutPreferenceFragment;
import io.phobotic.nodyn_app.fragment.preference.DataSyncPreferenceFragment;
import io.phobotic.nodyn_app.fragment.preference.EmailPreferenceFragment;
import io.phobotic.nodyn_app.fragment.preference.GeneralPreferenceFragment;
import io.phobotic.nodyn_app.fragment.preference.UsersPreferenceFragment;
import io.phobotic.nodyn_app.view.PreferenceSection;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static final String LULLABY = "453453";
    private static final String TIME = "312312";
    private LinearLayout holder;
    private View fragmentWrapper;
    private List<Integer> taps = new ArrayList<>();
    private boolean betaUnlocked = false;
    private MediaPlayer note1;
    private MediaPlayer note2;
    private MediaPlayer note3;
    private MediaPlayer note4;
    private MediaPlayer note5;
    private boolean isAnimating = false;
    private boolean handleOnBack = false;

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setupActionBar();

        init();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void init() {
        fragmentWrapper = findViewById(R.id.fragment_card);
        holder = (LinearLayout) findViewById(R.id.holder);

        showHideBeta();
        initNotes();
    }

    private void showHideBeta() {
        View beta = findViewById(R.id.beta);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        betaUnlocked = prefs.getBoolean(getString(R.string.beta_settings_unlocked), false);
        if (betaUnlocked) {
            beta.setVisibility(View.VISIBLE);
        } else {
            beta.setVisibility(View.GONE);
        }
    }

    private void initNotes() {
        note1 = MediaPlayer.create(this, R.raw.d);
        note2 = MediaPlayer.create(this, R.raw.f);
        note3 = MediaPlayer.create(this, R.raw.a);
        note4 = MediaPlayer.create(this, R.raw.b);
        note5 = MediaPlayer.create(this, R.raw.d2);
    }

    @Override
    public void onBackPressed() {
        if (handleOnBack) {
            hideChildFragment();
            handleOnBack = false;
        } else {
            super.onBackPressed();
        }
    }

    private void hideChildFragment() {

        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fragmentWrapper.setVisibility(View.GONE);

                FragmentManager fm = getSupportFragmentManager();
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame);
                fm.beginTransaction().remove(currentFragment).commit();
                fm.beginTransaction().replace(R.id.frame, new Fragment()).commit();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fragmentWrapper.startAnimation(fadeOut);

        expandAllSections();
    }

    private void expandAllSections() {
        //keep the beta settings hidden
        int betaID = R.id.beta;
        int count = holder.getChildCount();
        if (count > 0) {
            isAnimating = true;

            for (int i = 0; i < count; i++) {
                View v = holder.getChildAt(i);

                //make sure to only show the beta prefs if they are unlocked
                if (v.getId() == betaID) {
                    if (betaUnlocked) {
                        if (i == count - 1) {
                            expand(v, true);
                        } else {
                            expand(v, false);
                        }
                    }
                } else if (v.getVisibility() != View.VISIBLE) {
                    if (i == count - 1) {
                        expand(v, true);
                    } else {
                        expand(v, false);
                    }
                }

                if (v instanceof PreferenceSection) {
                    ((PreferenceSection) v).setHighlighted(false);
                }
            }
        }
    }

    private void expand(final View v, final boolean resetAnimated) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int height = v.getMeasuredHeight();
        ValueAnimator anim = ValueAnimator.ofInt(0, height);
        anim.setDuration(400);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                v.getLayoutParams().height = value.intValue();
                v.requestLayout();

                float alpha = ((float) height / (float) value);
                v.setAlpha(alpha);
            }
        });

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (resetAnimated) {
                    isAnimating = false;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        Random random = new Random();
        long offset = random.nextInt(500);
        anim.setStartDelay(offset);

        anim.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean itemHandled = false;

        switch (item.getItemId()) {
            case R.id.menu_share_settings:
                shareSettings();
                itemHandled = true;
                break;
            case R.id.menu_receive_settings:
                receiveSettings();
                itemHandled = true;
                break;
        }

        return itemHandled;
    }

    private void shareSettings() {
        Intent i = new Intent(this, ShareSettingsActivity.class);
        startActivity(i);
    }

    private void receiveSettings() {
        Intent i = new Intent(this, ReceiveSettingsActivity.class);
        startActivity(i);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragmentCompat.class.getName().equals(fragmentName)
                || UsersPreferenceFragment.class.getName().equals(fragmentName)
                || DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                || CheckInPreferenceFragment.class.getName().equals(fragmentName)
                || CheckOutPreferenceFragment.class.getName().equals(fragmentName)
                || AssetPreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        Fragment fragment = null;

        switch (id) {
            case R.id.general:
                fragment = new GeneralPreferenceFragment();
                break;
            case R.id.user:
                fragment = new UsersPreferenceFragment();
                break;
            case R.id.asset:
                fragment = new AssetPreferenceFragment();
                break;
            case R.id.checkout:
                fragment = new CheckOutPreferenceFragment();
                break;
            case R.id.checkin:
                fragment = new CheckInPreferenceFragment();
                break;
            case R.id.audit:
                fragment = new AuditPreferenceFragment();
                break;
            case R.id.data:
                fragment = new DataSyncPreferenceFragment();
                break;
            case R.id.email:
                fragment = new EmailPreferenceFragment();
                break;
            case R.id.beta:
                fragment = new BetaPreferenceFragment();
                break;
            case R.id.icon1:
                registerMagicTap(id, 1);
                break;
            case R.id.icon2:
                registerMagicTap(id, 2);
                break;
            case R.id.icon3:
                registerMagicTap(id, 3);
                break;
            case R.id.icon4:
                registerMagicTap(id, 4);
                break;
            case R.id.icon5:
                registerMagicTap(id, 5);
                break;
            default:
                Log.d(TAG, "Unknown preference category selected");
        }

        //only load the new fragment if we are not currently animating
        if (fragment != null && !isAnimating) {
            Fragment curChildFragment = getSupportFragmentManager().findFragmentById(R.id.frame);
            if (curChildFragment == null || !(curChildFragment instanceof PreferenceFragmentCompat)) {
                Log.d(TAG, "showing child fragment");
                showChildFragment(id, fragment);
            } else {
                Log.d(TAG, "hiding child fragment");
                hideChildFragment();
            }
        }
        Log.d(TAG, "Clicked " + id + " view");
    }

    private void registerMagicTap(int id, int i) {
        ImageView iv = (ImageView) findViewById(id);
        iv.setSoundEffectsEnabled(false);
        ObjectAnimator colorAnimator = ObjectAnimator.ofObject(iv, "colorFilter", new ArgbEvaluator(),
                0, 0);
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        @ColorInt int startColor = typedValue.data;

        int endColor = getHighlightColor(i, startColor);
        colorAnimator.setObjectValues(startColor, endColor, startColor);
        colorAnimator.setInterpolator(new FastOutSlowInInterpolator());
        colorAnimator.setDuration(1000);

        colorAnimator.start();

        recordTap(i);
    }

    private void showChildFragment(int sectionID, Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.frame, fragment).commit();

        Animation anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                fragmentWrapper.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fragmentWrapper.startAnimation(anim);
        collapseAllSectionsExcept(sectionID);
        handleOnBack = true;
    }

    private int getHighlightColor(int id, int defaultColor) {
        int endColor;
        switch (id) {
            case 1:
                endColor = getResources().getColor(R.color.icon1);
                break;
            case 2:
                endColor = getResources().getColor(R.color.icon2);
                break;
            case 3:
                endColor = getResources().getColor(R.color.icon3);
                break;
            case 4:
                endColor = getResources().getColor(R.color.icon4);
                break;
            case 5:
                endColor = getResources().getColor(R.color.icon5);
                break;
            default:
                endColor = defaultColor;
        }

        return endColor;
    }

    private void recordTap(int i) {
        //play the notes even if the setting is unlocked
        MediaPlayer notePlayer = getPlayer(i);
        if (notePlayer != null) {
            notePlayer.start();
        }


        taps.add(i);

        if (taps.size() > 6) {
            taps.remove(0);
        }

        String sequence = "";
        for (Integer s : taps) {
            sequence += s;
        }

        Log.d(TAG, "sequence: " + sequence);

        if (sequence.equals(LULLABY)) {
            if (!betaUnlocked) {
                Toast.makeText(this, "It's dangerous to go alone!  Take this.", Toast.LENGTH_LONG).show();
                MediaPlayer mp = MediaPlayer.create(this, R.raw.success);
                mp.start();
                betaUnlocked = true;
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit().putBoolean(getString(R.string.beta_settings_unlocked), true).commit();
                View beta = findViewById(R.id.beta);
                expand(beta, false);
            }
        } else if (sequence.equals(TIME)) {
            if (betaUnlocked) {
//                Toast.makeText(this, "It's dangerous to go alone!  Take this.", Toast.LENGTH_LONG).show();
                MediaPlayer mp = MediaPlayer.create(this, R.raw.put_away);
                mp.start();
                betaUnlocked = false;
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                prefs.edit().putBoolean(getString(R.string.beta_settings_unlocked), false).commit();
                View beta = findViewById(R.id.beta);
                collapse(beta, false);
            }
        }
    }

    private void collapseAllSectionsExcept(int sectionID) {
        int count = holder.getChildCount();
        if (count > 0) {
            isAnimating = true;

            for (int i = 0; i < count; i++) {
                View v = holder.getChildAt(i);


                if (v.getId() == sectionID) {
                    if (v instanceof PreferenceSection) {
                        //let the preference section know it has been selected
                        ((PreferenceSection) v).setHighlighted(true);
                    }
                } else if (v.getVisibility() != View.GONE) {
                    //skip over already hidden views
                    if (i == count - 1) {
                        collapse(v, true);
                    } else {
                        collapse(v, false);
                    }
                }
            }
        }
    }

    private MediaPlayer getPlayer(int id) {
        MediaPlayer mp = null;
        switch (id) {
            case 1:
                mp = note1;
                break;
            case 2:
                mp = note2;
                break;
            case 3:
                mp = note3;
                break;
            case 4:
                mp = note4;
                break;
            case 5:
                mp = note5;
                break;
        }

        return mp;
    }

    private void collapse(final View v, final boolean resetAnimated) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int height = v.getMeasuredHeight();
        ValueAnimator anim = ValueAnimator.ofInt(height, 0);
        anim.setDuration(400);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                v.getLayoutParams().height = value.intValue();
                v.requestLayout();

                float alpha = ((float) height / (float) value);
                v.setAlpha(alpha);
            }
        });

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(View.GONE);
                if (resetAnimated) {
                    isAnimating = false;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


        Random random = new Random();
        long offset = random.nextInt(500);
        anim.setStartDelay(offset);

        anim.start();
    }
}
