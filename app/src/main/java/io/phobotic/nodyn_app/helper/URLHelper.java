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

package io.phobotic.nodyn_app.helper;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by Jonathan Nelson on 2/13/18.
 */

public class URLHelper {

    /**
     * Decode the given string as urlencoded utf8 encoded text.  If the decode fails the original
     * string will be returned
     *
     * @param str
     * @return
     */
    public static String decode(String str) {
        if (str == null) return null;

        String decoded = str;
        try {
            decoded = URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return decoded;
    }
}
