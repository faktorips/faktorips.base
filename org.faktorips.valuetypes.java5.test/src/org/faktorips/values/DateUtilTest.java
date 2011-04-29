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

package org.faktorips.values;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.GregorianCalendar;

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
    public void testDatetoIsoDateString() {
        assertEquals("", DateUtil.dateToIsoDateString(null));
        Date date = new GregorianCalendar(2005, 8, 9).getTime();
        assertEquals("2005-09-09", DateUtil.dateToIsoDateString(date));
        date = new GregorianCalendar(2005, 9, 10).getTime();
        assertEquals("2005-10-10", DateUtil.dateToIsoDateString(date));
    }

    @Test
    public void testParseIsoDateStringToGregorianCalendar() {
        assertNull(DateUtil.parseIsoDateStringToGregorianCalendar(""));
        GregorianCalendar date = new GregorianCalendar(2005, 8, 9);
        assertEquals(date, DateUtil.parseIsoDateStringToGregorianCalendar("2005-09-09"));
        assertEquals(date, DateUtil.parseIsoDateStringToGregorianCalendar("2005-9-9"));
        date = new GregorianCalendar(2005, 9, 10);
        assertEquals(date, DateUtil.parseIsoDateStringToGregorianCalendar("2005-10-10"));
    }

    @Test
    public void testParseIsoDateStringToDate() {
        assertNull(DateUtil.parseIsoDateStringToDate(""));
        Date date = new GregorianCalendar(2005, 8, 9).getTime();
        assertEquals(date, DateUtil.parseIsoDateStringToDate("2005-09-09"));
        assertEquals(date, DateUtil.parseIsoDateStringToDate("2005-9-9"));
        date = new GregorianCalendar(2005, 9, 10).getTime();
        assertEquals(date, DateUtil.parseIsoDateStringToDate("2005-10-10"));
    }

}
