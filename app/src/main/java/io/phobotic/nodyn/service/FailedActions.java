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

package io.phobotic.nodyn.service;

import io.phobotic.nodyn.database.model.Action;

/**
 * Created by Jonathan Nelson on 8/20/17.
 */

public class FailedActions {
    private final Action action;
    private final Exception exception;
    private final String message;

    public FailedActions(Action action, Exception exception, String message) {
        this.action = action;
        this.exception = exception;
        this.message = message;
    }

    public Action getAction() {
        return action;
    }

    public Exception getException() {
        return exception;
    }

    public String getMessage() {
        return message;
    }
}
