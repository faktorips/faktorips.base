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

import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.ipsobject.DescriptionHelper;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

public class EnumTypeTest extends AbstractIpsEnumPluginTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testGetSetSuperEnumType() {
        assertEquals("", genderEnumType.getSuperEnumType());
        genderEnumType.setSuperEnumType("OtherEnumTypeName");
        assertEquals("OtherEnumTypeName", genderEnumType.getSuperEnumType());

        try {
            genderEnumType.setSuperEnumType(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetSetAbstract() {
        assertFalse(genderEnumType.isAbstract());
        genderEnumType.setAbstract(true);
        assertTrue(genderEnumType.isAbstract());
    }

    public void testGetSetContainingValues() {
        assertFalse(genderEnumType.isContainingValues());
        genderEnumType.setContainingValues(true);
        assertTrue(genderEnumType.isContainingValues());
    }

    public void testGetIpsObjectType() {
        assertEquals(IpsObjectType.ENUM_TYPE, genderEnumType.getIpsObjectType());
    }

    public void testGetEnumAttributes() throws CoreException {
        IEnumAttribute inheritedEnumAttribute = genderEnumType.newEnumAttribute();
        inheritedEnumAttribute.setInherited(true);

        List<IEnumAttribute> attributes = genderEnumType.getEnumAttributes();
        assertEquals(2, attributes.size());
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, attributes.get(0).getName());
        assertEquals(GENDER_ENUM_ATTRIBUTE_NAME_NAME, attributes.get(1).getName());
    }

    public void testGetEnumAttributesIncludeSupertypeCopies() throws CoreException {
        IEnumAttribute inheritedEnumAttribute = genderEnumType.newEnumAttribute();
        inheritedEnumAttribute.setName("foo");
        inheritedEnumAttribute.setInherited(true);

        List<IEnumAttribute> attributes = genderEnumType.getEnumAttributesIncludeSupertypeCopies();
        assertEquals(3, attributes.size());
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, attributes.get(0).getName());
        assertEquals(GENDER_ENUM_ATTRIBUTE_NAME_NAME, attributes.get(1).getName());
        assertEquals("foo", attributes.get(2).getName());
    }

    public void testFindAllEnumAttributesIncludeSupertypeOriginals() throws CoreException {
        genderEnumType.setAbstract(true);
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        IEnumAttribute ownedEnumAttribute = subEnumType.newEnumAttribute();
        ownedEnumAttribute.setName("ownedEnumAttribute");
        ownedEnumAttribute.setDatatype(STRING_DATATYPE_NAME);

        List<IEnumAttribute> enumAttributesIncludeSupertypeOriginals = subEnumType
                .findAllEnumAttributesIncludeSupertypeOriginals();
        assertEquals(3, enumAttributesIncludeSupertypeOriginals.size());
        assertEquals(ownedEnumAttribute, enumAttributesIncludeSupertypeOriginals.get(0));
        assertEquals(genderEnumAttributeId, enumAttributesIncludeSupertypeOriginals.get(1));
        assertEquals(genderEnumAttributeName, enumAttributesIncludeSupertypeOriginals.get(2));
    }

