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

package org.faktorips.values;

import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

public class DateUtilTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGregorianCalendarToIsoDateString() {
        assertEquals("", DateUtil.gregorianCalendarToIsoDateString(null));
        GregorianCalendar date = new GregorianCalendar(2005, 8, 9);
        assertEquals("2005-09-09", DateUtil.gregorianCalendarToIsoDateString(date));
        date = new GregorianCalendar(2005, 9, 10);
        assertEquals("2005-10-10", DateUtil.gregorianCalendarToIsoDateString(date));
    }

    public void testIsIsoDate() {
        assertTrue(DateUtil.isIsoDate("2008-01-01"));
        assertFalse(DateUtil.isIsoDate("01-01-2008"));
        assertFalse(DateUtil.isIsoDate("2008-01-01 "));
        assertFalse(DateUtil.isIsoDate(" 2008-01-01"));
        assertFalse(DateUtil.isIsoDate("01.01.2008"));
        assertFalse(DateUtil.isIsoDate(null));
    }

    public void testDatetoIsoDateString() {
        assertEquals("", DateUtil.dateToIsoDateString(null));
        Date date = new GregorianCalendar(2005, 8, 9).getTime();
        assertEquals("2005-09-09", DateUtil.dateToIsoDateString(date));
        date = new GregorianCalendar(2005, 9, 10).getTime();
        assertEquals("2005-10-10", DateUtil.dateToIsoDateString(date));
    }

    public void testParseIsoDateStringToGregorianCalendar() {
        assertNull(DateUtil.parseIsoDateStringToGregorianCalendar(""));
        GregorianCalendar date = new GregorianCalendar(2005, 8, 9);
        assertEquals(date, DateUtil.parseIsoDateStringToGregorianCalendar("2005-09-09"));
        assertEquals(date, DateUtil.parseIsoDateStringToGregorianCalendar("2005-9-9"));
        date = new GregorianCalendar(2005, 9, 10);
        assertEquals(date, DateUtil.parseIsoDateStringToGregorianCalendar("2005-10-10"));
    }

    public void testParseIsoDateStringToDate() {
        assertNull(DateUtil.parseIsoDateStringToDate(""));
        Date date = new GregorianCalendar(2005, 8, 9).getTime();
        assertEquals(date, DateUtil.parseIsoDateStringToDate("2005-09-09"));
        assertEquals(date, DateUtil.parseIsoDateStringToDate("2005-9-9"));
        date = new GregorianCalendar(2005, 9, 10).getTime();
        assertEquals(date, DateUtil.parseIsoDateStringToDate("2005-10-10"));
    }

}
