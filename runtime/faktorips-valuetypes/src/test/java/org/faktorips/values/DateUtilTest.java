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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

public class DateUtilTest {

    @Test
    public void testGregorianCalendarToIsoDateString() {
        assertEquals("", DateUtil.gregorianCalendarToIsoDateString(null));
        GregorianCalendar date = new GregorianCalendar(2005, 8, 9);
        assertEquals("2005-09-09", DateUtil.gregorianCalendarToIsoDateString(date));
        date = new GregorianCalendar(2005, 9, 10);
        assertEquals("2005-10-10", DateUtil.gregorianCalendarToIsoDateString(date));
    }

    @Test
    public void testIsIsoDate() {
        assertTrue(DateUtil.isIsoDate("2008-01-01"));
        assertFalse(DateUtil.isIsoDate("01-01-2008"));
        assertFalse(DateUtil.isIsoDate("2008-01-01 "));
        assertFalse(DateUtil.isIsoDate(" 2008-01-01"));
        assertFalse(DateUtil.isIsoDate("01.01.2008"));
        assertFalse(DateUtil.isIsoDate(null));
    }

    @Test
    public void testIsIsoDateInvalidInput() {
        Date test = DateUtil.parseIsoDateStringToDate("2008-13-35");
        assertTrue(test.toString().contains("Feb"));
        assertTrue(test.toString().contains("4"));
        assertTrue(test.toString().contains("2009"));
    }

    @Test
    public void testIsIsoTime() {
        assertFalse(DateUtil.isIsoTime(null));
        assertFalse(DateUtil.isIsoTime("1:2:3"));
        assertFalse(DateUtil.isIsoTime("24:00:00"));
        assertFalse(DateUtil.isIsoTime("00:60:00"));
        assertFalse(DateUtil.isIsoTime("00:00:60"));
        assertTrue(DateUtil.isIsoTime("00:00:00"));
        assertTrue(DateUtil.isIsoTime("01:02:03"));
        assertTrue(DateUtil.isIsoTime("20:00:00"));
        assertTrue(DateUtil.isIsoTime("20:10:10"));
        assertTrue(DateUtil.isIsoTime("20:59:59"));
        assertTrue(DateUtil.isIsoTime("23:59:59"));
    }

    @Test
    public void tesIsIsoDateTime() {
        assertFalse(DateUtil.isIsoDateTime(null));
        assertFalse(DateUtil.isIsoDateTime("2008-01-01T1:2:3"));
        assertFalse(DateUtil.isIsoDateTime("01-01-2008T23:59:59"));
        assertFalse(DateUtil.isIsoDateTime("2008-01-01 00:00:00"));
        assertTrue(DateUtil.isIsoDateTime("2008-01-01T00:00:00"));
        assertTrue(DateUtil.isIsoDateTime("2015-10-06T20:01:00"));
    }

    @Test
    public void testDatetoIsoDateString() {
        assertEquals("", DateUtil.dateToIsoDateString(null));
        Date date = new GregorianCalendar(2005, 8, 9).getTime();
        assertEquals("2005-09-09", DateUtil.dateToIsoDateString(date));
        date = new GregorianCalendar(2005, 9, 10).getTime();
        assertEquals("2005-10-10", DateUtil.dateToIsoDateString(date));
    }

