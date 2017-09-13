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
import io.phobotic.nodyn.database.exception.UserNotFoundException;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.User;
import io.phobotic.nodyn.service.FailedActions;

/**
 * Created by Jonathan Nelson on 8/29/17.
 */

public class ActionHtmlFormatter {
    public static String formatActionAsHtml(Context context, FailedActions action) {
        Database db = Database.getInstance(context);

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"box\">");
        sb.append("<div>Action: <span class=\"action\">" + action.getAction().getDirection()
                .toString() + "</span></div>");

        //wite the asset tag if possible, otherwise just use the asset id provided in the action
        sb.append("<div>Asset: <span class=\"asset\">");
        try {
            Asset asset = db.findAssetByID(action.getAction().getAssetID());
            sb.append(asset.getTag());
            if (asset.getSerial() != null && asset.getSerial().length() > 0) {
                sb.append("</span></div><div>Serial: <span class=\"serial\">");
                sb.append(asset.getSerial());

                sb.append("</span></div><div>Serial: <span class=\"status\">");
                sb.append(asset.getStatus());
            }
        } catch (AssetNotFoundException e) {
            sb.append(action.getAction().getAssetID());
        } finally {
            sb.append("</span></div>");
        }

        sb.append("<div>User ID: <span class=\"user\">");
        try {
            User u = db.findUserByID(action.getAction().getUserID());
            sb.append(u.getName());
            sb.append("</span></div><div>Login: <span class=\"login\">");
            sb.append(u.getUsername());
        } catch (UserNotFoundException e) {
            sb.append(action.getAction().getUserID());
        } finally {
            sb.append("</span></div>");
        }


        sb.append("<div>Message: <span class=\"message\">" + action.getMessage() + "</span></div>");
        if (action.getException().getMessage() != null) {
            sb.append("<div>Exception Message: <span class=\"message\">" + action.getException().getMessage() + "</span></div>");
        }
        sb.append("<div>Exception: <span class=\"exceptionType\">" + action.getException().getClass().getSimpleName() + "</span></div>");
        sb.append("<button class=\"accordion\">Stack trace</button>\n" +
                "\t\t<div class=\"panel\">\n" +
                "\t\t  <pre>");
        Gson gson = new Gson();
        String stacktraceJson = gson.toJson(action);
        try {
            stacktraceJson = new JSONObject(stacktraceJson).toString(4);
        } catch (Exception e) {
            //nothing to do here, formatting will just be ugly
        }

        sb.append(stacktraceJson);
        sb.append("</pre>\n" +
                "\t\t</div>\n" +
                "\t</div>");

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
