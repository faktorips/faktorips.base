/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util;

import java.util.GregorianCalendar;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.MonthDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Helper class for working with Joda-Time's {@link LocalDate}, {@link LocalTime} and
 * {@link LocalDateTime}.
 */
public class JodaUtil {

    private static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormat.forPattern("--MM-dd");

    private static final DateTimeFormatter LOCAL_DATE_FORMATTER = ISODateTimeFormat.date();

    private static final DateTimeFormatter LOCAL_TIME_FORMATTER = new DateTimeFormatterBuilder().appendHourOfDay(2)
            .appendLiteral(':').appendMinuteOfHour(2).appendLiteral(':').appendSecondOfMinute(2).toFormatter();

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .append(LOCAL_DATE_FORMATTER).appendLiteral('T').append(LOCAL_TIME_FORMATTER).toFormatter();

    private JodaUtil() {
        // do not instantiate
    }

    /**
     * Parses the given {@link String} and returns a {@link LocalDate}.
     * 
     * @param dateInIsoFormat Date in ISO format YYYY-MM-DD
     * 
     * @throws IllegalArgumentException if the String can't be parsed
     */
    public static final LocalDate toLocalDate(String dateInIsoFormat) {
        if (dateInIsoFormat == null || dateInIsoFormat.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateInIsoFormat, LOCAL_DATE_FORMATTER);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Date must be in ISO format 'YYYY-MM-DD'.", e); //$NON-NLS-1$
        }
    }

    /**
     * Formats a given {@link LocalDate} as a {@link String} in ISO format YYYY-MM-DD.
     */
    public static final String toString(LocalDate date) {
        if (date == null) {
            return ""; //$NON-NLS-1$
        }
        return LOCAL_DATE_FORMATTER.print(date);
    }

    /**
     * Parses the given {@link String} and returns a {@link LocalTime}.
     * 
     * @param time Time in ISO format hh:mm:ss
     * 
     * @throws IllegalArgumentException if the String can't be parsed
     */
    public static final LocalTime toLocalTime(String time) {
        if (time == null || time.isEmpty()) {
            return null;
        }
        try {
            return LocalTime.parse(time, LOCAL_TIME_FORMATTER);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Time must be in ISO format 'hh:mm:ss'.", e); //$NON-NLS-1$
        }
    }

    /**
     * Formats a given {@link LocalTime} as a {@link String} in ISO format hh:mm:ss.
     */
    public static final String toString(LocalTime time) {
        if (time == null) {
            return ""; //$NON-NLS-1$
        }
        return LOCAL_TIME_FORMATTER.print(time);
    }

    /**
     * Parses the given {@link String} and returns a {@link LocalDateTime}.
     * 
     * @param dateTime Date and time in ISO format YYYY-MM-DD hh:mm:ss
     * 
     * @throws IllegalArgumentException if the String can't be parsed
     */
    public static final LocalDateTime toLocalDateTime(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTime, LOCAL_DATE_TIME_FORMATTER);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Date/time must be in ISO format 'YYYY-MM-DDThh:mm:ss'.", e); //$NON-NLS-1$
        }
    }

    /**
     * Parses the given {@link String} and returns a {@link MonthDay}. Returns null if the string is
     * {@code null} or empty.
     * 
     * @param monthDay the month day in ISO format, e.g. --03-04
     * 
     * @throws IllegalArgumentException if the String can't be parsed
     */
    public static final MonthDay toMonthDay(String monthDay) {
        if (monthDay == null || monthDay.isEmpty()) {
            return null;
        }
        try {
            return MonthDay.parse(monthDay, MONTH_DAY_FORMATTER);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Month day must be in ISO format '--MM-dd'", e); //$NON-NLS-1$
        }
    }

    /**
     * Formats a given {@link LocalDateTime} as a {@link String} in ISO format YYYY-MM-DD hh:mm:ss.
     */
    public static final String toString(LocalDateTime dateTime) {
        if (dateTime == null) {
            return ""; //$NON-NLS-1$
        }
        return LOCAL_DATE_TIME_FORMATTER.print(dateTime);
    }

    /**
     * Converts a {@link LocalDate} to a {@link GregorianCalendar}
     */
    public static final GregorianCalendar toGregorianCalendar(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        GregorianCalendar calendar = (GregorianCalendar)GregorianCalendar.getInstance();
        calendar.setTime(localDate.toDate());
        return calendar;
    }

    /**
     * Converts a {@link LocalDateTime} to a {@link GregorianCalendar}
     */
    public static final GregorianCalendar toGregorianCalendar(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        GregorianCalendar calendar = (GregorianCalendar)GregorianCalendar.getInstance();
        calendar.setTime(localDateTime.toDate());
        return calendar;
    }

}
