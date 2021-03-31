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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.datatype.Datatype;
import org.faktorips.runtime.MessageList;
import org.junit.Test;

/**
 * 
 * @author Thorsten Guenther
 */
public class StringValueConverterTest {

    @Test
    public void testGetIpsValue() {
        MessageList ml = new MessageList();
        StringValueConverter converter = new StringValueConverter();
        String value = converter.getIpsValue("1234", ml);
        assertTrue(Datatype.STRING.isParsable(value));
        assertTrue(ml.isEmpty());

        value = converter.getIpsValue(Long.valueOf(Long.MAX_VALUE), ml);
        assertTrue(Datatype.STRING.isParsable(value));
        assertTrue(ml.isEmpty());
    }

    @Test
    public void testGetIpsValue_ReturnTrimmedStringValue() {
        MessageList ml = new MessageList();
        StringValueConverter converter = new StringValueConverter();
        String value = converter.getIpsValue("  1234  ", ml);
        assertTrue(Datatype.STRING.isParsable(value));
        assertTrue(ml.isEmpty());
        assertEquals("1234", value);
    }

    @Test
    public void testGetExternalDataValue() {
        MessageList ml = new MessageList();
        StringValueConverter converter = new StringValueConverter();
        String value = (String)converter.getExternalDataValue("VALID", ml);
        assertEquals("VALID", value);
        assertTrue(ml.isEmpty());

        value = (String)converter.getExternalDataValue(null, ml);
        assertNull(value);
        assertTrue(ml.isEmpty());
    }

}
