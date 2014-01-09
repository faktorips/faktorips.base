/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.MessageList;
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

        value = converter.getIpsValue(new Integer(0), ml);
        assertFalse(ml.isEmpty());
        assertEquals(value, new Integer(0).toString());
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
