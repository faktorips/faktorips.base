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

package org.faktorips.datatype;

import java.util.GregorianCalendar;

/**
 * Static utility methods for timed enum datatypes.
 * 
 * @see TimedEnumDatatype
 * 
 * @author Jan Ortmann
 */
public class TimedEnumDatatypeUtil {

    public enum ValidityCheck {
        WHOLE_PERIOD,
        SOME_TIME_OF_THE_PERIOD
    }

    /**
     * Returns <code>true</code> if the given value is valid in the given period (defined by begin
     * and end), otherwise <code>false</code>.
     * 
     * @param datatype The timed enum datatype the "value is an instance" of.
     * @param value The value to check.
     * @param begin Begin of the period to check. <null> means unlimited.
     * @param end End of the period to check <null> means unlimited.
     * @param check Flag whether the value must be valid in the whole period or only some time of
     *            the period.
     */
    public static final boolean isValid(TimedEnumDatatype datatype,
            String value,
            GregorianCalendar begin,
            GregorianCalendar end,
            ValidityCheck check) {

        if (check == ValidityCheck.WHOLE_PERIOD) {
            return isValidForWholePeriod(datatype, value, begin, end);
        } else {
            return isValidForSomeTimeInThePeriod(datatype, value, begin, end);
        }
    }

    /**
     * Returns <code>true</code> if the given value is valid for the whole period (defined by begin
     * and end), otherwise <code>false</code>.
     * 
     * @param datatype The timed enum datatype the "value is an instance" of.
     * @param value The value to check.
     * @param begin Begin of the period to check. <null> means unlimited.
     * @param end End of the period to check <null> means unlimited.
     */
    public static final boolean isValidForWholePeriod(TimedEnumDatatype datatype,
            String value,
            GregorianCalendar begin,
            GregorianCalendar end) {

        GregorianCalendar valueValidFrom = datatype.getValidFrom(value);
        if (valueValidFrom != null) {
            if (begin == null || valueValidFrom.after(begin)) {
                return false;
            }
        }
        GregorianCalendar valueValidTo = datatype.getValidTo(value);
        if (valueValidTo != null) {
            if (end == null || valueValidTo.before(end)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if the given value is valid for the some time in the period
     * (defined by begin and end), otherwise <code>false</code>.
     * 
     * @param datatype The timed enum datatype the "value is an instance" of.
     * @param value The value to check.
     * @param begin Begin of the period to check. <null> means unlimited.
     * @param end End of the period to check <null> means unlimited.
     */
    public static final boolean isValidForSomeTimeInThePeriod(TimedEnumDatatype datatype,
            String value,
            GregorianCalendar begin,
            GregorianCalendar end) {

        GregorianCalendar valueValidFrom = datatype.getValidFrom(value);
        if (valueValidFrom != null) {
            if (valueValidFrom.after(end)) {
                return false;
            }
        }
        GregorianCalendar valueValidTo = datatype.getValidTo(value);
        if (valueValidTo != null) {
            if (valueValidTo.before(begin)) {
                return false;
            }
        }
        return true;
    }

}
