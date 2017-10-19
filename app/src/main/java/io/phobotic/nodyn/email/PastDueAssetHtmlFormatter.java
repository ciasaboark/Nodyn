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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.phobotic.nodyn.database.Database;
import io.phobotic.nodyn.database.exception.ModelNotFoundException;
import io.phobotic.nodyn.database.exception.UserNotFoundException;
import io.phobotic.nodyn.database.model.Asset;
import io.phobotic.nodyn.database.model.Model;
import io.phobotic.nodyn.database.model.User;

/**
 * Created by Jonathan Nelson on 10/19/17.
 */

public class PastDueAssetHtmlFormatter {
    public static final String ASSET_TAG = "<div>Asset ID: <span class=\"asset\">%s</span></div>";
    public static final String SERIAL_NO = "<div>Serial: <span class=\"serial\">%s</span></div>";
    public static final String MODEL_NAME = "<div>Model: <span class=\"model\">%s</span></div>";
    public static final String USER_NAME = "<div>User ID: <span class=\"user\">%s</span></div>";
    public static final String CHECKOUT = "<div>Checkout: <span class=\"checkout\">%s</span></div>";
    public static final String EXPECTED_CHECKIN = "<div>Expected Checkin: <span class=\"checkin\">%s</span></div>";


    public static String formatAssetAsHtml(Context context, Asset asset) {
        Database db = Database.getInstance(context);

        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"box\">");

        sb.append(String.format(ASSET_TAG, asset.getTag()));
        sb.append(String.format(SERIAL_NO, asset.getSerial()));

        //try to find the model name if possible, otherwise fallback to the model ID
        try {
            Model m = db.findModelByID(asset.getModelID());
            sb.append(String.format(MODEL_NAME, m.getName()));
        } catch (ModelNotFoundException e) {
            sb.append(String.format(MODEL_NAME, asset.getModelID()));
        }

        //try to find the user's name if possible, otherwise fallback to the user ID
        try {
            User u = db.findUserByID(asset.getAssignedToID());
            sb.append(String.format(USER_NAME, u.getName()));
        } catch (UserNotFoundException e) {
            sb.append(String.format(USER_NAME, asset.getAssignedToID()));
        }

        String lastCheckout;
        if (asset.getLastCheckout() == -1) {
            lastCheckout = "No checkout information recorded";
        } else {
            Date date = new Date(asset.getLastCheckout());
            DateFormat df = new SimpleDateFormat();
            lastCheckout = df.format(date);
        }
        sb.append(String.format(CHECKOUT, lastCheckout));

        String expectedCheckin;
        if (asset.getExpectedCheckin() == -1) {
            expectedCheckin = "No checkout information recorded";
        } else {
            Date date = new Date(asset.getExpectedCheckin());
            DateFormat df = new SimpleDateFormat();
            expectedCheckin = df.format(date);
        }
        sb.append(String.format(EXPECTED_CHECKIN, expectedCheckin));

        sb.append("</div>");

        return sb.toString();
    }

    public static String getHeader() {
        String s = "<!doctype html>\n" +
                "<head><title>Past Due Assets</title>\n" +
                "<style type=\"text/css\">\n" +
                "\t.box {\n" +
                "\t\tborder-radius: 3px;\n" +
                "\t\tbackground-color: #fefefe;\n" +
                "\t\tpadding: 16px;\n" +
                "\t\tmargin: 16px;\n" +
                "\t\tbox-shadow: 0px 4px 2px #cfcfcf;\n" +
                "\t}\n" +
                "\n" +
                "\t.asset {\n" +
                "\t\tfont-weight: bold;\n" +
                "\t\tfont-size: larger;\n" +
                "\t}\n" +
                "\n" +
                "\t.serial {\n" +
                "\t\tfont-family: monospace;\n" +
                "\t\tfont-weight: bold;\n" +
                "\t}\n" +
                "\n" +
                "\t.user {\n" +
                "\t\tfont-weight: bold;\n" +
                "\t}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div>The following assets are past due:</div>\n" +
                "\n" +
                "<div>";

        return s;
    }

    public static String getFooter() {
        String s = "</div>\n" +
                "\n" +
                "</body></html>";

        return s;
    }
}