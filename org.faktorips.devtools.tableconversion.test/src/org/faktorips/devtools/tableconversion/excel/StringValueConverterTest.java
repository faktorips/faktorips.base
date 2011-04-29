/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.MessageList;
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

        value = converter.getIpsValue(new Long(Long.MAX_VALUE), ml);
        assertTrue(Datatype.STRING.isParsable(value));
        assertTrue(ml.isEmpty());
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
