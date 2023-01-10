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

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.abstracttest.AbstractIpsEnumPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ContentChangeEvent;
import org.faktorips.devtools.model.ContentsChangeListener;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IValidationMsgCodesForInvalidValues;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumAttributeValue;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.internal.dependency.DatatypeDependency;
import org.faktorips.devtools.model.internal.dependency.DependencyDetail;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.internal.value.StringValue;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.runtime.Message;
import org.faktorips.runtime.MessageList;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Element;

public class EnumTypeTest extends AbstractIpsEnumPluginTest {

    @Test
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

    @Test
    public void testGetSetAbstract() {
        assertFalse(genderEnumType.isAbstract());
        genderEnumType.setAbstract(true);
        assertTrue(genderEnumType.isAbstract());
    }

    @Test
    public void testGetSetExtensible() {
        genderEnumType.setExtensible(false);
        assertFalse(genderEnumType.isExtensible());
        genderEnumType.setExtensible(true);
    }

    @Test
    public void testGetSetIdentifierBoundary() {
        assertEquals(null, genderEnumType.getIdentifierBoundary());
        genderEnumType.setIdentifierBoundary("100");
        assertEquals("100", genderEnumType.getIdentifierBoundary());
    }

    @Test
    public void testGetIpsObjectType() {
        assertEquals(IpsObjectType.ENUM_TYPE, genderEnumType.getIpsObjectType());
    }

