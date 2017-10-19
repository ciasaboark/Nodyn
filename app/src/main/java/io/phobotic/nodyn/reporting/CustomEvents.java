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

package io.phobotic.nodyn.reporting;

/**
 * Created by Jonathan Nelson on 10/16/17.
 */

public class CustomEvents {
    public static final String SYNC_SCHEDULED = "Sync scheduled";
    public static final String SYNC_SUCCESS = "Sync Success";
    public static final String SYNC_ERROR_ACTION_FAILED = "Sync Error: Action failed";
    public static final String SYNC_ERROR_EMAIL_SENT = "Sync Error: Email sent";
    public static final String SYNC_ERROR_EMAIL_NOT_SENT = "Sync Error: Email not sent";
    public static final String UPGRADE_RECEIVER_FIRED = "Upgrade Receiver called";
    public static final String BOOT_RECEIVER_FIRED = "Boot Receiver called";
    public static final String SYNC_FAILED = "Sync failed";
    public static final String ASSET_PAST_DUE = "Asset Past Due";


    public static final String EXCEPTION_NAME = "exception name";
    public static final String PAST_DUE_EMAIL_NOT_SENT = "Asset past due email not sent";
    public static final String PAST_DUE_EMAIL_SENT = "Asset past due email sent";
}
