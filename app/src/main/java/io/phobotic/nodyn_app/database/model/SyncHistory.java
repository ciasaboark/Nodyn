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

package io.phobotic.nodyn_app.database.model;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Jonathan Nelson on 9/3/17.
 */

public class SyncHistory {
    private int id = -1;
    private long timestamp;
    private RESULT result;
    private String message;
    private Exception exception;
    private int resposeCode;
    private String responseMessage;

    public SyncHistory(int id, long timestamp, RESULT result, String message, Exception exception,
                       int resposeCode, String responseMessage) {
        this.id = id;
        this.timestamp = timestamp;
        this.result = result;
        this.message = message;
        this.exception = exception;
        this.resposeCode = resposeCode;
        this.responseMessage = responseMessage;
    }

    public int getId() {
        return id;
    }

    public SyncHistory setId(int id) {
        this.id = id;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public RESULT getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }

    public Exception getException() {
        return exception;
    }

    public int getResposeCode() {
        return resposeCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    @Override
    public String toString() {
        DateFormat df = DateFormat.getDateTimeInstance();
        Date d = new Date(timestamp);
        String dateString = df.format(d);
        return dateString + ": " + result;
    }

    public enum RESULT {
        SUCCESS,
        FAIL,
        UNDETERMINED
    }

    public class Columns {
        public static final String ID = "id";
        public static final String TIMESTAMP = "timestamp";
        public static final String RESULT = "result";
        public static final String MESSAGE = "message";
        public static final String EXCEPTION = "exception";
        public static final String RESPOSE_CODE = "response_code";
        public static final String RESPONSE_MESSAGE = "response_message";
    }
}
