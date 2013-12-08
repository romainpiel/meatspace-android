package com.romainpiel.model;

import android.util.Base64;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.romainpiel.lib.utils.Debug;
import com.romainpiel.lib.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MeatspaceProject
 * User: romainpiel
 * Date: 08/12/2013
 * Time: 09:13
 */
public class GifMedia {

    private static final String GIF_PREFIX = "data:image/gif;base64,";

    private byte[] bytes;

    public byte[] getBytes() {
        return bytes;
    }

    public static JsonDeserializer<GifMedia> getDeserializer() {
        return new JsonDeserializer<GifMedia>() {
            @Override
            public GifMedia deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                String raw = jsonElement.getAsString();
                GifMedia result = new GifMedia();
                result.bytes = mediaToGIFbytes(raw);
                return result;
            }
        };
    }

    public static String mediaFromGIFbytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        String result = null;
        try {
            result = GIF_PREFIX + new String(Base64.encode(bytes, Base64.DEFAULT), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Debug.out(e);
        }
        return result;
    }

    public static byte[] mediaToGIFbytes(String media) {
        if (media == null) {
            return null;
        }

        try {
            return Base64.decode(unescape(media), Base64.DEFAULT);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String unescape(String template) {
        Map<String,String> tokens = new HashMap<String,String>();
        tokens.put(GIF_PREFIX, "");
        tokens.put("%2b", "+");
        tokens.put("%2f", "/");

        String patternString = "(" + StringUtils.join(tokens.keySet(), "|") + ")";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(template);

        StringBuffer sb = new StringBuffer();
        while(matcher.find()) {
            matcher.appendReplacement(sb, tokens.get(matcher.group(1)));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
