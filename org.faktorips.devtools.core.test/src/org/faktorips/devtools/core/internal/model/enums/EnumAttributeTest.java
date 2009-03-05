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

package org.faktorips.devtools.core.internal.model.enums;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class EnumAttributeTest extends AbstractIpsEnumPluginTest {

    private final String ICON = "EnumAttribute.gif";
    private final String OVERRIDDEN_ICON = "EnumAttributeOverridden.gif";
    private final String IDENTIFIER_ICON = "EnumAttributeIdentifier.gif";
    private final String OVERRIDDEN_IDENTIFIER_ICON = "EnumAttributeOverriddenIdentifier.gif";

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

    public void testGetSetIsInherited() {
        assertFalse(genderEnumAttributeId.isInherited());
        genderEnumAttributeId.setInherited(true);
        assertTrue(genderEnumAttributeId.isInherited());
    }

    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = genderEnumType.toXml(createXmlDocument(IEnumAttribute.XML_TAG));
        NamedNodeMap attributes = xmlElement.getChildNodes().item(1).getAttributes();
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, attributes.getNamedItem(IEnumAttribute.PROPERTY_NAME)
                .getTextContent());
        assertEquals(STRING_DATATYPE_NAME, attributes.getNamedItem(IEnumAttribute.PROPERTY_DATATYPE).getTextContent());
        assertTrue(Boolean.parseBoolean(attributes.getNamedItem(IEnumAttribute.PROPERTY_IDENTIFIER).getTextContent()));
        assertFalse(Boolean.parseBoolean(attributes.getNamedItem(IEnumAttribute.PROPERTY_INHERITED).getTextContent()));
        assertEquals(1 + 2, xmlElement.getChildNodes().getLength());

        IEnumType loadedEnumType = newEnumType(ipsProject, "LoadedEnumType");
        loadedEnumType.initFromXml(xmlElement);
        IEnumAttribute idAttribute = loadedEnumType.getEnumAttributes().get(0);
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, idAttribute.getName());
        assertEquals(STRING_DATATYPE_NAME, idAttribute.getDatatype());
        assertTrue(idAttribute.isIdentifier());
        assertFalse(idAttribute.isInherited());
        assertEquals(2, loadedEnumType.getEnumAttributes().size());
    }

    public void testValidateThis() throws CoreException {
        assertTrue(genderEnumAttributeId.isValid());

        IIpsModel ipsModel = getIpsModel();

        // Test name missing
        ipsModel.clearValidationCache();
        genderEnumAttributeId.setName("");
        assertEquals(1, genderEnumAttributeId.validate(ipsProject).getNoOfMessages());
        genderEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);

        // Test duplicate attribute name
        ipsModel.clearValidationCache();
        genderEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_NAME_NAME);
        assertEquals(1, genderEnumAttributeId.validate(ipsProject).getNoOfMessages());
        genderEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);

        // Test datatype missing
        ipsModel.clearValidationCache();
        genderEnumAttributeId.setDatatype("");
        assertEquals(1, genderEnumAttributeId.validate(ipsProject).getNoOfMessages());
        genderEnumAttributeId.setDatatype(STRING_DATATYPE_NAME);

        // Test datatype does not exist
        ipsModel.clearValidationCache();
        genderEnumAttributeId.setDatatype("FooBar");
        assertEquals(1, genderEnumAttributeId.validate(ipsProject).getNoOfMessages());
        genderEnumAttributeId.setDatatype(STRING_DATATYPE_NAME);

        // Test duplicate identifiers
        ipsModel.clearValidationCache();
        genderEnumAttributeName.setIdentifier(true);
        assertEquals(1, genderEnumAttributeId.validate(ipsProject).getNoOfMessages());
        genderEnumAttributeName.setIdentifier(false);

        // Test identifier but datatype not String
        ipsModel.clearValidationCache();
        genderEnumAttributeId.setDatatype(INTEGER_DATATYPE_NAME);
        assertEquals(1, genderEnumAttributeId.validate(ipsProject).getNoOfMessages());
        genderEnumAttributeId.setDatatype(STRING_DATATYPE_NAME);

        // Test no such attribute in supertype hierarchy for inherited attribute
        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        superEnumType.setAbstract(true);
        genderEnumType.setSuperEnumType(superEnumType.getQualifiedName());
        IEnumAttribute inheritedAttribute = genderEnumType.newEnumAttribute();
        inheritedAttribute.setName("foo");
        inheritedAttribute.setDatatype(STRING_DATATYPE_NAME);
        inheritedAttribute.setInherited(true);
        ipsModel.clearValidationCache();
        assertEquals(1, inheritedAttribute.validate(ipsProject).getNoOfMessages());
        IEnumAttribute toInheritAttribute = superEnumType.newEnumAttribute();
        toInheritAttribute.setName("foo");
        toInheritAttribute.setDatatype(STRING_DATATYPE_NAME);
        ipsModel.clearValidationCache();
        assertTrue(inheritedAttribute.isValid());
    }

    public void testGetImage() throws CoreException {
        Image icon = IpsPlugin.getDefault().getImage(ICON);
        Image overriddenIcon = IpsPlugin.getDefault().getImage(OVERRIDDEN_ICON);
        Image identifierIcon = IpsPlugin.getDefault().getImage(IDENTIFIER_ICON);
        Image overriddenIdentifierIcon = IpsPlugin.getDefault().getImage(OVERRIDDEN_IDENTIFIER_ICON);

        assertEquals(icon, genderEnumAttributeName.getImage());
        assertEquals(identifierIcon, genderEnumAttributeId.getImage());

        genderEnumType.setAbstract(true);
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        IEnumAttribute subAttributeId = subEnumType.newEnumAttribute();
        subAttributeId.setIdentifier(true);
        subAttributeId.setInherited(true);
        IEnumAttribute subAttributeName = subEnumType.newEnumAttribute();
        subAttributeName.setInherited(true);

        assertEquals(overriddenIcon, subAttributeName.getImage());
        assertEquals(overriddenIdentifierIcon, subAttributeId.getImage());
    }

    public void testGetEnumType() {
        assertEquals(genderEnumType, genderEnumAttributeId.getEnumType());
    }

}
