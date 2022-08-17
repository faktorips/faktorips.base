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
public class BooleanValueConverterTest {

    @Test
    public void testGetIpsValue() {
        MessageList ml = new MessageList();
        BooleanValueConverter converter = new BooleanValueConverter();
        String value = converter.getIpsValue(Boolean.FALSE, ml);
        assertTrue(Datatype.BOOLEAN.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(Boolean.TRUE, ml);
        assertTrue(Datatype.BOOLEAN.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue("true", ml);
        assertTrue(Datatype.BOOLEAN.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue("1", ml);
        assertTrue(Datatype.BOOLEAN.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue("3", ml);
        assertFalse(ml.isEmpty());
        assertEquals(value, Integer.toString(3));

        ml.clear();

        value = converter.getIpsValue(new BigDecimal(1), ml);
        assertTrue(Datatype.BOOLEAN.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(Integer.valueOf(0), ml);
        assertTrue(Datatype.BOOLEAN.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(3, ml);
        assertFalse(ml.isEmpty());
        assertEquals(value, Integer.toString(3));

        ml.clear();

        value = converter.getIpsValue(null, ml);
        assertFalse(ml.isEmpty());
        assertEquals(value, String.valueOf((Object)null));
    }

    @Test
    public void testGetExternalDataValue() {
        MessageList ml = new MessageList();
        BooleanValueConverter converter = new BooleanValueConverter();
        final String TRUE = "true";
        final String FALSE = "false";
        final String INVALID = "invalid";

        assertTrue(Datatype.BOOLEAN.isParsable(TRUE));
        assertTrue(Datatype.BOOLEAN.isParsable(FALSE));

        Boolean value = (Boolean)converter.getExternalDataValue(TRUE, ml);
        assertTrue(value.booleanValue());
        assertTrue(ml.isEmpty());

        value = (Boolean)converter.getExternalDataValue(FALSE, ml);
        assertFalse(value.booleanValue());
        assertTrue(ml.isEmpty());

        value = (Boolean)converter.getExternalDataValue(INVALID, ml);
        assertFalse(value.booleanValue());
        assertTrue(ml.isEmpty());

        value = (Boolean)converter.getExternalDataValue(null, ml);
        assertNull(value);
        assertTrue(ml.isEmpty());
    }

}
