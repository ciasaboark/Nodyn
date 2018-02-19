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
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import io.phobotic.nodyn_app.R;

/**
 * Created by Jonathan Nelson on 1/23/18.
 */

public class EmailRecipientView extends RelativeLayout {
    private static final String TAG = EmailRecipientView.class.getSimpleName();
    private final View rootView;
    private TextView recipientTextView;
    private String recipient;

    public EmailRecipientView(Context context) {
        this(context, null);
    }

    public EmailRecipientView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmailRecipientView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public EmailRecipientView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        rootView = inflate(context, R.layout.view_email_recipient, this);
        init();
    }

    private void init() {
        recipientTextView = (TextView) rootView.findViewById(R.id.recipient);
    }

    public EmailRecipientView setRecipient(String recipient) {
        this.recipient = recipient;
        recipientTextView.setText(recipient);
        return this;
    }
}
