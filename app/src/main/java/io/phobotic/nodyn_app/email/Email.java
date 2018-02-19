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

package io.phobotic.nodyn_app.email;

import android.util.Patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;


/**
 * Created by Jonathan Nelson on 8/12/16.
 */
class Email {
    private String username;
    private String password;
    private String server;
    private int port;
    private String recipients;
    private String subject;
    private String body;
    private List<Attachment> attachments = new ArrayList<>();

    public Email addAttachment(Attachment attachment) {
        this.attachments.add(attachment);
        return this;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public Email setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public Email setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public Email setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getServer() {
        return server;
    }

    public Email setServer(String server) {
        this.server = server;
        return this;
    }

    public int getPort() {
        return port;
    }

    public Email setPort(int port) {
        this.port = port;
        return this;
    }

    public String getRecipients() {
        return recipients;
    }

    public Email setRecipients(String recipients) {
        this.recipients = recipients;
        return this;
    }

    public String getSubject() {
        return subject;
    }

    public Email setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getBody() {
        return body;
    }

    public Email setBody(String body) {
        this.body = body;
        return this;
    }

    public boolean isValid() {
        Matcher pattern = Patterns.EMAIL_ADDRESS.matcher(recipients);

        if (server == null ||
                port == -1 ||
                username == null ||
                password == null ||
                recipients == null) {
            return false;
        } else {
            return true;
        }
    }
}
