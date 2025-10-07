/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.extproperties;

import static org.faktorips.runtime.internal.XmlUtil.getDocumentBuilder;
import static org.junit.Assert.assertEquals;

import javax.naming.directory.Attribute;
import javax.xml.parsers.FactoryConfigurationError;

import org.faktorips.devtools.model.util.XmlUtil;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Jan Ortmann
 */
public class IpsObjectPartStringExtPropertyTest {

    private StringExtensionPropertyDefinition property;

    @Before
    public void setUp() throws Exception {
        property = new StringExtensionPropertyDefinition();
        property.setPropertyId("id");
        property.setExtendedType(Attribute.class);
        property.setDefaultValue("defaultValue");
    }

    @Test
    public void testValueToXml() throws FactoryConfigurationError {
        Document doc = getDocumentBuilder().newDocument();
        Element el = doc.createElement("Value");

        // not null
        property.valueToXml(el, "blabla");
        assertEquals("blabla", XmlUtil.getCDATAorTextContent(el));
        assertEquals("blabla", property.getValueFromXml(el));

        // not null, special characters
        el = doc.createElement("Value");
        property.valueToXml(el, "<>&");
        assertEquals("<>&", XmlUtil.getCDATAorTextContent(el));
        assertEquals("<>&", property.getValueFromXml(el));
    }

    @Test
    public void testGetValueFromXml() throws FactoryConfigurationError {
        Document doc = getDocumentBuilder().newDocument();
        Element el = doc.createElement("Value");

        // not null
        el.appendChild(doc.createCDATASection("blabla"));
        assertEquals("blabla", property.getValueFromXml(el));

        // not null, special characters
        el = doc.createElement("Value");
        el.appendChild(doc.createCDATASection("<>&"));
        assertEquals("<>&", property.getValueFromXml(el));
    }

    @Test
    public void testSetDefaultValue() {
        property.setDefaultValue("blabla");
        assertEquals("blabla", property.getDefaultValue());
    }

}
