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

package io.phobotic.nodyn_app.database.helper;

import android.database.sqlite.SQLiteDatabase;

import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * Created by Jonathan Nelson on 7/9/17.
 */

public abstract class TableHelper<K> {
    protected SQLiteDatabase db;

    public TableHelper(@NotNull SQLiteDatabase db) {
        this.db = db;
    }

    public abstract void replace(@NotNull List<K> list);

    public abstract long insert(@NotNull K item);

    public abstract K findByID(@NotNull int id);

    public abstract K findByName(@NotNull String name);

    public abstract
    @NotNull
    List<K> findAll();
}
