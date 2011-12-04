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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

public class DateTimeTest {

    @Test
    public void testToDate() {
        DateTime dt = new DateTime(2005, 5, 1);
        GregorianCalendar cal = new GregorianCalendar(2005, 4, 1);
        Date date = cal.getTime();
        assertEquals(date, dt.toDate(cal.getTimeZone()));
    }

    @Test
    public void testToGregorianCalendar() {
        DateTime dt = new DateTime(2005, 5, 1);
        GregorianCalendar cal = new GregorianCalendar(2005, 4, 1);
        assertEquals(cal, dt.toGregorianCalendar(cal.getTimeZone()));
    }

    @Test
    public void testCreateDateOnly() {
        GregorianCalendar cal = new GregorianCalendar(2005, 4, 1);
        assertEquals(new DateTime(2005, 5, 1), DateTime.createDateOnly(cal));
        assertNull(DateTime.createDateOnly(null));
    }

    @Test
    public void testToString() {
        DateTime dt = new DateTime(2005, 5, 1, 10, 10, 10);
        assertEquals("2005-05-01 10:10:10", dt.toString());
    }

    @Test
    public void testToString_PrefixTimeComponentsLessThan10With0() {
        DateTime dt = new DateTime(2005, 5, 1, 9, 9, 9);
        assertEquals("2005-05-01 09:09:09", dt.toString());
    }

}
