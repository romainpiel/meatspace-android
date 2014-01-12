package com.romainpiel.lib.utils;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;

/**
 * meatspace-android
 * romainpiel
 * 12/01/2014
 */
public class CacheManager {

    private static final String CACHE_PREFIX = "cache_";

    private String cachePath;

    public CacheManager(Context context, boolean useExternalCache) {

        if (useExternalCache) {
            if (context.getExternalCacheDir() != null) {
                cachePath = context.getExternalCacheDir().getAbsolutePath();

                return;
            }
        }

        cachePath = context.getCacheDir().getAbsolutePath();
    }

    public boolean writeFile(String mFileName, Serializable mContents) {
        return writeFile(null, mFileName, mContents);
    }

    public boolean writeFile(String mFolderName, String mFileName, Serializable mContents) {
        try {
            String outputPath;
            if (mFolderName != null && mFolderName.length() > 0) {
                File f = new File(cachePath + "/" + CACHE_PREFIX + mFolderName);
                f.mkdir();

                outputPath = CACHE_PREFIX + mFolderName + "/" + CACHE_PREFIX + mFileName;
            } else {
                outputPath = CACHE_PREFIX + mFileName;
            }

            FileOutputStream fos = null;
            try {
                if (fos == null) {
                    File f = new File(cachePath + "/" + outputPath);
                    fos = new FileOutputStream(cachePath + "/" + outputPath);
                }

                fos.write((byte[]) mContents);
                fos.close();
            } catch (Exception e) {
                if (fos == null) {
                    File f = new File(cachePath + "/" + outputPath);
                    fos = new FileOutputStream(cachePath + "/" + outputPath);
                }

                ObjectOutputStream stream = new ObjectOutputStream(fos);

                stream.writeObject(mContents);
                stream.flush();
                fos.flush();
                stream.close();
                fos.close();
            } finally {
                System.gc();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public Object readFile(String fileName) {
        return readFile(null, fileName, null);
    }

    public Object readFile(String fileName, Object def) {
        return readFile(null, fileName, def);
    }

    public Object readFile(String folderName, String fileName, Object def) {
        FileInputStream input = null;
        ObjectInputStream stream = null;

        String filePath = cachePath;
        if (folderName != null && !folderName.equals("")) {
            filePath += "/" + CACHE_PREFIX + folderName + "/";
        }

        File file = new File(filePath, CACHE_PREFIX + fileName);

        try {
            input = new FileInputStream(file);
            stream = new ObjectInputStream(input);

            Object data = stream.readObject();
            stream.close();
            input.close();

            if (data == null) {
                return def;
            }

            return data;
        } catch (OptionalDataException e) {
            System.err.println(file.getAbsolutePath());
            file.delete();
            return def;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return def;
        } catch (Exception e) {
            return def;
        }
    }
}
