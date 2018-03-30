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

import com.google.gson.annotations.SerializedName;

import org.jsoup.Jsoup;

import io.phobotic.nodyn_app.database.model.Action;

import static io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow.TimeHelper.toTimestamp;

/**
 * Created by Jonathan Nelson on 10/26/17.
 */

public class Snipeit4Activity {
    private int id;

    @SerializedName("action_type")
    private String actionType = null;

    @SerializedName("created_at")
    private DateSnippet createdAt;

    private Item item;
    private Item location;
    private String note;
    private Item target;
    private DateSnippet updatedAt;


    public Action toAction(boolean isTimeUTC) {
        Action.Direction direction;
        try {
            String type = actionType.toUpperCase();
            //specific workaround for check ins
            if ("CHECKIN FROM".equals(type)) {
                type = "CHECKIN";
            }

            direction = Action.Direction.valueOf(type);
        } catch (Exception e) {
            direction = Action.Direction.UNKNOWN;
        }

        int itemID = item == null ? -1 : item.id;
        int userID = target == null ? -1 : target.id;
        long timestamp = toTimestamp(createdAt.getDatetimme(), isTimeUTC);
        long expectedCheckin = -1;


        Action a = new Action(itemID, userID, timestamp, -1, direction, true);
        if (note != null) {
            a.setNotes(Jsoup.parse(note).text());
        }
        return a;
    }


    private class Item {
        public int id;
        public String name;
        public String type;
    }

    private class Admin {
        public String firstName;
        public String lastName;
        public String name;
        public int id;
    }


}



