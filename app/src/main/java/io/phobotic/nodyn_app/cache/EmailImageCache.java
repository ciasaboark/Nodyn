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

package io.phobotic.nodyn_app.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

public class EmailImageCache {
    private static final String TAG = EmailImageCache.class.getSimpleName();
    private static final int THUMBNAIL_WIDTH = 96;
    private static final int THUMBNAIL_HEIGHT = 96;
    private static final String CACHE_DIR = "email_image_cache";
    private static EmailImageCache instance;
    private static Context context;
    //    Map<Model, String> modelImageCache = new HashMap<>();
    Map<String, String> imageCache = new HashMap<>();

    public static EmailImageCache getInstance(Context context) {
        if (instance == null) {
            instance = new EmailImageCache(context);
        }

        return instance;
    }

    private EmailImageCache(Context context) {
        this.context = context;

        //clear the cache directory
        try {
            File f = new File(String.format("%s/%s", context.getCacheDir(), CACHE_DIR));
            f.mkdirs();
            FileUtils.cleanDirectory(f);
            File[] fileList = f.listFiles();
            //this should come back clean
            if (fileList.length > 0) {
                throw new Exception("Files remain after purging cache directory: " + f.getAbsolutePath());
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            Log.e(TAG, "Unable to clean cache directory");
        }
    }


    public void cacheImage(String key, File image, boolean isThumbnail) throws IOException {
        InputStream is = new FileInputStream(image);
        String md5sum = getMD5EncryptedString(image.getAbsolutePath());

        if (isThumbnail) {
            storeResizedImage(key, md5sum, is);
        } else {
            storeImage(key, md5sum, is);
        }
    }

    private static String getMD5EncryptedString(String encTarget) {
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

    private void storeResizedImage(String key, String filename, InputStream inputStream)
            throws IOException {
        File f = new File(String.format("%s/%s/%s", context.getCacheDir(), CACHE_DIR, filename));
        if (!f.exists()) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            bitmap = Bitmap.createScaledBitmap(bitmap, THUMBNAIL_WIDTH,
                    THUMBNAIL_HEIGHT, true);


            FileOutputStream fos = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }

        imageCache.put(key, filename);
    }

    private void storeImage(String key, String filename, InputStream inputStream)
            throws IOException {
        File f = new File(String.format("%s/%s/%s", context.getCacheDir(), CACHE_DIR, filename));
        if (!f.exists()) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            FileOutputStream fos = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        }

        imageCache.put(key, filename);
    }

    public void updateModelImageCache(List<Model> modelList) {
        for (Model m : modelList) {
            String cachedFilename = imageCache.get(m.getId());
            if (cachedFilename == null) {
                cacheModelImage(m);
            }
        }
    }

    private void cacheModelImage(Model model) {
        try {
            cacheImage(String.valueOf(model.getId()), model.getImage(), true);
        } catch (Exception e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            Log.e(TAG, String.format("Caught Exception trying to cache image for model id %d " +
                    "using URL %s, %s:%s", model.getId(), model.getImage(), e.getClass().getSimpleName(), e.getMessage()));
        }
    }

    public void cacheImage(String key, String imageURL, boolean isThumbnail) throws Exception {
        if (imageURL == null) throw new Exception("Null model image");

        String md5sum = getMD5EncryptedString(imageURL);
        if (md5sum == null || md5sum.length() == 0) {
            throw new Exception(String.format("Unable to calculate md5sum of image url: %s", imageURL));
        }

        URL url = new URL(imageURL);
        InputStream inputStream = (InputStream) url.getContent();

        if (isThumbnail) {
            storeResizedImage(key, md5sum, inputStream);
        } else {
            storeImage(key, md5sum, inputStream);
        }
    }

    /**
     * Use the default assets image from the assets folder as the cached image for this model
     */
    private void cacheModelFallbackImage(Model model) throws Exception {
        InputStream is = context.getAssets().open("devices_generic_48.png");
        storeResizedImage(String.valueOf(model.getId()), "default", is);
    }

    public String getCachedImage(String key) {
        String img = null;
        if (key != null) {
            img = imageCache.get(key);
        }

        return img;
    }

    /**
     * Use the default assets image from the assets folder as the cached image for this model
     */
    public void cacheAssetImage(String key, String assetFileImage) {
        try {
            InputStream is = context.getAssets().open(assetFileImage);
            String md5 = getMD5EncryptedString(assetFileImage);
            storeImage(key, md5, is);
        } catch (Exception e) {
            Log.e(TAG, String.format("Caught Exception trying to cache image from assets folder " +
                    "with filename %s, %s:%s", assetFileImage, e.getClass().getSimpleName(), e.getMessage()));
        }
    }

    public File getFileForFilename(String filename) {
        File f = new File(String.format("%s/%s/%s", context.getCacheDir(), CACHE_DIR, filename));
        return f;
    }
}
