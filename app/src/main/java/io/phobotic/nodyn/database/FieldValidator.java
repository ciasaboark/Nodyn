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

package io.phobotic.nodyn.database;

import android.util.Log;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

/**
 * Created by Jonathan Nelson on 8/31/17.
 */

public class FieldValidator {
    public static final String TAG = FieldValidator.class.getSimpleName();

    public static boolean isFieldMatch(Object obj, String validationField, String inputString) {
        List<Field> fields = FieldUtils.getFieldsListWithAnnotation(obj.getClass(), Verifiable.class);
        for (Field field : fields) {
            String fieldValue = field.getAnnotation(Verifiable.class).value();
            if (validationField.equals(fieldValue)) {
                field.setAccessible(true);
                Object val = null;
                try {
                    val = field.get(obj);
                } catch (IllegalAccessException e) {
                    Log.e(TAG, "Caught IllegalAccessException trying to pull field " +
                            field.toString() + " from " + obj.getClass().getSimpleName() +
                            ". Validation may fail");
                    e.printStackTrace();
                }

                boolean authenticated = Objects.equals(val, inputString);
                if (authenticated) {
                    return true;
                }
            }
        }

        return false;
    }
}
