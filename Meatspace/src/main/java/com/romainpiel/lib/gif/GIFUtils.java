package com.romainpiel.lib.gif;

import android.util.Base64;

import com.romainpiel.lib.utils.Debug;

import java.io.UnsupportedEncodingException;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 10/11/2013
 * Time: 18:27
 */
public class GIFUtils {

    private static final String GIF_PREFIX = "data:image/gif;base64,";

    public static String mediaFromGIFbytes(byte[] bytes) {
        String result = null;
        try {
            result = GIF_PREFIX + new String(Base64.encode(bytes, Base64.DEFAULT), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Debug.out(e);
        }
        return result;
    }

    public static byte[] mediaToGIFbytes(String media) {
        try {
            String gifData = media.replace(GIF_PREFIX, "");
            return Base64.decode(gifData, Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
