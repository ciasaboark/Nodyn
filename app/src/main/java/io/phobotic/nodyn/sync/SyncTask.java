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

import android.os.AsyncTask;
import android.util.Log;

import io.phobotic.nodyn.database.model.FullDataModel;
import io.phobotic.nodyn.sync.adapter.snipeit3.SnipeItSyncAdapter;
import io.phobotic.nodyn.sync.adapter.SyncAdapter;

/**
 * Created by Jonathan Nelson on 7/7/17.
 */

public class SyncTask extends AsyncTask<SyncAdapter, Void, Void> {
    private static final String TAG = SyncTask.class.getSimpleName();

    @Override
    protected Void doInBackground(SyncAdapter... params) {
        SnipeItSyncAdapter adapter = new SnipeItSyncAdapter();
        try {
            FullDataModel model = adapter.fetchFullModel();
        } catch (SyncException e) {
            e.printStackTrace();
            Log.e(TAG, "Caught SyncException: " + e.getMessage());
            //todo
        }

        return null;
    }
}