    @Test
    public void testGetEnumAttributes() {
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

    @Test
    public void testGetEnumAttributesIncludeSupertypeCopies() {
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

    @Test
    public void testFindAllEnumAttributes_allInherited() {
        EnumType abstractGrandParent = newAbstractEnumType("A", null);
        IEnumAttribute idAttributeGP = newEnumAttribute(abstractGrandParent, "id");
        IEnumAttribute nameAttributeGP = newEnumAttribute(abstractGrandParent, "name");

        EnumType abstractParent = newAbstractEnumType("B", abstractGrandParent);
        IEnumAttribute idAttributeP = newEnumAttribute(abstractParent, "id", true);
        IEnumAttribute otherAttributeP = newEnumAttribute(abstractParent, "other");
        IEnumAttribute nameAttributeP = newEnumAttribute(abstractParent, "name", true);

        EnumType concrete = newEnumType(ipsProject, "C");
        concrete.setSuperEnumType(abstractParent.getQualifiedName());
        IEnumAttribute otherAttributeC = newEnumAttribute(concrete, "other", true);
        IEnumAttribute idAttributeC = newEnumAttribute(concrete, "id", true);
        IEnumAttribute localAttribute = newEnumAttribute(concrete, "local");
        IEnumAttribute nameAttributeC = newEnumAttribute(concrete, "name", true);
        IEnumLiteralNameAttribute literalNameAttribute = concrete.newEnumLiteralNameAttribute();

        assertEquals(Arrays.asList(idAttributeGP, nameAttributeGP),
                abstractGrandParent.findAllEnumAttributes(true, ipsProject));
        assertEquals(Arrays.asList(idAttributeP, otherAttributeP, nameAttributeP),
                abstractParent.findAllEnumAttributes(true, ipsProject));
        assertEquals(Arrays.asList(otherAttributeC, idAttributeC, localAttribute, nameAttributeC, literalNameAttribute),
                concrete.findAllEnumAttributes(true, ipsProject));
    }

    @Test
    public void testFindAllEnumAttributes_onlyNew() {
        EnumType abstractGrandParent = newAbstractEnumType("A", null);
        IEnumAttribute idAttributeGP = newEnumAttribute(abstractGrandParent, "id");
        IEnumAttribute nameAttributeGP = newEnumAttribute(abstractGrandParent, "name");

        EnumType abstractParent = newAbstractEnumType("B", abstractGrandParent);
        IEnumAttribute otherAttributeP = newEnumAttribute(abstractParent, "other");

        EnumType concrete = newEnumType(ipsProject, "c");
        concrete.setSuperEnumType(abstractParent.getQualifiedName());
        IEnumAttribute idAttributeC = newEnumAttribute(concrete, "id", true);
        IEnumAttribute otherAttributeC = newEnumAttribute(concrete, "other", true);
        IEnumLiteralNameAttribute literalNameAttribute = concrete.newEnumLiteralNameAttribute();
        IEnumAttribute localAttribute = newEnumAttribute(concrete, "local");
        IEnumAttribute nameAttributeC = newEnumAttribute(concrete, "name", true);

        assertEquals(Arrays.asList(idAttributeGP, nameAttributeGP),
                abstractGrandParent.findAllEnumAttributes(true, ipsProject));
        assertEquals(Arrays.asList(idAttributeGP, nameAttributeGP, otherAttributeP),
                abstractParent.findAllEnumAttributes(true, ipsProject));
        assertEquals(Arrays.asList(idAttributeC, otherAttributeC, literalNameAttribute, localAttribute, nameAttributeC),
                concrete.findAllEnumAttributes(true, ipsProject));
    }

    @Test
    public void testFindAllEnumAttributes_duplicateAttributeName() {
        EnumType parent = newAbstractEnumType("A", null);
        IEnumAttribute idAttributeGP = newEnumAttribute(parent, "id");

        EnumType concrete = newEnumType(ipsProject, "C");
        concrete.setSuperEnumType(parent.getQualifiedName());
        IEnumAttribute idAttributeC = newEnumAttribute(concrete, "id", false);

        assertEquals(Arrays.asList(idAttributeGP), parent.findAllEnumAttributes(true, ipsProject));
        assertEquals(Arrays.asList(idAttributeGP, idAttributeC), concrete.findAllEnumAttributes(true, ipsProject));
    }

    private IEnumAttribute newEnumAttribute(EnumType abstractGrandParent, String name) {
        return newEnumAttribute(abstractGrandParent, name, false);
    }

    private IEnumAttribute newEnumAttribute(EnumType abstractGrandParent, String name, boolean inherited) {
        IEnumAttribute attribute = abstractGrandParent.newEnumAttribute();
        attribute.setName(name);
        if (inherited) {
            attribute.setInherited(inherited);
        }
        return attribute;
    }

    private EnumType newAbstractEnumType(String name, EnumType parent) {
        EnumType abstractEnum = newEnumType(ipsProject, name);
        abstractEnum.setAbstract(true);
        if (parent != null) {
            abstractEnum.setSuperEnumType(parent.getQualifiedName());
        }
        return abstractEnum;
    }

    @Test
    public void testGetEnumAttribute() {
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

    @Test
    public void testGetEnumAttributeIncludeSupertypeCopies() {
        try {
            genderEnumType.getEnumAttributeIncludeSupertypeCopies(null);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumAttribute inheritedEnumAttribute = genderEnumType.newEnumAttribute();
        inheritedEnumAttribute.setInherited(true);
        inheritedEnumAttribute.setName("foo");

        assertEquals(genderEnumAttributeId,
                genderEnumType.getEnumAttributeIncludeSupertypeCopies(GENDER_ENUM_ATTRIBUTE_ID_NAME));
        assertEquals(genderEnumAttributeName,
                genderEnumType.getEnumAttributeIncludeSupertypeCopies(GENDER_ENUM_ATTRIBUTE_NAME_NAME));
        assertEquals(inheritedEnumAttribute, genderEnumType.getEnumAttributeIncludeSupertypeCopies("foo"));
    }

    @Test
    public void testFindEnumAttributeIncludeSupertypeOriginals() {
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

        assertEquals(genderEnumAttributeId,
                subEnumType.findEnumAttributeIncludeSupertypeOriginals(ipsProject, GENDER_ENUM_ATTRIBUTE_ID_NAME));
    }

    @Test
    public void testGetEnumAttributesCount() {
        IEnumAttribute inheritedEnumAttribute = genderEnumType.newEnumAttribute();
        inheritedEnumAttribute.setInherited(true);

        assertEquals(3, genderEnumType.getEnumAttributesCountIncludeSupertypeCopies(false));
        assertEquals(2, genderEnumType.getEnumAttributesCount(false));
        assertEquals(4, genderEnumType.getEnumAttributesCountIncludeSupertypeCopies(true));
        assertEquals(3, genderEnumType.getEnumAttributesCount(true));

        inheritedEnumAttribute = paymentMode.newEnumAttribute();
        inheritedEnumAttribute.setInherited(true);
        assertEquals(3, paymentMode.getEnumAttributesCount(true));
        assertEquals(2, paymentMode.getEnumAttributesCount(false));
        assertEquals(4, paymentMode.getEnumAttributesCountIncludeSupertypeCopies(true));
        assertEquals(3, paymentMode.getEnumAttributesCountIncludeSupertypeCopies(false));
    }

    @Test
    public void testGetChildren() {
        IIpsElement[] children = genderEnumType.getChildren();
        List<IIpsElement> childrenList = Arrays.asList(children);
        assertTrue(childrenList.contains(genderEnumAttributeId));
        assertTrue(childrenList.contains(genderEnumAttributeName));
    }

    @Test
    public void testNewEnumAttribute() {
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

    @Test
    public void testNewEnumLiteralNameAttribute() {
        genderEnumType.setExtensible(false);
        IEnumLiteralNameAttribute literal = genderEnumType.getEnumLiteralNameAttribute();
        IEnumValue modelSideEnumValue = genderEnumType.newEnumValue();

        List<IEnumAttributeValue> attributeValues = modelSideEnumValue.getEnumAttributeValues();
        assertEquals(3, attributeValues.size());
        assertEquals(literal, attributeValues.get(0).findEnumAttribute(ipsProject));
    }

    @Test
    public void testFindEnumType() {
        try {
            genderEnumType.findEnumType(null);
            fail();
        } catch (NullPointerException e) {
        }

        assertEquals(genderEnumType, genderEnumType.findEnumType(ipsProject));
    }

    @Test
    public void testMoveEnumAttributeUp() {
        try {
            genderEnumType.moveEnumAttribute(null, true);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumAttribute newEnumAttribute = genderEnumType.newEnumAttribute();
        IEnumValue newEnumValue = genderEnumType.newEnumValue();
        genderEnumType.setExtensible(false);

        IEnumAttributeValue valueId = newEnumValue.getEnumAttributeValues().get(1);
        IEnumAttributeValue valueName = newEnumValue.getEnumAttributeValues().get(2);
        IEnumAttributeValue valueNew = newEnumValue.getEnumAttributeValues().get(3);

        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(3));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(2));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(3));

        int newIndex;
        contentsChangeCounter.reset();
        newIndex = genderEnumType.moveEnumAttribute(newEnumAttribute, true);
        assertEquals(1, contentsChangeCounter.getCounts());
        assertEquals(2, newIndex);
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(3));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(2));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(3));

        newIndex = genderEnumType.moveEnumAttribute(newEnumAttribute, true);
        assertEquals(1, newIndex);
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(3));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(2));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(3));

        newIndex = genderEnumType.moveEnumAttribute(newEnumAttribute, true);
        assertEquals(0, newIndex);
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(3));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(2));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(3));

        // Nothing must change if the enumeration attribute is the first one already.
        newIndex = genderEnumType.moveEnumAttribute(newEnumAttribute, true);
        assertEquals(0, newIndex);
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(3));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(0));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(2));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(3));

    }

    @Test
    public void testMoveEnumAttributeUpValuesPartOfModel() {
        IEnumAttributeValue valueId = genderEnumValueMale.getEnumAttributeValues().get(0);
        IEnumAttributeValue valueName = genderEnumValueMale.getEnumAttributeValues().get(1);

        genderEnumType.setExtensible(false);
        genderEnumType.moveEnumAttribute(genderEnumAttributeName, true);

        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(2));

        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(1));
    }

    @Test
    public void testMoveEnumAttributeDown() {
        try {
            genderEnumType.moveEnumAttribute(null, false);
            fail();
        } catch (NullPointerException e) {
        }

        IEnumAttribute newEnumAttribute = genderEnumType.newEnumAttribute();
        IEnumValue newEnumValue = genderEnumType.newEnumValue();
        genderEnumType.setExtensible(false);

        IEnumAttributeValue valueId = newEnumValue.getEnumAttributeValues().get(1);
        IEnumAttributeValue valueName = newEnumValue.getEnumAttributeValues().get(2);
        IEnumAttributeValue valueNew = newEnumValue.getEnumAttributeValues().get(3);

        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(3));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(2));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(3));

        int newIndex;
        newIndex = genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        assertEquals(2, newIndex);
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(3));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(2));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(3));

        newIndex = genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        assertEquals(3, newIndex);
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(3));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(2));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(3));

        // Nothing must change if the EnumAttribute is the last one already.
        newIndex = genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        assertEquals(3, newIndex);
        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(true).get(1));
        assertEquals(newEnumAttribute, genderEnumType.getEnumAttributes(true).get(2));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(true).get(3));
        assertEquals(valueName, newEnumValue.getEnumAttributeValues().get(1));
        assertEquals(valueNew, newEnumValue.getEnumAttributeValues().get(2));
        assertEquals(valueId, newEnumValue.getEnumAttributeValues().get(3));
    }

    @Test
    public void testMoveEnumAttributeDownValuesPartOfModel() {
        IEnumAttributeValue valueId = genderEnumValueMale.getEnumAttributeValues().get(0);
        IEnumAttributeValue valueName = genderEnumValueMale.getEnumAttributeValues().get(1);

        genderEnumType.setExtensible(false);
        genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);

        assertEquals(genderEnumAttributeName, genderEnumType.getEnumAttributes(false).get(0));
        assertEquals(genderEnumAttributeId, genderEnumType.getEnumAttributes(false).get(1));

        assertEquals(valueId, genderEnumValueMale.getEnumAttributeValues().get(0));
        assertEquals(valueName, genderEnumValueMale.getEnumAttributeValues().get(1));
    }

    @Test
    public void testXml() throws IpsException, ParserConfigurationException {
        IEnumType newEnumType = newEnumType(ipsProject, "NewEnumType");
        newEnumType.setAbstract(true);
        newEnumType.setExtensible(true);
        newEnumType.setIdentifierBoundary("100");
        newEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        newEnumType.setEnumContentName("bar");
        newEnumType.newEnumAttribute();

        Element xmlElement = newEnumType.toXml(createXmlDocument(IEnumType.XML_TAG));

        assertTrue(Boolean.parseBoolean(xmlElement.getAttribute(IEnumType.PROPERTY_ABSTRACT)));
        assertTrue(Boolean.parseBoolean(xmlElement.getAttribute(IEnumType.PROPERTY_EXTENSIBLE)));
        assertTrue(xmlElement.hasAttribute(IEnumType.PROPERTY_IDENTIFIER_BOUNDARY));
        assertEquals("100", xmlElement.getAttribute(IEnumType.PROPERTY_IDENTIFIER_BOUNDARY));
        assertEquals(genderEnumType.getQualifiedName(), xmlElement.getAttribute(IEnumType.PROPERTY_SUPERTYPE));
        assertEquals("bar", xmlElement.getAttribute(IEnumType.PROPERTY_ENUM_CONTENT_NAME));

        IEnumType loadedEnumType = newEnumType(ipsProject, "LoadedEnumType");
        loadedEnumType.initFromXml(xmlElement);
        assertTrue(loadedEnumType.isAbstract());
        assertTrue(loadedEnumType.isExtensible());
        assertEquals("100", loadedEnumType.getIdentifierBoundary());
        assertEquals(genderEnumType.getQualifiedName(), loadedEnumType.getSuperEnumType());
        assertEquals("bar", loadedEnumType.getEnumContentName());
    }

    @Test
    public void testXmlBoundary() throws IpsException, ParserConfigurationException {
        IEnumType newEnumType = newEnumType(ipsProject, "NewEnumType");
        newEnumType.setAbstract(true);
        newEnumType.setExtensible(true);
        newEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        newEnumType.setEnumContentName("bar");
        newEnumType.newEnumAttribute();

        Element xmlElement = newEnumType.toXml(createXmlDocument(IEnumType.XML_TAG));

        assertFalse(xmlElement.hasAttribute(IEnumType.PROPERTY_IDENTIFIER_BOUNDARY));

        IEnumType loadedEnumType = newEnumType(ipsProject, "LoadedEnumType");
        loadedEnumType.initFromXml(xmlElement);
        assertEquals(null, loadedEnumType.getIdentifierBoundary());
    }

    @Test
    public void testIsExtensibleAndSavingValuesInType() {
        IEnumType extensibleEnumType = newEnumType(ipsProject, "ExtensibleEnum");
        extensibleEnumType.setExtensible(true);
        IEnumAttribute enumAttributeId = extensibleEnumType.newEnumAttribute();
        IEnumAttribute enumAttributeName = extensibleEnumType.newEnumAttribute();
        enumAttributeId.setName("ID");
        enumAttributeName.setName("NAME");
        IEnumValue enumValue1 = extensibleEnumType.newEnumValue();
        IEnumValue enumValue2 = extensibleEnumType.newEnumValue();
        enumValue1.setEnumAttributeValue(enumAttributeId, ValueFactory.createStringValue("1"));
        enumValue2.setEnumAttributeValue(enumAttributeName, ValueFactory.createStringValue("Name"));
        assertEquals(extensibleEnumType.getEnumValues().get(0), enumValue1);
        assertEquals(extensibleEnumType.getEnumValues().get(1), enumValue2);
    }

    @Test
    public void testDeleteEnumAttributeWithValues() {
        IEnumType newEnumType = newEnumType(ipsProject, "NewEnumType");
        IEnumAttribute newEnumAttribute = newEnumType.newEnumAttribute();
        newEnumAttribute.delete();
        assertEquals(2, genderEnumType.getEnumAttributesCount(false));

        IEnumValue modelValue = genderEnumType.newEnumValue();

        contentsChangeCounter.reset();
        genderEnumAttributeId.delete();
        assertEquals(2, contentsChangeCounter.getCounts());
        List<IEnumAttribute> enumAttributes = genderEnumType.getEnumAttributes(false);
        assertEquals(1, enumAttributes.size());
        assertEquals(genderEnumAttributeName, enumAttributes.get(0));
        List<IEnumAttributeValue> enumAttributeValues = genderEnumValueMale.getEnumAttributeValues();
        assertEquals(2, enumAttributeValues.size());
        assertEquals(2, modelValue.getEnumAttributeValues().size());

        genderEnumAttributeName.delete();
        genderEnumType.getEnumLiteralNameAttribute().delete();
        assertEquals(0, genderEnumType.getEnumAttributes(true).size());
        assertEquals(2, genderEnumValueMale.getEnumAttributeValues().size());
        assertEquals(0, modelValue.getEnumAttributeValues().size());
        assertEquals(0, genderEnumType.getEnumValuesCount());
    }

    @Test
    public void testValidateThis() {
        assertTrue(genderEnumType.isValid(ipsProject));
    }

    @Test
    public void testValidateSuperEnumType() {
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
        assertTrue(genderEnumType.isValid(ipsProject));
    }

    @Test
    public void testValidateInheritedAttributes() {
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
        assertTrue(superEnumType.isValid(ipsProject));

        attr1 = genderEnumType.newEnumAttribute();
        attr1.setName("attr1");
        attr1.setInherited(true);
        attr2 = genderEnumType.newEnumAttribute();
        attr2.setName("attr2");
        attr2.setInherited(true);
        ipsModel.clearValidationCache();
        assertTrue(genderEnumType.isValid(ipsProject));
    }

    @Test
    public void testValidateLiteralNameAttribute() {
        genderEnumType.setExtensible(false);
        genderEnumType.getEnumLiteralNameAttribute().delete();
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_NO_LITERAL_NAME_ATTRIBUTE));

        genderEnumType.setAbstract(true);
        getIpsModel().clearValidationCache();
        assertTrue(genderEnumType.isValid(ipsProject));

        genderEnumType.setAbstract(false);
        genderEnumType.setExtensible(true);
        genderEnumType.newEnumLiteralNameAttribute();
        getIpsModel().clearValidationCache();
        assertTrue(genderEnumType.isValid(ipsProject));

        genderEnumType.setExtensible(false);
        getIpsModel().clearValidationCache();
        assertTrue(genderEnumType.isValid(ipsProject));

        IEnumLiteralNameAttribute literal2 = genderEnumType.newEnumLiteralNameAttribute();
        literal2.setName("LITERAL_NAME2");
        getIpsModel().clearValidationCache();
        validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(
                validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_MULTIPLE_LITERAL_NAME_ATTRIBUTES));
    }

    @Test
    public void testValidateUsedAsIdInFaktorIpsUiAttribute() {
        genderEnumAttributeId.setIdentifier(false);
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_NO_USED_AS_ID_IN_FAKTOR_IPS_UI_ATTRIBUTE));
    }

    @Test
    public void testValidateUsedAsNameInFaktorIpsUiAttribute() {
        genderEnumAttributeName.setUsedAsNameInFaktorIpsUi(false);
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_NO_USED_AS_NAME_IN_FAKTOR_IPS_UI_ATTRIBUTE));
    }

    @Test
    public void testValidateEnumContentPackageFragment() {
        genderEnumType.setEnumContentName("");
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_ENUM_CONTENT_NAME_EMPTY));
    }

    @Test
    public void testValidateObsoleteValues() {
        paymentMode.setEnumContentName("EnumContentPlaceholder");
        paymentMode.setExtensible(true);
        MessageList validationMessageList = paymentMode.validate(ipsProject);
        assertEquals(0, validationMessageList.getNoOfMessages(Message.WARNING));
        assertNull(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_ENUM_VALUES_OBSOLETE));

        paymentMode.setExtensible(false);
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

    @Test
    public void testValidateEnumContentAlreadyUsed() {
        paymentMode.setEnumContentName(ENUMCONTENTS_NAME);
        assertTrue(paymentMode.isValid(ipsProject));

        paymentMode.setExtensible(true);
        paymentMode.deleteEnumValues(paymentMode.getEnumValues());

        MessageList validationMessageList = paymentMode.validate(ipsProject);
        assertEquals(IEnumType.MSGCODE_ENUM_TYPE_ENUM_CONTENT_ALREADY_USED,
                validationMessageList.getFirstMessage(Message.ERROR).getCode());
        assertEquals(1, validationMessageList.size());
    }

    /**
     * <strong>Performance Test:</strong><br>
     * The container is filled with 2000 enum values containing 10 enum attributes
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The time to validate the identifier violations should be less than 3 second. In fact it
     * should be very much faster but three seconds is the maximum for slow test machines!
     */
    @Ignore("fails if build slave has a high load")
    @Test
    public void testValidate_performance() throws Exception {
        EnumType enumType = newEnumType(ipsProject, "PerformanceTestEnum");
        enumType.setExtensible(false);
        for (int column = 0; column < 10; column++) {
            IEnumAttribute enumAttribute = enumType.newEnumAttribute();
            enumAttribute.setUnique(true);
            if ((column % 2) == 0) {
                enumAttribute.setMultilingual(true);
            }
        }
        for (int i = 0; i < 2000; i++) {
            IEnumValue enumValue = enumType.newEnumValue();
            fillAttributeValues(enumValue, i);
        }

        long time = System.nanoTime();
        enumType.validate(ipsProject);
        double duration = (System.nanoTime() - time) / 1000000000.0;
        System.out.println("DURATION: " + duration);
        assertTrue("Needed " + duration + " seconds. (should be less than 3)", duration < 3);
    }

    @Test
    public void testValidateIdentifierBoundaryOnDatatype() {
        MessageList validate = genderEnumType.validate(ipsProject);
        assertTrue(validate.isEmpty());

        genderEnumAttributeId.setDatatype(Datatype.INTEGER.getQualifiedName());
        genderEnumType.setIdentifierBoundary("String");
        validate = genderEnumType.validate(ipsProject);
        assertFalse(validate.isEmpty());
        assertEquals(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE,
                validate.getFirstMessage(Message.ERROR).getCode());

        genderEnumType.setIdentifierBoundary("1000");
        validate = genderEnumType.validate(ipsProject);
        assertTrue(validate.isEmpty());
    }

    @Test
    public void testValidateIdentifierBoundaryOnDatatype_NotExtensible() {
        MessageList validate = genderEnumType.validate(ipsProject);
        assertTrue(validate.isEmpty());

        genderEnumAttributeId.setDatatype(Datatype.INTEGER.getQualifiedName());
        genderEnumType.setIdentifierBoundary("String");
        validate = genderEnumType.validate(ipsProject);
        assertFalse(validate.isEmpty());
        assertEquals(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE,
                validate.getFirstMessage(Message.ERROR).getCode());

        genderEnumType.setExtensible(false);
        validate = genderEnumType.validate(ipsProject);
        assertTrue(validate.isEmpty());
    }

    @Test
    public void testValidateIdentifierBoundaryOnDatatype_EmptyStringAndNull() {
        MessageList validate = genderEnumType.validate(ipsProject);
        genderEnumAttributeId.setDatatype(Datatype.INTEGER.getQualifiedName());

        genderEnumType.setIdentifierBoundary("");
        validate = genderEnumType.validate(ipsProject);
        assertTrue(validate.isEmpty());

        genderEnumType.setIdentifierBoundary(null);
        validate = genderEnumType.validate(ipsProject);
        assertTrue(validate.isEmpty());
    }

    @Test
    public void testValidateIdentifierBoundaryOnDatatype_WithEnumSuperType() {
        MessageList validate = genderEnumType.validate(ipsProject);

        EnumType enumSuperType = newEnumType(ipsProject, "enumSuperType");
        enumSuperType.setAbstract(true);
        IEnumAttribute enumAttribute = enumSuperType.newEnumAttribute();
        enumAttribute.setName("Id");
        enumAttribute.setUnique(true);
        enumAttribute.setIdentifier(true);
        enumAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());

        assertFalse(genderEnumType.hasSuperEnumType());
        validate = genderEnumType.validate(ipsProject);
        assertTrue(validate.isEmpty());

        genderEnumType.setSuperEnumType("enumSuperType");
        genderEnumAttributeId.setDatatype(Datatype.INTEGER.getQualifiedName());
        genderEnumAttributeId.setInherited(true);
        assertTrue(genderEnumType.hasSuperEnumType());
        genderEnumType.setIdentifierBoundary("String");
        validate = genderEnumType.validate(ipsProject);
        assertFalse(validate.isEmpty());
        assertEquals(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE,
                validate.getFirstMessage(Message.ERROR).getCode());

        genderEnumType.setIdentifierBoundary("100");
        validate = genderEnumType.validate(ipsProject);
        assertTrue(validate.isEmpty());
    }

    private void fillAttributeValues(IEnumValue enumValue, int i) {
        List<IEnumAttributeValue> enumAttributeValues = enumValue.getEnumAttributeValues();
        for (int j = 0; j < 10; j++) {
            enumAttributeValues.get(j).setValue(new StringValue("abc" + i + j));
        }
    }

    @Test
    public void testFindSuperEnumType() {
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        assertEquals(genderEnumType, subEnumType.findSuperEnumType(ipsProject));

        try {
            subEnumType.findSuperEnumType(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSearchSubclassingEnumTypesNoSubclasses() {
        Set<IEnumType> subclasses = genderEnumType.searchSubclassingEnumTypes();
        assertEquals(0, subclasses.size());
    }

    @Test
    public void testSearchSubclassingEnumTypesOneSubclass() {
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());

        Set<IEnumType> subclasses = genderEnumType.searchSubclassingEnumTypes();
        assertEquals(1, subclasses.size());
        assertTrue(subclasses.contains(subEnumType));
    }

    @Test
    public void testSearchSubclassingEnumTypesSubclassInOtherProject() {
        IIpsProject otherProject = newIpsProject("OtherProject");
        IIpsObjectPath ipsObjectPath = otherProject.getIpsObjectPath();
        ipsObjectPath.newIpsProjectRefEntry(ipsProject);
        otherProject.setIpsObjectPath(ipsObjectPath);

        IEnumType subEnumType = newEnumType(otherProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());

        Set<IEnumType> subclasses = genderEnumType.searchSubclassingEnumTypes();
        assertEquals(1, subclasses.size());
        assertTrue(subclasses.contains(subEnumType));
    }

    @Test
    public void testSearchSubclassingEnumTypesTwoSubclasses() {
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        IEnumType deepEnumType = newEnumType(ipsProject, "DeepEnumType");
        deepEnumType.setSuperEnumType(subEnumType.getQualifiedName());

        Set<IEnumType> subclasses = genderEnumType.searchSubclassingEnumTypes();
        assertEquals(2, subclasses.size());
        assertTrue(subclasses.contains(subEnumType));
        assertTrue(subclasses.contains(deepEnumType));
    }

    @Test
    public void testGetIndexOfEnumAttribute() {
        assertEquals(1, genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeId, true));
        assertEquals(2, genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeName, true));

        genderEnumType.moveEnumAttribute(genderEnumAttributeName, true);
        assertEquals(2, genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeId, true));
        assertEquals(1, genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeName, true));

        assertEquals(-1, genderEnumType.getIndexOfEnumAttribute(paymentMode.getEnumAttributes(false).get(0), true));
    }

    @Test
    public void testGetIndexOfEnumAttributeIEnumAttributeBoolean_considerLiteralName() throws Exception {
        IEnumAttribute identiferAttribute = paymentMode.findIdentiferAttribute(ipsProject);
        int indexOfEnumAttribute = paymentMode.getIndexOfEnumAttribute(identiferAttribute, true);

        assertEquals(1, indexOfEnumAttribute);
    }

    @Test
    public void testGetIndexOfEnumAttributeIEnumAttributeBoolean_ignoreLiteralName() throws Exception {
        IEnumAttribute identiferAttribute = paymentMode.findIdentiferAttribute(ipsProject);
        int indexOfEnumAttribute = paymentMode.getIndexOfEnumAttribute(identiferAttribute, false);

        assertEquals(0, indexOfEnumAttribute);
    }

    @Test
    public void testGetIndexOfEnumLiteralNameAttribute() {
        assertEquals(0, paymentMode.getIndexOfEnumLiteralNameAttribute());
        paymentMode.moveEnumAttribute(paymentMode.getEnumLiteralNameAttribute(), false);
        assertEquals(1, paymentMode.getIndexOfEnumLiteralNameAttribute());
    }

    @Test
    public void testHasEnumLiteralNameAttribute() {
        genderEnumType.getEnumLiteralNameAttribute().delete();

        assertFalse(genderEnumType.hasEnumLiteralNameAttribute());
        assertTrue(paymentMode.hasEnumLiteralNameAttribute());
    }

    @Test
    public void testHasSuperEnumType() {
        assertFalse(genderEnumType.hasSuperEnumType());

        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        genderEnumType.setSuperEnumType(superEnumType.getQualifiedName());
        assertTrue(genderEnumType.hasSuperEnumType());
    }

    @Test
    public void testHasExistingSuperEnumType() {
        assertFalse(genderEnumType.hasExistingSuperEnumType(ipsProject));

        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        genderEnumType.setSuperEnumType(superEnumType.getQualifiedName());
        assertTrue(genderEnumType.hasExistingSuperEnumType(ipsProject));

        genderEnumType.setSuperEnumType("lila_laune_baer");
        assertFalse(genderEnumType.hasExistingSuperEnumType(ipsProject));
    }

    @Test
    public void testFindAllSuperEnumTypes() {
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

    @Test
    public void testFindAllSuperEnumTypesWithCycle() {
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

    @Test
    public void testIsSubEnumTypeOf() {
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

    @Test
    public void testIsSubEnumTypeOrSelf() {
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

    @Test
    public void testGetSetEnumContentPackageFragment() {
        assertEquals(ENUMCONTENTS_NAME, genderEnumType.getEnumContentName());
        genderEnumType.setEnumContentName("bar");
        assertEquals("bar", genderEnumType.getEnumContentName());
    }

    @Test
    public void testFindInheritEnumAttributeCandidates() {
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

    @Test
    public void testInheritEnumAttributes() {
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
            subEnumType.inheritEnumAttributes(Arrays.asList(notInSupertypeHierarchyAttribute));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testDependsOn() {
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
        IDependency superEnumTypeDependency = IpsObjectDependency.createSubtypeDependency(
                subEnumType.getQualifiedNameType(),
                new QualifiedNameType(genderEnumType.getQualifiedName(), IpsObjectType.ENUM_TYPE));
        assertTrue(depencendiesListSubEnumType.contains(superEnumTypeDependency));

        List<IDependencyDetail> details = subEnumType.getDependencyDetails(dependenciesSubEnumType[0]);
        DependencyDetail detail = new DependencyDetail(subEnumType, IEnumType.PROPERTY_SUPERTYPE);
        assertEquals(1, details.size());
        assertTrue(details.contains(detail));

        List<IDependency> depencendiesListSubSubEnumType = Arrays.asList(dependenciesSubSubEnumType);
        superEnumTypeDependency = IpsObjectDependency.createSubtypeDependency(subSubEnumType.getQualifiedNameType(),
                new QualifiedNameType(subEnumType.getQualifiedName(), IpsObjectType.ENUM_TYPE));
        assertTrue(depencendiesListSubSubEnumType.contains(superEnumTypeDependency));

        details = subSubEnumType.getDependencyDetails(dependenciesSubSubEnumType[0]);
        detail = new DependencyDetail(subSubEnumType, IEnumType.PROPERTY_SUPERTYPE);
        assertEquals(1, details.size());
        assertTrue(details.contains(detail));
    }

    @Test
    public void testDependsOn_Datatypes() {
        EnumType depEnumType = newEnumType(ipsProject, "DependantEnumType");
        EnumType enumType = newEnumType(ipsProject, "AnyEnumType");
        IEnumAttribute enumAttribute1 = enumType.newEnumAttribute();
        enumAttribute1.setDatatype(ValueDatatype.STRING.getQualifiedName());
        IEnumAttribute enumAttribute2 = enumType.newEnumAttribute();
        enumAttribute2.setDatatype(depEnumType.getQualifiedName());

        IDependency[] dependencies = enumType.dependsOn();

        assertThat(dependencies.length, is(2));
        assertThat(dependencies[0], is((IDependency)new DatatypeDependency(enumType.getQualifiedNameType(),
                ValueDatatype.STRING.getQualifiedName())));
        assertThat(dependencies[1], is(
                (IDependency)new DatatypeDependency(enumType.getQualifiedNameType(), depEnumType.getQualifiedName())));
        List<IDependencyDetail> detail1 = enumType.getDependencyDetails(dependencies[0]);
        assertThat(detail1.size(), is(1));
        assertThat(detail1, hasItem(new DependencyDetail(enumAttribute1, IEnumAttribute.PROPERTY_DATATYPE)));
        List<IDependencyDetail> detail2 = enumType.getDependencyDetails(dependencies[1]);
        assertThat(detail2.size(), is(1));
        assertThat(detail2, hasItem(new DependencyDetail(enumAttribute2, IEnumAttribute.PROPERTY_DATATYPE)));
    }

    @Test
    public void testFindAllMetaObjects() {
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

        Collection<IIpsSrcFile> resultList = enumType.searchMetaObjectSrcFiles(true);
        assertEquals(2, resultList.size());
        assertTrue(resultList.contains(enum1.getIpsSrcFile()));
        assertTrue(resultList.contains(enum2.getIpsSrcFile()));
        assertFalse(resultList.contains(enum3.getIpsSrcFile()));

        EnumContent enumProj2 = newEnumContent(referencingProject, enumProj2QName);
        enumProj2.setEnumType(enumTypeQName);

        resultList = enumType.searchMetaObjectSrcFiles(true);
        assertEquals(3, resultList.size());
        assertTrue(resultList.contains(enum1.getIpsSrcFile()));
        assertTrue(resultList.contains(enum2.getIpsSrcFile()));
        assertTrue(resultList.contains(enumProj2.getIpsSrcFile()));
        assertFalse(resultList.contains(enum3.getIpsSrcFile()));

        EnumType enumTypeProj2 = newEnumType(independentProject, enumTypeProj2QName);

        resultList = enumTypeProj2.searchMetaObjectSrcFiles(true);
        assertEquals(0, resultList.size());

        EnumType superEnum = newEnumType(ipsProject, "superEnum");
        superEnum.setAbstract(true);
        enumType.setSuperEnumType(superEnum.getQualifiedName());

        resultList = enumTypeProj2.searchMetaObjectSrcFiles(false);
        assertEquals(0, resultList.size());

        resultList = superEnum.searchMetaObjectSrcFiles(true);
        assertEquals(3, resultList.size());
        assertTrue(resultList.contains(enum1.getIpsSrcFile()));
        assertTrue(resultList.contains(enum2.getIpsSrcFile()));
        assertTrue(resultList.contains(enumProj2.getIpsSrcFile()));
        assertFalse(resultList.contains(enum3.getIpsSrcFile()));
    }

    @Test
    public void testFindIsUsedAsIdInFaktorIpsUiAttribute() throws Exception {
        IEnumType enum1 = newEnumType(ipsProject, "enum1");
        enum1.setAbstract(false);
        enum1.setExtensible(false);

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

    @Test
    public void testfindIsUsedAsNameInFaktorIpsUiAttribute() throws Exception {
        IEnumType enum1 = newEnumType(ipsProject, "enum1");
        enum1.setAbstract(false);
        enum1.setExtensible(false);

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

    @Test
    public void testContainsEnumAttribute() {
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

    @Test
    public void testContainsEnumLiteralNameAttribute() {
        assertTrue(genderEnumType.containsEnumLiteralNameAttribute());
        genderEnumType.getEnumLiteralNameAttribute().delete();
        assertFalse(genderEnumType.containsEnumLiteralNameAttribute());
    }

    @Test
    public void testGetEnumLiteralNameAttribute() {
        assertEquals(paymentMode.getEnumAttributes(true).get(0), paymentMode.getEnumLiteralNameAttribute());
    }

    @Test
    public void testGetEnumLiteralNameAttributesCount() {
        assertEquals(1, paymentMode.getEnumLiteralNameAttributesCount());
        paymentMode.newEnumLiteralNameAttribute();
        assertEquals(2, paymentMode.getEnumLiteralNameAttributesCount());
    }

    @Test
    public void testIsCapableOfContainingValues() {
        assertTrue(genderEnumType.isCapableOfContainingValues());
        genderEnumType.setAbstract(true);
        genderEnumType.setAbstract(true);
        genderEnumType.setExtensible(false);
        assertFalse(genderEnumType.isCapableOfContainingValues());
        genderEnumType.setAbstract(false);
        assertTrue(genderEnumType.isCapableOfContainingValues());

        genderEnumType.setAbstract(true);
        genderEnumType.setExtensible(true);
        assertFalse(genderEnumType.isCapableOfContainingValues());
        genderEnumType.setAbstract(false);
        assertTrue(genderEnumType.isCapableOfContainingValues());
    }

    @Test
    public void testGetDescriptionFromThisOrSuper() {
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());

        genderEnumType.setDescriptionText(Locale.ENGLISH, "english description");
        assertEquals("english description", subEnumType.getDescriptionTextFromThisOrSuper(Locale.ENGLISH));
        subEnumType.setDescriptionText(Locale.ENGLISH, "overwritten description");
        assertEquals("overwritten description", subEnumType.getDescriptionTextFromThisOrSuper(Locale.ENGLISH));
    }

    public class ContentsChangeCounter implements ContentsChangeListener {

        private int counter = 0;

        public int getCounts() {
            return counter;
        }

        public void reset() {
            counter = 0;
        }

        @Override
        public void contentsChanged(ContentChangeEvent event) {
            counter++;
        }
    }

}
