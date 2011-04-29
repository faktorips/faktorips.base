/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal;

import java.io.Serializable;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * Represents date and time information like 2006-01-01 10:00pm independent of the time zone. To
 * convert a date and time object to a point in time (represented in the Java class libraries by
 * Date and Calendar) a time zone has to be provided in the conversion method toDate().
 * 
 * @see java.util.Date
 * @see java.util.GregorianCalendar
 * @see java.util.TimeZone
 * 
 * @author Jan Ortmann
 */
public class DateTime implements Comparable<DateTime>, Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 908669872768116989L;

    /**
     * Parses the given String s to a DateTime object. The string should have the ISO date format
     * (YYYY-MM-DD). Time information is initialized with 0. Returns <code>null</code> if s if
     * <code>null</code> or an empty String.
     * 
     * @throws IllegalArgumentException if s has a wrong format and can't be parsed.
     */
    public final static DateTime parseIso(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        try {
            StringTokenizer tokenizer = new StringTokenizer(s, "-");
            int year = Integer.parseInt(tokenizer.nextToken());
            int month = Integer.parseInt(tokenizer.nextToken());
            int date = Integer.parseInt(tokenizer.nextToken());
            return new DateTime(year, month, date);
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't parse " + s + " to a DateTime!");
        }
    }

    /**
     * Creates a new date time object with the year, month and day information from the
     * GregorianCalendar. Time information is initialized with 0. Returns <code>null</code> if
     * calendar is<code>null</code>.
     */
    public final static DateTime createDateOnly(GregorianCalendar calendar) {
        if (calendar == null) {
            return null;
        }
        int year = calendar.get(GregorianCalendar.YEAR);
        int month = calendar.get(GregorianCalendar.MONTH) + 1;
        int date = calendar.get(GregorianCalendar.DAY_OF_MONTH);
        return new DateTime(year, month, date);
    }

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;

    private int hashCode;

    public DateTime(int year, int month, int day) {
        this(year, month, day, 0, 0, 0);
    }

    public DateTime(int year, int month, int day, int hour, int minute, int second) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        hashCode = year + 17 * month + 34 * day + hour + 41 * hour;

    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }

    public int getSecond() {
        return second;
    }

    public long toTimeInMillisecs(TimeZone zone) {
        return toGregorianCalendar(zone).getTimeInMillis();
    }

    public Date toDate(TimeZone zone) {
        return toGregorianCalendar(zone).getTime();
    }

    public GregorianCalendar toGregorianCalendar(TimeZone zone) {
        GregorianCalendar cal = new GregorianCalendar(zone);
        cal.set(year, month - 1, day, hour, minute, second);
        cal.set(GregorianCalendar.MILLISECOND, 0);
        return cal;
    }

    public String toIsoFormat() {
        return year + "-" + (month < 10 ? "0" + month : "" + month) + "-" + (day < 10 ? "0" + day : "" + day);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DateTime)) {
            return false;
        }
        DateTime other = (DateTime)o;
        return year == other.year && month == other.month && day == other.day && hour == other.hour
                && minute == other.minute && second == other.second;
    }

    @Override
    public String toString() {
        return toIsoFormat() + ' ' + hour + ':' + minute + ':' + second;
    }

    public int compareTo(DateTime other) {
        if (year != other.year) {
            return year - other.year;
        }
        if (month != other.month) {
            return month - other.month;
        }
        if (day != other.day) {
            return day - other.day;
        }
        if (hour != other.hour) {
            return hour - other.hour;
        }
        if (minute != other.minute) {
            return minute - other.minute;
        }
        return second - other.second;
    }

}
