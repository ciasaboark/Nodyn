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

package io.phobotic.nodyn;

/**
 * Created by Jonathan Nelson on 10/12/17.
 */

public class Versioning {
    public static String getVersionCode() {
        String versionCode = BuildConfig.APP_VERSION_MAJOR + "."
                + BuildConfig.APP_VERSION_MINOR + "."
                + BuildConfig.APP_VERSION_RELEASE;
        if (BuildConfig.DEBUG) {
            versionCode += " pre-release debug build (" + BuildConfig.BUILD_DATE + ")";
        }

        return versionCode;
    }

    public static String getReleaseName() {
        return BuildConfig.VERSION_NAME;
    }

    public static int getReleaseNumber() {
        return BuildConfig.VERSION_CODE;
    }


}