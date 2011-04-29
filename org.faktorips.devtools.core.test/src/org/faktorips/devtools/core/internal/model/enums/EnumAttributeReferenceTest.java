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

package org.faktorips.devtools.core.internal.model.enums;

import static org.junit.Assert.assertEquals;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.enums.IEnumAttributeReference;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class EnumAttributeReferenceTest extends AbstractIpsEnumPluginTest {

    private IEnumAttributeReference genderIdReference;
    private IEnumAttributeReference genderNameReference;

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
        Element xmlElement = genderIdReference.toXml(createXmlDocument(IEnumAttributeReference.XML_TAG));
        NamedNodeMap attributes = xmlElement.getAttributes();
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, attributes.getNamedItem(IIpsElement.PROPERTY_NAME).getTextContent());

        genderNameReference.initFromXml(xmlElement);
        assertEquals(genderIdReference.getName(), genderNameReference.getName());
    }

}
