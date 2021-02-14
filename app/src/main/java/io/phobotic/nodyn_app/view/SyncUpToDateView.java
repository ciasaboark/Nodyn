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

package io.phobotic.nodyn_app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.sync.SyncManager;
import io.phobotic.nodyn_app.sync.adapter.SyncAdapter;

/**
 * Created by Jonathan Nelson on 10/26/17.
 */

public class SyncUpToDateView extends RelativeLayout {
    private final View rootView;

    public SyncUpToDateView(Context context, AttributeSet attrs) {
        super(context, attrs);

        rootView = inflate(context, R.layout.view_sync_up_to_date, this);
        init();
    }

    private void init() {
        SyncAdapter adapter = SyncManager.getPrefferedSyncAdapter(getContext());
        String backendName = adapter.getAdapterName();
        TextView message = rootView.findViewById(R.id.message);
        String msg = String.format(getResources().getString(R.string.sync_all_records_pushed_to_backend), backendName);
        message.setText(msg);
    }
}
