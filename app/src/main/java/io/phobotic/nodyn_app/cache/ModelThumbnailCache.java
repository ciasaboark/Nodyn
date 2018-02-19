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

package io.phobotic.nodyn_app.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.crashlytics.android.Crashlytics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.phobotic.nodyn_app.database.model.Model;

/**
 * Created by Jonathan Nelson on 2/3/18.
 */

public class ModelThumbnailCache {
    private static final int MODEL_THUMBNAIL_WIDTH = 48;
    private static final int MODEL_THUMBNAIL_HEIGHT = 48;
    private static ModelThumbnailCache instance;
    private static Context context;
    Map<Model, String> modelImageCache = new HashMap<>();

    public static ModelThumbnailCache getInstance(Context context) {
        if (instance == null) {
            instance = new ModelThumbnailCache(context);
        }

        return instance;
    }

    private ModelThumbnailCache(Context context) {
        this.context = context;
    }

    public String getCachedImage(Model m) {
        String s = modelImageCache.get(m);
        return s;
    }

    public void updateModelImageCache(List<Model> modelList) {
        for (Model m : modelList) {
            String cachedFilename = modelImageCache.get(m);
            if (cachedFilename == null) {
                cacheModelImage(m);
            }
        }
    }

    private void cacheModelImage(Model model) {
        try {
            cacheModelNetworkImage(model);
        } catch (Exception e1) {
            try {
                // TODO: 2/3/18 the imagecache should probably not default to using a fallback image.  Let that be handled somewhere else
                cacheModelFallbackImage(model);
            } catch (Exception e2) {

            }
        }
    }

    /**
     * Try to use the image referenced by the model as the models cached image
     */
    private void cacheModelNetworkImage(Model model) throws Exception {
        String imageURL = model.getImage();
        if (imageURL == null) throw new Exception("Null model image");

        String md5sum = getMD5EncryptedString(imageURL);
        if (md5sum == null || md5sum.length() == 0) {
            throw new Exception("Unable to calculate md5sum of image url");
        }

        URL url = new URL(imageURL);
        InputStream inputStream = (InputStream) url.getContent();

        cacheModelThumbnail(model, md5sum, inputStream);
    }

    /**
     * Use the default assets image from the assets folder as the cached image for this model
     */
    private void cacheModelFallbackImage(Model model) throws Exception {
        InputStream is = context.getAssets().open("devices_generic_48.png");
        cacheModelThumbnail(model, "default", is);
    }

    public static String getMD5EncryptedString(String encTarget) {
        String md5 = "";
        try {
            MessageDigest mdEnc = MessageDigest.getInstance("MD5");
            mdEnc.update(encTarget.getBytes(), 0, encTarget.length());
            md5 = new BigInteger(1, mdEnc.digest()).toString(16);
            while (md5.length() < 32) {
                md5 = "0" + md5;
            }
        } catch (NoSuchAlgorithmException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
        }

        return md5;
    }

    private void cacheModelThumbnail(Model model, String filename, InputStream inputStream)
            throws Exception {
        File f = new File(String.format("%s/%s", context.getCacheDir(), filename));
        if (!f.exists()) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            bitmap = Bitmap.createScaledBitmap(bitmap, MODEL_THUMBNAIL_WIDTH,
                    MODEL_THUMBNAIL_HEIGHT, true);


            FileOutputStream fos = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }
        modelImageCache.put(model, filename);
    }
}
