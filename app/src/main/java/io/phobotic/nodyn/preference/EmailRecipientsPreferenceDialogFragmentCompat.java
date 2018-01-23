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

package io.phobotic.nodyn.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.apache.commons.validator.routines.EmailValidator;

import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn.R;
import io.phobotic.nodyn.view.EmailRecipientView;


/**
 * Created by Jonathan Nelson on 1/23/18.
 */

public class EmailRecipientsPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    private EditText input;
    //    private String recipientsString;
    private List<String> recipientsList;
    private View rootView;
    private Button addButton;
    private RecyclerView list;
    private EmailValidator validator;

    public static EmailRecipientsPreferenceDialogFragmentCompat newInstance(
            String key) {
        EmailRecipientsPreferenceDialogFragmentCompat fragment = new EmailRecipientsPreferenceDialogFragmentCompat();
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        rootView = view;

        init();
    }

    private void init() {
        DialogPreference preference = getPreference();
        if (preference instanceof EmailRecipentsPreference) {
            String recipientsString = ((EmailRecipentsPreference) preference).getRecipientsString();
            buildRecipientsList(recipientsString);
        }

        validator = EmailValidator.getInstance(false);
        findViews();

        initInput();
        initButton();
        initList();
    }

    private void buildRecipientsList(String recipientsString) {
        recipientsList = new ArrayList<>();
        if (recipientsString != null) {
            String[] parts = recipientsString.split(",");
            for (String part : parts) {
                recipientsList.add(part);
            }
        }
    }

    private void findViews() {
        input = (EditText) rootView.findViewById(R.id.input);
        addButton = (Button) rootView.findViewById(R.id.add_button);
        list = (RecyclerView) rootView.findViewById(R.id.list);
    }

    private void initInput() {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (validator.isValid(s.toString())) {
                    addButton.setEnabled(true);
                } else {
                    addButton.setEnabled(false);
                }
            }
        });
    }

    private void initButton() {
        addButton.setEnabled(false);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recipient = input.getText().toString();
                if (recipientsList.contains(recipient)) {
                    AlertDialog d = new AlertDialog.Builder(getContext())
                            .setTitle("Duplicate address")
                            .setMessage("The email address \"" + recipient + "\" has already been added")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    input.setText(null);
                                }
                            })
                            .create();
                    d.show();
                    ;
                } else {
                    addRecipient(recipient);
                    input.setText(null);
                }
            }
        });
    }

    private void initList() {
        list.setLayoutManager(new LinearLayoutManager(getContext()));
        updateList();
    }

    private void addRecipient(String recipient) {
        if (recipient != null) {
            recipientsList.add(recipient);
            updateList();
        }
    }

    private void updateList() {
        list.setAdapter(new EmailRecipientRecyclerViewAdapter(getContext(), recipientsList));
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {

            DialogPreference preference = getPreference();
            if (preference instanceof EmailRecipentsPreference) {
                String recipientsString = getRecipientsString();
                if (preference.callChangeListener(recipientsString)) {
                    // Save the value
                    ((EmailRecipentsPreference) preference).setRecipentsString(recipientsString);
                }
            }
        }
    }

    private String getRecipientsString() {
        String recipientsString = null;
        String prefix = "";
        if (recipientsList.size() > 0) {
            recipientsString = "";
            for (String recipient : recipientsList) {
                recipientsString += prefix + recipient;
                prefix = ",";
            }
        }

        return recipientsString;
    }

    private class EmailRecipientRecyclerViewAdapter extends RecyclerView.Adapter<EmailViewHolder> {
        private final List<String> recipients;
        private final Context context;

        public EmailRecipientRecyclerViewAdapter(Context context, List<String> recipients) {
            this.recipients = recipients;
            this.context = context;
        }

        @Override
        public EmailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            EmailRecipientView view = new EmailRecipientView(getContext());
            view.setRecipient(null);
            return new EmailViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final EmailViewHolder holder, int position) {
            String recipient = recipients.get(position);
            holder.recipient = recipient;
            holder.view.setRecipient(recipient);

        }

        @Override
        public int getItemCount() {
            return recipients.size();
        }
    }

    public class EmailViewHolder extends RecyclerView.ViewHolder {
        public final EmailRecipientView view;
        public String recipient;

        public EmailViewHolder(EmailRecipientView view) {
            super(view);
            this.view = view;
        }
    }
}
