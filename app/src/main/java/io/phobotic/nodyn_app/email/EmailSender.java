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

package io.phobotic.nodyn_app.email;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import io.phobotic.nodyn_app.R;

/**
 * Created by Jonathan Nelson on 8/10/16.
 */

public class EmailSender {
    public static final String EMAIL_SEND_SUCCESS = "email_sent";
    public static final String EMAIL_SEND_FAILED = "email_failed";
    public static final String EMAIL_SEND_START = "email_start";

    private static final String TAG = EmailSender.class.getSimpleName();
    private Context context;
    private EmailStatusListener failedListener;
    private EmailStatusListener successListener;
    private List<Attachment> attachments;
    private List<EmailRecipient> recipientList = new ArrayList<>();
    private Object successTag;
    private Object failedTag;

    private String subject = "";
    private String body = "";

    public EmailSender(Context context) {
        this.context = context;
    }

    public EmailSender setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public EmailSender setBody(String body) {
        this.body = body;
        return this;
    }

    public EmailSender withAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public EmailSender setFailedListener(EmailStatusListener failedListener, @Nullable Object tag) {
        this.failedListener = failedListener;
        this.failedTag = tag;
        return this;
    }

    public EmailSender setSuccessListener(EmailStatusListener successListener, @Nullable Object tag) {
        this.successListener = successListener;
        this.successTag = tag;
        return this;
    }

    public EmailSender setRecipientList(List<EmailRecipient> recipientList) {
        this.recipientList = recipientList;
        return this;
    }

    private void notifyStart() {
        Intent i = new Intent(EMAIL_SEND_START);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }

    private void notifySuccess() {
        //send broadcast notification
        Intent i = new Intent(EMAIL_SEND_SUCCESS);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
        if (successListener != null) {
            successListener.onEmailSendResult(null, successTag);
        }
    }

    public EmailSender send() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            String server = prefs.getString(context.getString(R.string.pref_key_email_server), "");
            String portString = prefs.getString(context.getString(R.string.pref_key_email_port), "");
            int port = -1;
            try {
                port = Integer.parseInt(portString);
            } catch (Exception e) {
                //nothing to do here
            }
            String username = prefs.getString(context.getString(R.string.pref_key_email_username), "");
            String password = prefs.getString(context.getString(R.string.pref_key_email_password), "");
            String recipients = "";
            String prefix = "";
            for (EmailRecipient recipient : recipientList) {
                recipients += prefix + recipient.getEmail();
                prefix = ",";
            }

            Email email = new Email()
                    .setServer(server)
                    .setPort(port)
                    .setUsername(username)
                    .setPassword(password)
                    .setRecipients(recipients)
                    .setSubject(getSubjectWithDeviceName(subject))
                    .setBody(body);

            if (attachments == null) {
                Log.d(TAG, "no attachments given");
            } else {
                for (Attachment attachment : attachments) {
                    email.addAttachment(attachment);
                }
            }

