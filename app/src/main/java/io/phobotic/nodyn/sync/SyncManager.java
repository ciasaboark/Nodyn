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

package io.phobotic.nodyn.sync;

import android.content.Context;
import android.support.v7.preference.PreferenceManager;

import io.phobotic.nodyn.sync.adapter.SyncAdapter;
import io.phobotic.nodyn.sync.adapter.dummy.DummyAdapter;
import io.phobotic.nodyn.sync.adapter.snipeit3.SnipeIt3SyncAdapter;
import io.phobotic.nodyn.sync.adapter.snipeit4.SnipeIt4SyncAdapter;

/**
 * Created by Jonathan Nelson on 7/19/17.
 */

public class SyncManager {
    private static SyncAdapter adapter;

    public static SyncAdapter getPrefferedSyncAdapter(Context context) {
        String selectedBackend = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("sync_backend", "foobar");

        SyncAdapter adapter;

        switch (selectedBackend) {
            case "snipe_it_3":
                adapter = new SnipeIt3SyncAdapter();
                break;
            case "snipe_it_4":
                adapter = new SnipeIt4SyncAdapter();
                break;
            default:
                adapter = new DummyAdapter();
        }

        return adapter;
    }
}
