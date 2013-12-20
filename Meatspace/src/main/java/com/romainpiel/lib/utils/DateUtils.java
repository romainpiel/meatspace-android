package com.romainpiel.lib.utils;

import android.content.Context;

import java.util.Date;

/**
 * Meatspace
 * User: romainpiel
 * Date: 20/08/2013
 * Time: 11:45
 */
public class DateUtils extends android.text.format.DateUtils {

    public static String formatTime(Context context, Date date) {
        return android.text.format.DateUtils.formatDateTime(context, date.getTime(),
                android.text.format.DateUtils.FORMAT_SHOW_TIME
                        | android.text.format.DateUtils.FORMAT_NO_NOON
                        | android.text.format.DateUtils.FORMAT_NO_MIDNIGHT
        );
    }
}
