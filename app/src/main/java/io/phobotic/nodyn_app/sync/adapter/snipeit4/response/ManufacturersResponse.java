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

package io.phobotic.nodyn_app.sync.adapter.snipeit4.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.phobotic.nodyn_app.sync.adapter.snipeit4.shadow.Snipeit4Manufacturer;

/**
 * Created by Jonathan Nelson on 9/14/17.
 */

public class ManufacturersResponse {
    private int total;
    @SerializedName("rows")
    private List<Snipeit4Manufacturer> manufacturers;

    public int getTotal() {
        return total;
    }

    public ManufacturersResponse setTotal(int total) {
        this.total = total;
        return this;
    }

    public List<Snipeit4Manufacturer> getManufacturers() {
        return manufacturers;
    }

    public ManufacturersResponse setManufacturers(List<Snipeit4Manufacturer> manufacturers) {
        this.manufacturers = manufacturers;
        return this;
    }
}
