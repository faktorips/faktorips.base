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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class EnumAttributeTest extends AbstractIpsEnumPluginTest {

    private final static String ICON = "EnumAttribute.gif";
    private final static String OVERRIDDEN_ICON = "EnumAttributeOverridden.gif";
    private final static String UNIQUE_IDENTIFIER_ICON = "EnumAttributeUniqueIdentifier.gif";
    private final static String OVERRIDDEN_UNIQUE_IDENTIFIER_ICON = "EnumAttributeOverriddenUniqueIdentifier.gif";

    private IEnumType subEnumType;
    private IEnumAttribute inheritedEnumAttributeId;
    private IEnumAttribute inheritedEnumAttributeName;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        inheritedEnumAttributeId = subEnumType.newEnumAttribute();
        inheritedEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);
        inheritedEnumAttributeId.setInherited(true);
        inheritedEnumAttributeName = subEnumType.newEnumAttribute();
        inheritedEnumAttributeName.setName(GENDER_ENUM_ATTRIBUTE_NAME_NAME);
        inheritedEnumAttributeName.setInherited(true);
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

    public void testGetSetIsLiteralName() {
        assertTrue(genderEnumAttributeId.isLiteralName());
        assertFalse(genderEnumAttributeName.isLiteralName());

        genderEnumAttributeName.setLiteralName(true);
        assertTrue(genderEnumAttributeName.isLiteralName());
    }

    public void testGetSetIsInherited() {
        assertFalse(genderEnumAttributeId.isInherited());
        genderEnumAttributeId.setInherited(true);

        assertTrue(genderEnumAttributeId.isInherited());
        assertEquals("", genderEnumAttributeId.getDatatype());
        assertFalse(genderEnumAttributeId.isLiteralName());
        assertFalse(genderEnumAttributeId.isUniqueIdentifier());
        assertFalse(genderEnumAttributeId.isUsedAsNameInFaktorIpsUi());
        assertFalse(genderEnumAttributeId.isUsedAsIdInFaktorIpsUi());
    }

    public void testGetSetIsUniqueIdentifier() {
        assertTrue(genderEnumAttributeId.isUniqueIdentifier());
        genderEnumAttributeId.setUniqueIdentifier(false);
        assertFalse(genderEnumAttributeId.isUniqueIdentifier());
    }

    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = genderEnumType.toXml(createXmlDocument(IEnumAttribute.XML_TAG));
        NamedNodeMap attributes = xmlElement.getChildNodes().item(1).getAttributes();
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, attributes.getNamedItem(IEnumAttribute.PROPERTY_NAME)
                .getTextContent());
        assertEquals(STRING_DATATYPE_NAME, attributes.getNamedItem(IEnumAttribute.PROPERTY_DATATYPE).getTextContent());
        assertTrue(Boolean.parseBoolean(attributes.getNamedItem(IEnumAttribute.PROPERTY_LITERAL_NAME).getTextContent()));
        assertTrue(Boolean.parseBoolean(attributes.getNamedItem(IEnumAttribute.PROPERTY_UNIQUE_IDENTIFIER)
                .getTextContent()));
        assertFalse(Boolean.parseBoolean(attributes.getNamedItem(IEnumAttribute.PROPERTY_INHERITED).getTextContent()));
        assertEquals(1 + 2, xmlElement.getChildNodes().getLength());

        IEnumType loadedEnumType = newEnumType(ipsProject, "LoadedEnumType");
        loadedEnumType.initFromXml(xmlElement);
        IEnumAttribute idAttribute = loadedEnumType.getEnumAttributes().get(0);
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, idAttribute.getName());
        assertEquals(STRING_DATATYPE_NAME, idAttribute.getDatatype());
        assertTrue(idAttribute.isLiteralName());
        assertTrue(idAttribute.isUniqueIdentifier());
        assertFalse(idAttribute.isInherited());
        assertEquals(2, loadedEnumType.getEnumAttributes().size());
    }

    public void testValidateThis() throws CoreException {
        assertTrue(genderEnumAttributeId.isValid());
    }

    public void testValidateName() throws CoreException {
        IIpsModel ipsModel = getIpsModel();

        // Test name missing
        genderEnumAttributeId.setName("");
        MessageList validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_NAME_MISSING));
        genderEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);

        // Test duplicate attribute name
        ipsModel.clearValidationCache();
        genderEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_NAME_NAME);
        validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME));
        genderEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);
    }

    public void testValidateDatatype() throws CoreException {
        IIpsModel ipsModel = getIpsModel();

        // Test datatype missing
        genderEnumAttributeId.setDatatype("");
        MessageList validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_DATATYPE_MISSING));
        genderEnumAttributeId.setDatatype(STRING_DATATYPE_NAME);

        // Test datatype does not exist
        ipsModel.clearValidationCache();
        genderEnumAttributeId.setDatatype("FooBar");
        validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_DATATYPE_DOES_NOT_EXIST));
        genderEnumAttributeId.setDatatype(STRING_DATATYPE_NAME);
    }

    public void testValidateLiteralName() throws CoreException {
        IIpsModel ipsModel = getIpsModel();

        // Test duplicate literal names
        genderEnumAttributeName.setLiteralName(true);
        MessageList validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_LITERAL_NAME));
        genderEnumAttributeName.setLiteralName(false);

        // Test literal name but datatype not String
        ipsModel.clearValidationCache();
        genderEnumAttributeId.setDatatype(INTEGER_DATATYPE_NAME);
        validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_LITERAL_NAME_NOT_OF_DATATYPE_STRING));
        genderEnumAttributeId.setDatatype(STRING_DATATYPE_NAME);

        // Test literal name but not unique identifier
        ipsModel.clearValidationCache();
        genderEnumAttributeId.setUniqueIdentifier(false);
        validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_LITERAL_NAME_BUT_NOT_UNIQUE_IDENTIFIER));
        genderEnumAttributeId.setUniqueIdentifier(true);
    }

    public void testValidateInherited() throws CoreException {
        IIpsModel ipsModel = getIpsModel();

        // Test no such attribute in supertype hierarchy for inherited attribute
        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        superEnumType.setAbstract(true);
        genderEnumType.setSuperEnumType(superEnumType.getQualifiedName());
        IEnumAttribute inheritedAttribute = genderEnumType.newEnumAttribute();
        inheritedAttribute.setName("foo");
        inheritedAttribute.setInherited(true);

        MessageList validationMessageList = inheritedAttribute.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_NO_SUCH_ATTRIBUTE_IN_SUPERTYPE_HIERARCHY));
        IEnumAttribute toInheritAttribute = superEnumType.newEnumAttribute();
        toInheritAttribute.setName("foo");
        toInheritAttribute.setDatatype(STRING_DATATYPE_NAME);

        ipsModel.clearValidationCache();
        assertTrue(inheritedAttribute.isValid());
    }

    public void testValidateUsedAsNameInFaktorIpsUi() throws CoreException {
        genderEnumAttributeId.setUsedAsNameInFaktorIpsUi(true);
        MessageList validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_USED_AS_NAME_IN_FAKTOR_IPS_UI));
    }

    public void testValidateUsedAsIdInFaktorIpsUi() throws CoreException {
        genderEnumAttributeName.setUsedAsIdInFaktorIpsUi(true);
        MessageList validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_USED_AS_ID_IN_FAKTOR_IPS_UI));
    }

    public void testGetImage() throws CoreException {
        Image icon = IpsPlugin.getDefault().getImage(ICON);
        Image overriddenIcon = IpsPlugin.getDefault().getImage(OVERRIDDEN_ICON);
        Image uniqueIdentifierIcon = IpsPlugin.getDefault().getImage(UNIQUE_IDENTIFIER_ICON);
        Image overriddenUniqueIdentifierIcon = IpsPlugin.getDefault().getImage(OVERRIDDEN_UNIQUE_IDENTIFIER_ICON);

        assertEquals(icon, genderEnumAttributeName.getImage());
        assertEquals(uniqueIdentifierIcon, genderEnumAttributeId.getImage());
        assertEquals(overriddenIcon, inheritedEnumAttributeName.getImage());
        assertEquals(overriddenUniqueIdentifierIcon, inheritedEnumAttributeId.getImage());
    }

    public void testGetEnumType() {
        assertEquals(genderEnumType, genderEnumAttributeId.getEnumType());
    }

    public void testFindDatatype() throws CoreException {
        ValueDatatype datatype = genderEnumAttributeId.findDatatype(ipsProject);
        assertEquals(STRING_DATATYPE_NAME, datatype.getName());

        genderEnumAttributeId.setDatatype("foo");
        assertNull(genderEnumAttributeId.findDatatype(ipsProject));

        try {
            genderEnumAttributeId.findDatatype(null);
            fail();
        } catch (NullPointerException e) {
        }

        // Test inherited
        genderEnumAttributeId.setDatatype(STRING_DATATYPE_NAME);
        assertEquals(STRING_DATATYPE_NAME, inheritedEnumAttributeId.findDatatype(ipsProject).getName());

        genderEnumAttributeId.setInherited(true);
        assertNull(genderEnumAttributeId.findDatatype(ipsProject));
    }

    public void testFindIsLiteralName() throws CoreException {
        assertTrue(inheritedEnumAttributeId.findIsLiteralName());
        inheritedEnumAttributeId.setInherited(false);
        assertFalse(inheritedEnumAttributeId.findIsLiteralName());

        genderEnumAttributeId.setInherited(true);
        assertNull(genderEnumAttributeId.findIsLiteralName());
    }

    public void testFindIsUniqueIdentifier() throws CoreException {
        assertTrue(inheritedEnumAttributeId.findIsUniqueIdentifier());
        inheritedEnumAttributeId.setInherited(false);
        assertFalse(inheritedEnumAttributeId.findIsUniqueIdentifier());

        genderEnumAttributeId.setInherited(true);
        assertNull(genderEnumAttributeId.findIsUniqueIdentifier());
    }

    public void testFindIsUsedAsIdInFaktorIpsUi() throws CoreException {
        assertTrue(inheritedEnumAttributeId.findIsUsedAsIdInFaktorIpsUi());
        inheritedEnumAttributeId.setInherited(false);
        assertFalse(inheritedEnumAttributeId.findIsUsedAsIdInFaktorIpsUi());

        genderEnumAttributeId.setInherited(true);
        assertNull(genderEnumAttributeId.findIsUsedAsIdInFaktorIpsUi());
    }

    public void testFindIsUsedAsNameInFaktorIpsUi() throws CoreException {
        assertTrue(inheritedEnumAttributeName.findIsUsedAsNameInFaktorIpsUi());
        inheritedEnumAttributeName.setInherited(false);
        assertFalse(inheritedEnumAttributeName.findIsUsedAsNameInFaktorIpsUi());

        genderEnumAttributeName.setInherited(true);
        assertNull(genderEnumAttributeName.findIsUsedAsNameInFaktorIpsUi());
    }

    public void testGetSetUsedAsNameInFaktorIpsUi() {
        assertTrue(genderEnumAttributeName.isUsedAsNameInFaktorIpsUi());
        assertFalse(genderEnumAttributeId.isUsedAsNameInFaktorIpsUi());
        genderEnumAttributeName.setUsedAsNameInFaktorIpsUi(false);
        assertFalse(genderEnumAttributeName.isUsedAsNameInFaktorIpsUi());
    }

    public void testGetSetUsedAsIdInFaktorIpsUi() {
        assertTrue(genderEnumAttributeId.isUsedAsIdInFaktorIpsUi());
        assertFalse(genderEnumAttributeName.isUsedAsIdInFaktorIpsUi());
        genderEnumAttributeId.setUsedAsIdInFaktorIpsUi(false);
        assertFalse(genderEnumAttributeId.isUsedAsIdInFaktorIpsUi());
    }

}
