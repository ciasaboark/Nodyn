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

package io.phobotic.nodyn_app.helper;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.reporting.CustomEvents;
import io.phobotic.nodyn_app.sync.adapter.SyncAdapter;
import io.phobotic.nodyn_app.sync.adapter.SyncException;

/**
 * Created by Jonathan Nelson on 2020-02-20.
 */
public class ExecutorHelper {
    private static final String TAG = ExecutorHelper.class.getSimpleName();

    public static void fetchAssetInformation(@NotNull final Context context,
                                                       @NotNull final SyncAdapter syncAdapter,
                                                       @NotNull final Asset asset,
                                                       final long timeoutMs,
                                                       @Nullable final ExecutorListener listener) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();


        final Future<Asset> future = executorService.submit(new Callable<Asset>() {
            @Override
            public Asset call() throws Exception {
                return syncAdapter.getAsset(context, asset);
            }
        });

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Bundle b = new Bundle();
                try {
                    Asset result = future.get(timeoutMs, TimeUnit.MILLISECONDS);
                    if (listener != null) {
                        if (result == null) {
                            String message = String.format("Unknown error fetching " +
                                    "asset information for id %d. Returned asset was null.", asset.getId());
                            b.putString(CustomEvents.ASSET_AVAILABLITY_CHECK_RESULTS, message);
                            FirebaseAnalytics.getInstance(context).logEvent(CustomEvents.ASSET_AVAILABLITY_CHECK, b);
                            listener.onException(result, new SyncException(message));
                        } else {
                            listener.onResult(result);
                        }
                    }
                } catch (TimeoutException e) {
                    b.putString(CustomEvents.ASSET_AVAILABLITY_CHECK_RESULTS, String.format("Timeout after %d ms", timeoutMs));
                    FirebaseAnalytics.getInstance(context).logEvent(CustomEvents.ASSET_AVAILABLITY_CHECK, b);
                    if (listener != null) {
                        listener.onTimeoutException(asset, e);
                    }
                } catch (Exception e) {
                    String message = String.format("Caught exception while waiting for sync adapter to " +
                            "return asset information for id %d: %s", asset.getId(), e.getMessage());
                    b.putString(CustomEvents.ASSET_AVAILABLITY_CHECK_RESULTS, message);
                    FirebaseAnalytics.getInstance(context).logEvent(CustomEvents.ASSET_AVAILABLITY_CHECK, b);
                    FirebaseCrashlytics.getInstance().recordException(e);
                    Log.e(TAG, message);
                    if (listener != null) {
                        listener.onException(asset, new SyncException(message));
                    }
                } finally {
                    future.cancel(true);
                }
            }
        });
    }

    public interface ExecutorListener {
        void onTimeoutException(@NotNull Asset asset, @NotNull TimeoutException e);
        void onException(@NotNull Asset asset, @NotNull Exception e);
        void onResult(@NotNull Asset asset);
    }
}
