/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

/**
 * A collection of utility methods for Strings. We don't use a class library like apache-commons
 * here to minimize the dependencies for the generated code.
 * 
 * @author Jan Ortmann
 */
public class StringUtils {

    public static final String EMPTY = "";

    private StringUtils() {
        // Utility class not to be instantiated.
    }

    /**
     * Returns <code>true</code> if s is either null or the empty string, otherwise
     * <code>false</code>.
     */
    public static final boolean isEmpty(String s) {
        return s == null || EMPTY.equals(s.trim());
    }

}
