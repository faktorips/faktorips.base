/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.util;

import org.faktorips.annotation.UtilityClass;

@UtilityClass
public class IntegerUtils {

    // Utility class that should not be instantiated
    private IntegerUtils() {
        super();
    }

    /**
     * Compares two ints.
     *
     * @param x the first {@code int} to compare
     * @param y the second {@code int} to compare
     * @return the value {@code 0} if {@code x == y}; a value less than {@code 0} if {@code x < y};
     *         and a value greater than {@code 0} if {@code x > y}
     */
    public static int compare(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }
}
