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

package io.phobotic.nodyn.email;

import android.content.Context;

import com.google.gson.Gson;

import org.json.JSONObject;

import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.exception.AssetNotFoundException;
import io.phobotic.nodyn.database.exception.StatusNotFoundException;
import io.phobotic.nodyn.database.exception.UserNotFoundException;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.Status;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.service.FailedActions;

/**
 * Created by Jonathan Nelson on 8/29/17.
 */

public class ActionHtmlFormatter {
    public static final String PRE = "<div class=\"box\">";
    public static final String POST = "</div>";
    public static final String ACTION = "<div>Action: <span class=\"action\">%s</span></div>";
    public static final String ASSET_ID = "<div>Asset: <span class=\"asset\">%s</span></div>";
    public static final String SERIAL_NO = "<div>Serial: <span class=\"serial\">%s</span></div>";
    public static final String STATUS = "<div>Status: <span class=\"status\">%s</span></div>";
    public static final String USER = "<div>User ID: <span class=\"user\">%s</span></div>";
    public static final String LOGIN = "<div>Login: <span class=\"login\">%s</span></div>";
    public static final String MESSAGE = "<div>Message: <span class=\"message\">%s</span></div>";
    public static final String EXCEPTION_MESSAGE = "<div>Exception Message: <span class=\"message\">%s</span></div>";
    public static final String EXCEPTION_CLASS = "<div>Exception: <span class=\"exceptionType\">%s</span></div>";
    public static final String STACK_TRACE = "<button class=\"accordion\">Stack trace</button>\n" +
            "<div class=\"panel\">\n" +
            "<pre>%s</pre>\n" +
            "</div>";



    public static String formatActionAsHtml(Context context, FailedActions action) {
        Database db = Database.getInstance(context);

        StringBuilder sb = new StringBuilder();
        sb.append(PRE);
        sb.append(String.format(ACTION, action.getAction().getDirection().toString()));

        //wite the asset tag if possible, otherwise just use the asset id provided in the action
        try {
            Asset asset = db.findAssetByID(action.getAction().getAssetID());
            sb.append(asset.getTag());
            if (asset.getSerial() != null && asset.getSerial().length() > 0) {
                sb.append(String.format(ASSET_ID, asset.getTag()));
                sb.append(String.format(SERIAL_NO, asset.getSerial()));

                //try to write the status name if possible, use the recorded status ID if required
                try {
                    Status status = db.findStatusByID(asset.getStatusID());
                    sb.append(String.format(STATUS, status.getName()));
                } catch (StatusNotFoundException e) {
                    sb.append(String.format(STATUS, asset.getStatusID()));
                }
            }
        } catch (AssetNotFoundException e) {
            //if fetching the asset failed, then only write the asset id and do not insert
            //+ the serial or status
            sb.append(String.format(ASSET_ID, action.getAction().getAssetID()));

        }

        //try to write the username if possible, use the user ID number if required
        try {
            User user = db.findUserByID(action.getAction().getUserID());
            sb.append(String.format(USER, user.getName()));
            sb.append(String.format(LOGIN, user.getUsername()));
        } catch (UserNotFoundException e) {
            sb.append(String.format(USER, action.getAction().getUserID()));
        }

        //the rest of the properties are nullable
        if (action.getMessage() != null) {
            sb.append(String.format(MESSAGE, action.getMessage()));
        }

        if (action.getException() != null) {
            sb.append(String.format(EXCEPTION_CLASS, action.getException().getClass().getSimpleName()));
            sb.append(String.format(EXCEPTION_MESSAGE, action.getException().getMessage()));

            Gson gson = new Gson();
            String stacktraceJson = gson.toJson(action);
            try {
                stacktraceJson = new JSONObject(stacktraceJson).toString(4);
                sb.append(String.format(STACK_TRACE, stacktraceJson));
            } catch (Exception e) {
                //nothing to do here, formatting will just be ugly
            }
        }

        return sb.toString();
    }

    public static String getHeader() {
        String s = "<!doctype html>\n" +
                "<head><title>Sync Failed</title>\n" +
                "<style type=\"text/css\">\n" +
                "\t.box {\n" +
                "\t\tborder-radius: 3px;\n" +
                "\t\tbackground-color: #fefefe;\n" +
                "\t\tpadding: 16px;\n" +
                "\t\tmargin: 16px;\n" +
                "\t\tbox-shadow: 0px 4px 2px #cfcfcf;\n" +
                "\t}\n" +
                "\n" +
                "\t.action {\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\tfont-size: larger;\n" +
                "\t}\n" +
                "\n" +
                "\t.asset {\n" +
                "\t\tfont-weight: bold;\n" +
                "\t}\n" +
                "\n" +
                "\t.user {\n" +
                "\t\tfont-weight: bold;\n" +
                "\t}\n" +
                "\n" +
                "\t.message {\n" +
                "\n" +
                "\t}\n" +
                "\n" +
                "\t.exceptionType {\n" +
                "\t\tfont-style: italic;\n" +
                "\t\tfont-family: monospace;\n" +
                "\t\tpadding-bottom: 16px;\n" +
                "\t}\n" +
                "\n" +
                "\tbutton.accordion {\n" +
                "\t    background-color: #eee;\n" +
                "\t    color: #444;\n" +
                "\t    cursor: pointer;\n" +
                "\t    margin-top: 8px;\n" +
                "\t    border: none;\n" +
                "\t    text-align: left;\n" +
                "\t    outline: none;\n" +
                "\t    font-size: 15px;\n" +
                "\t    transition: 0.4s;\n" +
                "\n" +
                "\t}\n" +
                "\n" +
                "\tbutton.accordion.active, button.accordion:hover {\n" +
                "\t    background-color: #ddd;\n" +
                "\t}\n" +
                "\n" +
                "\tdiv.panel {\n" +
                "\t    padding: 0 18px;\n" +
                "\t    background-color: #fcfcfc;\n" +
                "\t    border-radius: 3px;" +
                "\t    max-height: 0;\n" +
                "\t    overflow: scroll;\n" +
                "\t    transition: max-height 0.2s ease-out;\n" +
                "\t}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div>Sync failed with the following errors:</div>\n" +
                "<div>";

        return s;
    }

    public static String getFooter() {
        String s = "</div>\n" +
                "\n" +
                "<script>\n" +
                "\tvar acc = document.getElementsByClassName(\"accordion\");\n" +
                "\tvar i;\n" +
                "\n" +
                "\tfor (i = 0; i < acc.length; i++) {\n" +
                "\t  acc[i].onclick = function() {\n" +
                "\t    this.classList.toggle(\"active\");\n" +
                "\t    var panel = this.nextElementSibling;\n" +
                "\t    if (panel.style.maxHeight){\n" +
                "\t      panel.style.maxHeight = null;\n" +
                "\t    } else {\n" +
                "\t      panel.style.maxHeight = panel.scrollHeight + \"px\";\n" +
                "\t    } \n" +
                "\t  };\n" +
                "\t}\n" +
                "</script>\n" +
                "\n" +
                "</body></html>";

        return s;
    }
}
