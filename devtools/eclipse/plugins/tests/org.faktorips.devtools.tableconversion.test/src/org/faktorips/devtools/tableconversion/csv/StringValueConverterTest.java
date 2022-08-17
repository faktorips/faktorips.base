/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.csv;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringValueConverterTest {

    @Test
    public void testGetIpsValue() {
        StringValueConverter converter = new StringValueConverter();
        String value = converter.getIpsValue("1234", null);

        assertEquals("1234", value);
    }

    @Test
    public void testGetIpsValue_ReturnTrimmedStringValue() {
        StringValueConverter converter = new StringValueConverter();
        String value = converter.getIpsValue("  1234  ", null);

        assertEquals("1234", value);
    }

}
