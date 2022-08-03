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

import java.math.BigDecimal;

import org.faktorips.datatype.Datatype;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

/**
 * 
 * @author Thorsten Guenther
 */
public class LongValueConverterTest {

    @Test
    public void testGetIpsValue() {
        MessageList ml = new MessageList();
        LongValueConverter converter = new LongValueConverter();
        String value = converter.getIpsValue(Long.valueOf(1234), ml);
        assertTrue(Datatype.LONG.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(Long.valueOf(Long.MAX_VALUE), ml);
        assertTrue(Datatype.LONG.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(Long.valueOf(Long.MIN_VALUE), ml);
        assertTrue(Datatype.LONG.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue("0", ml);
        assertFalse(ml.isEmpty());
        assertEquals("0", value);

        ml.clear();
        value = converter.getIpsValue(Double.valueOf(Double.MAX_VALUE), ml);
        assertFalse(ml.isEmpty());
        assertEquals(Double.toString(Double.MAX_VALUE), value);
    }

    @Test
    public void testGetExternalDataValue() {
        MessageList ml = new MessageList();
        LongValueConverter converter = new LongValueConverter();
        final String VALID = "1234";
        final String INVALID = "invalid";

        assertTrue(Datatype.LONG.isParsable(VALID));
        assertFalse(Datatype.LONG.isParsable(INVALID));

        Object value = converter.getExternalDataValue(VALID, ml);
        assertEquals(Long.valueOf(1234), value);
        assertTrue(ml.isEmpty());

        value = converter.getExternalDataValue(null, ml);
        assertNull(value);
        assertTrue(ml.isEmpty());

        value = converter.getExternalDataValue(INVALID, ml);
        assertFalse(ml.isEmpty());
        assertEquals(INVALID, value);
    }

    @Test
    public void testGetIpsValueTooBigDecimal() {
        MessageList ml = new MessageList();
        LongValueConverter converter = new LongValueConverter();
        converter.getIpsValue(new BigDecimal(9.22337203685478E+180), ml);
        assertFalse(ml.isEmpty());
    }

    @Test
    public void testGetIpsValueTooSmallDecimal() {
        MessageList ml = new MessageList();
        LongValueConverter converter = new LongValueConverter();
        converter.getIpsValue(new BigDecimal(-9.22337203685478E+180), ml);
        assertFalse(ml.isEmpty());
    }

}
