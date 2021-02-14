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

package io.phobotic.nodyn_app.sync;


import androidx.annotation.Nullable;
import io.phobotic.nodyn_app.database.sync.Action;

/**
 * Created by Jonathan Nelson on 7/10/17.
 */

public interface ActionSyncListener {
    void onActionSyncSuccess(Action action);
    void onActionSyncFatalError(Action action, @Nullable Exception e, @Nullable String message);
    void onActionSyncRecoverableError(Action action, @Nullable Exception e, @Nullable String message);
}
