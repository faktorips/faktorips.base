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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.GregorianCalendar;

import org.faktorips.valuetypes.joda.util.JodaUtil;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.MonthDay;
import org.junit.jupiter.api.Test;

public class JodaUtilTest {
    @Test
    public void testToLocalDate() {
        assertNull(JodaUtil.toLocalDate(null));
        assertNull(JodaUtil.toLocalDate(""));

        assertEquals(new LocalDate(2010, 4, 10), JodaUtil.toLocalDate("2010-04-10"));
    }

    @Test
    public void testToStringLocalDate() {
        assertEquals("", JodaUtil.toString((LocalDate)null));
        assertEquals("2010-04-10", JodaUtil.toString(new LocalDate(2010, 4, 10)));
    }

    @Test
    public void testToGregorianCalendarLocalDate() {
        assertEquals(null, JodaUtil.toGregorianCalendar((LocalDate)null));
        assertEquals(new GregorianCalendar(2010, 3, 10), JodaUtil.toGregorianCalendar(new LocalDate(2010, 4, 10)));
    }

    @Test
    public void testToLocalTime() {
        assertNull(JodaUtil.toLocalTime(null));
        assertNull(JodaUtil.toLocalTime(""));

        assertEquals(new LocalTime(8, 4, 10), JodaUtil.toLocalTime("08:04:10"));
    }

    @Test
    public void testToStringLocalTime() {
        assertEquals("", JodaUtil.toString((LocalTime)null));
        assertEquals("08:04:10", JodaUtil.toString(new LocalTime(8, 4, 10)));
    }

    @Test
    public void testToLocalDateTime() {
        assertNull(JodaUtil.toLocalDateTime(null));
        assertNull(JodaUtil.toLocalDateTime(""));

        assertEquals(new LocalDateTime(2010, 3, 5, 8, 4, 10), JodaUtil.toLocalDateTime("2010-03-05T08:04:10"));
    }

    @Test
    public void testToLocalDateTime_WrongISOFormatWithMissingT() {
        assertThrows(IllegalArgumentException.class, () -> {
            JodaUtil.toLocalDateTime("2010-03-05 08:04:10");
        });
    }

    @Test
    public void testToLocalDateTime_NonDateTimeString() {
        assertThrows(IllegalArgumentException.class, () -> {
            JodaUtil.toLocalDateTime("not a date time");
        });
    }

    @Test
    public void testToStringLocalDateTime() {
        assertEquals("", JodaUtil.toString((LocalDateTime)null));
        assertEquals("2010-03-05T08:04:10", JodaUtil.toString(new LocalDateTime(2010, 3, 5, 8, 4, 10)));
    }

    @Test
    public void testToGregorianCalendarLocalDateTime() {
        assertEquals(null, JodaUtil.toGregorianCalendar((LocalDateTime)null));
        assertEquals(new GregorianCalendar(2010, 2, 5, 8, 4, 10),
                JodaUtil.toGregorianCalendar(new LocalDateTime(2010, 3, 5, 8, 4, 10)));
    }

    @Test
    public void testToMonthDay() {
        assertNull(JodaUtil.toMonthDay(null));
        assertNull(JodaUtil.toMonthDay(""));
        assertEquals(new MonthDay(1, 4), JodaUtil.toMonthDay("--01-04"));
    }

    @Test
    public void testToMonthDay_MissingLeadingDashes() {
        assertThrows(IllegalArgumentException.class, () -> {
            JodaUtil.toMonthDay("01-04");
        });
    }

    @Test
    public void testToMonthDay_AmericanStyle() {
        assertThrows(IllegalArgumentException.class, () -> {
            JodaUtil.toMonthDay("01/04/");
        });
    }

    @Test
    public void testToMonthDay_GermanStyle() {
        assertThrows(IllegalArgumentException.class, () -> {
            JodaUtil.toMonthDay("04.01.");
        });
    }

    @Test
    public void testToMonthDay_InvalidDayAndMonth() {
        assertThrows(IllegalArgumentException.class, () -> {
            JodaUtil.toMonthDay("--86-98");
        });
    }

}
