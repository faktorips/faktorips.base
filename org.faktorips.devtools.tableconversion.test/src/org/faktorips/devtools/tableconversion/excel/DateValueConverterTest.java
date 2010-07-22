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

package org.faktorips.devtools.tableconversion.excel;

import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.faktorips.datatype.classtypes.DateDatatype;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Thorsten Guenther
 */
public class DateValueConverterTest extends TestCase {
    private MessageList ml;
    private DateValueConverter converter;
    private DateDatatype datatype;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ml = new MessageList();
        converter = new DateValueConverter();
        datatype = new DateDatatype();
    }

    public void testGetIpsValue() {
        String value = converter.getIpsValue(new Long(1234), ml);
        assertTrue(datatype.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(new Date(), ml);
        assertTrue(datatype.isParsable(value));
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
        final String VALID = "2001-03-26";
        final String INVALID = "invalid";

        assertTrue(datatype.isParsable(VALID));
        assertFalse(datatype.isParsable(INVALID));

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
