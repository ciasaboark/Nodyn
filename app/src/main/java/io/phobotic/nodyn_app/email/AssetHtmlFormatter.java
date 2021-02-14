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

package io.phobotic.nodyn_app.email;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import io.phobotic.nodyn_app.cache.EmailImageCache;
import io.phobotic.nodyn_app.database.Database;
import io.phobotic.nodyn_app.database.exception.ModelNotFoundException;
import io.phobotic.nodyn_app.database.exception.UserNotFoundException;
import io.phobotic.nodyn_app.database.model.Asset;
import io.phobotic.nodyn_app.database.model.Model;
import io.phobotic.nodyn_app.database.model.User;

/**
 * Created by Jonathan Nelson on 10/19/17.
 */

public class AssetHtmlFormatter {

    private static final String MARKER_ASSET_TAG = "%assettag%";
    private static final String MARKER_IMAGE_URL = "%imageurl%";
    private static final String MARKER_SERIAL_NO = "%serial%";
    private static final String MARKER_MODEL_NAME = "%model%";
    private static final String MARKER_USER_NAME = "%user%";
    private static final String MARKER_CHECKOUT = "%checkout%";
    private static final String MARKER_EXPECTED_CHECKIN = "%expectedcheckin%";
    private static final String MARKER_BOX_IMAGE = "%box_image%";

    private static final String KEY_ASSET_PAST_DUE_BOX = "past_due_box";
    private static final String KEY_ASSET_DUE_SOON_BOX = "due_soon_box";

    private final Context context;

    public AssetHtmlFormatter(Context context) {
        this.context = context;
    }

    public String formatPastDueAssetAsHtml(List<Attachment> attachments, EmailImageCache cache,
                                           Asset asset) throws IOException {
        Database db = Database.getInstance(context);
        String html = getUnformattedHtml();

        cache.cacheAssetImage(KEY_ASSET_PAST_DUE_BOX, "past_due_box.png");

        //insert the static boxes
        String pastDueBox = cache.getCachedImage(KEY_ASSET_PAST_DUE_BOX);
        addCachedFileAsAttachment(attachments, pastDueBox, cache);
        pastDueBox = "cid:" + pastDueBox;
        html = html.replaceAll(MARKER_BOX_IMAGE, pastDueBox);

        html = formatAssetAsHtml(attachments, cache, asset, html, db);

        return html;
    }

    private String getUnformattedHtml() throws IOException {
        InputStream is = context.getAssets().open("template_email_past_due_asset");
        int size = is.available();

        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();

        String htmlString = new String(buffer);
        return htmlString;
    }

    private void addCachedFileAsAttachment(List<Attachment> attachments, String filename,
                                           EmailImageCache cache) {
        File f = cache.getFileForFilename(filename);
        Attachment attachment = new Attachment(f, filename + ".png", filename);
        attachment.setInline(true);
        attachment.setContentType("image/png");
        attachments.add(attachment);
    }

    private String formatAssetAsHtml(List<Attachment> attachments, EmailImageCache cache,
                                     Asset asset, String html, Database db) throws IOException {


        html = html.replaceAll(MARKER_ASSET_TAG, asset.getTag());
        html = html.replaceAll(MARKER_SERIAL_NO, asset.getSerial());


        //try to find the model name if possible, otherwise fallback to the model ID
        Model m = null;
        try {
            m = db.findModelByID(asset.getModelID());
            html = html.replaceAll(MARKER_MODEL_NAME, m.getName());
        } catch (ModelNotFoundException e) {
            html = html.replaceAll(MARKER_MODEL_NAME, String.valueOf(asset.getModelID()));
        }

        //try to insert the assets image.  Fall back to the model image, then to no image
        String modelImageSrc = cache.getCachedImage(String.valueOf(m.getId()));
        if (modelImageSrc == null) {
            modelImageSrc = "";
        } else {
            //add this image as an attachment
            addCachedFileAsAttachment(attachments, modelImageSrc, cache);
            modelImageSrc = "cid:" + modelImageSrc;
        }
        html = html.replace(MARKER_IMAGE_URL, modelImageSrc);

        //try to find the user's name if possible, otherwise fallback to the user ID
        try {
            User u = db.findUserByID(asset.getAssignedToID());
            html = html.replaceAll(MARKER_USER_NAME, u.getName());
        } catch (UserNotFoundException e) {
            html = html.replaceAll(MARKER_USER_NAME, String.valueOf(asset.getAssignedToID()));
        }

        DateFormat df = DateFormat.getDateTimeInstance();
        String lastCheckout;
        if (asset.getLastCheckout() == -1) {
            lastCheckout = "No checkout information recorded";
        } else {
            Date date = new Date(asset.getLastCheckout());
            lastCheckout = df.format(date);
        }
        html = html.replaceAll(MARKER_CHECKOUT, lastCheckout);

        String expectedCheckin;
        if (asset.getExpectedCheckin() == -1) {
            expectedCheckin = "No checkout information recorded";
        } else {
            Date date = new Date(asset.getExpectedCheckin());
            expectedCheckin = df.format(date);
        }
        html = html.replaceAll(MARKER_EXPECTED_CHECKIN, expectedCheckin);

        return html;
    }

    public String formatDueSoonAssetAsHtml(List<Attachment> attachments, EmailImageCache cache,
                                           Asset asset) throws IOException {
        Database db = Database.getInstance(context);
        String html = getUnformattedHtml();

        cache.cacheAssetImage(KEY_ASSET_DUE_SOON_BOX, "due_soon_box.png");

        String dueSoonBox = cache.getCachedImage(KEY_ASSET_DUE_SOON_BOX);
        addCachedFileAsAttachment(attachments, dueSoonBox, cache);
        dueSoonBox = "cid:" + dueSoonBox;
        html = html.replaceAll(MARKER_BOX_IMAGE, dueSoonBox);

        html = formatAssetAsHtml(attachments, cache, asset, html, db);

        return html;
    }


}