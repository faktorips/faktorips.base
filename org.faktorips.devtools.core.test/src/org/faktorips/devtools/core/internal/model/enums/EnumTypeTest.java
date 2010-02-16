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
import org.faktorips.devtools.core.model.ContentChangeEvent;
import org.faktorips.devtools.core.model.ContentsChangeListener;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
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
        IEnumAttribute inheritedEnumAttribute = paymentMode.newEnumAttribute();
        inheritedEnumAttribute.setInherited(true);

        List<IEnumAttribute> attributes = paymentMode.getEnumAttributes(false);
        assertEquals(2, attributes.size());
        assertEquals("id", attributes.get(0).getName());
        assertEquals("name", attributes.get(1).getName());

        attributes = paymentMode.getEnumAttributes(true);
        assertEquals(3, attributes.size());
        assertEquals("LITERAL_NAME", attributes.get(0).getName());
        assertEquals("id", attributes.get(1).getName());
        assertEquals("name", attributes.get(2).getName());
    }

    public void testGetEnumAttributesIncludeSupertypeCopies() throws CoreException {
        IEnumAttribute inheritedEnumAttribute = paymentMode.newEnumAttribute();
        inheritedEnumAttribute.setName("foo");
        inheritedEnumAttribute.setInherited(true);

        List<IEnumAttribute> attributes = paymentMode.getEnumAttributesIncludeSupertypeCopies(false);
        assertEquals(3, attributes.size());
        assertEquals("id", attributes.get(0).getName());
        assertEquals("name", attributes.get(1).getName());
        assertEquals("foo", attributes.get(2).getName());

        attributes = paymentMode.getEnumAttributesIncludeSupertypeCopies(true);
        assertEquals(4, attributes.size());
        assertEquals("LITERAL_NAME", attributes.get(0).getName());
        assertEquals("id", attributes.get(1).getName());
        assertEquals("name", attributes.get(2).getName());
        assertEquals("foo", attributes.get(3).getName());
    }

    public void testFindAllEnumAttributesIncludeSupertypeOriginals() throws CoreException {
        try {
            genderEnumType.findAllEnumAttributesIncludeSupertypeOriginals(false, null);
            fail();
        } catch (NullPointerException e) {
        }

        genderEnumType.setAbstract(true);
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        subEnumType.newEnumLiteralNameAttribute();
        IEnumAttribute ownedEnumAttribute = subEnumType.newEnumAttribute();
        ownedEnumAttribute.setName("ownedEnumAttribute");
        ownedEnumAttribute.setDatatype(Datatype.STRING.getQualifiedName());

        List<IEnumAttribute> allWithoutLiterals = subEnumType.findAllEnumAttributesIncludeSupertypeOriginals(false,
                ipsProject);
        assertEquals(3, allWithoutLiterals.size());
        assertEquals(ownedEnumAttribute, allWithoutLiterals.get(0));
        assertEquals(genderEnumAttributeId, allWithoutLiterals.get(1));
        assertEquals(genderEnumAttributeName, allWithoutLiterals.get(2));

        List<IEnumAttribute> allWithLiterals = subEnumType.findAllEnumAttributesIncludeSupertypeOriginals(true,
                ipsProject);
        assertEquals(4, allWithLiterals.size());
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
            genderEnumType.findEnumAttributeIncludeSupertypeOriginals(ipsProject, null);
            fail();
        } catch (NullPointerException e) {
        }

        try {
            genderEnumType.findEnumAttributeIncludeSupertypeOriginals(null, GENDER_ENUM_ATTRIBUTE_ID_NAME);
            fail();
        } catch (NullPointerException e) {
        }

        genderEnumType.setAbstract(true);
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        IEnumAttribute inheritedId = subEnumType.newEnumAttribute();
        inheritedId.setInherited(true);
        inheritedId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);

        assertEquals(genderEnumAttributeId, subEnumType.findEnumAttributeIncludeSupertypeOriginals(ipsProject,
                GENDER_ENUM_ATTRIBUTE_ID_NAME));
    }

    public void testGetEnumAttributesCount() throws CoreException {
        IEnumAttribute inheritedEnumAttribute = genderEnumType.newEnumAttribute();
        inheritedEnumAttribute.setInherited(true);

        assertEquals(3, genderEnumType.getEnumAttributesCountIncludeSupertypeCopies(true));
        assertEquals(2, genderEnumType.getEnumAttributesCount(true));

        inheritedEnumAttribute = paymentMode.newEnumAttribute();
        inheritedEnumAttribute.setInherited(true);
        assertEquals(3, paymentMode.getEnumAttributesCount(true));
        assertEquals(2, paymentMode.getEnumAttributesCount(false));
        assertEquals(4, paymentMode.getEnumAttributesCountIncludeSupertypeCopies(true));
        assertEquals(3, paymentMode.getEnumAttributesCountIncludeSupertypeCopies(false));
    }

    public void testGetChildren() throws CoreException {
        assertEquals(2, genderEnumType.getChildren().length);
    }

    public void testNewEnumAttribute() throws CoreException {
        IEnumValue newPaymentMode = paymentMode.newEnumValue();
        contentsChangeCounter.reset();
        IEnumAttribute description = paymentMode.newEnumAttribute();
        assertEquals(1, contentsChangeCounter.getCounts());
        description.setName("description");
        description.setDatatype(Datatype.STRING.getQualifiedName());

        List<IEnumAttribute> attributes = paymentMode.getEnumAttributes(true);
        assertEquals(4, attributes.size());
        assertEquals("description", attributes.get(3).getName());

        List<IEnumAttributeValue> attributeValues = newPaymentMode.getEnumAttributeValues();
        assertEquals(4, attributeValues.size());
        assertEquals(attributes.get(0), attributeValues.get(0).findEnumAttribute(ipsProject));
        assertEquals(attributes.get(1), attributeValues.get(1).findEnumAttribute(ipsProject));
        assertEquals(attributes.get(2), attributeValues.get(2).findEnumAttribute(ipsProject));
        assertEquals(attributes.get(3), attributeValues.get(3).findEnumAttribute(ipsProject));
        assertEquals(paymentMode.getEnumLiteralNameAttribute(), attributeValues.get(0).findEnumAttribute(ipsProject));

        try {
            attributeValues.get(0).findEnumAttribute(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testNewEnumLiteralNameAttribute() throws CoreException {
        genderEnumType.setContainingValues(true);
        IEnumValue modelSideEnumValue = genderEnumType.newEnumValue();
        IEnumLiteralNameAttribute literal = genderEnumType.newEnumLiteralNameAttribute();
        assertEquals(literal, genderEnumType.getEnumLiteralNameAttribute());

        List<IEnumAttributeValue> attributeValues = modelSideEnumValue.getEnumAttributeValues();
        assertEquals(3, attributeValues.size());
        assertEquals(literal, attributeValues.get(2).findEnumAttribute(ipsProject));
    }

    public void testFindEnumType() throws CoreException {
        try {
            genderEnumType.findEnumType(null);
            fail();
        } catch (NullPointerException e) {
        }

        assertEquals(genderEnumType, genderEnumType.findEnumType(ipsProject));
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

        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(0));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(2));

        int newIndex;
        contentsChangeCounter.reset();
        newIndex = genderEnumType.moveEnumAttribute(newEnumAttribute, true);
        assertEquals(1, contentsChangeCounter.getCounts());
        assertEquals(1, newIndex);
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(0));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(2));

        newIndex = genderEnumType.moveEnumAttribute(newEnumAttribute, true);
        assertEquals(0, newIndex);
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(2));

        // Nothing must change if the enum attribute is the first one already
        newIndex = genderEnumType.moveEnumAttribute(newEnumAttribute, true);
        assertEquals(0, newIndex);
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(2));
    }

    public void testMoveEnumAttributeUpValuesPartOfModel() throws CoreException {
        IEnumAttributeValue valueId = genderEnumValueMale.getEnumAttributeValues().get(0);
        IEnumAttributeValue valueName = genderEnumValueMale.getEnumAttributeValues().get(1);

        genderEnumType.setContainingValues(true);
        genderEnumType.moveEnumAttribute(genderEnumAttributeName, true);

        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(1));

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

        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(0));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(2));

        int newIndex;
        newIndex = genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        assertEquals(1, newIndex);
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(2));

        newIndex = genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        assertEquals(2, newIndex);
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(0));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(2));

        // Nothing must change if the EnumAttribute is the last one already.
        newIndex = genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        assertEquals(2, newIndex);
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(0));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(2));
    }

    public void testMoveEnumAttributeDownValuesPartOfModel() throws CoreException {
        IEnumAttributeValue valueId = genderEnumValueMale.getEnumAttributeValues().get(0);
        IEnumAttributeValue valueName = genderEnumValueMale.getEnumAttributeValues().get(1);

        genderEnumType.setContainingValues(true);
        genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);

        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(1));

        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(1));
    }

    public void testXml() throws CoreException, ParserConfigurationException {
        IEnumType newEnumType = newEnumType(ipsProject, "NewEnumType");
        newEnumType.setAbstract(true);
        newEnumType.setContainingValues(true);
        newEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        newEnumType.setEnumContentName("bar");
        newEnumType.newEnumAttribute();

        Element xmlElement = newEnumType.toXml(createXmlDocument(IEnumType.XML_TAG));
        assertNotNull(XmlUtil.getFirstElement(xmlElement, DescriptionHelper.XML_ELEMENT_NAME));

        assertTrue(Boolean.parseBoolean(xmlElement.getAttribute(IEnumType.PROPERTY_ABSTRACT)));
        assertTrue(Boolean.parseBoolean(xmlElement.getAttribute(IEnumType.PROPERTY_CONTAINING_VALUES)));
        assertEquals(genderEnumType.getQualifiedName(), xmlElement.getAttribute(IEnumType.PROPERTY_SUPERTYPE));
        assertEquals("bar", xmlElement.getAttribute(IEnumType.PROPERTY_ENUM_CONTENT_NAME));

        IEnumType loadedEnumType = newEnumType(ipsProject, "LoadedEnumType");
        loadedEnumType.initFromXml(xmlElement);
        assertTrue(loadedEnumType.isAbstract());
        assertTrue(loadedEnumType.isContainingValues());
        assertEquals(genderEnumType.getQualifiedName(), loadedEnumType.getSuperEnumType());
        assertEquals("bar", loadedEnumType.getEnumContentName());
    }

    public void testDeleteEnumAttributeWithValues() throws CoreException {
        IEnumType newEnumType = newEnumType(ipsProject, "NewEnumType");
        IEnumAttribute newEnumAttribute = newEnumType.newEnumAttribute();
        assertFalse(genderEnumType.deleteEnumAttributeWithValues(newEnumAttribute));
        assertEquals(2, genderEnumType.getEnumAttributesCount(false));

        IEnumValue modelValue = genderEnumType.newEnumValue();

        contentsChangeCounter.reset();
        genderEnumType.deleteEnumAttributeWithValues(genderEnumAttributeId);
        assertEquals(1, contentsChangeCounter.getCounts());
        List<IEnumAttribute> enumAttributes = genderEnumType.getEnumAttributes(true);
        assertEquals(1, enumAttributes.size());
        assertEquals(genderEnumAttributeName, enumAttributes.get(0));
        List<IEnumAttributeValue> enumAttributeValues = genderEnumValueMale.getEnumAttributeValues();
        assertEquals(2, enumAttributeValues.size());
        assertEquals(1, modelValue.getEnumAttributeValues().size());

        assertFalse(genderEnumType.deleteEnumAttributeWithValues(null));
        assertFalse(genderEnumType.deleteEnumAttributeWithValues(genderEnumAttributeId));

        genderEnumType.deleteEnumAttributeWithValues(genderEnumAttributeName);
        assertEquals(0, genderEnumType.getEnumAttributes(true).size());
        assertEquals(2, genderEnumValueMale.getEnumAttributeValues().size());
        assertEquals(0, modelValue.getEnumAttributeValues().size());
        assertEquals(0, genderEnumType.getEnumValuesCount());
    }

    public void testValidateThis() throws CoreException {
        assertTrue(genderEnumType.isValid());
    }

    public void testValidateSuperEnumType() throws CoreException {
        IIpsModel ipsModel = getIpsModel();

        // Test super enumeration type does not exit.
        genderEnumType.setSuperEnumType("FooBar");
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_DOES_NOT_EXIST));

        // Test super enumeration type is not abstract.
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
        IIpsModel ipsModel = getIpsModel();

        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        superEnumType.setAbstract(true);
        genderEnumType.setSuperEnumType(superEnumType.getQualifiedName());
        IEnumType superSuperEnumType = newEnumType(ipsProject, "SuperSuperEnumType");
        superSuperEnumType.setAbstract(true);
        superEnumType.setSuperEnumType(superSuperEnumType.getQualifiedName());

        IEnumAttribute attr1 = superEnumType.newEnumAttribute();
        attr1.setName("attr1");
        attr1.setDatatype(Datatype.STRING.getQualifiedName());
        attr1.setUnique(true);
        IEnumAttribute attr2 = superSuperEnumType.newEnumAttribute();
        attr2.setName("attr2");
        attr2.setDatatype(Datatype.INTEGER.getQualifiedName());
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_NOT_INHERITED_ATTRIBUTES_IN_SUPERTYPE_HIERARCHY));

        // Test abstract super enumeration type to be valid despite missing inherited attribute.
        ipsModel.clearValidationCache();
        assertTrue(superEnumType.isValid());

        attr1 = genderEnumType.newEnumAttribute();
        attr1.setName("attr1");
        attr1.setInherited(true);
        attr2 = genderEnumType.newEnumAttribute();
        attr2.setName("attr2");
        attr2.setInherited(true);
        ipsModel.clearValidationCache();
        assertTrue(genderEnumType.isValid());
    }

    public void testValidateLiteralNameAttribute() throws CoreException {
        genderEnumType.setContainingValues(true);
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_NO_LITERAL_NAME_ATTRIBUTE));

        genderEnumType.setAbstract(true);
        getIpsModel().clearValidationCache();
        assertTrue(genderEnumType.isValid());

        genderEnumType.setAbstract(false);
        genderEnumType.setContainingValues(false);
        getIpsModel().clearValidationCache();
        assertTrue(genderEnumType.isValid());

        genderEnumType.setContainingValues(true);
        genderEnumType.newEnumLiteralNameAttribute();
        getIpsModel().clearValidationCache();
        assertTrue(genderEnumType.isValid());

        IEnumLiteralNameAttribute literal2 = genderEnumType.newEnumLiteralNameAttribute();
        literal2.setName("LITERAL_NAME2");
        getIpsModel().clearValidationCache();
        validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_MULTIPLE_LITERAL_NAME_ATTRIBUTES));
    }

    public void testValidateUsedAsIdInFaktorIpsUiAttribute() throws CoreException {
        genderEnumAttributeId.setIdentifier(false);
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_NO_USED_AS_ID_IN_FAKTOR_IPS_UI_ATTRIBUTE));
    }

    public void testValidateUsedAsNameInFaktorIpsUiAttribute() throws CoreException {
        genderEnumAttributeName.setUsedAsNameInFaktorIpsUi(false);
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_NO_USED_AS_NAME_IN_FAKTOR_IPS_UI_ATTRIBUTE));
    }

    public void testValidateEnumContentPackageFragment() throws CoreException {
        genderEnumType.setEnumContentName("");
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_ENUM_CONTENT_NAME_EMPTY));
    }

    public void testValidateObsoleteValues() throws CoreException {
        paymentMode.setEnumContentName("EnumContentPlaceholder");
        paymentMode.setContainingValues(false);
        MessageList validationMessageList = paymentMode.validate(ipsProject);
        assertEquals(1, validationMessageList.getNoOfMessages(Message.WARNING));
        assertNotNull(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_ENUM_VALUES_OBSOLETE));

        paymentMode.setContainingValues(true);
        paymentMode.setAbstract(true);
        getIpsModel().clearValidationCache();
        validationMessageList = paymentMode.validate(ipsProject);
        assertEquals(1, validationMessageList.getNoOfMessages(Message.WARNING));
        assertNotNull(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_ENUM_VALUES_OBSOLETE));

        paymentMode.setAbstract(false);
        getIpsModel().clearValidationCache();
        validationMessageList = paymentMode.validate(ipsProject);
        assertEquals(0, validationMessageList.getNoOfMessages(Message.WARNING));
    }

    public void testFindSuperEnumType() throws CoreException {
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        assertEquals(genderEnumType, subEnumType.findSuperEnumType(ipsProject));

        try {
            subEnumType.findSuperEnumType(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    public void testGetIndexOfEnumAttribute() throws CoreException {
        assertEquals(0, genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeId));
        assertEquals(1, genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeName));

        genderEnumType.moveEnumAttribute(genderEnumAttributeName, true);
        assertEquals(1, genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeId));
        assertEquals(0, genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeName));

        assertEquals(-1, genderEnumType.getIndexOfEnumAttribute(paymentMode.getEnumAttributes(false).get(0)));
    }

    public void testGetIndexOfEnumLiteralNameAttribute() throws CoreException {
        assertEquals(0, paymentMode.getIndexOfEnumLiteralNameAttribute());
        paymentMode.moveEnumAttribute(paymentMode.getEnumLiteralNameAttribute(), false);
        assertEquals(1, paymentMode.getIndexOfEnumLiteralNameAttribute());
    }

    public void testHasEnumLiteralNameAttribute() {
        assertFalse(genderEnumType.hasEnumLiteralNameAttribute());
        assertTrue(paymentMode.hasEnumLiteralNameAttribute());
    }

    public void testHasSuperEnumType() throws CoreException {
        assertFalse(genderEnumType.hasSuperEnumType());

        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        genderEnumType.setSuperEnumType(superEnumType.getQualifiedName());
        assertTrue(genderEnumType.hasSuperEnumType());
    }

    public void testFindAllSuperEnumTypes() throws CoreException {
        try {
            genderEnumType.findAllSuperEnumTypes(null);
            fail();
        } catch (NullPointerException e) {
        }

        assertEquals(0, genderEnumType.findAllSuperEnumTypes(ipsProject).size());

        IEnumType rootEnumType = newEnumType(ipsProject, "RootEnumType");
        IEnumType level1EnumType = newEnumType(ipsProject, "Level1EnumType");
        level1EnumType.setSuperEnumType(rootEnumType.getQualifiedName());
        genderEnumType.setSuperEnumType(level1EnumType.getQualifiedName());

        List<IEnumType> superEnumTypes = genderEnumType.findAllSuperEnumTypes(ipsProject);
        assertEquals(2, superEnumTypes.size());
        assertEquals(level1EnumType, superEnumTypes.get(0));
        assertEquals(rootEnumType, superEnumTypes.get(1));
    }

    public void testFindAllSuperEnumTypesWithCycle() throws CoreException {
        IEnumType rootEnumType = newEnumType(ipsProject, "RootEnumType");
        IEnumType level1EnumType = newEnumType(ipsProject, "Level1EnumType");
        IEnumType level2EnumType = newEnumType(ipsProject, "Level2EnumType");
        IEnumType level3EnumType = newEnumType(ipsProject, "Level3EnumType");

        level1EnumType.setSuperEnumType(rootEnumType.getQualifiedName());
        level2EnumType.setSuperEnumType(level3EnumType.getQualifiedName());
        level3EnumType.setSuperEnumType(level2EnumType.getQualifiedName());

        List<IEnumType> superEnumTypes = level3EnumType.findAllSuperEnumTypes(ipsProject);
        assertEquals(2, superEnumTypes.size());
        assertEquals(level2EnumType, superEnumTypes.get(0));
        assertEquals(level3EnumType, superEnumTypes.get(1));
    }

    public void testIsSubEnumTypeOf() throws CoreException {
        IEnumType rootEnumType = newEnumType(ipsProject, "RootEnumType");
        IEnumType level1EnumType = newEnumType(ipsProject, "Level1EnumType");
        IEnumType level2EnumType = newEnumType(ipsProject, "Level2EnumType");
        IEnumType level3EnumType = newEnumType(ipsProject, "Level3EnumType");

        level1EnumType.setSuperEnumType(rootEnumType.getQualifiedName());
        level2EnumType.setSuperEnumType(level1EnumType.getQualifiedName());
        level3EnumType.setSuperEnumType(level2EnumType.getQualifiedName());

        assertFalse(rootEnumType.isSubEnumTypeOf(null, null));
        assertFalse(rootEnumType.isSubEnumTypeOf(null, ipsProject));
        try {
            assertFalse(level1EnumType.isSubEnumTypeOf(rootEnumType, null));
            fail();
        } catch (NullPointerException e) {
        }

        assertFalse(rootEnumType.isSubEnumTypeOf(rootEnumType, ipsProject));
        assertFalse(rootEnumType.isSubEnumTypeOf(level1EnumType, ipsProject));
        assertFalse(rootEnumType.isSubEnumTypeOf(level2EnumType, ipsProject));
        assertFalse(rootEnumType.isSubEnumTypeOf(level3EnumType, ipsProject));

        assertFalse(level1EnumType.isSubEnumTypeOf(level1EnumType, ipsProject));
        assertTrue(level1EnumType.isSubEnumTypeOf(rootEnumType, ipsProject));
        assertFalse(level1EnumType.isSubEnumTypeOf(level2EnumType, ipsProject));
        assertFalse(level1EnumType.isSubEnumTypeOf(level3EnumType, ipsProject));

        assertFalse(level2EnumType.isSubEnumTypeOf(level2EnumType, ipsProject));
        assertTrue(level2EnumType.isSubEnumTypeOf(rootEnumType, ipsProject));
        assertTrue(level2EnumType.isSubEnumTypeOf(level1EnumType, ipsProject));
        assertFalse(level2EnumType.isSubEnumTypeOf(level3EnumType, ipsProject));

        assertFalse(level3EnumType.isSubEnumTypeOf(level3EnumType, ipsProject));
        assertTrue(level3EnumType.isSubEnumTypeOf(rootEnumType, ipsProject));
        assertTrue(level3EnumType.isSubEnumTypeOf(level1EnumType, ipsProject));
        assertTrue(level3EnumType.isSubEnumTypeOf(level2EnumType, ipsProject));

        IIpsProject otherProject = newIpsProject("otherProject");
        IEnumType enumTypeInOtherProject = newEnumType(otherProject, "enumTypeInOtherProject");
        enumTypeInOtherProject.setSuperEnumType(level1EnumType.getQualifiedName());

        assertFalse(enumTypeInOtherProject.isSubEnumTypeOf(rootEnumType, otherProject));
        assertFalse(enumTypeInOtherProject.isSubEnumTypeOf(level1EnumType, otherProject));
        assertFalse(enumTypeInOtherProject.isSubEnumTypeOf(level2EnumType, otherProject));
        assertFalse(enumTypeInOtherProject.isSubEnumTypeOf(level3EnumType, otherProject));

        IIpsObjectPath path = otherProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        otherProject.setIpsObjectPath(path);

        assertTrue(enumTypeInOtherProject.isSubEnumTypeOf(rootEnumType, otherProject));
        assertTrue(enumTypeInOtherProject.isSubEnumTypeOf(level1EnumType, otherProject));
        assertFalse(enumTypeInOtherProject.isSubEnumTypeOf(level2EnumType, otherProject));
        assertFalse(enumTypeInOtherProject.isSubEnumTypeOf(level3EnumType, otherProject));

        level2EnumType.setSuperEnumType(enumTypeInOtherProject.getQualifiedName());
        assertFalse(level2EnumType.isSubEnumTypeOf(enumTypeInOtherProject, ipsProject));
        assertTrue(level2EnumType.isSubEnumTypeOf(enumTypeInOtherProject, otherProject));

        rootEnumType.setSuperEnumType(level3EnumType.getQualifiedName());
        // false because one one super class is in wrong project
        assertFalse(rootEnumType.isSubEnumTypeOf(rootEnumType, ipsProject));
        // true because of a hierarchy-cycle
        assertTrue(rootEnumType.isSubEnumTypeOf(rootEnumType, otherProject));
        // false because of wrong project
        assertFalse(rootEnumType.isSubEnumTypeOf(enumTypeInOtherProject, ipsProject));
        assertFalse(rootEnumType.isSubEnumTypeOf(level1EnumType, ipsProject));
        assertTrue(rootEnumType.isSubEnumTypeOf(enumTypeInOtherProject, otherProject));
        assertTrue(rootEnumType.isSubEnumTypeOf(level1EnumType, otherProject));
        assertTrue(rootEnumType.isSubEnumTypeOf(level2EnumType, otherProject));
        assertTrue(rootEnumType.isSubEnumTypeOf(level3EnumType, otherProject));

    }

    public void testIsSubEnumTypeOrSelf() throws CoreException {
        /*
         * The method isSubEnumTypeOrSelf only checks for self and calls isSubEnumTypeOf so only the
         * "self-case" have to be tested.
         */
        IEnumType rootEnumType = newEnumType(ipsProject, "RootEnumType");

        assertFalse(rootEnumType.isSubEnumTypeOrSelf(null, null));
        assertFalse(rootEnumType.isSubEnumTypeOrSelf(null, ipsProject));
        assertTrue(rootEnumType.isSubEnumTypeOrSelf(rootEnumType, null));
        assertTrue(rootEnumType.isSubEnumTypeOrSelf(rootEnumType, ipsProject));
    }

    public void testGetSetEnumContentPackageFragment() {
        assertEquals(ENUMCONTENTS_NAME, genderEnumType.getEnumContentName());
        genderEnumType.setEnumContentName("bar");
        assertEquals("bar", genderEnumType.getEnumContentName());
    }

    public void testGetEnumValue() throws Exception {
        IEnumValue annually = paymentMode.findEnumValue("P1", ipsProject);
        assertNotNull(annually);
        IEnumValue monthly = paymentMode.findEnumValue("P2", ipsProject);
        assertNotNull(monthly);
        IEnumValue quarterly = paymentMode.findEnumValue("P3", ipsProject);
        assertNull(quarterly);
        assertNull(paymentMode.findEnumValue(null, ipsProject));
    }

    public void testFindInheritEnumAttributeCandidates() throws CoreException {
        try {
            genderEnumType.findInheritEnumAttributeCandidates(null);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());

        List<IEnumAttribute> inheritEnumAttributeCandidates = subEnumType
                .findInheritEnumAttributeCandidates(ipsProject);
        assertEquals(2, inheritEnumAttributeCandidates.size());
        assertEquals(genderEnumAttributeId, inheritEnumAttributeCandidates.get(0));
        assertEquals(genderEnumAttributeName, inheritEnumAttributeCandidates.get(1));

        assertEquals(0, genderEnumType.findInheritEnumAttributeCandidates(ipsProject).size());
    }

    public void testInheritEnumAttributes() throws CoreException {
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());

        List<IEnumAttribute> inheritEnumAttributeCandidates = subEnumType
                .findInheritEnumAttributeCandidates(ipsProject);
        // Inherit one manually, this one needs to be skipped by the method later.
        IEnumAttribute inheritedId = subEnumType.newEnumAttribute();
        inheritedId.setName(GENDER_ENUM_ATTRIBUTE_ID_NAME);
        inheritedId.setInherited(true);

        subEnumType.inheritEnumAttributes(inheritEnumAttributeCandidates);
        assertEquals(2, subEnumType.getEnumAttributesCountIncludeSupertypeCopies(false));
        IEnumAttribute inheritedName = subEnumType
                .getEnumAttributeIncludeSupertypeCopies((GENDER_ENUM_ATTRIBUTE_NAME_NAME));
        assertNotNull(inheritedName);
        assertTrue(inheritedName.isInherited());

        try {
            IEnumAttribute notInSupertypeHierarchyAttribute = paymentMode.newEnumAttribute();
            notInSupertypeHierarchyAttribute.setName("foo");
            notInSupertypeHierarchyAttribute.setDatatype(Datatype.STRING.getQualifiedName());
            subEnumType.inheritEnumAttributes(Arrays.asList(new IEnumAttribute[] { notInSupertypeHierarchyAttribute }));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    public void testDependsOn() throws CoreException {
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnum");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        subEnumType.setAbstract(true);
        IEnumType subSubEnumType = newEnumType(ipsProject, "SubSubEnum");
        subSubEnumType.setSuperEnumType(subEnumType.getQualifiedName());

        IDependency[] dependenciesSubEnumType = subEnumType.dependsOn();
        assertEquals(1, dependenciesSubEnumType.length);
        IDependency[] dependenciesSubSubEnumType = subSubEnumType.dependsOn();
        assertEquals(1, dependenciesSubSubEnumType.length);

        List<IDependency> depencendiesListSubEnumType = Arrays.asList(dependenciesSubEnumType);
        IDependency superEnumTypeDependency = IpsObjectDependency.createReferenceDependency(subEnumType
                .getQualifiedNameType(), subEnumType, IEnumType.PROPERTY_SUPERTYPE, new QualifiedNameType(
                genderEnumType.getQualifiedName(), IpsObjectType.ENUM_TYPE));
        assertTrue(depencendiesListSubEnumType.contains(superEnumTypeDependency));

        List<IDependency> depencendiesListSubSubEnumType = Arrays.asList(dependenciesSubSubEnumType);
        superEnumTypeDependency = IpsObjectDependency.createReferenceDependency(subSubEnumType.getQualifiedNameType(),
                subSubEnumType, IEnumType.PROPERTY_SUPERTYPE, new QualifiedNameType(subEnumType.getQualifiedName(),
                        IpsObjectType.ENUM_TYPE));
        assertTrue(depencendiesListSubSubEnumType.contains(superEnumTypeDependency));
    }

    public void testFindAllMetaObjects() throws CoreException {
        String enumTypeQName = "pack.MyEnumType";
        String enumTypeProj2QName = "otherpack.MyEnumTypeProj2";
        String enum1QName = "pack.MyEnum1";
        String enum2QName = "pack.MyEnum2";
        String enum3QName = "pack.MyEnum3";
        String enumProj2QName = "otherpack.MyEnumProj2";

        IIpsProject referencingProject = newIpsProject("referencingProject");
        IIpsObjectPath path = referencingProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        referencingProject.setIpsObjectPath(path);

        IIpsProject independentProject = newIpsProject("independentProject");

        /*
         * leaveProject1 and leaveProject2 are not directly integrated in any test. But the tested
         * instance search methods have to search in all project that holds a reference to the
         * project of the object. So the search for a Object in e.g. ipsProject have to search for
         * instances in leaveProject1 and leaveProject2. The tests implicit that no duplicates are
         * found.
         */

        IIpsProject leaveProject1 = newIpsProject("LeaveProject1");
        path = leaveProject1.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencingProject);
        leaveProject1.setIpsObjectPath(path);

        IIpsProject leaveProject2 = newIpsProject("LeaveProject2");
        path = leaveProject2.getIpsObjectPath();
        path.newIpsProjectRefEntry(referencingProject);
        leaveProject2.setIpsObjectPath(path);

        EnumType enumType = newEnumType(ipsProject, enumTypeQName);
        EnumContent enum1 = newEnumContent(enumType, enum1QName);
        EnumContent enum2 = newEnumContent(enumType, enum2QName);
        EnumContent enum3 = newEnumContent(ipsProject, enum3QName);

        Object[] result = enumType.searchMetaObjectSrcFiles(true);
        List<Object> resultList = Arrays.asList(result);
        assertEquals(2, result.length);
        assertTrue(resultList.contains(enum1.getIpsSrcFile()));
        assertTrue(resultList.contains(enum2.getIpsSrcFile()));
        assertFalse(resultList.contains(enum3.getIpsSrcFile()));

        EnumContent enumProj2 = newEnumContent(referencingProject, enumProj2QName);
        enumProj2.setEnumType(enumTypeQName);

        result = enumType.searchMetaObjectSrcFiles(true);
        resultList = Arrays.asList(result);
        assertEquals(3, result.length);
        assertTrue(resultList.contains(enum1.getIpsSrcFile()));
        assertTrue(resultList.contains(enum2.getIpsSrcFile()));
        assertTrue(resultList.contains(enumProj2.getIpsSrcFile()));
        assertFalse(resultList.contains(enum3.getIpsSrcFile()));

        EnumType enumTypeProj2 = newEnumType(independentProject, enumTypeProj2QName);

        result = enumTypeProj2.searchMetaObjectSrcFiles(true);
        assertEquals(0, result.length);

        EnumType superEnum = newEnumType(ipsProject, "superEnum");
        superEnum.setAbstract(true);
        enumType.setSuperEnumType(superEnum.getQualifiedName());

        result = enumTypeProj2.searchMetaObjectSrcFiles(false);
        assertEquals(0, result.length);

        result = superEnum.searchMetaObjectSrcFiles(true);
        resultList = Arrays.asList(result);
        assertEquals(3, result.length);
        assertTrue(resultList.contains(enum1.getIpsSrcFile()));
        assertTrue(resultList.contains(enum2.getIpsSrcFile()));
        assertTrue(resultList.contains(enumProj2.getIpsSrcFile()));
        assertFalse(resultList.contains(enum3.getIpsSrcFile()));
    }

    public void testFindIsUsedAsIdInFaktorIpsUiAttribute() throws Exception {
        IEnumType enum1 = newEnumType(ipsProject, "enum1");
        enum1.setAbstract(false);
        enum1.setContainingValues(true);

        IEnumAttribute attr1 = enum1.newEnumAttribute();
        attr1.setDatatype(Datatype.STRING.getQualifiedName());
        attr1.setName("id");
        attr1.setUnique(true);
        attr1.setIdentifier(true);

        IEnumAttribute attr2 = enum1.newEnumAttribute();
        attr2.setDatatype(Datatype.STRING.getQualifiedName());
        attr2.setName("name");
        attr2.setUnique(true);
        attr2.setUsedAsNameInFaktorIpsUi(true);

        IEnumAttribute attr3 = enum1.newEnumAttribute();
        attr3.setDatatype(Datatype.STRING.getQualifiedName());
        attr3.setName("description");
        attr3.setUnique(false);

        IEnumAttribute resultAttr = enum1.findIdentiferAttribute(ipsProject);
        assertEquals(attr1, resultAttr);
    }

    public void testfindIsUsedAsNameInFaktorIpsUiAttribute() throws Exception {
        IEnumType enum1 = newEnumType(ipsProject, "enum1");
        enum1.setAbstract(false);
        enum1.setContainingValues(true);

        IEnumAttribute attr1 = enum1.newEnumAttribute();
        attr1.setDatatype(Datatype.STRING.getQualifiedName());
        attr1.setName("id");
        attr1.setUnique(true);
        attr1.setIdentifier(true);

        IEnumAttribute attr2 = enum1.newEnumAttribute();
        attr2.setDatatype(Datatype.STRING.getQualifiedName());
        attr2.setName("name");
        attr2.setUnique(true);
        attr2.setUsedAsNameInFaktorIpsUi(true);

        IEnumAttribute attr3 = enum1.newEnumAttribute();
        attr3.setDatatype(Datatype.STRING.getQualifiedName());
        attr3.setName("description");
        attr3.setUnique(false);

        IEnumAttribute resultAttr = enum1.findUsedAsNameInFaktorIpsUiAttribute(ipsProject);
        assertEquals(attr2, resultAttr);
    }

    public void testContainsEnumAttribute() throws CoreException {
        genderEnumType.setAbstract(true);
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        IEnumAttribute subAttribute = subEnumType.newEnumAttribute();
        subAttribute.setName("sub");
        subAttribute.setInherited(true);

        assertTrue(genderEnumType.containsEnumAttribute(GENDER_ENUM_ATTRIBUTE_ID_NAME));
        assertFalse(subEnumType.containsEnumAttribute("sub"));
        assertTrue(subEnumType.containsEnumAttributeIncludeSupertypeCopies("sub"));
    }

    public void testContainsEnumLiteralNameAttribute() throws CoreException {
        assertFalse(genderEnumType.containsEnumLiteralNameAttribute());
        genderEnumType.newEnumLiteralNameAttribute();
        assertTrue(genderEnumType.containsEnumLiteralNameAttribute());
    }

    public void testGetEnumLiteralNameAttribute() {
        assertEquals(paymentMode.getEnumAttributes(true).get(0), paymentMode.getEnumLiteralNameAttribute());
    }

    public void testGetEnumLiteralNameAttributesCount() throws CoreException {
        assertEquals(1, paymentMode.getEnumLiteralNameAttributesCount());
        paymentMode.newEnumLiteralNameAttribute();
        assertEquals(2, paymentMode.getEnumLiteralNameAttributesCount());
    }

    public void testIsCapableOfContainingValues() throws CoreException {
        assertFalse(genderEnumType.isCapableOfContainingValues());
        genderEnumType.setAbstract(true);
        genderEnumType.setContainingValues(true);
        assertFalse(genderEnumType.isCapableOfContainingValues());
        genderEnumType.setAbstract(false);
        assertTrue(genderEnumType.isCapableOfContainingValues());
    }

    public class ContentsChangeCounter implements ContentsChangeListener {

        private int counter = 0;

        public int getCounts() {
            return counter;
        }

        public void reset() {
            counter = 0;
        }

        public void contentsChanged(ContentChangeEvent event) {
            counter++;
        }
    }
}
