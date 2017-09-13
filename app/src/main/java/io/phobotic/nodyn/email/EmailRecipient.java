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

package io.phobotic.nodyn.email;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by Jonathan Nelson on 11/12/16.
 */

public class EmailRecipient implements Comparable<EmailRecipient>, Serializable {
    private String email;
    private boolean saved;

    public EmailRecipient(@NotNull String email) {
        if (email == null) throw new IllegalArgumentException("Email address can not be null");
        this.email = email;
        this.saved = false;
    }

    public boolean isSaved() {
        return saved;
    }

    public EmailRecipient setSaved(boolean saved) {
        this.saved = saved;
        return this;
    }

    @Override
    public int compareTo(EmailRecipient o) {
        return email.compareTo(o.getEmail());
    }

    public String getEmail() {
        return email;
    }

    public EmailRecipient setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EmailRecipient)) return false;

        return (this.getEmail().equals(((EmailRecipient) obj).getEmail()));
    }
}
