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

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;

public class DateTimeTest {

    /*
     * Test method for 'org.faktorips.runtime.internal.DateTime.toDate(TimeZone)'
     */@Test
    public void testToDate() {
        DateTime dt = new DateTime(2005, 5, 1);
        GregorianCalendar cal = new GregorianCalendar(2005, 4, 1);
        Date date = cal.getTime();
        assertEquals(date, dt.toDate(cal.getTimeZone()));
    }

    /*
     * Test method for 'org.faktorips.runtime.internal.DateTime.toGregorianCalendar(TimeZone)'
     */@Test
    public void testToGregorianCalendar() {
        DateTime dt = new DateTime(2005, 5, 1);
        GregorianCalendar cal = new GregorianCalendar(2005, 4, 1);
        assertEquals(cal, dt.toGregorianCalendar(cal.getTimeZone()));
    }

    /*
     * Test method for 'org.faktorips.runtime.internal.DateTime.toGregorianCalendar(TimeZone)'
     */@Test
    public void testCreateDateOnly() {
        GregorianCalendar cal = new GregorianCalendar(2005, 4, 1);
        assertEquals(new DateTime(2005, 5, 1), DateTime.createDateOnly(cal));
        assertNull(DateTime.createDateOnly(null));
    }
}
