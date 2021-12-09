/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
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
    public void testGetLocalizedContent() {
        assertEquals("Versicherung", new StringValue("Versicherung").getLocalizedContent());
    }

    @Test
    public void testGetLocalizedContentLocale() {
        assertEquals("Versicherung", new StringValue("Versicherung").getLocalizedContent(Locale.GERMAN));
    }

    @Test
    public void testGetDefaultLocalizedContent() throws CoreRuntimeException {
        assertEquals("Versicherung", new StringValue("Versicherung").getDefaultLocalizedContent(newIpsProject()));
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

    @Test
    public void testCompare_OtherType() throws Exception {
        StringValue stringValue = new StringValue("abc");
        InternationalStringValue internationalStringValue = new InternationalStringValue();

        assertTrue(stringValue.compare(internationalStringValue, ValueDatatype.STRING) > 0);
    }

    @Test
    public void testCompare_Equal() throws Exception {
        StringValue stringValue1 = new StringValue("abc");
        StringValue stringValue2 = new StringValue("abc");

        assertTrue(stringValue1.compare(stringValue2, ValueDatatype.STRING) == 0);
        assertTrue(stringValue2.compare(stringValue1, ValueDatatype.STRING) == 0);
    }

    @Test
    public void testCompare_EqualNullContent() throws Exception {
        StringValue stringValue1 = new StringValue(null);
        StringValue stringValue2 = new StringValue(null);

        assertTrue(stringValue1.compare(stringValue2, ValueDatatype.STRING) == 0);
        assertTrue(stringValue2.compare(stringValue1, ValueDatatype.STRING) == 0);
    }

    @Test
    public void testCompare_CompareToNull() throws Exception {
        StringValue stringValue1 = new StringValue("abc");
        StringValue stringValue2 = new StringValue(null);

        assertTrue(stringValue1.compare(null, ValueDatatype.STRING) > 0);
        assertTrue(stringValue1.compare(stringValue2, ValueDatatype.STRING) > 0);
        assertTrue(stringValue2.compare(stringValue1, ValueDatatype.STRING) < 0);
    }

    @Test
    public void testCompare() throws Exception {
        StringValue stringValue1 = new StringValue("abc");
        StringValue stringValue2 = new StringValue("xyz");

        assertTrue(stringValue1.compare(stringValue2, ValueDatatype.STRING) < 0);
        assertTrue(stringValue2.compare(stringValue1, ValueDatatype.STRING) > 0);
    }

    @Test
    public void testCompare_Integer() throws Exception {
        StringValue stringValue1 = new StringValue("03");
        StringValue stringValue2 = new StringValue("010");

        assertTrue(stringValue1.compare(stringValue2, ValueDatatype.INTEGER) < 0);
        assertTrue(stringValue2.compare(stringValue1, ValueDatatype.INTEGER) > 0);
    }

}
