package com.romainpiel.lib.utils;

import java.util.Collection;

/**
 * Meatspace
 * User: romainpiel
 * Date: 28/08/2013
 * Time: 11:50
 */
public class StringUtils {

    /**
     * Utility method to build a [spearator] separated string of a collection
     * @param collection collection to read
     * @param separator separator
     * @param <E> generic parameter of the collection
     * @return a string representation of the collection using the given separator
     */
    public static <E> String join(Collection<E> collection, String separator) {
        StringBuilder result = new StringBuilder();
        for(E object : collection) {
            result.append(object.toString());
            result.append(separator);
        }
        return result.length() > 0 ? result.substring(0, result.length() - 1): "";
    }
}
