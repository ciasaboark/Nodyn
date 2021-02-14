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

/**
 * A record for asset usage noting both the starting and ending times.  The start or end time (but
 * not both) may be null.  If the checkin timestamp is null the asset is still checked out during
 * this duration.  If the checkout timestamp is null then this record was created without
 * having records of sufficient length
 * Created by Jonathan Nelson on 2019-05-11.
 */

public class UsageRecord {
    private long checkoutTimestamp = 0;
    private long checkinTimestamp = 0;
    private int assignedUser = -1;

    public UsageRecord() {
    }


    public long getCheckoutTimestamp() {
        return checkoutTimestamp;
    }

    public void setCheckoutTimestamp(long checkoutTimestamp) {
        this.checkoutTimestamp = checkoutTimestamp;
    }

    public long getCheckinTimestamp() {
        return checkinTimestamp;
    }

    public void setCheckinTimestamp(long checkinTimestamp) {
        this.checkinTimestamp = checkinTimestamp;
    }

    public int getAssignedUser() {
        return assignedUser;
    }

    public void setAssignedUser(int assignedUser) {
        this.assignedUser = assignedUser;
    }
}
