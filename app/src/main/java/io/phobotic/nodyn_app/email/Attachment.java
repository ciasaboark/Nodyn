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

import android.support.annotation.Nullable;

import java.io.File;

/**
 * Created by Jonathan Nelson on 8/12/16.
 */
public class Attachment {
    private File file;
    private String name;
    private String contentID;
    private boolean inline = false;
    private String contentType;

    public Attachment(File file, String name) {
        this(file, name, null);
    }

    public Attachment(File file, @Nullable String name, @Nullable String contentID) {
        this.file = file;
        this.name = name;
        this.contentID = contentID;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public String getContentID() {
        return contentID;
    }

    public boolean isInline() {
        return inline;
    }

    public Attachment setInline(boolean inline) {
        this.inline = inline;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public Attachment setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }
}


