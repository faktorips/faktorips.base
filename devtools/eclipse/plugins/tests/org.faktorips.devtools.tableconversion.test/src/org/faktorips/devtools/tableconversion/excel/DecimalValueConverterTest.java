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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.faktorips.datatype.Datatype;
import org.faktorips.runtime.MessageList;
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
        String value = converter.getIpsValue(Long.valueOf(1234), ml);
        assertTrue(Datatype.DECIMAL.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(Long.valueOf(Long.MAX_VALUE), ml);
        assertTrue(Datatype.DECIMAL.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(Long.valueOf(Long.MIN_VALUE), ml);
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
        assertEquals(Decimal.valueOf(VALID).doubleValue(), value);
        assertTrue(ml.isEmpty());

        value = converter.getExternalDataValue(null, ml);
        // do not use assertEquals because instances of Number are compared by getting longValue
        assertNull(value);
        assertTrue(ml.isEmpty());

        value = converter.getExternalDataValue(INVALID, ml);
        assertFalse(ml.isEmpty());
        assertEquals(INVALID, value);
    }

    @Test
    public void testGetExternalDataValueForNull() {
        MessageList ml = new MessageList();
        DecimalValueConverter converter = new DecimalValueConverter();
        final String EMPTY = "";
        final String DECIMAL_NULL = "DecimalNull";
        final String VALID = "1234";

        assertTrue(Datatype.DECIMAL.isParsable(null));
        assertTrue(Datatype.DECIMAL.isParsable(EMPTY));
        assertTrue(Datatype.DECIMAL.isParsable(DECIMAL_NULL));
        assertTrue(Datatype.DECIMAL.isParsable(VALID));

        Object value;
        value = converter.getExternalDataValue(null, ml);
        assertNull(value);
        assertTrue(ml.isEmpty());
        value = converter.getExternalDataValue(EMPTY, ml);
        assertNull(value);
        assertTrue(ml.isEmpty());
        value = converter.getExternalDataValue(DECIMAL_NULL, ml);
        assertNull(value);
        assertTrue(ml.isEmpty());
        value = converter.getExternalDataValue(VALID, ml);
        assertNotNull(value);
        assertTrue(ml.isEmpty());
    }

}
