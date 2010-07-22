/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.tableconversion.excel;

import junit.framework.TestCase;

import org.faktorips.datatype.Datatype;
import org.faktorips.util.message.MessageList;

/**
 * 
 * @author Thorsten Guenther
 */
public class StringValueConverterTest extends TestCase {

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