            EmailSendTask emailSendTask = new EmailSendTask();
            emailSendTask.execute(email);
        } catch (Exception e) {
            String message = "Caught exception while trying to send email: " + e.getMessage();
            Log.e(TAG, message);
            e.printStackTrace();
            notifyFail(message);
        }


        return this;
    }

    private String getSubjectWithDeviceName(String subject) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        //append the device name to the subject (if one is set)
        String newSubject = subject;
        String deviceName = prefs.getString(context.getString(R.string.pref_key_general_id),
                context.getString(R.string.pref_default_general_id));
        if (deviceName != null && deviceName.length() > 0) {
            newSubject += " <" + deviceName + ">";
        }

        return newSubject;
    }

    private void notifyFail(String message) {
        //send broadcast notification
        Intent i = new Intent(EMAIL_SEND_FAILED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
        if (failedListener != null) {
            failedListener.onEmailSendResult(message, failedTag);
        }
    }

    public EmailSender sendTestEmail() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            String server = prefs.getString("email_server", "");
            String portString = prefs.getString("email_port", "");
            int port = -1;
            try {
                port = Integer.parseInt(portString);
            } catch (Exception e) {
                //nothing to do here
            }
            String username = prefs.getString("email_username", "");
            String password = prefs.getString("email_password", "");

            String recipients = "";
            String prefix = "";
            for (EmailRecipient recipient : recipientList) {
                recipients += prefix + recipient.getEmail();
                prefix = ",";
            }

            Email email = new Email()
                    .setServer(server)
                    .setPort(port)
                    .setUsername(username)
                    .setPassword(password)
                    .setRecipients(recipients)
                    .setSubject(getSubjectWithDeviceName("Test message"))
                    .setBody("This is a test message from the Nodyn asset tracker Android app.  You can " +
                            "safely ignore this message");
            EmailSendTask emailSendTask = new EmailSendTask();
            emailSendTask.execute(email);
        } catch (Exception e) {
            String message = "Caught exception while sending test email: " + e.getMessage();
            Log.d(TAG, message);
            e.printStackTrace();
            notifyFail(message);
        }

        return this;
    }

    public interface EmailStatusListener {
        void onEmailSendResult(@Nullable String message, @Nullable Object tag);
    }

    private class EmailSendTask extends AsyncTask<Email, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Email[] emails) {
            notifyStart();
            for (Email email : emails) {
                if (!email.isValid()) {
                    String message = "Unable to send email. One or more of server, port, username, or password " +
                            "has not been set in settings";
                    Log.d(TAG, message);
                    notifyFail(message);
                } else {
                    Properties props = new Properties();
                    props.put("mail.smtp.user", email.getUsername());
                    props.put("mail.smtp.host", email.getServer());
                    props.put("mail.smtp.port", email.getPort());
//                    props.put("mail.debug", "true");
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.EnableSSL.enable", "true");

                    props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    props.setProperty("mail.smtp.socketFactory.fallback", "false");
                    props.setProperty("mail.smtp.socketFactory.port", "465");

                    Session mailSession = Session.getDefaultInstance(props, null);

                    MimeMessage emailMessage = new MimeMessage(mailSession);
                    try {
                        InternetAddress[] recipientAddresses = InternetAddress.parse(email.getRecipients());
                        emailMessage.setRecipients(Message.RecipientType.TO, recipientAddresses);
                        String fromAddress = "\"Nodyn Asset Tracker\" <" + email.getUsername() + ">";
                        emailMessage.setFrom(new InternetAddress(fromAddress));
                        emailMessage.setSubject(email.getSubject());
                        Multipart multipart = new MimeMultipart();
                        MimeBodyPart messageBodyPart = new MimeBodyPart();
                        messageBodyPart.setContent(email.getBody(), "text/html");
                        multipart.addBodyPart(messageBodyPart);

                        for (Attachment attachment : email.getAttachments()) {
                            MimeBodyPart attachmentPart = new MimeBodyPart();
                            try {
                                attachmentPart.attachFile(attachment.getFile());
                                attachmentPart.setFileName(attachment.getName());

                                if (attachment.getContentID() != null) {
                                    attachmentPart.setHeader("Content-ID", String.format("<%s>", attachment.getContentID()));
                                }

                                if (attachment.isInline()) {
//                                    attachmentPart.setHeader("Content-Type", "multipart/related");
                                    attachmentPart.setDisposition(MimeMessage.INLINE);
                                }

                                if (attachment.getContentType() != null) {
                                    attachmentPart.setHeader("Content-Type", attachment.getContentType());
                                }

                                multipart.addBodyPart(attachmentPart);
                            } catch (IOException e) {
                                Log.e(TAG, "unable to attach file '" + attachment.getFile() + "' to email");
                            }
                        }

                        Address[] recipients = emailMessage.getAllRecipients();
                        if (recipients == null) {
                            throw new AddressException("No recipients defined");
                        }

                        emailMessage.setContent(multipart);

                        Transport transport = mailSession.getTransport("smtps");
                        transport.connect(email.getServer(), email.getUsername(), email.getPassword());
                        transport.sendMessage(emailMessage, recipients);
                        transport.close();

                        Log.d(TAG, "email sent");
                        notifySuccess();
                    } catch (Exception e) {
                        e.printStackTrace();
                        String message = e.getMessage();
                        if (message != null) {
                            Log.e(TAG, message);
                        }
                        Crashlytics.logException(e);
                        notifyFail(message);
                    }
                }
            }


            return true;
        }
    }

}



