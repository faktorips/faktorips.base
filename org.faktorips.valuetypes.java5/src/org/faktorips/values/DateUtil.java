/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.values;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * A collection of utility methods for Date to String and String to Date handling. Strings are
 * expected to be in ISO format yyyy-mm-dd (or any single-digit m or d). Generated Strings are
 * returned in the same format.
 * 
 * @author Thorsten Waertel
 */
public class DateUtil {

    /**
     * Creates an ISO String of the date the given GregorianCalendar is set to or an empty String if
     * calendar is <code>null</code>.
     */
    public final static String gregorianCalendarToIsoDateString(GregorianCalendar calendar) {
        if (calendar == null) {
            return "";
        }
        int month = calendar.get(GregorianCalendar.MONTH) + 1;
        int date = calendar.get(GregorianCalendar.DATE);
        return calendar.get(GregorianCalendar.YEAR) + "-" + (month < 10 ? "0" + month : "" + month) + "-"
                + (date < 10 ? "0" + date : "" + date);
    }

    /**
     * Creates an ISO String of the given Date or an empty String if date is <code>null</code>.
     */
    public final static String dateToIsoDateString(Date date) {
        if (date == null) {
            return "";
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return gregorianCalendarToIsoDateString(calendar);
    }

    /**
     * Parses the given ISO-formatted date String to a Gregorian calendar.
     */
    public final static GregorianCalendar parseIsoDateStringToGregorianCalendar(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        try {
            StringTokenizer tokenizer = new StringTokenizer(s, "-");
            int year = Integer.parseInt(tokenizer.nextToken());
            int month = Integer.parseInt(tokenizer.nextToken());
            int date = Integer.parseInt(tokenizer.nextToken());
            return new GregorianCalendar(year, month - 1, date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't parse " + s + " to a date!");
        }
    }

    /**
     * Returns <code>true</code> if the String value is conform to the ISO standard (YYYY-MM-DD),
     * otherwise <code>false</code>.
     */
    public final static boolean isIsoDate(String value) {
        if (value == null) {
            return false;
        }
        String regex = "^\\d{4}-\\d{1,2}-\\d{1,2}$";
        return Pattern.matches(regex, value);
    }

    /**
     * Parses the given ISO-formattted date String to a Date.
     */
    public final static Date parseIsoDateStringToDate(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        return parseIsoDateStringToGregorianCalendar(s).getTime();
    }

}
