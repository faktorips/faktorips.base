/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.util;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.faktorips.datatype.NumericDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.BigDecimalDatatype;
import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.datatype.classtypes.GregorianCalendarDatatype;
import org.faktorips.datatype.classtypes.StringDatatype;

/**
 * A utility class for persistence (validation of table names, ..).
 * 
 * @author Roman Grutza
 */
public class PersistenceUtil {

    private static final Pattern NAME_PATTERN = Pattern.compile("[_\\p{Alpha}]\\w*");

    /**
     * Checks if given String is a valid table or column name.
     * <p/>
     * The name must start with an ASCII character followed by more optional ASCII-characters,
     * numbers or the underscore character.
     * 
     * @param name The String to check
     * @return <code>false</code> if it empty or <code>null</code> or contains invalid characters,
     *         <code>true</code> otherwise
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
        } else if (valueDatatype instanceof BigDecimalDatatype) {
            // TODO Joerg ausbauen wenn BigDecimalDatatype .. implements NumericDatatype
            return true;
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
