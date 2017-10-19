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

package io.phobotic.nodyn.sync.adapter.snipeit4.shadow;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jonathan Nelson on 9/13/17.
 */

public class DateSnippet {
    @SerializedName(value = "datetime", alternate = {"date"})
    private String datetimme;
    private String formatted;

    public String getDatetimme() {
        return datetimme;
    }

    public DateSnippet setDatetimme(String datetimme) {
        this.datetimme = datetimme;
        return this;
    }

    public String getFormatted() {
        return formatted;
    }

    public DateSnippet setFormatted(String formatted) {
        this.formatted = formatted;
        return this;
    }
}
