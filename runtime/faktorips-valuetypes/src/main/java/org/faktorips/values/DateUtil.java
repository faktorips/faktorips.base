/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.values;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * A collection of utility methods for Date to String and String to Date handling. Strings are
 * expected to be in ISO format yyyy-mm-dd (or any single-digit m or d). Generated Strings are
 * returned in the same format.
 * 
 * @author Thorsten Waertel
 */
public class DateUtil {

    private static final ThreadLocal<SimpleDateFormat> ISO_DATE_TIME_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        }
    };

    private static final ThreadLocal<SimpleDateFormat> ISO_TIME_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm:ss");
        }
    };

    private static final ThreadLocal<SimpleDateFormat> ISO_DATE_FORMAT = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yy-MM-dd");
        }
    };

    private static final String REGEX_ISO_DATE = "\\d{4}-\\d{1,2}-\\d{1,2}";
    private static final String REGEX_ISO_TIME = "([01]\\d|[2][0-3]):([0-5]\\d):([0-5]\\d)";
    private static final Pattern PATERN_ISO_DATE = Pattern.compile("^" + REGEX_ISO_DATE + "$");
    private static final Pattern PATERN_ISO_TIME = Pattern.compile("^" + REGEX_ISO_TIME + "$");
    private static final Pattern PATERN_ISO_DATE_TIME = Pattern.compile("^" + REGEX_ISO_DATE + "T" + REGEX_ISO_TIME
            + "$");

    private DateUtil() {
    }

    /**
     * Creates an ISO String of the date the given GregorianCalendar is set to or an empty String if
     * calendar is <code>null</code>.
     */
    public static final String gregorianCalendarToIsoDateString(GregorianCalendar calendar) {
        if (calendar == null) {
            return "";
        }
        return String.format("%tF", calendar);
    }

    /**
     * Creates an ISO date String of the given Date or an empty String if date is <code>null</code>.
     */
    public static final String dateToIsoDateString(Date date) {
        if (date == null) {
            return "";
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return gregorianCalendarToIsoDateString(calendar);
    }

    /**
     * Creates an ISO date/time String of the given Date or an empty String if date is
     * <code>null</code>.
     */
    public static final String dateToIsoDateTimeString(Date date) {
        if (date == null) {
            return "";
        }
        return String.format("%1$tFT%1$tT", date);
    }

    /**
     * Creates an ISO time String of the given Date or an empty String if date is <code>null</code>.
     */
    public static final String dateToIsoTimeString(Date date) {
        if (date == null) {
            return "";
        }
        return String.format("%tT", date);
    }

    /**
     * Parses the given ISO-formatted date String to a Gregorian calendar.
     */
    public static final GregorianCalendar parseIsoDateStringToGregorianCalendar(String s) {
        if (s == null || "".equals(s)) {
            return null;
        }
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(parseIsoDateStringToDate(s));
        return calendar;
    }

    /**
     * Returns <code>true</code> if the String value is conform to the ISO-Date standard
     * (YYYY-MM-DD), otherwise <code>false</code>.
     */
    public static final boolean isIsoDate(String value) {
        if (value == null) {
            return false;
        }
        return PATERN_ISO_DATE.matcher(value).matches();
    }

    /**
     * Returns <code>true</code> if the String value is conform to the ISO-Time standard (HH:MM:SS),
     * otherwise <code>false</code>.
     */
    public static final boolean isIsoTime(String value) {
        if (value == null) {
            return false;
        }
        return PATERN_ISO_TIME.matcher(value).matches();
    }

    /**
     * Returns <code>true</code> if the String value is conform to the ISO-Date-Time standard
     * (YYYY-MM-DD HH:MM:SS), otherwise <code>false</code>.
     */
    public static final boolean isIsoDateTime(String value) {
        if (value == null) {
            return false;
        }
        return PATERN_ISO_DATE_TIME.matcher(value).matches();
    }

    /**
     * Parses the given ISO-formattted date String to a Date.
     */
    public static final Date parseIsoDateStringToDate(String s) {
        if (s == null || "".equals(s)) {
            return null;
        }

        if (!isIsoDate(s)) {
            throw new IllegalArgumentException("Can't parse " + s + " to a date!");
        }

        try {
            return ISO_DATE_FORMAT.get().parse(s);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Can't parse " + s + " to a date!");
        }
    }

    /**
     * Parses the given ISO-formattted date/time String to a Date.
     */
    public static final Date parseIsoDateTimeStringToDate(String s) {
        if (s == null || "".equals(s)) {
            return null;
        }
        if (!isIsoDateTime(s)) {
            throw new IllegalArgumentException("Can't parse " + s + " to a date and time!");
        }

        try {
            return ISO_DATE_TIME_FORMAT.get().parse(s);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Can't parse " + s + " to a date and time!");
        }
    }

    /**
     * Parses the given ISO-formattted time String to a Date.
     */
    public static final Date parseIsoTimeStringToDate(String s) {
        if (s == null || "".equals(s)) {
            return null;
        }
        try {
            return ISO_TIME_FORMAT.get().parse(s);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Can't parse " + s + " to a time!");
        }
    }

    /**
     * Returns the absolute difference between the given {@link Calendar} objects in full years with
     * accuracy of one day. Examples:
     * <ul>
     * <li>the difference between 1980-03-01 and 2012-02-29 is 31 years</li>
     * <li>the difference between 1980-01-01 and 2012-01-01 is 32 years</li>
     * <li>the difference between 1980-01-01 and 1960-11-11 is 19 years</li>
     * </ul>
     * 
     * @param start the start of the period
     * @param end the end of the period
     * @return the length of the period
     */
    public static final int getDifferenceInYears(Calendar start, Calendar end) {
        if (start == null) {
            throw new IllegalArgumentException("The start date may not be null");
        }
        if (end == null) {
            throw new IllegalArgumentException("The end date may not be null");
        }
        Calendar startCal = toGmtCalendar(start);
        Calendar endCal = toGmtCalendar(end);
        if (endCal.before(startCal)) {
            Calendar tmpCal = endCal;
            endCal = startCal;
            startCal = tmpCal;
        }
        int startYear = startCal.get(Calendar.YEAR);
        int endYear = endCal.get(Calendar.YEAR);
        int age = endYear - startYear;
        endCal.add(Calendar.YEAR, 0 - age);
        if (endCal.before(startCal)) {
            age--;
        }
        return age;
    }

    /**
     * Returns the absolute difference between the given {@link Date} objects in full years with
     * accuracy of one day. Examples:
     * <ul>
     * <li>the difference between 1980-03-01 and 2012-02-29 is 31 years</li>
     * <li>the difference between 1980-01-01 and 2012-01-01 is 32 years</li>
     * <li>the difference between 1980-01-01 and 1960-11-11 is 19 years</li>
     * </ul>
     * 
     * @param start the start of the period
     * @param end the end of the period
     * @return the length of the period
     */
    public static final int getDifferenceInYears(Date start, Date end) {
        if (start == null) {
            throw new IllegalArgumentException("The start date may not be null");
        }
        if (end == null) {
            throw new IllegalArgumentException("The end date may not be null");
        }
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(start);
        Calendar endCal = Calendar.getInstance();
        endCal.setTime(end);
        return getDifferenceInYears(startCal, endCal);
    }

    private static Calendar toGmtCalendar(Calendar calendar) {
        Calendar gmtCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        gmtCalendar.setTimeInMillis(0);
        gmtCalendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR));
        gmtCalendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH));
        gmtCalendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH));
        return gmtCalendar;
    }

}
