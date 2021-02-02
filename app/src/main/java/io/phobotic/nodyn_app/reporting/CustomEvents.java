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

package io.phobotic.nodyn_app.reporting;

/**
 * Created by Jonathan Nelson on 10/16/17.
 */

public class CustomEvents {
    public static final String SYNC_SCHEDULED = "sync_scheduled";
    public static final String SYNC_SUCCESS = "sync_success";
    public static final String SYNC_ERROR_ACTION_FAILED = "sync_error__action_failed";
    public static final String SYNC_ERROR_EMAIL_SENT = "sync_error__email_sent";
    public static final String SYNC_ERROR_EMAIL_NOT_SENT = "sync_error__email_not_sent";
    public static final String UPGRADE_RECEIVER_FIRED = "upgrade_receiver_called";
    public static final String BOOT_RECEIVER_FIRED = "boot_receiver_called";
    public static final String SYNC_FAILED = "sync_failed";
    public static final String ASSET_PAST_DUE = "asset_past_due";


    public static final String EXCEPTION_NAME = "exception_name";
    public static final String PAST_DUE_EMAIL_NOT_SENT = "asset_past_due_reminder_not_sent";
    public static final String PAST_DUE_EMAIL_SENT = "asset_past_due_reminder_not_sent";

    public static final String USER_CHECKOUT = "user_checked_out_devices";
    public static final String USER_CHECKOUT_COUNT = "asset_count_checked_out_by_user";
    public static final String CHECKOUT_SESSION_COMPLETE = "checkout_session_ended";
    public static final String CHECKOUT_COUNTS_FOR_SESSION = "checkout_user_count";


    public static final String CHECKIN_COMPLETE = "assets_checked_in";
    public static final String CHECKIN_ASST_COUNT = "number_of_assets_checked_in_this_session";
    public static final String AUDIT_RESULTS_ERROR_EMAIL_NOT_SENT = "audit_results_email_could_not_be_sent";
    public static final String AUDIT_RESULTS_EMAIL_SENT = "audit_results_email_sent_successfully";
    public static final String STATISTICS_EMAIL_SENT = "stats_rslts_email_sent";
    public static final String STATISTICS_EMAIL_NOT_SENT = "stats_rslts_email_not_sent";
}
