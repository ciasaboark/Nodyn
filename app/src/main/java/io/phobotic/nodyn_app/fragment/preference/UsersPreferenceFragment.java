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

package io.phobotic.nodyn_app.fragment.preference;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.phobotic.nodyn_app.R;
import io.phobotic.nodyn_app.database.Verifiable;
import io.phobotic.nodyn_app.database.model.User;
import io.phobotic.nodyn_app.preference.ConfigureAvatarsDialogFragment;

/**
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class UsersPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_users, rootKey);
        setHasOptionsMenu(true);

        Preference avatarConfig = findPreference(getString(R.string.pref_key_users_enable_avatars_configure));
        avatarConfig.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogFragment dialog = new ConfigureAvatarsDialogFragment();
                FragmentManager fm = getChildFragmentManager();
                dialog.show(fm, "dialog");

                return false;
            }
        });
        initScanFieldSelect();
    }

    private void initScanFieldSelect() {
        ListPreference lp = (ListPreference) findPreference(getResources()
                .getString(R.string.pref_key_user_scan_field));

        List<String> fieldStringList = new ArrayList<>();
        List<Field> fields = FieldUtils.getFieldsListWithAnnotation(User.class, Verifiable.class);

        for (Field field : fields) {
            String fieldValue = field.getAnnotation(Verifiable.class).value();
            fieldStringList.add(fieldValue);
        }

        String[] selectableFields = fieldStringList.toArray(new String[]{});
        lp.setEntries(selectableFields);
        lp.setEntryValues(selectableFields);

        PreferenceListeners.bindPreferenceSummaryToValue(lp);
    }
}
