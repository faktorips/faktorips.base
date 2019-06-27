/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * A collection of utility methods for Strings. We don't use a class library like apache-commons
 * here to minimise the dependencies for the generated code.
 */
public final class IpsStringUtils {

    public static final String EMPTY = "";

    private IpsStringUtils() {
        /* no instances */
    }

    /**
     * Returns {@code true} if {@code s} is either {@code null} or the empty string, otherwise
     * {@code false}.
     */
    public static final boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Returns {@code true} if {@code s} is neither {@code null} nor the empty string, otherwise
     * {@code false}.
     */
    public static final boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    /**
     * Returns {@code true} if {@code s} is either {@code null}, the empty string or a string that
     * only contains whitespace, otherwise {@code false}.
     */
    public static final boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Joins the elements of the provided Collection into a single String containing the provided
     * elements with the given separator. No delimiter is added before or after the list.
     * 
     * @param collection the Collection of values to join together, may be null
     * @param separator the separator to use, null treated as ""
     * @return the joined String, null if the collection is null
     */
    public static final String join(Collection<?> collection, String separator) {
        if (collection == null) {
            return null;
        } else {
            Iterator<?> it = collection.iterator();
            if (it == null) {
                return null;
            } else {
                // 16 (default) may be too small
                StringBuilder stringBuilder = new StringBuilder(256);
                stringBuilder.append("");

                if (it.hasNext()) {
                    stringBuilder.append(it.next());
                }

                while (it.hasNext()) {
                    stringBuilder.append(separator + it.next().toString());
                }

                return stringBuilder.toString();
            }

        }
    }

    public static final String join(Object[] objectArray, String separator) {
        return join(Arrays.asList(objectArray), separator);
    }

    public static final String toLowerFirstChar(String string) {
        if (isEmpty(string)) {
            return string;
        }
        char firstChar = string.charAt(0);
        if (Character.isLowerCase(firstChar)) {
            return string;
        }
        return Character.toLowerCase(firstChar) + string.substring(1);
    }
}
