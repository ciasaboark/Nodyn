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

package io.phobotic.nodyn_app.database.statistics;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.room.TypeConverter;

/**
 * Created by Jonathan Nelson on 2019-05-11.
 */
public class ShortActionConverter {
    @TypeConverter
    public String fromShortActionList(List<UsageRecord> actions) {
        if (actions == null) {
            return null;
        }

        Gson gson = new Gson();
        TypeToken<List<UsageRecord>> tt = new TypeToken<List<UsageRecord>>() {
        };
        Type type = tt.getType();
        String json = gson.toJson(actions, type);
        return json;
    }

    @TypeConverter
    public List<UsageRecord> toShortActionList(String json) {
        if (json == null) {
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        TypeToken<List<UsageRecord>> tt = new TypeToken<List<UsageRecord>>() {
        };
        Type type = tt.getType();
        List<UsageRecord> list = gson.fromJson(json, type);
        return list;
    }

    @TypeConverter
    public List<SummedAction> toSummedActionList(String json) {
        if (json == null) {
            return new ArrayList<>();
        }

        Gson gson = new Gson();
        TypeToken<List<SummedAction>> tt = new TypeToken<List<SummedAction>>() {
        };
        Type type = tt.getType();
        List<SummedAction> list = gson.fromJson(json, type);
        return list;
    }


    @TypeConverter
    public String fromSummedActionList(List<SummedAction> actions) {
        if (actions == null) {
            return null;
        }

        Gson gson = new Gson();
        TypeToken<List<SummedAction>> tt = new TypeToken<List<SummedAction>>() {
        };
        Type type = tt.getType();
        String json = gson.toJson(actions, type);
        return json;
    }
}
