package com.romainpiel.lib.utils;

import android.text.Editable;
import android.widget.EditText;

/**
 * BlaBlaCar
 * User: romainpiel
 * Date: 28/08/2013
 * Time: 11:46
 */
public class UIUtils {

    /**
     * Utility method to simply read an EditText's text
     * @param editText EditText to inspect
     * @return the text
     */
    public static String getText(EditText editText) {
        Editable editable = editText.getText();
        String result = null;
        if (editable != null) {
            result = editable.toString();
        }
        return result;
    }
}
