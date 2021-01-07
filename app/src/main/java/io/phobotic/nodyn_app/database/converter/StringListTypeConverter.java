/*
 * Copyright (c) 2020 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.database.converter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import androidx.room.TypeConverter;

/**
 * Created by Jonathan Nelson on 2020-02-18.
 */
public class StringListTypeConverter {
    private static final Gson gson = new Gson();

    @TypeConverter
    public static String fromList(List<String> list) {
        if (list == null) {
            return null;
        }

        String json = gson.toJson(list);
        return json;
    }

    @TypeConverter
    public static List<String> fromJson(String json) {
        if (json == null) {
            return null;
        }

        List<String> list = gson.fromJson(json, new TypeToken<List<String>>(){}.getType());
        return list;
    }
}