/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

/**
 * A collection of utility methods for Strings. We don't use a class library like apache-commons
 * here to minimise the dependencies for the generated code.
 * 
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
     * Returns {@code true} if {@code s} is either {@code null}, the empty string or a string that
     * only contains whitespace, otherwise {@code false}.
     */
    public static final boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

}
