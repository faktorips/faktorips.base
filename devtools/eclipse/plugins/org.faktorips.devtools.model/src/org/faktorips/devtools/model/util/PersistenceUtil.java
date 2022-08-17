/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.util;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;
import org.faktorips.datatype.classtypes.StringDatatype;

/**
 * A utility class for persistence (validation of table names, ...).
 * 
 * @author Roman Grutza
 */
public class PersistenceUtil {

    private static final Pattern NAME_PATTERN = Pattern.compile("[_\\p{Alpha}]\\w*"); //$NON-NLS-1$

    private PersistenceUtil() {
        // Utility class not to be instantiated.
    }

    /**
     * Checks if given String is a valid table or column name.
     * <p>
     * The name must start with an ASCII character followed by more optional ASCII-characters,
     * numbers or the underscore character.
     * <p>
     * Returns <code>false</code> if it empty or <code>null</code> or contains invalid characters,
     * <code>true</code> otherwise.
     * 
     * @param name The string to check.
     */
    public static boolean isValidDatabaseIdentifier(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isSupportingDecimalPlaces(ValueDatatype valueDatatype) {
        if (valueDatatype instanceof NumericDatatype) {
            return ((NumericDatatype)valueDatatype).hasDecimalPlaces();
        }
        return false;
    }

    public static boolean isSupportingTemporalType(ValueDatatype valueDatatype) {
        return valueDatatype instanceof GregorianCalendarDatatype || valueDatatype instanceof DateDatatype;
    }

    public static boolean isSupportingLenght(ValueDatatype valueDatatype) {
        return valueDatatype instanceof StringDatatype;
    }

}
