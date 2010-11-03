/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class EnumAttributeTest extends AbstractIpsEnumPluginTest {

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

    public void testFindSuperEnumAttribute() throws CoreException {
        try {
            genderEnumAttributeId.findSuperEnumAttribute(null);
            fail();
        } catch (NullPointerException e) {
        }

        assertNull(genderEnumAttributeId.findSuperEnumAttribute(ipsProject));

        assertEquals(genderEnumAttributeId, inheritedEnumAttributeId.findSuperEnumAttribute(ipsProject));
    }

    public void testSearchInheritedCopiesOneSubtype() throws CoreException {
        List<IEnumAttribute> attributes = genderEnumAttributeId.searchInheritedCopies(ipsProject);
        assertEquals(1, attributes.size());
        assertEquals(inheritedEnumAttributeId, attributes.get(0));
    }

    public void testSearchInheritedCopiesNoSubtypes() throws CoreException {
        List<IEnumAttribute> attributes = inheritedEnumAttributeId.searchInheritedCopies(ipsProject);
        assertEquals(0, attributes.size());
    }

    public void testSearchInheritedCopiesTwoSubtypes() throws CoreException {
        IEnumType deepEnumType = newEnumType(ipsProject, "DeepEnumType");
        deepEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        IEnumAttribute deepEnumAttributeId = deepEnumType.newEnumAttribute();
        deepEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);
        deepEnumAttributeId.setInherited(true);

        List<IEnumAttribute> attributes = genderEnumAttributeId.searchInheritedCopies(ipsProject);
        assertEquals(2, attributes.size());
        assertTrue(attributes.contains(inheritedEnumAttributeId));
        assertTrue(attributes.contains(deepEnumAttributeId));
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
        assertEquals(Datatype.STRING.getQualifiedName(), genderEnumAttributeId.getDatatype());
        genderEnumAttributeId.setDatatype(Datatype.INTEGER.getQualifiedName());
        assertEquals(Datatype.INTEGER.getQualifiedName(), genderEnumAttributeId.getDatatype());

        try {
            genderEnumAttributeId.setDatatype(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetSetIsInherited() {
        assertFalse(genderEnumAttributeId.isInherited());
        genderEnumAttributeId.setInherited(true);

        assertTrue(genderEnumAttributeId.isInherited());
        assertEquals("", genderEnumAttributeId.getDatatype());
        assertFalse(genderEnumAttributeId.isUnique());
        assertFalse(genderEnumAttributeId.isUsedAsNameInFaktorIpsUi());
        assertFalse(genderEnumAttributeId.isIdentifier());
    }

    public void testGetSetIsUniqueIdentifier() {
        assertTrue(genderEnumAttributeId.isUnique());
        genderEnumAttributeId.setUnique(false);
        assertFalse(genderEnumAttributeId.isUnique());
    }

    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = genderEnumAttributeId.toXml(createXmlDocument(IEnumAttribute.XML_TAG));
        NamedNodeMap attributes = xmlElement.getAttributes();
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, attributes.getNamedItem(IIpsElement.PROPERTY_NAME).getTextContent());
        assertEquals(Datatype.STRING.getQualifiedName(), attributes.getNamedItem(IEnumAttribute.PROPERTY_DATATYPE)
                .getTextContent());
        assertTrue(Boolean.parseBoolean(attributes.getNamedItem(IEnumAttribute.PROPERTY_UNIQUE).getTextContent()));
        assertTrue(Boolean.parseBoolean(attributes.getNamedItem(IEnumAttribute.PROPERTY_IDENTIFIER).getTextContent()));
        assertFalse(Boolean.parseBoolean(attributes.getNamedItem(IEnumAttribute.PROPERTY_USED_AS_NAME_IN_FAKTOR_IPS_UI)
                .getTextContent()));
        assertFalse(Boolean.parseBoolean(attributes.getNamedItem(IEnumAttribute.PROPERTY_INHERITED).getTextContent()));

        IEnumAttribute loadedEnumAttribute = genderEnumType.newEnumAttribute();
        loadedEnumAttribute.initFromXml(xmlElement);
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, loadedEnumAttribute.getName());
        assertEquals(Datatype.STRING.getQualifiedName(), loadedEnumAttribute.getDatatype());
        assertTrue(loadedEnumAttribute.isUnique());
        assertTrue(loadedEnumAttribute.isIdentifier());
        assertFalse(loadedEnumAttribute.isUsedAsNameInFaktorIpsUi());
        assertFalse(loadedEnumAttribute.isInherited());
    }

    public void testValidateThis() throws CoreException {
        assertTrue(genderEnumAttributeId.isValid());
    }

    public void testValidateName() throws CoreException {
        IIpsModel ipsModel = getIpsModel();

        // Test name missing.
        genderEnumAttributeId.setName("");
        MessageList validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_NAME_MISSING));

        // Test name not a valid java attribute name.
        genderEnumAttributeId.setName("test test");
        ipsModel.clearValidationCache();
        validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_NAME_NOT_A_VALID_FIELD_NAME));
        genderEnumAttributeId.setName("class");
        ipsModel.clearValidationCache();
        validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_NAME_NOT_A_VALID_FIELD_NAME));
    }

    public void testValidateDuplicateName() throws CoreException {
        genderEnumAttributeId.setName(GENDER_ENUM_ATTRIBUTE_NAME_NAME);
        MessageList validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME));
    }

    public void testValidateDuplicateNameInSupertypeHierarchy() throws CoreException {
        inheritedEnumAttributeId.setInherited(false);
        MessageList validationMessageList = inheritedEnumAttributeId.validate(ipsProject);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_NAME_IN_SUPERTYPE_HIERARCHY));
    }

    public void testValidateDatatype() throws CoreException {
        IIpsModel ipsModel = getIpsModel();

        // Test data type missing.
        genderEnumAttributeId.setDatatype("");
        MessageList validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_DATATYPE_MISSING));
        genderEnumAttributeId.setDatatype(Datatype.STRING.getQualifiedName());

        // Test data type does not exist.
        ipsModel.clearValidationCache();
        genderEnumAttributeId.setDatatype("FooBar");
        validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_DATATYPE_DOES_NOT_EXIST));
        genderEnumAttributeId.setDatatype(Datatype.STRING.getQualifiedName());

        // Test data type void.
        ipsModel.clearValidationCache();
        genderEnumAttributeId.setDatatype("void");
        validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_VOID));
        genderEnumAttributeId.setDatatype(Datatype.STRING.getQualifiedName());

        // Test data type abstract.
        ipsModel.clearValidationCache();
        IEnumType newEnumType = newEnumType(ipsProject, "NewEnumType");
        newEnumType.setAbstract(true);
        genderEnumAttributeId.setDatatype(newEnumType.getQualifiedName());
        validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_ABSTRACT));
        genderEnumAttributeId.setDatatype(Datatype.STRING.getQualifiedName());

        // Test data type is containing EnumType.
        ipsModel.clearValidationCache();
        genderEnumAttributeId.setDatatype(genderEnumType.getQualifiedName());
        validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_CONTAINING_ENUM_TYPE_OR_SUBCLASS));
        genderEnumAttributeId.setDatatype(Datatype.STRING.getQualifiedName());

        // Test data type is subclass of containing EnumType.
        ipsModel.clearValidationCache();
        genderEnumAttributeId.setDatatype(subEnumType.getQualifiedName());
        validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_DATATYPE_IS_CONTAINING_ENUM_TYPE_OR_SUBCLASS));
        genderEnumAttributeId.setDatatype(Datatype.STRING.getQualifiedName());

        // Test data type is EnumType that does not contain values but parent EnumType does.
        ipsModel.clearValidationCache();
        IEnumAttribute attribute = paymentMode.newEnumAttribute();
        attribute.setName("test");
        attribute.setDatatype(genderEnumType.getQualifiedName());
        validationMessageList = attribute.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_ENUM_DATATYPE_DOES_NOT_CONTAIN_VALUES_BUT_PARENT_ENUM_TYPE_DOES));
    }

    public void testValidateInheritedNoSuchAttributeInSupertypeHierarchy() throws CoreException {
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
    }

    public void testValidateInheritedNoSupertype() throws CoreException {
        IEnumAttribute inheritedAttribute = genderEnumType.newEnumAttribute();
        inheritedAttribute.setName("foo");
        inheritedAttribute.setInherited(true);

        MessageList validationMessageList = inheritedAttribute.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_INHERITED_BUT_NO_SUPERTYPE));
    }

    public void testValidateInheritedNoExistingSupertype() throws CoreException {
        genderEnumType.setSuperEnumType("foo");
        IEnumAttribute inheritedAttribute = genderEnumType.newEnumAttribute();
        inheritedAttribute.setName("foo");
        inheritedAttribute.setInherited(true);

        assertTrue(inheritedAttribute.isValid());
    }

    public void testValidateInherited() throws CoreException {
        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        superEnumType.setAbstract(true);
        genderEnumType.setSuperEnumType(superEnumType.getQualifiedName());
        IEnumAttribute inheritedAttribute = genderEnumType.newEnumAttribute();
        inheritedAttribute.setName("foo");
        inheritedAttribute.setInherited(true);

        IEnumAttribute toInheritAttribute = superEnumType.newEnumAttribute();
        toInheritAttribute.setName("foo");
        toInheritAttribute.setDatatype(Datatype.STRING.getQualifiedName());

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
        genderEnumAttributeName.setIdentifier(true);
        MessageList validationMessageList = genderEnumAttributeId.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumAttribute.MSGCODE_ENUM_ATTRIBUTE_DUPLICATE_USED_AS_ID_IN_FAKTOR_IPS_UI));
    }

    public void testGetEnumType() {
        assertEquals(genderEnumType, genderEnumAttributeId.getEnumType());
    }

    public void testFindDatatype() throws CoreException {
        ValueDatatype datatype = genderEnumAttributeId.findDatatype(ipsProject);
        assertEquals(Datatype.STRING.getQualifiedName(), datatype.getName());

        genderEnumAttributeId.setDatatype("foo");
        assertNull(genderEnumAttributeId.findDatatype(ipsProject));

        try {
            genderEnumAttributeId.findDatatype(null);
            fail();
        } catch (NullPointerException e) {
        }

        // Test inherited.
        genderEnumAttributeId.setDatatype(Datatype.STRING.getQualifiedName());
        assertEquals(Datatype.STRING.getQualifiedName(), inheritedEnumAttributeId.findDatatype(ipsProject).getName());

        genderEnumAttributeId.setInherited(true);
        assertNull(genderEnumAttributeId.findDatatype(ipsProject));
    }

    public void testFindIsUniqueIdentifier() throws CoreException {
        try {
            inheritedEnumAttributeId.findIsUnique(null);
            fail();
        } catch (NullPointerException e) {
        }

        assertTrue(inheritedEnumAttributeId.findIsUnique(ipsProject));
        inheritedEnumAttributeId.setInherited(false);
        assertFalse(inheritedEnumAttributeId.findIsUnique(ipsProject));

        genderEnumAttributeId.setInherited(true);
        assertNull(genderEnumAttributeId.findIsUnique(ipsProject));
    }

    public void testFindIsIdentifier() throws CoreException {
        try {
            inheritedEnumAttributeId.findIsIdentifier(null);
            fail();
        } catch (NullPointerException e) {
        }

        assertTrue(inheritedEnumAttributeId.findIsIdentifier(ipsProject));
        inheritedEnumAttributeId.setInherited(false);
        assertFalse(inheritedEnumAttributeId.findIsIdentifier(ipsProject));

        genderEnumAttributeId.setInherited(true);
        assertNull(genderEnumAttributeId.findIsIdentifier(ipsProject));
    }

    public void testFindIsUsedAsNameInFaktorIpsUi() throws CoreException {
        try {
            inheritedEnumAttributeId.findIsUsedAsNameInFaktorIpsUi(null);
            fail();
        } catch (NullPointerException e) {
        }

        assertTrue(inheritedEnumAttributeName.findIsUsedAsNameInFaktorIpsUi(ipsProject));
        inheritedEnumAttributeName.setInherited(false);
        assertFalse(inheritedEnumAttributeName.findIsUsedAsNameInFaktorIpsUi(ipsProject));

        genderEnumAttributeName.setInherited(true);
        assertNull(genderEnumAttributeName.findIsUsedAsNameInFaktorIpsUi(ipsProject));
    }

    public void testGetSetUsedAsNameInFaktorIpsUi() {
        assertTrue(genderEnumAttributeName.isUsedAsNameInFaktorIpsUi());
        assertFalse(genderEnumAttributeId.isUsedAsNameInFaktorIpsUi());
        genderEnumAttributeName.setUsedAsNameInFaktorIpsUi(false);
        assertFalse(genderEnumAttributeName.isUsedAsNameInFaktorIpsUi());
    }

    public void testGetSetUsedAsIdInFaktorIpsUi() {
        assertTrue(genderEnumAttributeId.isIdentifier());
        assertFalse(genderEnumAttributeName.isIdentifier());
        genderEnumAttributeId.setIdentifier(false);
        assertFalse(genderEnumAttributeId.isIdentifier());
    }

    public void testIsLiteralNameDefaultValueProvider() {
        assertFalse(paymentMode.getEnumAttribute("id").isLiteralNameDefaultValueProvider());
        assertTrue(paymentMode.getEnumAttribute("name").isLiteralNameDefaultValueProvider());
    }

    public void testIsEnumLiteralNameAttribute() {
        assertFalse(genderEnumAttributeId.isEnumLiteralNameAttribute());
        assertTrue(paymentMode.getEnumAttributes(true).get(0).isEnumLiteralNameAttribute());
    }

}
