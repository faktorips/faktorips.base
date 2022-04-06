/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.values;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A collection of general utility methods for lists.
 */
public class ListUtil {

    private ListUtil() {
        // Prevent initialization.
    }

    /**
     * Initializes an empty list. Uses the call-site type as the list's (generic) element type.
     */
    public static final <T> List<T> newList() {
        return new ArrayList<>();
    }

    /**
     * Initializes a list with a given value. Uses the value's type as the list's (generic) element
     * type.
     */
    public static final <T> List<T> newList(T value) {
        List<T> newList = new ArrayList<>();
        newList.add(value);
        return newList;
    }

    /**
     * Initializes a list with the given values. Uses the value's type as the list's (generic)
     * element type.
     */
    @SafeVarargs
    public static final <T> List<T> newList(T firstValue, T... moreValues) {
        List<T> newList = new ArrayList<>();
        newList.add(firstValue);
        for (T value : moreValues) {
            newList.add(value);
        }
        return newList;
    }

    /**
     * Converts a list with generic type T to a list of a sub-type of T (R). If there is any element
     * not of type R in the list, a {@link ClassCastException} is thrown.
     * 
     * @param list The list that should be converted
     * @param newType The subclass (R) this method should try to cast all list elements to. Also the
     *            element type of the returned list (if possible).
     * @return The converted list with the given subclass element type (R).
     * @throws ClassCastException in case of there is any element in the list that is not of the
     *             given subclass (R).
     */
    public static final <T, R extends T> List<? extends R> convert(List<? extends T> list, Class<R> newType) {
        for (T member : list) {
            if (!(newType.isAssignableFrom(member.getClass()))) {
                throw new ClassCastException(member + " not instance of " + newType);
            }
        }
        @SuppressWarnings("unchecked")
        List<? extends R> convertList = (List<? extends R>)list;
        return convertList;
    }

    /**
     * Creates a new unmodifiable list from the given values.
     * 
     * @param values The values that should be part of the new list
     * 
     * @return A new unmodifiable list containing all values
     */
    @SafeVarargs
    public static final <T> List<T> unmodifiableList(T... values) {
        return Collections.unmodifiableList(Arrays.asList(values));
    }

}