    public void testGetEnumAttribute() throws CoreException {
        try {
            genderEnumType.getEnumAttribute(null);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumAttribute inheritedEnumAttribute = genderEnumType.newEnumAttribute();
        inheritedEnumAttribute.setInherited(true);
        inheritedEnumAttribute.setName("foo");

        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttribute(GENDER_ENUM_ATTRIBUTE_ID_NAME));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttribute(GENDER_ENUM_ATTRIBUTE_NAME_NAME));
        assertNull(genderEnumType.getEnumAttribute("foo"));
    }

    public void testGetEnumAttributeIncludeSupertypeCopies() throws CoreException {
        try {
            genderEnumType.getEnumAttributeIncludeSupertypeCopies(null);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumAttribute inheritedEnumAttribute = genderEnumType.newEnumAttribute();
        inheritedEnumAttribute.setInherited(true);
        inheritedEnumAttribute.setName("foo");

        assertEquals(genderEnumAttributeId, genderEnumType
                .getEnumAttributeIncludeSupertypeCopies(GENDER_ENUM_ATTRIBUTE_ID_NAME));
        assertEquals(genderEnumAttributeName, genderEnumType
                .getEnumAttributeIncludeSupertypeCopies(GENDER_ENUM_ATTRIBUTE_NAME_NAME));
        assertEquals(inheritedEnumAttribute, genderEnumType.getEnumAttributeIncludeSupertypeCopies("foo"));
    }

    public void testFindEnumAttributeIncludeSupertypeOriginals() throws CoreException {
        try {
            genderEnumType.findEnumAttributeIncludeSupertypeOriginals(null);
            fail();
        } catch (NullPointerException e) {
        }

        genderEnumType.setAbstract(true);
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        IEnumAttribute inheritedId = subEnumType.newEnumAttribute();
        inheritedId.setInherited(true);
        inheritedId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);

        assertEquals(genderEnumAttributeId, subEnumType
                .findEnumAttributeIncludeSupertypeOriginals(GENDER_ENUM_ATTRIBUTE_ID_NAME));
    }

    public void testGetEnumAttributesCount() throws CoreException {
        IEnumAttribute inheritedEnumAttribute = genderEnumType.newEnumAttribute();
        inheritedEnumAttribute.setInherited(true);

        assertEquals(3, genderEnumType.getEnumAttributesCount(true));
        assertEquals(2, genderEnumType.getEnumAttributesCount(false));
    }

    public void testGetChildren() throws CoreException {
        assertEquals(2, genderEnumType.getChildren().length);
    }

    public void testNewEnumAttribute() throws CoreException {
        IEnumValue modelSideEnumValue = genderEnumType.newEnumValue();
        IEnumAttribute description = genderEnumType.newEnumAttribute();
        description.setName("Description");
        description.setDatatype(STRING_DATATYPE_NAME);

        List<IEnumAttribute> attributes = genderEnumType.getEnumAttributes();
        assertEquals(3, attributes.size());
        assertEquals("Description", attributes.get(2).getName());

        List<IEnumAttributeValue> attributeValues = modelSideEnumValue.getEnumAttributeValues();
        assertEquals(3, attributeValues.size());
        assertEquals(attributes.get(0), attributeValues.get(0).findEnumAttribute());
        assertEquals(attributes.get(1), attributeValues.get(1).findEnumAttribute());
        assertEquals(attributes.get(2), attributeValues.get(2).findEnumAttribute());
    }

    public void testFindEnumType() throws CoreException {
        assertEquals(genderEnumType, genderEnumType.findEnumType());
    }

    public void testMoveEnumAttributeUp() throws CoreException {
        try {
            genderEnumType.moveEnumAttribute(null, true);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumAttribute newEnumAttribute = genderEnumType.newEnumAttribute();
        IEnumValue newEnumValue = genderEnumType.newEnumValue();
        genderEnumType.setContainingValues(true);

        IEnumAttributeValue valueId = newEnumValue.getEnumAttributeValues().get(0);
        IEnumAttributeValue valueName = newEnumValue.getEnumAttributeValues().get(1);
        IEnumAttributeValue valueNew = newEnumValue.getEnumAttributeValues().get(2);

        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(1));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(2));

        int newIndex;
        newIndex = genderEnumType.moveEnumAttribute(newEnumAttribute, true);
        assertEquals(1, newIndex);
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(0));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(1));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(2));

        newIndex = genderEnumType.moveEnumAttribute(newEnumAttribute, true);
        assertEquals(0, newIndex);
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(1));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(2));

        // Nothing must change if the enum attribute is the first one already
        newIndex = genderEnumType.moveEnumAttribute(newEnumAttribute, true);
        assertEquals(0, newIndex);
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(1));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(2));
    }

    public void testMoveEnumAttributeUpValuesPartOfModel() throws CoreException {
        IEnumAttributeValue valueId = genderEnumValueMale.getEnumAttributeValues().get(0);
        IEnumAttributeValue valueName = genderEnumValueMale.getEnumAttributeValues().get(1);

        genderEnumType.setContainingValues(true);
        genderEnumType.moveEnumAttribute(genderEnumAttributeName, true);

        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(1));

        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(1));
    }

    public void testMoveEnumAttributeDown() throws CoreException {
        try {
            genderEnumType.moveEnumAttribute(null, false);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumAttribute newEnumAttribute = genderEnumType.newEnumAttribute();
        IEnumValue newEnumValue = genderEnumType.newEnumValue();
        genderEnumType.setContainingValues(true);

        IEnumAttributeValue valueId = newEnumValue.getEnumAttributeValues().get(0);
        IEnumAttributeValue valueName = newEnumValue.getEnumAttributeValues().get(1);
        IEnumAttributeValue valueNew = newEnumValue.getEnumAttributeValues().get(2);

        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(1));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(2));

        int newIndex;
        newIndex = genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        assertEquals(1, newIndex);
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(1));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(2));

        newIndex = genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        assertEquals(2, newIndex);
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(0));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(1));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(2));

        // Nothing must change if the enum attribute is the last one already
        newIndex = genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        assertEquals(2, newIndex);
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(0));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes().get(1));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(2));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(2));
    }

    public void testMoveEnumAttributeDownValuesPartOfModel() throws CoreException {
        IEnumAttributeValue valueId = genderEnumValueMale.getEnumAttributeValues().get(0);
        IEnumAttributeValue valueName = genderEnumValueMale.getEnumAttributeValues().get(1);

        genderEnumType.setContainingValues(true);
        genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);

        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes().get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes().get(1));

        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(1));
    }

    public void testXml() throws CoreException, ParserConfigurationException {
        IEnumType newEnumType = newEnumType(ipsProject, "NewEnumType");
        newEnumType.setAbstract(true);
        newEnumType.setContainingValues(true);
        newEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        newEnumType.setEnumContentPackageFragment("bar");
        newEnumType.newEnumAttribute();

        Element xmlElement = newEnumType.toXml(createXmlDocument(IEnumType.XML_TAG));
        assertNotNull(XmlUtil.getFirstElement(xmlElement, DescriptionHelper.XML_ELEMENT_NAME));

        assertTrue(Boolean.parseBoolean(xmlElement.getAttribute(IEnumType.PROPERTY_ABSTRACT)));
        assertTrue(Boolean.parseBoolean(xmlElement.getAttribute(IEnumType.PROPERTY_CONTAINING_VALUES)));
        assertEquals(genderEnumType.getQualifiedName(), xmlElement.getAttribute(IEnumType.PROPERTY_SUPERTYPE));
        assertEquals("bar", xmlElement.getAttribute(IEnumType.PROPERTY_ENUM_CONTENT_PACKAGE_FRAGMENT));

        IEnumType loadedEnumType = newEnumType(ipsProject, "LoadedEnumType");
        loadedEnumType.initFromXml(xmlElement);
        assertTrue(loadedEnumType.isAbstract());
        assertTrue(loadedEnumType.isContainingValues());
        assertEquals(genderEnumType.getQualifiedName(), loadedEnumType.getSuperEnumType());
        assertEquals("bar", loadedEnumType.getEnumContentPackageFragment());
    }

    public void testDeleteEnumAttributeWithValues() throws CoreException {
        IEnumType newEnumType = newEnumType(ipsProject, "NewEnumType");
        IEnumAttribute newEnumAttribute = newEnumType.newEnumAttribute();
        try {
            genderEnumType.deleteEnumAttributeWithValues(newEnumAttribute);
            fail();
        } catch (IllegalArgumentException e) {
        }

        IEnumValue modelValue = genderEnumType.newEnumValue();

        genderEnumType.deleteEnumAttributeWithValues(genderEnumAttributeId);
        List<IEnumAttribute> enumAttributes = genderEnumType.getEnumAttributes();
        assertEquals(1, enumAttributes.size());
        assertEquals(genderEnumAttributeName, enumAttributes.get(0));
        List<IEnumAttributeValue> enumAttributeValues = genderEnumValueMale.getEnumAttributeValues();
        assertEquals(2, enumAttributeValues.size());
        assertEquals(1, modelValue.getEnumAttributeValues().size());

        try {
            genderEnumType.deleteEnumAttributeWithValues(null);
        } catch (NullPointerException e) {
        }

        try {
            genderEnumType.deleteEnumAttributeWithValues(genderEnumAttributeId);
            fail();
        } catch (IllegalArgumentException e) {
        }

        genderEnumType.deleteEnumAttributeWithValues(genderEnumAttributeName);
        assertEquals(0, genderEnumType.getEnumAttributes().size());
        assertEquals(2, genderEnumValueMale.getEnumAttributeValues().size());
        assertEquals(0, modelValue.getEnumAttributeValues().size());
    }

    public void testGetJavaClassName() {
        try {
            genderEnumType.getJavaClassName();
        } catch (UnsupportedOperationException e) {
        }
    }

    public void testHasNullObject() {
        assertFalse(genderEnumType.hasNullObject());
    }

    public void testIsPrimitive() {
        assertFalse(genderEnumType.isPrimitive());
    }

    public void testIsValueDatatype() {
        assertTrue(genderEnumType.isValueDatatype());
    }

    public void testIsVoid() {
        assertFalse(genderEnumType.isVoid());
    }

    @SuppressWarnings("unchecked")
    public void testCompreTo() throws Exception {
        try {
            genderEnumType.compareTo(new Object());
            fail("ClassCastException is expected.");
        } catch (ClassCastException e) {
        }
        assertEquals(0, genderEnumType.compareTo(genderEnumType));
        assertTrue(genderEnumType.compareTo(paymentMode) < 0);
        assertTrue(paymentMode.compareTo(genderEnumType) > 0);

    }

    public void testGetAllValueIds() throws Exception {
        String[] ids = paymentMode.getAllValueIds(false);
        assertEquals(2, ids.length);
        List<String> idList = Arrays.asList(ids);
        assertTrue(idList.contains("monthly"));
        assertTrue(idList.contains("annually"));

        IEnumType color = newEnumType(ipsProject, "Color");
        color.setAbstract(false);
        color.setContainingValues(true);

        IEnumAttribute id = color.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setLiteralName(false);
        id.setName("name");
        IEnumValue red = color.newEnumValue();
        IEnumAttributeValue redN = red.getEnumAttributeValues().get(0);
        redN.setValue("red");
        IEnumValue blue = color.newEnumValue();
        IEnumAttributeValue blueN = blue.getEnumAttributeValues().get(0);
        blueN.setValue("blue");

        String[] colorIds = color.getAllValueIds(false);
        // is expected to be null because the literal name attribute is not specified for the enum
        // type
        assertNull(colorIds);
    }

    public void testgGetValueName() {
        assertNotNull(paymentMode.getValueName("monthly"));
        assertNotNull(paymentMode.getValueName("annually"));
        assertNull(paymentMode.getValueName("quarterly"));
    }

    public void testAreValuesEqual() {
        assertTrue(paymentMode.areValuesEqual("monthly", "monthly"));
        assertFalse(paymentMode.areValuesEqual("monthly", "annually"));
        try {
            paymentMode.areValuesEqual("monthly", "quarterly");
            fail("");
        } catch (Exception e) {
        }
    }

    public void testCheckReadyToUse() {
        MessageList msgList = paymentMode.checkReadyToUse();
        assertFalse(msgList.containsErrorMsg());
        paymentMode.getEnumAttributes().get(0).delete();
        msgList = paymentMode.checkReadyToUse();
        assertTrue(msgList.containsErrorMsg());
    }

    public void testIsParsable() {
        assertTrue(paymentMode.isParsable("monthly"));
        assertFalse(paymentMode.isParsable("quarterly"));
    }

    public void testEnumAttributeExists() {
        try {
            genderEnumType.enumAttributeExists(null);
            fail();
        } catch (NullPointerException e) {
        }

        assertTrue(genderEnumType.enumAttributeExists(GENDER_ENUM_ATTRIBUTE_ID_NAME));
        assertFalse(genderEnumType.enumAttributeExists("FooBar"));
    }

    public void testValidateThis() throws CoreException {
        assertTrue(genderEnumType.isValid());
    }

    public void testValidateSuperEnumType() throws CoreException {
        IIpsModel ipsModel = getIpsModel();

        // Test super enum type does not exit
        genderEnumType.setSuperEnumType("FooBar");
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_DOES_NOT_EXIST));

        // Test super enum type is not abstract
        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        genderEnumType.setSuperEnumType(superEnumType.getQualifiedName());
        ipsModel.clearValidationCache();
        validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_IS_NOT_ABSTRACT));
        superEnumType.setAbstract(true);
        ipsModel.clearValidationCache();
        assertTrue(genderEnumType.isValid());
    }

    public void testValidateInheritedAttributes() throws CoreException {
        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        superEnumType.setAbstract(true);
        genderEnumType.setSuperEnumType(superEnumType.getQualifiedName());
        IEnumType superSuperEnumType = newEnumType(ipsProject, "SuperSuperEnumType");
        superSuperEnumType.setAbstract(true);
        superEnumType.setSuperEnumType(superSuperEnumType.getQualifiedName());

        IEnumAttribute attr1 = superEnumType.newEnumAttribute();
        attr1.setName("attr1");
        attr1.setDatatype(STRING_DATATYPE_NAME);
        attr1.setLiteralName(true);
        attr1.setUniqueIdentifier(true);
        IEnumAttribute attr2 = superSuperEnumType.newEnumAttribute();
        attr2.setName("attr2");
        attr2.setDatatype(INTEGER_DATATYPE_NAME);
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_NOT_INHERITED_ATTRIBUTES_IN_SUPERTYPE_HIERARCHY));

        attr1 = genderEnumType.newEnumAttribute();
        attr1.setName("attr1");
        attr1.setInherited(true);
        attr2 = genderEnumType.newEnumAttribute();
        attr2.setName("attr2");
        attr2.setInherited(true);
        genderEnumAttributeId.setLiteralName(false);
        getIpsModel().clearValidationCache();
        assertTrue(genderEnumType.isValid());
    }

    public void testValidateLiteralNameAttribute() throws CoreException {
        genderEnumAttributeId.setLiteralName(false);
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_NO_LITERAL_NAME_ATTRIBUTE));

        genderEnumType.setAbstract(true);
        getIpsModel().clearValidationCache();
        assertTrue(genderEnumType.isValid());
    }

    public void testFindSuperEnumType() throws CoreException {
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        assertEquals(genderEnumType, subEnumType.findSuperEnumType());
    }

    public void testGetIndexOfEnumAttribute() throws CoreException {
        assertEquals(0, genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeId));
        assertEquals(1, genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeName));

        genderEnumType.moveEnumAttribute(genderEnumAttributeName, true);
        assertEquals(1, genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeId));
        assertEquals(0, genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeName));
    }

    public void testHasSuperEnumType() throws CoreException {
        assertFalse(genderEnumType.hasSuperEnumType());

        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        genderEnumType.setSuperEnumType(superEnumType.getQualifiedName());
        assertTrue(genderEnumType.hasSuperEnumType());
    }

    public void testFindSuperEnumTypes() throws CoreException {
        assertEquals(0, genderEnumType.findAllSuperEnumTypes().size());

        IEnumType rootEnumType = newEnumType(ipsProject, "RootEnumType");
        IEnumType level1EnumType = newEnumType(ipsProject, "Level1EnumType");
        level1EnumType.setSuperEnumType(rootEnumType.getQualifiedName());
        genderEnumType.setSuperEnumType(level1EnumType.getQualifiedName());

        List<IEnumType> superEnumTypes = genderEnumType.findAllSuperEnumTypes();
        assertEquals(2, superEnumTypes.size());
        assertEquals(level1EnumType, superEnumTypes.get(0));
        assertEquals(rootEnumType, superEnumTypes.get(1));
    }

    public void testGetSetEnumContentPackageFragment() {
        assertEquals(DEFAULT_PACKAGE_FRAGMENT, genderEnumType.getEnumContentPackageFragment());
        genderEnumType.setEnumContentPackageFragment("bar");
        assertEquals("bar", genderEnumType.getEnumContentPackageFragment());
    }

    public void testGetLiteralNameAttribute() throws CoreException {
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        IEnumAttribute inheritedLiteralNameAttribute = subEnumType.newEnumAttribute();
        inheritedLiteralNameAttribute.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);
        inheritedLiteralNameAttribute.setInherited(true);

        assertEquals(inheritedLiteralNameAttribute, subEnumType.getLiteralNameAttribute());
    }

    public void testGetEnumValue() throws Exception {
        IEnumValue annually = paymentMode.getEnumValue("annually");
        assertNotNull(annually);
        IEnumValue monthly = paymentMode.getEnumValue("monthly");
        assertNotNull(monthly);
        IEnumValue quarterly = paymentMode.getEnumValue("quarterly");
        assertNull(quarterly);
        assertNull(paymentMode.getEnumValue(null));
    }

    public void testFindInheritEnumAttributeCandidates() throws CoreException {
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());

        List<IEnumAttribute> inheritEnumAttributeCandidates = subEnumType.findInheritEnumAttributeCandidates();
        assertEquals(2, inheritEnumAttributeCandidates.size());
        assertEquals(genderEnumAttributeId, inheritEnumAttributeCandidates.get(0));
        assertEquals(genderEnumAttributeName, inheritEnumAttributeCandidates.get(1));

        assertEquals(0, genderEnumType.findInheritEnumAttributeCandidates().size());
    }

    public void testInheritEnumAttributes() throws CoreException {
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());

        List<IEnumAttribute> inheritEnumAttributeCandidates = subEnumType.findInheritEnumAttributeCandidates();
        // Inherit one manually, this one needs to be skipped by the method later
        IEnumAttribute inheritedId = subEnumType.newEnumAttribute();
        inheritedId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);
        inheritedId.setInherited(true);

        subEnumType.inheritEnumAttributes(inheritEnumAttributeCandidates);
        assertEquals(2, subEnumType.getEnumAttributesCount(true));
        IEnumAttribute inheritedName = subEnumType
                .getEnumAttributeIncludeSupertypeCopies((GENDER_ENUM_ATTRIBUTE_NAME_NAME));
        assertNotNull(inheritedName);
        assertTrue(inheritedName.isInherited());

        try {
            IEnumAttribute notInSupertypeHierarchyAttribute = paymentMode.newEnumAttribute();
            notInSupertypeHierarchyAttribute.setName("foo");
            notInSupertypeHierarchyAttribute.setDatatype(STRING_DATATYPE_NAME);
            subEnumType.inheritEnumAttributes(Arrays.asList(new IEnumAttribute[] { notInSupertypeHierarchyAttribute }));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }
    
}
