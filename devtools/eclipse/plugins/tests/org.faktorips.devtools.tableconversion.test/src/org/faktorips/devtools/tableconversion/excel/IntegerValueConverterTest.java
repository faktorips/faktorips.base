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

import org.faktorips.datatype.Datatype;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

/**
 * 
 * @author Thorsten Guenther
 */
public class IntegerValueConverterTest {

    @Test
    public void testGetIpsValue() {
        MessageList ml = new MessageList();
        IntegerValueConverter converter = new IntegerValueConverter();
        String value = converter.getIpsValue(Integer.valueOf(1234), ml);
        assertTrue(Datatype.INTEGER.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(Integer.valueOf(Integer.MAX_VALUE), ml);
        assertTrue(Datatype.INTEGER.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(Integer.valueOf(Integer.MIN_VALUE), ml);
        assertTrue(Datatype.INTEGER.isParsable(value));
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
        IntegerValueConverter converter = new IntegerValueConverter();
        final String VALID = "1234";
        final String INVALID = "invalid";

        assertTrue(Datatype.INTEGER.isParsable(VALID));
        assertFalse(Datatype.INTEGER.isParsable(INVALID));

        Object value = converter.getExternalDataValue(VALID, ml);
        assertEquals(Integer.valueOf(1234), value);
        assertTrue(ml.isEmpty());

        value = converter.getExternalDataValue(null, ml);
        assertNull(value);
        assertTrue(ml.isEmpty());

        value = converter.getExternalDataValue(INVALID, ml);
        assertFalse(ml.isEmpty());
        assertEquals(INVALID, value);
    }

}
