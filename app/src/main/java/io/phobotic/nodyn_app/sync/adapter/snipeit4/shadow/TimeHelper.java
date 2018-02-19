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

package io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Jonathan Nelson on 10/26/17.
 */

public class TimeHelper {
    private static final String TAG = TimeHelper.class.getSimpleName();

    public static long toTimestamp(String dateString, boolean isUTC) {
        long timestamp = -1;
        DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");

        if (isUTC) {
            df1.setTimeZone(TimeZone.getTimeZone("UTC"));
            df1.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        try {
            Date d1 = df1.parse(dateString);
            timestamp = d1.getTime();
        } catch (Exception e1) {
            //if the first format did not match try the second
            try {
                Date d2 = df2.parse(dateString);
                timestamp = d2.getTime();
            } catch (Exception e2) {
                e2.printStackTrace();
                Log.e(TAG, "Unable to convert date string '" + dateString +
                        "' into a timestamp: " + e2.getMessage());
            }
        }

        return timestamp;
    }
}
