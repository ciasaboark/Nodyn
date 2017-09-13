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

package io.phobotic.nodyn.sync;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Fields annotated with HtmlEnocded will have go through additional processing during the sync
 * process.  It is expected that the remote server will return these values wrapped in arbitrary
 * HTML.  Annotated fields will have the HTML String value replaced with the text within the HTML
 * tags during the sync process.
 *
 * For example if the remote server returned a value of <pre><div><span>foo</span></div></pre>"
 * the field value will be converted into "foo"
 *
 * This annotiation will be ignored if the field is not of type String
 * Created by Jonathan Nelson on 7/7/17.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HtmlEncoded {
}
