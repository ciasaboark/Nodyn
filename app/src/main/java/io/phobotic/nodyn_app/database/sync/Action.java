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

package io.phobotic.nodyn_app.database.sync;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.DateFormat;
import java.util.Date;

import androidx.preference.PreferenceManager;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 7/10/17.
 */

@Entity(tableName = "actions_table")
public class Action {
    @PrimaryKey
    private Integer id;

    private Direction direction;
    private int assetID;
    private int userID;
    private String authorization;
    private long timestamp;
    private long expectedCheckin = -1;
    private boolean synced = false;
    private boolean verified = false;
    private String notes;

    public Action(int id, Asset asset, User user, long timestamp, long expectedCheckin,
                  Direction direction, boolean synced) {
        this(id, asset.getId(), user.getId(), timestamp, expectedCheckin,
                direction, synced);
    }

    public Action(Integer id, int assetID, int userID, long timestamp, long expectedCheckin,
                  Direction direction, boolean synced) {
        this.id = id;
        this.assetID = assetID;
        this.userID = userID;
        this.timestamp = timestamp;
        this.expectedCheckin = expectedCheckin;
        this.direction = direction;
        this.synced = synced;
    }

    public Action(Asset asset, User user, long timestamp, Long expectedCheckin,
                  Direction direction, boolean synced) {
        this(null, asset.getId(), user == null ? -1 : user.getId(), timestamp,
                expectedCheckin == null ? -1 : expectedCheckin, direction, synced);
    }

    public Action(int assetID, int userID, long timestamp, long expectedCheckin,
                  Direction direction, boolean synced) {
        this(null, assetID, userID, timestamp, expectedCheckin, direction, synced);
    }

    public Integer getId() {
        return id;
    }

    public Direction getDirection() {
        return direction;
    }

    public Action setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    public int getAssetID() {
        return assetID;
    }

    public Action setAssetID(int assetID) {
        this.assetID = assetID;
        return this;
    }

    public int getUserID() {
        return userID;
    }

    public Action setUser(int userID) {
        this.userID = userID;
        return this;
    }

    public String getAuthorization() {
        return authorization;
    }

    public Action setAuthorization(String authorization) {
        this.authorization = authorization;
        return this;
    }

    public boolean isVerified() {
        return verified;
    }

    public Action setVerified(boolean verified) {
        this.verified = verified;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Action setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public long getExpectedCheckin() {
        return expectedCheckin;
    }

    public Action setExpectedCheckin(long expectedCheckin) {
        this.expectedCheckin = expectedCheckin;
        return this;
    }

    public boolean isSynced() {
        return synced;
    }

    public Action setSynced(boolean synced) {
        this.synced = synced;
        return this;
    }

    public String getNotes() {
        return notes;
    }

    public Action setNotes(String notes) {
        this.notes = notes;
        return this;
    }


    @Override
    public String toString() {
        DateFormat df = DateFormat.getDateTimeInstance();
        Date d = new Date(timestamp);

        return df.format(d) + " <" + userID + "> " + direction.toString() + " [" + assetID + "]";
    }

    /**
     * Returns a String sutable to be used in the notes field
     * @param a
     * @return
     */
    public String generateNotes(Context context) {
        return Action.generateNotesFromAction(context, this);
    }

    public static String generateNotesFromAction(Context context, Action action) {
        return generateNotesFromData(context, action.getDirection(),
                action.getAuthorization(), action.isVerified());
    }

    public static String generateNotesFromData(Context context,
                   Direction direction, String authorizingUser, boolean isVerified) {
        StringBuilder notes = new StringBuilder("Nodyn ");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Database db = Database.getInstance(context);

        String deviceName = prefs.getString(context.getString(R.string.pref_key_general_id),
                context.getString(R.string.pref_default_general_id));
        if (deviceName != null && deviceName.length() > 0) {
            notes.append("<" + deviceName + "> ");
        }

        switch (direction) {
            case CHECKIN:
                notes.append("checkin.");

                //who (if anyone) authorized the asset check in?
                if (authorizingUser != null) {
                    notes.append(" Authorization: '" + authorizingUser + "'.");
                }

                //did the authorizing user verify the asset was undamaged during checkin?
                notes.append(" Asset verified undamaged: " + isVerified + ".");
                break;
            case CHECKOUT:
                notes.append("checkout.");

                //who (if anyone) authorized the asset check out?
                if (authorizingUser != null) {
                    notes.append(" Authorization: '" + authorizingUser + "'.");
                }

                //did the associate checking out the asset have to agree to the eula?
                notes.append(" EULA shown: " + isVerified + ".");
                break;
        }

        return notes.toString();
    }

    public enum Direction {
        CHECKIN,
        CHECKOUT,
        CREATE,
        UPDATE,
        UNKNOWN
    }

    public class Columns {
        public static final String ID = "id";
        public static final String DIRECTION = "direction";
        public static final String ASSET_ID = "asset_id";
        public static final String USER_ID = "user_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String EXPECTED_CHECKIN = "expected_checkin";
        public static final String SYNCED = "synced";
        public static final String AUTHORIZATION = "authorization";
        public static final String VERIFIED = "verified";
    }
}
