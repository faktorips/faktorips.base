package org.faktorips.devtools.stdbuilder.dthelper;

import org.apache.commons.lang.StringUtils;

/**
 * Supported variants of helpers for local date datatypes (
 * {@link org.faktorips.datatype.joda.LocalDateDatatype LocalDateDatatype} etc.).
 */
public enum LocalDateHelperVariant {
    JODA,
    JAVA8;

    public static LocalDateHelperVariant fromString(String s) {
        if (StringUtils.equalsIgnoreCase(JAVA8.name(), s)) {
            return JAVA8;
        }
        return JODA;
    }
}