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

package io.phobotic.nodyn_app.database.statistics.model;

/**
 * Created by Jonathan Nelson on 12/6/17.
 */

public class DayActivity {
    private long timestamp;
    private int checkoutCount;
    private int checkinCount;

    public DayActivity(long timestamp, int checkoutCount, int checkinCount) {
        this.timestamp = timestamp;
        this.checkoutCount = checkoutCount;
        this.checkinCount = checkinCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public DayActivity setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public int getCheckoutCount() {
        return checkoutCount;
    }

    public DayActivity setCheckoutCount(int checkoutCount) {
        this.checkoutCount = checkoutCount;
        return this;
    }

    public int getCheckinCount() {
        return checkinCount;
    }

    public DayActivity setCheckinCount(int checkinCount) {
        this.checkinCount = checkinCount;
        return this;
    }
}
