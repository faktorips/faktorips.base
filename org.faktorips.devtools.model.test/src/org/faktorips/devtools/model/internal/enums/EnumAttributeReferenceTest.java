/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.enums;

import static org.junit.Assert.assertEquals;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.abstracttest.AbstractIpsEnumPluginTest;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IPartReference;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class EnumAttributeReferenceTest extends AbstractIpsEnumPluginTest {

    private IPartReference genderIdReference;
    private IPartReference genderNameReference;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        genderIdReference = genderEnumContent.getEnumAttributeReferences().get(0);
        genderNameReference = genderEnumContent.getEnumAttributeReferences().get(1);
    }

    @Test
    public void testGetSetName() {
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, genderIdReference.getName());
        genderIdReference.setName("foo");
        assertEquals("foo", genderIdReference.getName());
    }

    @Test
    public void testXml() throws ParserConfigurationException {
        Element xmlElement = genderIdReference.toXml(createXmlDocument(EnumAttributeReference.XML_TAG));
        NamedNodeMap attributes = xmlElement.getAttributes();
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME,
                attributes.getNamedItem(IIpsElement.PROPERTY_NAME).getTextContent());

        genderNameReference.initFromXml(xmlElement);
        assertEquals(genderIdReference.getName(), genderNameReference.getName());
    }

}
