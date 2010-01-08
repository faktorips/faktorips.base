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

package org.faktorips.devtools.tableconversion.excel;

import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.tableconversion.excel.GregorianCalendarValueConverter;
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
        assertTrue(Datatype.GREGORIAN_CALENDAR.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(new Date(), ml);
        assertTrue(Datatype.GREGORIAN_CALENDAR.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue("0", ml);
        assertFalse(ml.isEmpty());
        assertEquals("0", value);
        
        ml.clear();
        assertEquals("2008-07-06", converter.getIpsValue("2008-07-06", ml));
        assertTrue(ml.isEmpty());

        ml.clear();
        assertEquals("01.02.2009", converter.getIpsValue("01.02.2009", ml));
        assertFalse(ml.isEmpty());
    }

    public void testGetExternalDataValue() {
        MessageList ml = new MessageList();
        GregorianCalendarValueConverter converter = new GregorianCalendarValueConverter();
        final String VALID = "2001-03-26";
        final String INVALID = "invalid";

        assertTrue(Datatype.GREGORIAN_CALENDAR.isParsable(VALID));
        assertFalse(Datatype.GREGORIAN_CALENDAR.isParsable(INVALID));

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
