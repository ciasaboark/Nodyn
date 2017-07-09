package io.phobotic.nodyn.sync;

import android.os.AsyncTask;
import android.util.Log;

import io.phobotic.nodyn.database.model.FullDataModel;

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
