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

package io.phobotic.nodyn.sync.adapter.snipeit4.shadow;

import com.google.gson.Gson;

import org.junit.Test;

/**
 * Created by Jonathan Nelson on 9/15/17.
 */
public class SnippetTest {

    @Test
    public void testUserDetails() {
        String json = "{\"id\": 36,\"username\": \"172270\",\"name\": \"Some One\",\"first_name\": \"some\",\"last_name\": \"one\",\"employee_number\": \"\",\"type\": \"user\"}";
        Gson gson = new Gson();
        Snippet snippet = gson.fromJson(json, Snippet.class);
        assert snippet.getId() == 36 : "ID should have matched JSON value";
        assert snippet.getName().equals("Some One") : "Username should have matched JSON value";
    }

    @Test
    public void testAssignmentJson() {
        String json = "{\n" +
                "      \"id\": 237,\n" +
                "      \"name\": \"071324097\",\n" +
                "      \"asset_tag\": \"071324097\",\n" +
                "      \"serial\": \"071324097\",\n" +
                "      \"model\": {\n" +
                "        \"id\": 11,\n" +
                "        \"name\": \"SR20\"\n" +
                "      },\n" +
                "      \"model_number\": \"\",\n" +
                "      \"status_label\": {\n" +
                "        \"id\": 2,\n" +
                "        \"name\": \"Ready to Deploy\"\n" +
                "      },\n" +
                "      \"category\": {\n" +
                "        \"id\": 3,\n" +
                "        \"name\": \"Vocollect Headsets\"\n" +
                "      },\n" +
                "      \"manufacturer\": {\n" +
                "        \"id\": 4,\n" +
                "        \"name\": \"Vocollect\"\n" +
                "      },\n" +
                "      \"supplier\": null,\n" +
                "      \"notes\": \"\",\n" +
                "      \"order_number\": \"\",\n" +
                "      \"company\": {\n" +
                "        \"id\": 1,\n" +
                "        \"name\": \"MDV SpartanNash Columbus\"\n" +
                "      },\n" +
                "      \"location\": null,\n" +
                "      \"rtd_location\": null,\n" +
                "      \"image\": \"http://192.168.0.50/uploads/models/IWFBHCwo8tPzyFIRsiEjmr3zh.png\",\n" +
                "      \"assigned_to\": {\n" +
                "        \"id\": 158,\n" +
                "        \"username\": \"172255\",\n" +
                "        \"name\": \"Some user\",\n" +
                "        \"first_name\": \"Some\",\n" +
                "        \"last_name\": \"user\",\n" +
                "        \"employee_number\": \"11111\",\n" +
                "        \"type\": \"user\"\n" +
                "      },\n" +
                "      \"warranty\": null,\n" +
                "      \"warranty_expires\": null,\n" +
                "      \"created_at\": {\n" +
                "        \"datetime\": \"2017-06-22 11:27:06\",\n" +
                "        \"formatted\": \"2017-06-22 11:27 AM\"\n" +
                "      },\n" +
                "      \"updated_at\": {\n" +
                "        \"datetime\": \"2017-09-11 22:51:01\",\n" +
                "        \"formatted\": \"2017-09-11 10:51 PM\"\n" +
                "      },\n" +
                "      \"purchase_date\": null,\n" +
                "      \"last_checkout\": {\n" +
                "        \"datetime\": \"2017-06-22 11:29:18\",\n" +
                "        \"formatted\": \"2017-06-22 11:29 AM\"\n" +
                "      },\n" +
                "      \"expected_checkin\": null,\n" +
                "      \"purchase_cost\": null,\n" +
                "      \"user_can_checkout\": false,\n" +
                "      \"custom_fields\": [],\n" +
                "      \"available_actions\": {\n" +
                "        \"checkout\": true,\n" +
                "        \"checkin\": true,\n" +
                "        \"clone\": true,\n" +
                "        \"update\": true,\n" +
                "        \"delete\": true\n" +
                "      }\n" +
                "    }";
        Snipeit4Asset snipeit4Asset = new Gson().fromJson(json, Snipeit4Asset.class);
        assert snipeit4Asset.assignedTo != null : "Expected user assignment to be non null";
        assert snipeit4Asset.assignedTo.getId() == 158 : "Expected user assignment ID to be 158";
        assert snipeit4Asset.assignedTo.getName().equals("Some user") : "Expected user assignment name to be 'Some user'";
    }

}