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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.Decimal;
import org.junit.Test;

/**
 * 
 * @author Thorsten Guenther
 */
public class DecimalValueConverterTest {

    @Test
    public void testGetIpsValue() {
        MessageList ml = new MessageList();
        DecimalValueConverter converter = new DecimalValueConverter();
        String value = converter.getIpsValue(new Long(1234), ml);
        assertTrue(Datatype.DECIMAL.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(new Long(Long.MAX_VALUE), ml);
        assertTrue(Datatype.DECIMAL.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(new Long(Long.MIN_VALUE), ml);
        assertTrue(Datatype.DECIMAL.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue("1234567890.0987654321", ml);
        assertTrue(Datatype.DECIMAL.isParsable(value));
        assertTrue(ml.isEmpty());

        Date date = new Date();
        value = converter.getIpsValue(date, ml);
        assertFalse(ml.isEmpty());
        assertEquals(date.toString(), value);

        ml.clear();
        value = converter.getIpsValue("Egon", ml);
        assertFalse(ml.isEmpty());
        assertEquals("Egon", value);
    }

    @Test
    public void testGetExternalDataValue() {
        MessageList ml = new MessageList();
        DecimalValueConverter converter = new DecimalValueConverter();
        final String VALID = "1234";
        final String INVALID = "invalid";

        assertTrue(Datatype.DECIMAL.isParsable(VALID));
        assertFalse(Datatype.DECIMAL.isParsable(INVALID));

        Object value = converter.getExternalDataValue(VALID, ml);
        assertEquals(Decimal.valueOf(VALID), value);
        assertTrue(ml.isEmpty());

        value = converter.getExternalDataValue(null, ml);
        // do not use assertEquals because instances of Number are compared by getting longValue
        assertSame(Decimal.NULL, value);
        assertTrue(ml.isEmpty());

        value = converter.getExternalDataValue(INVALID, ml);
        assertFalse(ml.isEmpty());
        assertEquals(INVALID, value);
    }

}
