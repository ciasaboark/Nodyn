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

package io.phobotic.nodyn_app.fragment.listener;

import android.view.View;

import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 8/8/17.
 */

public interface CheckInOutListener {
    void onCheckOutAuthorized(User authorizedUser);

    void onCheckOutFinished(View transitonView, String transitionName);

    void onCheckInFinished(View transitonView, String transitionName);
}