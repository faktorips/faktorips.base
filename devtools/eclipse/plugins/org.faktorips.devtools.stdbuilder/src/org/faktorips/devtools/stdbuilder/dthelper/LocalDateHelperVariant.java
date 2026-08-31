/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.dthelper;

import org.apache.commons.lang3.Strings;

/**
 * Supported variants of helpers for local date datatypes (
 * {@link org.faktorips.datatype.joda.LocalDateDatatype LocalDateDatatype} etc.).
 */
public enum LocalDateHelperVariant {
    JODA,
    JAVA8;

    public static LocalDateHelperVariant fromString(String s) {
        if (Strings.CI.equals(JAVA8.name(), s)) {
            return JAVA8;
        }
        return JODA;
    }
}
