/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
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
