/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class StringValueTest extends AbstractIpsPluginTest {

    @Test
    public void testToXml() {
        StringValue stringValue = new StringValue("Versicherung");
        Node xml = stringValue.toXml(getTestDocument());
        assertNotNull(xml);

        StringValue copy = StringValue.createFromXml((Text)xml);
        assertNotNull(copy);
        assertEquals("Versicherung", copy.getContent());
    }

    @Test
        public void testGetContent() {
            assertEquals("Versicherung", new StringValue("Versicherung").getContent());
            assertNull(new StringValue(null).getContent());
        }

    @Test
            public void testGetContentAsString() {
                assertEquals("Versicherung", new StringValue("Versicherung").getContentAsString());
                assertNull(new StringValue(null).getContentAsString());
            }

    @Test
    public void testToString() {
        assertEquals("Versicherung", new StringValue("Versicherung").toString());
        assertNull(new StringValue(null).toString());
    }

    @Test
    public void testEquals() {
        StringValue stringValue = new StringValue("Versicherung");
        assertTrue(stringValue.equals(stringValue));
        assertFalse(stringValue.equals(null));
        assertFalse(stringValue.equals(new String("Versicherung")));
        StringValue copy = new StringValue("");
        assertFalse(stringValue.equals(copy));
        copy = new StringValue("Versicherung");
        assertTrue(stringValue.equals(copy));
        stringValue = new StringValue(null);
        assertFalse(stringValue.equals(copy));
        stringValue = new StringValue("");
        assertFalse(stringValue.equals(copy));
        copy = new StringValue("");
        assertTrue(stringValue.equals(copy));

    }
}
