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

package io.phobotic.nodyn_app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.MaintenanceRecord;
import io.phobotic.nodyn_app.database.model.User;

import static io.phobotic.nodyn_app.helper.TextHelper.setTextOrHide;

/**
 * Created by Jonathan Nelson on 10/29/17.
 */

public class MaintenanceRecordView extends RelativeLayout {
    private static final String TAG = MaintenanceRecordView.class.getSimpleName();
    private final Context context;
    private MaintenanceRecord record;
    private View rootView;
    private TextView type;
    private TextView createdAt;
    private TextView startDate;
    private TextView completeDate;
    private TextView notes;
    private TextView title;
    private TextView supplier;
    private TextView user;
    private View typeBox;
    private View startDateBox;
    private View completeDateBox;
    private View notesBox;
    private View supplierBox;
    private View userBox;


    public MaintenanceRecordView(Context context, AttributeSet attrs) {
        this(context, attrs, null);
    }

    public MaintenanceRecordView(Context context, AttributeSet attrs, MaintenanceRecord record) {
        super(context, attrs);
        this.context = context;
        this.record = record;

        init();
    }

    private void init() {
        rootView = inflate(context, R.layout.view_maintenance_record, this);

        title = (TextView) rootView.findViewById(R.id.title);
        createdAt = (TextView) rootView.findViewById(R.id.create_date);
        typeBox = rootView.findViewById(R.id.type_box);
        type = (TextView) rootView.findViewById(R.id.type);

        startDateBox = rootView.findViewById(R.id.start_date_box);
        startDate = (TextView) rootView.findViewById(R.id.start_date);

        completeDateBox = rootView.findViewById(R.id.complete_date_box);
        completeDate = (TextView) rootView.findViewById(R.id.complete_date);

        notesBox = rootView.findViewById(R.id.notes_box);
        notes = (TextView) rootView.findViewById(R.id.notes);

        supplierBox = rootView.findViewById(R.id.supplier_box);
        supplier = (TextView) rootView.findViewById(R.id.supplier);

        userBox = rootView.findViewById(R.id.user_box);
        user = (TextView) rootView.findViewById(R.id.user);

        setFields();
    }

    private void setFields() {
        if (!isInEditMode()) {
            title.setText(record.getTitle());

            setTextOrHide(typeBox, type, record.getType());
            setTextOrHide(notesBox, notes, record.getNotes());
            setTextOrHide(supplierBox, supplier, record.getSupplier());

            DateFormat df = DateFormat.getDateTimeInstance();
            String startDateText = null;
            if (record.getStartTime() != -1) {
                Date d = new Date(record.getStartTime());
                startDateText = df.format(d);
            }
            setTextOrHide(startDateBox, startDate, startDateText);

            String completeDateText = null;
            if (record.getCompleteTime() != -1) {
                Date d = new Date(record.getCompleteTime());
                completeDateText = df.format(d);
            }
            setTextOrHide(completeDateBox, completeDate, completeDateText);

            String createdDateText = null;
            if (record.getCreatedAt() != -1) {
                Date d = new Date(record.getCreatedAt());
                createdDateText = df.format(d);
            }
            setTextOrHide(createdAt, createdAt, createdDateText);

            String userText = null;
            if (record.getUserID() != -1) {
                try {
                    Database db = Database.getInstance(context);
                    User u = db.findUserByID(record.getUserID());
                    userText = u.getName();
                } catch (UserNotFoundException e) {
                    Log.d(TAG, "User cound not be found with ID: " + record.getUserID());
                }
            }
            setTextOrHide(userBox, user, userText);
        }
    }

    public void setMaintenanceRecord(MaintenanceRecord record) {
        this.record = record;
        setFields();
    }
}
