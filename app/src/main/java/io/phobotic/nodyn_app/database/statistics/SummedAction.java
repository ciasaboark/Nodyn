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
 * Created by Jonathan Nelson on 2019-05-11.
 */

public class SummedAction {
    private long timestamp;
    private int totalCheckouts;
    private int totalCheckins;
    private int totalAudits;

    public SummedAction(long timestamp, int totalCheckouts, int totalCheckins, int totalAudits) {
        this.timestamp = timestamp;
        this.totalCheckouts = totalCheckouts;
        this.totalCheckins = totalCheckins;
        this.totalAudits = totalAudits;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getTotalCheckouts() {
        return totalCheckouts;
    }

    public void setTotalCheckouts(int totalCheckouts) {
        this.totalCheckouts = totalCheckouts;
    }

    public int getTotalCheckins() {
        return totalCheckins;
    }

    public void setTotalCheckins(int totalCheckins) {
        this.totalCheckins = totalCheckins;
    }

    public int getTotalAudits() {
        return totalAudits;
    }

    public void setTotalAudits(int totalAudits) {
        this.totalAudits = totalAudits;
    }
}
