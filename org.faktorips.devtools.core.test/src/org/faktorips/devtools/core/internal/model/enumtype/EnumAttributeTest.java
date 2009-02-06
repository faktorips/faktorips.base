/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.enumtype;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.enumtype.IEnumAttribute;
import org.faktorips.devtools.core.model.enumtype.IEnumType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class EnumAttributeTest extends AbstractIpsEnumPluginTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testGetSetName() {
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, genderEnumAttributeId.getName());
        genderEnumAttributeId.setName("OtherEnumAttributeName");
        assertEquals("OtherEnumAttributeName", genderEnumAttributeId.getName());

        try {
            genderEnumAttributeId.setName(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetSetDatatype() {
        assertEquals(STRING_DATATYPE_NAME, genderEnumAttributeId.getDatatype());
        genderEnumAttributeId.setDatatype(INTEGER_DATATYPE_NAME);
        assertEquals(INTEGER_DATATYPE_NAME, genderEnumAttributeId.getDatatype());

        try {
            genderEnumAttributeId.setDatatype(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetSetIsIdentifier() {
        assertTrue(genderEnumAttributeId.isIdentifier());
        assertFalse(genderEnumAttributeName.isIdentifier());

        genderEnumAttributeName.setIdentifier(true);
        assertTrue(genderEnumAttributeName.isIdentifier());
    }

    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = genderEnumType.toXml(createXmlDocument(IEnumAttribute.XML_TAG));
        NamedNodeMap attributes = xmlElement.getChildNodes().item(1).getAttributes();
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, attributes.getNamedItem(IEnumAttribute.PROPERTY_NAME)
                .getTextContent());
        assertEquals(STRING_DATATYPE_NAME, attributes.getNamedItem(IEnumAttribute.PROPERTY_DATATYPE).getTextContent());
        assertTrue(Boolean.parseBoolean(attributes.getNamedItem(IEnumAttribute.PROPERTY_IDENTIFIER).getTextContent()));
        assertEquals(1 + 2, xmlElement.getChildNodes().getLength());

        IEnumType loadedEnumType = newEnumType(ipsProject, "LoadedEnumType");
        loadedEnumType.initFromXml(xmlElement);
        IEnumAttribute idAttribute = loadedEnumType.getEnumAttribute(0);
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, idAttribute.getName());
        assertEquals(STRING_DATATYPE_NAME, idAttribute.getDatatype());
        assertTrue(idAttribute.isIdentifier());
        assertEquals(2, loadedEnumType.getEnumAttributes().size());
    }

    public void testValidateThis() throws CoreException {
        assertTrue(genderEnumAttributeId.isValid());

        IIpsModel ipsModel = getIpsModel();

        ipsModel.clearValidationCache();
        genderEnumAttributeId.setName("");
        assertEquals(1, genderEnumAttributeId.validate(ipsProject).getNoOfMessages());
        genderEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);

        ipsModel.clearValidationCache();
        genderEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_NAME_NAME);
        assertEquals(1, genderEnumAttributeId.validate(ipsProject).getNoOfMessages());
        genderEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);

        ipsModel.clearValidationCache();
        genderEnumAttributeId.setDatatype("");
        assertEquals(1, genderEnumAttributeId.validate(ipsProject).getNoOfMessages());
        genderEnumAttributeId.setDatatype(STRING_DATATYPE_NAME);

        ipsModel.clearValidationCache();
        genderEnumAttributeId.setDatatype("FooBar");
        assertEquals(1, genderEnumAttributeId.validate(ipsProject).getNoOfMessages());
        genderEnumAttributeId.setDatatype(STRING_DATATYPE_NAME);
    }

}
