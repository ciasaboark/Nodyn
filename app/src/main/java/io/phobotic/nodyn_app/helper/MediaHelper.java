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

package io.phobotic.nodyn_app.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;

/**
 * Created by Jonathan Nelson on 10/27/17.
 */

public class MediaHelper {
    public static void playSoundEffect(Context context, int soundResID) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isSoundEffectsEnabled = prefs.getBoolean(context.getString(
                R.string.pref_key_general_sounds_enabled), Boolean.parseBoolean(
                context.getString(R.string.pref_default_general_sounds_enabled)));

        if (isSoundEffectsEnabled) {
            try {
                final MediaPlayer mp = MediaPlayer.create(context, soundResID);
                mp.start();
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                e.printStackTrace();
            }
        }
    }
}
