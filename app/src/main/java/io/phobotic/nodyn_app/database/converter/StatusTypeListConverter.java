/*
 * Copyright (c) 2020 Jonathan Nelson <ciasaboark@gmail.com>
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

package io.phobotic.nodyn_app.database.converter;

import androidx.room.TypeConverter;
import io.phobotic.nodyn_app.database.audit.model.AuditDetail;

/**
 * Created by Jonathan Nelson on 2020-02-18.
 */
public class StatusTypeListConverter {
    @TypeConverter
    public static String fromStatus(AuditDetail.Status status) {
        if (status == null) {
            return null;
        }

        String s = status.name();
        return s;
    }

    @TypeConverter
    public static AuditDetail.Status fromString(String s) {
        if (s == null || s.length() == 0) {
            return null;
        }

        AuditDetail.Status status = AuditDetail.Status.valueOf(s);
        return status;
    }
}