    @Test
    public void testParseIsoDateStringToGregorianCalendar() {
        assertNull(DateUtil.parseIsoDateStringToGregorianCalendar(null));
        assertNull(DateUtil.parseIsoDateStringToGregorianCalendar(""));
        GregorianCalendar date = new GregorianCalendar(2005, 8, 9);
        assertEquals(date, DateUtil.parseIsoDateStringToGregorianCalendar("2005-09-09"));
        assertEquals(date, DateUtil.parseIsoDateStringToGregorianCalendar("2005-9-9"));
        date = new GregorianCalendar(2005, 9, 10);
        assertEquals(date, DateUtil.parseIsoDateStringToGregorianCalendar("2005-10-10"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseIsoDateStringToGregorianCalendar_InvalidFormat() {
        DateUtil.parseIsoDateStringToGregorianCalendar("10-10-10");
    }

    @Test
    public void testParseIsoDateStringToDate() {
        assertNull(DateUtil.parseIsoDateStringToDate(null));
        assertNull(DateUtil.parseIsoDateStringToDate(""));
        Date date = new GregorianCalendar(2005, 8, 9).getTime();
        assertEquals(date, DateUtil.parseIsoDateStringToDate("2005-09-09"));
        assertEquals(date, DateUtil.parseIsoDateStringToDate("2005-9-9"));
        date = new GregorianCalendar(2005, 9, 10).getTime();
        assertEquals(date, DateUtil.parseIsoDateStringToDate("2005-10-10"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseIsoDateStringToDate_InvalidFormat() {
        DateUtil.parseIsoDateStringToDate("10-10-10");
    }

    @Test
    public void testParseIsoDateStringToDate_WithMoreThreads() {
        Thread t1 = new Thread(new Task());
        Thread t2 = new Thread(new Task());

        t1.start();
        t2.start();
    }

    @Test
    public void testParseIsoDateTimeStringToDate() {
        assertNull(DateUtil.parseIsoDateTimeStringToDate(null));
        assertNull(DateUtil.parseIsoDateTimeStringToDate(""));
        Date date = new GregorianCalendar(2005, 8, 9, 1, 2, 3).getTime();
        assertEquals(date, DateUtil.parseIsoDateTimeStringToDate("2005-09-09T01:02:03"));
        assertEquals(date, DateUtil.parseIsoDateTimeStringToDate("2005-9-9T01:02:03"));
        date = new GregorianCalendar(2005, 9, 10, 1, 2, 3).getTime();
        assertEquals(date, DateUtil.parseIsoDateTimeStringToDate("2005-10-10T01:02:03"));
        date = new GregorianCalendar(2005, 9, 10, 20, 2, 3).getTime();
        assertEquals(date, DateUtil.parseIsoDateTimeStringToDate("2005-10-10T20:02:03"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParseIsoDateTimeStringToDate_InvalidFormat() {
        DateUtil.parseIsoDateTimeStringToDate("10-10-10T20:02:03");
    }

    @Test
    public void testParseIsoTimeStringToDate() {
        assertNull(DateUtil.parseIsoTimeStringToDate(""));
        Date date = new GregorianCalendar(1970, 0, 1, 1, 2, 3).getTime();
        assertEquals(date, DateUtil.parseIsoTimeStringToDate("01:02:03"));
        assertEquals(date, DateUtil.parseIsoTimeStringToDate("01:02:03"));
    }

    @Test
    public void testGetDifferenceInYearsCalendar() {
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        assertEquals(0, DateUtil.getDifferenceInYears(start, end));
        start.set(1980, 0, 1);
        end.set(2012, 1, 29);
        assertEquals(32, DateUtil.getDifferenceInYears(start, end));
        start.set(1980, 2, 1);
        assertEquals(31, DateUtil.getDifferenceInYears(start, end));
        start.set(1980, 1, 29);
        assertEquals(32, DateUtil.getDifferenceInYears(start, end));
        start.set(1979, 1, 28);
        assertEquals(33, DateUtil.getDifferenceInYears(start, end));
        start.set(1979, 2, 1);
        assertEquals(32, DateUtil.getDifferenceInYears(start, end));
        end.set(1960, 10, 11);
        assertEquals(18, DateUtil.getDifferenceInYears(start, end));
        start.set(1980, 1, 29);
        end.set(2011, 1, 28);
        assertEquals(30, DateUtil.getDifferenceInYears(start, end));
        end.set(2011, 2, 1);
        assertEquals(31, DateUtil.getDifferenceInYears(start, end));

        start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        assertEquals(0, DateUtil.getDifferenceInYears(start, end));
        start.set(1980, 0, 1);
        end.set(2012, 1, 29);
        assertEquals(32, DateUtil.getDifferenceInYears(start, end));
        start.set(1980, 2, 1);
        assertEquals(31, DateUtil.getDifferenceInYears(start, end));
        start.set(1980, 1, 29);
        assertEquals(32, DateUtil.getDifferenceInYears(start, end));
    }

    @Test
    public void testGetDifferenceInYearsDate() {
        Calendar start = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Calendar end = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        assertEquals(0, DateUtil.getDifferenceInYears(start.getTime(), end.getTime()));
        start.set(1980, 0, 1);
        end.set(2012, 1, 29);
        assertEquals(32, DateUtil.getDifferenceInYears(start.getTime(), end.getTime()));
        start.set(1980, 2, 1);
        assertEquals(31, DateUtil.getDifferenceInYears(start.getTime(), end.getTime()));
        start.set(1980, 1, 29);
        assertEquals(32, DateUtil.getDifferenceInYears(start.getTime(), end.getTime()));
        end.set(1960, 10, 11);
        assertEquals(19, DateUtil.getDifferenceInYears(start.getTime(), end.getTime()));

        start = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        end = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        assertEquals(0, DateUtil.getDifferenceInYears(start.getTime(), end.getTime()));
        start.set(1980, 0, 1);
        end.set(2012, 1, 29);
        assertEquals(32, DateUtil.getDifferenceInYears(start.getTime(), end.getTime()));
        start.set(1980, 2, 1);
        assertEquals(31, DateUtil.getDifferenceInYears(start.getTime(), end.getTime()));
        start.set(1980, 1, 29);
        assertEquals(32, DateUtil.getDifferenceInYears(start.getTime(), end.getTime()));
    }

    @Test
    public void testDateToIsoDateTimeString() {
        assertEquals("", DateUtil.dateToIsoDateTimeString(null));
        Date date = new GregorianCalendar(2009, 0, 1, 0, 0, 0).getTime();
        assertEquals("2009-01-01T00:00:00", DateUtil.dateToIsoDateTimeString(date));
    }

    private static class Task implements Runnable {

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                Date date = new GregorianCalendar(2005, 8, 9).getTime();
                assertEquals(date, DateUtil.parseIsoDateStringToDate("2005-09-09"));
            }
        }
    }
}
