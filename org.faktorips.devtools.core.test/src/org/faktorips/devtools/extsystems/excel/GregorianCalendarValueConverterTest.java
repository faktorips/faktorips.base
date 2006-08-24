/***************************************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) dürfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1
 * (vor Gründung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorips.org/legal/cl-v01.html eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn GmbH - initial API and implementation
 * 
 **************************************************************************************************/

package org.faktorips.devtools.extsystems.excel;

import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Thorsten Guenther
 */
public class GregorianCalendarValueConverterTest extends TestCase {

    public void testGetIpsValue() {
        MessageList ml = new MessageList();
        GregorianCalendarValueConverter converter = new GregorianCalendarValueConverter();
        String value = converter.getIpsValue(new Long(1234), ml);
        assertTrue(Datatype.GREGORIAN_CALENDAR_DATE.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(new Date(), ml);
        assertTrue(Datatype.GREGORIAN_CALENDAR_DATE.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue("0", ml);
        assertFalse(ml.isEmpty());
        assertEquals("0", value);
    }

    public void testGetExternalDataValue() {
        MessageList ml = new MessageList();
        GregorianCalendarValueConverter converter = new GregorianCalendarValueConverter();
        final String VALID = "2001-03-26";
        final String INVALID = "invalid";

        assertTrue(Datatype.GREGORIAN_CALENDAR_DATE.isParsable(VALID));
        assertFalse(Datatype.GREGORIAN_CALENDAR_DATE.isParsable(INVALID));

        Object value = converter.getExternalDataValue(VALID, ml);
        assertEquals(new GregorianCalendar(2001, 02, 26).getTime(), value);
        assertTrue(ml.isEmpty());

        value = converter.getExternalDataValue(null, ml);
        assertNull(value);
        assertTrue(ml.isEmpty());

        value = converter.getExternalDataValue(INVALID, ml);
        assertFalse(ml.isEmpty());
        assertEquals(INVALID, value);
    }

}
