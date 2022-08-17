/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.GregorianCalendar;

import org.faktorips.datatype.Datatype;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

/**
 * 
 * @author Thorsten Guenther
 */
public class GregorianCalendarValueConverterTest {

    @Test
    public void testGetIpsValue() {
        MessageList ml = new MessageList();
        GregorianCalendarValueConverter converter = new GregorianCalendarValueConverter();
        String value = converter.getIpsValue(Long.valueOf(1234), ml);
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

    @Test
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
