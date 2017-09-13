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
 * Fields annotated with Link will go through additional processing during the sync process.
 * The String value pulled from the remote server is expected to be an HTML &lta&gt element.
 * The field value should be replaced with the link elements href attribute.
 * <p>
 * For example if the remote server returns a value of <pre><a href="http://foo">bar</a></pre>
 * the field value will be replaced with "http://foo".  If multiple link elements are present the
 * href value from the first element will be used.
 * <p>
 * This annotation will be ignored if the field is not of type String.
 * Created by Jonathan Nelson on 7/15/17.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Link {
}
