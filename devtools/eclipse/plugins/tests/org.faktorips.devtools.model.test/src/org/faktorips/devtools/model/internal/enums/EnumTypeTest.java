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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
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
        assertThat(genderEnumType.getSuperEnumType(), is(""));
        genderEnumType.setSuperEnumType("OtherEnumTypeName");
        assertThat(genderEnumType.getSuperEnumType(), is("OtherEnumTypeName"));

        try {
            genderEnumType.setSuperEnumType(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testGetSetAbstract() {
        assertThat(genderEnumType.isAbstract(), is(false));
        genderEnumType.setAbstract(true);
        assertThat(genderEnumType.isAbstract(), is(true));
    }

    @Test
    public void testGetSetExtensible() {
        genderEnumType.setExtensible(false);
        assertThat(genderEnumType.isExtensible(), is(false));
        genderEnumType.setExtensible(true);
    }

    @Test
    public void testGetSetIdentifierBoundary() {
        assertThat(genderEnumType.getIdentifierBoundary(), is(nullValue()));
        genderEnumType.setIdentifierBoundary("100");
        assertThat(genderEnumType.getIdentifierBoundary(), is("100"));
    }

    @Test
    public void testGetIpsObjectType() {
        assertThat(genderEnumType.getIpsObjectType(), is(IpsObjectType.ENUM_TYPE));
    }

    @Test
    public void testGetEnumAttributes() {
        IEnumAttribute inheritedEnumAttribute = paymentMode.newEnumAttribute();
        inheritedEnumAttribute.setInherited(true);

        List<IEnumAttribute> attributes = paymentMode.getEnumAttributes(false);
        assertThat(attributes.size(), is(2));
        assertThat(attributes.get(0).getName(), is("id"));
        assertThat(attributes.get(1).getName(), is("name"));

        attributes = paymentMode.getEnumAttributes(true);
        assertThat(attributes.size(), is(3));
        assertThat(attributes.get(0).getName(), is("LITERAL_NAME"));
        assertThat(attributes.get(1).getName(), is("id"));
        assertThat(attributes.get(2).getName(), is("name"));
    }

    @Test
    public void testGetEnumAttributesIncludeSupertypeCopies() {
        IEnumAttribute inheritedEnumAttribute = paymentMode.newEnumAttribute();
        inheritedEnumAttribute.setName("foo");
        inheritedEnumAttribute.setInherited(true);

        List<IEnumAttribute> attributes = paymentMode.getEnumAttributesIncludeSupertypeCopies(false);
        assertThat(attributes.size(), is(3));
        assertThat(attributes.get(0).getName(), is("id"));
        assertThat(attributes.get(1).getName(), is("name"));
        assertThat(attributes.get(2).getName(), is("foo"));

        attributes = paymentMode.getEnumAttributesIncludeSupertypeCopies(true);
        assertThat(attributes.size(), is(4));
        assertThat(attributes.get(0).getName(), is("LITERAL_NAME"));
        assertThat(attributes.get(1).getName(), is("id"));
        assertThat(attributes.get(2).getName(), is("name"));
        assertThat(attributes.get(3).getName(), is("foo"));
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

        assertThat(abstractGrandParent.findAllEnumAttributes(true, ipsProject),
                is(Arrays.asList(idAttributeGP, nameAttributeGP)));
        assertThat(abstractParent.findAllEnumAttributes(true, ipsProject),
                is(Arrays.asList(idAttributeP, otherAttributeP, nameAttributeP)));
        assertThat(concrete.findAllEnumAttributes(true, ipsProject),
                is(Arrays.asList(otherAttributeC, idAttributeC, localAttribute, nameAttributeC, literalNameAttribute)));
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

        assertThat(abstractGrandParent.findAllEnumAttributes(true, ipsProject),
                is(Arrays.asList(idAttributeGP, nameAttributeGP)));
        assertThat(abstractParent.findAllEnumAttributes(true, ipsProject),
                is(Arrays.asList(idAttributeGP, nameAttributeGP, otherAttributeP)));
        assertThat(concrete.findAllEnumAttributes(true, ipsProject),
                is(Arrays.asList(idAttributeC, otherAttributeC, literalNameAttribute, localAttribute, nameAttributeC)));
    }

    @Test
    public void testFindAllEnumAttributes_duplicateAttributeName() {
        EnumType parent = newAbstractEnumType("A", null);
        IEnumAttribute idAttributeGP = newEnumAttribute(parent, "id");

        EnumType concrete = newEnumType(ipsProject, "C");
        concrete.setSuperEnumType(parent.getQualifiedName());
        IEnumAttribute idAttributeC = newEnumAttribute(concrete, "id", false);

        assertThat(parent.findAllEnumAttributes(true, ipsProject), is(Arrays.asList(idAttributeGP)));
        assertThat(concrete.findAllEnumAttributes(true, ipsProject),
                is(Arrays.asList(idAttributeGP, idAttributeC)));
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

        assertThat(genderEnumType.getEnumAttribute(GENDER_ENUM_ATTRIBUTE_ID_NAME), is(genderEnumAttributeId));
        assertThat(genderEnumType.getEnumAttribute(GENDER_ENUM_ATTRIBUTE_NAME_NAME), is(genderEnumAttributeName));
        assertThat(genderEnumType.getEnumAttribute("foo"), is(nullValue()));
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

        assertThat(genderEnumType.getEnumAttributeIncludeSupertypeCopies(GENDER_ENUM_ATTRIBUTE_ID_NAME),
                is(genderEnumAttributeId));
        assertThat(genderEnumType.getEnumAttributeIncludeSupertypeCopies(GENDER_ENUM_ATTRIBUTE_NAME_NAME),
                is(genderEnumAttributeName));
        assertThat(genderEnumType.getEnumAttributeIncludeSupertypeCopies("foo"), is(inheritedEnumAttribute));
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

        assertThat(subEnumType.findEnumAttributeIncludeSupertypeOriginals(ipsProject, GENDER_ENUM_ATTRIBUTE_ID_NAME),
                is(genderEnumAttributeId));
    }

    @Test
    public void testGetEnumAttributesCount() {
        IEnumAttribute inheritedEnumAttribute = genderEnumType.newEnumAttribute();
        inheritedEnumAttribute.setInherited(true);

        assertThat(genderEnumType.getEnumAttributesCountIncludeSupertypeCopies(false), is(3));
        assertThat(genderEnumType.getEnumAttributesCount(false), is(2));
        assertThat(genderEnumType.getEnumAttributesCountIncludeSupertypeCopies(true), is(4));
        assertThat(genderEnumType.getEnumAttributesCount(true), is(3));

        inheritedEnumAttribute = paymentMode.newEnumAttribute();
        inheritedEnumAttribute.setInherited(true);
        assertThat(paymentMode.getEnumAttributesCount(true), is(3));
        assertThat(paymentMode.getEnumAttributesCount(false), is(2));
        assertThat(paymentMode.getEnumAttributesCountIncludeSupertypeCopies(true), is(4));
        assertThat(paymentMode.getEnumAttributesCountIncludeSupertypeCopies(false), is(3));
    }

    @Test
    public void testGetChildren() {
        IIpsElement[] children = genderEnumType.getChildren();
        List<IIpsElement> childrenList = Arrays.asList(children);
        assertThat(childrenList.contains(genderEnumAttributeId), is(true));
        assertThat(childrenList.contains(genderEnumAttributeName), is(true));
    }

    @Test
    public void testNewEnumAttribute() {
        IEnumValue newPaymentMode = paymentMode.newEnumValue();
        contentsChangeCounter.reset();
        IEnumAttribute description = paymentMode.newEnumAttribute();
        assertThat(contentsChangeCounter.getCounts(), is(1));
        description.setName("description");
        description.setDatatype(Datatype.STRING.getQualifiedName());

        List<IEnumAttribute> attributes = paymentMode.getEnumAttributes(true);
        assertThat(attributes.size(), is(4));
        assertThat(attributes.get(3).getName(), is("description"));

        List<IEnumAttributeValue> attributeValues = newPaymentMode.getEnumAttributeValues();
        assertThat(attributeValues.size(), is(4));
        assertThat(attributeValues.get(0).findEnumAttribute(ipsProject), is(attributes.get(0)));
        assertThat(attributeValues.get(1).findEnumAttribute(ipsProject), is(attributes.get(1)));
        assertThat(attributeValues.get(2).findEnumAttribute(ipsProject), is(attributes.get(2)));
        assertThat(attributeValues.get(3).findEnumAttribute(ipsProject), is(attributes.get(3)));
        assertThat(attributeValues.get(0).findEnumAttribute(ipsProject),
                is(paymentMode.getEnumLiteralNameAttribute()));

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
        assertThat(attributeValues.size(), is(3));
        assertThat(attributeValues.get(0).findEnumAttribute(ipsProject), is(literal));
    }

    @Test
    public void testFindEnumType() {
        try {
            genderEnumType.findEnumType(null);
            fail();
        } catch (NullPointerException e) {
        }

        assertThat(genderEnumType.findEnumType(ipsProject), is(genderEnumType));
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

        assertThat(genderEnumType.getEnumAttributes(true).get(1), is(genderEnumAttributeId));
        assertThat(genderEnumType.getEnumAttributes(true).get(2), is(genderEnumAttributeName));
        assertThat(genderEnumType.getEnumAttributes(true).get(3), is(newEnumAttribute));
        assertThat(newEnumValue.getEnumAttributeValues().get(1), is(valueId));
        assertThat(newEnumValue.getEnumAttributeValues().get(2), is(valueName));
        assertThat(newEnumValue.getEnumAttributeValues().get(3), is(valueNew));

        int newIndex;
        contentsChangeCounter.reset();
        newIndex = genderEnumType.moveEnumAttribute(newEnumAttribute, true);
        assertThat(contentsChangeCounter.getCounts(), is(1));
        assertThat(newIndex, is(2));
        assertThat(genderEnumType.getEnumAttributes(true).get(1), is(genderEnumAttributeId));
        assertThat(genderEnumType.getEnumAttributes(true).get(2), is(newEnumAttribute));
        assertThat(genderEnumType.getEnumAttributes(true).get(3), is(genderEnumAttributeName));
        assertThat(newEnumValue.getEnumAttributeValues().get(1), is(valueId));
        assertThat(newEnumValue.getEnumAttributeValues().get(2), is(valueNew));
        assertThat(newEnumValue.getEnumAttributeValues().get(3), is(valueName));

        newIndex = genderEnumType.moveEnumAttribute(newEnumAttribute, true);
        assertThat(newIndex, is(1));
        assertThat(genderEnumType.getEnumAttributes(true).get(1), is(newEnumAttribute));
        assertThat(genderEnumType.getEnumAttributes(true).get(2), is(genderEnumAttributeId));
        assertThat(genderEnumType.getEnumAttributes(true).get(3), is(genderEnumAttributeName));
        assertThat(newEnumValue.getEnumAttributeValues().get(1), is(valueNew));
        assertThat(newEnumValue.getEnumAttributeValues().get(2), is(valueId));
        assertThat(newEnumValue.getEnumAttributeValues().get(3), is(valueName));

        newIndex = genderEnumType.moveEnumAttribute(newEnumAttribute, true);
        assertThat(newIndex, is(0));
        assertThat(genderEnumType.getEnumAttributes(true).get(0), is(newEnumAttribute));
        assertThat(genderEnumType.getEnumAttributes(true).get(2), is(genderEnumAttributeId));
        assertThat(genderEnumType.getEnumAttributes(true).get(3), is(genderEnumAttributeName));
        assertThat(newEnumValue.getEnumAttributeValues().get(0), is(valueNew));
        assertThat(newEnumValue.getEnumAttributeValues().get(2), is(valueId));
        assertThat(newEnumValue.getEnumAttributeValues().get(3), is(valueName));

        // Nothing must change if the enumeration attribute is the first one already.
        newIndex = genderEnumType.moveEnumAttribute(newEnumAttribute, true);
        assertThat(newIndex, is(0));
        assertThat(genderEnumType.getEnumAttributes(true).get(0), is(newEnumAttribute));
        assertThat(genderEnumType.getEnumAttributes(true).get(2), is(genderEnumAttributeId));
        assertThat(genderEnumType.getEnumAttributes(true).get(3), is(genderEnumAttributeName));
        assertThat(newEnumValue.getEnumAttributeValues().get(0), is(valueNew));
        assertThat(newEnumValue.getEnumAttributeValues().get(2), is(valueId));
        assertThat(newEnumValue.getEnumAttributeValues().get(3), is(valueName));

    }

    @Test
    public void testMoveEnumAttributeUpValuesPartOfModel() {
        IEnumAttributeValue valueId = genderEnumValueMale.getEnumAttributeValues().get(0);
        IEnumAttributeValue valueName = genderEnumValueMale.getEnumAttributeValues().get(1);

        genderEnumType.setExtensible(false);
        genderEnumType.moveEnumAttribute(genderEnumAttributeName, true);

        assertThat(genderEnumType.getEnumAttributes(true).get(1), is(genderEnumAttributeName));
        assertThat(genderEnumType.getEnumAttributes(true).get(2), is(genderEnumAttributeId));

        assertThat(genderEnumValueMale.getEnumAttributeValues().get(0), is(valueId));
        assertThat(genderEnumValueMale.getEnumAttributeValues().get(1), is(valueName));
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

        assertThat(genderEnumType.getEnumAttributes(true).get(1), is(genderEnumAttributeId));
        assertThat(genderEnumType.getEnumAttributes(true).get(2), is(genderEnumAttributeName));
        assertThat(genderEnumType.getEnumAttributes(true).get(3), is(newEnumAttribute));
        assertThat(newEnumValue.getEnumAttributeValues().get(1), is(valueId));
        assertThat(newEnumValue.getEnumAttributeValues().get(2), is(valueName));
        assertThat(newEnumValue.getEnumAttributeValues().get(3), is(valueNew));

        int newIndex = genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        assertThat(newIndex, is(2));
        assertThat(genderEnumType.getEnumAttributes(true).get(1), is(genderEnumAttributeName));
        assertThat(genderEnumType.getEnumAttributes(true).get(2), is(genderEnumAttributeId));
        assertThat(genderEnumType.getEnumAttributes(true).get(3), is(newEnumAttribute));
        assertThat(newEnumValue.getEnumAttributeValues().get(1), is(valueName));
        assertThat(newEnumValue.getEnumAttributeValues().get(2), is(valueId));
        assertThat(newEnumValue.getEnumAttributeValues().get(3), is(valueNew));

        newIndex = genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        assertThat(newIndex, is(3));
        assertThat(genderEnumType.getEnumAttributes(true).get(1), is(genderEnumAttributeName));
        assertThat(genderEnumType.getEnumAttributes(true).get(2), is(newEnumAttribute));
        assertThat(genderEnumType.getEnumAttributes(true).get(3), is(genderEnumAttributeId));
        assertThat(newEnumValue.getEnumAttributeValues().get(1), is(valueName));
        assertThat(newEnumValue.getEnumAttributeValues().get(2), is(valueNew));
        assertThat(newEnumValue.getEnumAttributeValues().get(3), is(valueId));

        // Nothing must change if the EnumAttribute is the last one already.
        newIndex = genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        assertThat(newIndex, is(3));
        assertThat(genderEnumType.getEnumAttributes(true).get(1), is(genderEnumAttributeName));
        assertThat(genderEnumType.getEnumAttributes(true).get(2), is(newEnumAttribute));
        assertThat(genderEnumType.getEnumAttributes(true).get(3), is(genderEnumAttributeId));
        assertThat(newEnumValue.getEnumAttributeValues().get(1), is(valueName));
        assertThat(newEnumValue.getEnumAttributeValues().get(2), is(valueNew));
        assertThat(newEnumValue.getEnumAttributeValues().get(3), is(valueId));
    }

    @Test
    public void testMoveEnumAttributeDownValuesPartOfModel() {
        IEnumAttributeValue valueId = genderEnumValueMale.getEnumAttributeValues().get(0);
        IEnumAttributeValue valueName = genderEnumValueMale.getEnumAttributeValues().get(1);

        genderEnumType.setExtensible(false);
        genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);

        assertThat(genderEnumType.getEnumAttributes(false).get(0), is(genderEnumAttributeName));
        assertThat(genderEnumType.getEnumAttributes(false).get(1), is(genderEnumAttributeId));

        assertThat(genderEnumValueMale.getEnumAttributeValues().get(0), is(valueId));
        assertThat(genderEnumValueMale.getEnumAttributeValues().get(1), is(valueName));
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

        assertThat(Boolean.parseBoolean(xmlElement.getAttribute(IEnumType.PROPERTY_ABSTRACT)), is(true));
        assertThat(Boolean.parseBoolean(xmlElement.getAttribute(IEnumType.PROPERTY_EXTENSIBLE)), is(true));
        assertThat(xmlElement.hasAttribute(IEnumType.PROPERTY_IDENTIFIER_BOUNDARY), is(true));
        assertThat(xmlElement.getAttribute(IEnumType.PROPERTY_IDENTIFIER_BOUNDARY), is("100"));
        assertThat(xmlElement.getAttribute(IEnumType.PROPERTY_SUPERTYPE), is(genderEnumType.getQualifiedName()));
        assertThat(xmlElement.getAttribute(IEnumType.PROPERTY_ENUM_CONTENT_NAME), is("bar"));

        IEnumType loadedEnumType = newEnumType(ipsProject, "LoadedEnumType");
        loadedEnumType.initFromXml(xmlElement);
        assertThat(loadedEnumType.isAbstract(), is(true));
        assertThat(loadedEnumType.isExtensible(), is(true));
        assertThat(loadedEnumType.getIdentifierBoundary(), is("100"));
        assertThat(loadedEnumType.getSuperEnumType(), is(genderEnumType.getQualifiedName()));
        assertThat(loadedEnumType.getEnumContentName(), is("bar"));
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

        assertThat(xmlElement.hasAttribute(IEnumType.PROPERTY_IDENTIFIER_BOUNDARY), is(false));

        IEnumType loadedEnumType = newEnumType(ipsProject, "LoadedEnumType");
        loadedEnumType.initFromXml(xmlElement);
        assertThat(loadedEnumType.getIdentifierBoundary(), is(nullValue()));
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
        assertThat(extensibleEnumType.getEnumValues().get(0), is(enumValue1));
        assertThat(extensibleEnumType.getEnumValues().get(1), is(enumValue2));
    }

    @Test
    public void testDeleteEnumAttributeWithValues() {
        IEnumType newEnumType = newEnumType(ipsProject, "NewEnumType");
        IEnumAttribute newEnumAttribute = newEnumType.newEnumAttribute();
        newEnumAttribute.delete();
        assertThat(genderEnumType.getEnumAttributesCount(false), is(2));

        IEnumValue modelValue = genderEnumType.newEnumValue();

        contentsChangeCounter.reset();
        genderEnumAttributeId.delete();
        assertThat(contentsChangeCounter.getCounts(), is(2));
        List<IEnumAttribute> enumAttributes = genderEnumType.getEnumAttributes(false);
        assertThat(enumAttributes.size(), is(1));
        assertThat(enumAttributes.get(0), is(genderEnumAttributeName));
        List<IEnumAttributeValue> enumAttributeValues = genderEnumValueMale.getEnumAttributeValues();
        assertThat(enumAttributeValues.size(), is(2));
        assertThat(modelValue.getEnumAttributeValues().size(), is(2));

        genderEnumAttributeName.delete();
        genderEnumType.getEnumLiteralNameAttribute().delete();
        assertThat(genderEnumType.getEnumAttributes(true).size(), is(0));
        assertThat(genderEnumValueMale.getEnumAttributeValues().size(), is(2));
        assertThat(modelValue.getEnumAttributeValues().size(), is(0));
        assertThat(genderEnumType.getEnumValuesCount(), is(0));
    }

    @Test
    public void testValidateThis() {
        assertThat(genderEnumType.isValid(ipsProject), is(true));
    }

    @Test
    public void testValidateSuperEnumType() {
        IIpsModel ipsModel = getIpsModel();

        // Test super enumeration type does not exit.
        genderEnumType.setSuperEnumType("FooBar");
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertThat(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_DOES_NOT_EXIST),
                is(notNullValue()));

        // Test super enumeration type is not abstract.
        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        genderEnumType.setSuperEnumType(superEnumType.getQualifiedName());
        ipsModel.clearValidationCache();
        validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertThat(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_SUPERTYPE_IS_NOT_ABSTRACT),
                is(notNullValue()));
        superEnumType.setAbstract(true);
        ipsModel.clearValidationCache();
        assertThat(genderEnumType.isValid(ipsProject), is(true));
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
        assertThat(validationMessageList
                .getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_NOT_INHERITED_ATTRIBUTES_IN_SUPERTYPE_HIERARCHY),
                is(notNullValue()));

        // Test abstract super enumeration type to be valid despite missing inherited attribute.
        ipsModel.clearValidationCache();
        assertThat(superEnumType.isValid(ipsProject), is(true));

        attr1 = genderEnumType.newEnumAttribute();
        attr1.setName("attr1");
        attr1.setInherited(true);
        attr2 = genderEnumType.newEnumAttribute();
        attr2.setName("attr2");
        attr2.setInherited(true);
        ipsModel.clearValidationCache();
        assertThat(genderEnumType.isValid(ipsProject), is(true));
    }

    @Test
    public void testValidateLiteralNameAttribute() {
        genderEnumType.setExtensible(false);
        genderEnumType.getEnumLiteralNameAttribute().delete();
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertThat(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_NO_LITERAL_NAME_ATTRIBUTE),
                is(notNullValue()));

        genderEnumType.setAbstract(true);
        getIpsModel().clearValidationCache();
        assertThat(genderEnumType.isValid(ipsProject), is(true));

        genderEnumType.setAbstract(false);
        genderEnumType.setExtensible(true);
        genderEnumType.newEnumLiteralNameAttribute();
        getIpsModel().clearValidationCache();
        assertThat(genderEnumType.isValid(ipsProject), is(true));

        genderEnumType.setExtensible(false);
        getIpsModel().clearValidationCache();
        assertThat(genderEnumType.isValid(ipsProject), is(true));

        IEnumLiteralNameAttribute literal2 = genderEnumType.newEnumLiteralNameAttribute();
        literal2.setName("LITERAL_NAME2");
        getIpsModel().clearValidationCache();
        validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertThat(
                validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_MULTIPLE_LITERAL_NAME_ATTRIBUTES),
                is(notNullValue()));
    }

    @Test
    public void testValidateUsedAsIdInFaktorIpsUiAttribute() {
        genderEnumAttributeId.setIdentifier(false);
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertThat(validationMessageList
                .getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_NO_USED_AS_ID_IN_FAKTOR_IPS_UI_ATTRIBUTE),
                is(notNullValue()));
    }

    @Test
    public void testValidateUsedAsNameInFaktorIpsUiAttribute() {
        genderEnumAttributeName.setUsedAsNameInFaktorIpsUi(false);
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertThat(validationMessageList
                .getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_NO_USED_AS_NAME_IN_FAKTOR_IPS_UI_ATTRIBUTE),
                is(notNullValue()));
    }

    @Test
    public void testValidateEnumContentPackageFragment() {
        genderEnumType.setEnumContentName("");
        MessageList validationMessageList = genderEnumType.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertThat(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_ENUM_CONTENT_NAME_EMPTY),
                is(notNullValue()));
    }

    @Test
    public void testValidateObsoleteValues() {
        paymentMode.setEnumContentName("EnumContentPlaceholder");
        paymentMode.setExtensible(true);
        MessageList validationMessageList = paymentMode.validate(ipsProject);
        assertThat(validationMessageList.getNoOfMessages(Message.WARNING), is(0));
        assertThat(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_ENUM_VALUES_OBSOLETE),
                is(nullValue()));

        paymentMode.setExtensible(false);
        paymentMode.setAbstract(true);
        getIpsModel().clearValidationCache();
        validationMessageList = paymentMode.validate(ipsProject);
        assertThat(validationMessageList.getNoOfMessages(Message.WARNING), is(1));
        assertThat(validationMessageList.getMessageByCode(IEnumType.MSGCODE_ENUM_TYPE_ENUM_VALUES_OBSOLETE),
                is(notNullValue()));

        paymentMode.setAbstract(false);
        getIpsModel().clearValidationCache();
        validationMessageList = paymentMode.validate(ipsProject);
        assertThat(validationMessageList.getNoOfMessages(Message.WARNING), is(0));
    }

    @Test
    public void testValidateEnumContentAlreadyUsed() {
        paymentMode.setEnumContentName(ENUMCONTENTS_NAME);
        assertThat(paymentMode.isValid(ipsProject), is(true));

        paymentMode.setExtensible(true);
        paymentMode.deleteEnumValues(paymentMode.getEnumValues());

        MessageList validationMessageList = paymentMode.validate(ipsProject);
        assertThat(validationMessageList.getFirstMessage(Message.ERROR).getCode(),
                is(IEnumType.MSGCODE_ENUM_TYPE_ENUM_CONTENT_ALREADY_USED));
        assertThat(validationMessageList.size(), is(1));
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
        assertThat("Needed " + duration + " seconds. (should be less than 3)", duration < 3, is(true));
    }

    @Test
    public void testValidateIdentifierBoundaryOnDatatype() {
        MessageList validate = genderEnumType.validate(ipsProject);
        assertThat(validate.isEmpty(), is(true));

        genderEnumAttributeId.setDatatype(Datatype.INTEGER.getQualifiedName());
        genderEnumType.setIdentifierBoundary("String");
        validate = genderEnumType.validate(ipsProject);
        assertThat(validate.isEmpty(), is(false));
        assertThat(validate.getFirstMessage(Message.ERROR).getCode(),
                is(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        genderEnumType.setIdentifierBoundary("1000");
        validate = genderEnumType.validate(ipsProject);
        assertThat(validate.isEmpty(), is(true));
    }

    @Test
    public void testValidateIdentifierBoundaryOnDatatype_NotExtensible() {
        MessageList validate = genderEnumType.validate(ipsProject);
        assertThat(validate.isEmpty(), is(true));

        genderEnumAttributeId.setDatatype(Datatype.INTEGER.getQualifiedName());
        genderEnumType.setIdentifierBoundary("String");
        validate = genderEnumType.validate(ipsProject);
        assertThat(validate.isEmpty(), is(false));
        assertThat(validate.getFirstMessage(Message.ERROR).getCode(),
                is(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        genderEnumType.setExtensible(false);
        validate = genderEnumType.validate(ipsProject);
        assertThat(validate.isEmpty(), is(true));
    }

    @Test
    public void testValidateIdentifierBoundaryOnDatatype_EmptyStringAndNull() {
        MessageList validate = genderEnumType.validate(ipsProject);
        genderEnumAttributeId.setDatatype(Datatype.INTEGER.getQualifiedName());

        genderEnumType.setIdentifierBoundary("");
        validate = genderEnumType.validate(ipsProject);
        assertThat(validate.isEmpty(), is(true));

        genderEnumType.setIdentifierBoundary(null);
        validate = genderEnumType.validate(ipsProject);
        assertThat(validate.isEmpty(), is(true));
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

        assertThat(genderEnumType.hasSuperEnumType(), is(false));
        validate = genderEnumType.validate(ipsProject);
        assertThat(validate.isEmpty(), is(true));

        genderEnumType.setSuperEnumType("enumSuperType");
        genderEnumAttributeId.setDatatype(Datatype.INTEGER.getQualifiedName());
        genderEnumAttributeId.setInherited(true);
        assertThat(genderEnumType.hasSuperEnumType(), is(true));
        genderEnumType.setIdentifierBoundary("String");
        validate = genderEnumType.validate(ipsProject);
        assertThat(validate.isEmpty(), is(false));
        assertThat(validate.getFirstMessage(Message.ERROR).getCode(),
                is(IValidationMsgCodesForInvalidValues.MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE));

        genderEnumType.setIdentifierBoundary("100");
        validate = genderEnumType.validate(ipsProject);
        assertThat(validate.isEmpty(), is(true));
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
        assertThat(subEnumType.findSuperEnumType(ipsProject), is(genderEnumType));

        try {
            subEnumType.findSuperEnumType(null);
            fail();
        } catch (NullPointerException e) {
        }
    }

    @Test
    public void testSearchSubclassingEnumTypesNoSubclasses() {
        Set<IEnumType> subclasses = genderEnumType.searchSubclassingEnumTypes();
        assertThat(subclasses.size(), is(0));
    }

    @Test
    public void testSearchSubclassingEnumTypesOneSubclass() {
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());

        Set<IEnumType> subclasses = genderEnumType.searchSubclassingEnumTypes();
        assertThat(subclasses.size(), is(1));
        assertThat(subclasses.contains(subEnumType), is(true));
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
        assertThat(subclasses.size(), is(1));
        assertThat(subclasses.contains(subEnumType), is(true));
    }

    @Test
    public void testSearchSubclassingEnumTypesTwoSubclasses() {
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        IEnumType deepEnumType = newEnumType(ipsProject, "DeepEnumType");
        deepEnumType.setSuperEnumType(subEnumType.getQualifiedName());

        Set<IEnumType> subclasses = genderEnumType.searchSubclassingEnumTypes();
        assertThat(subclasses.size(), is(2));
        assertThat(subclasses.contains(subEnumType), is(true));
        assertThat(subclasses.contains(deepEnumType), is(true));
    }

    @Test
    public void testGetIndexOfEnumAttribute() {
        assertThat(genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeId, true), is(1));
        assertThat(genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeName, true), is(2));

        genderEnumType.moveEnumAttribute(genderEnumAttributeName, true);
        assertThat(genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeId, true), is(2));
        assertThat(genderEnumType.getIndexOfEnumAttribute(genderEnumAttributeName, true), is(1));

        assertThat(genderEnumType.getIndexOfEnumAttribute(paymentMode.getEnumAttributes(false).get(0), true), is(-1));
    }

    @Test
    public void testGetIndexOfEnumAttributeIEnumAttributeBoolean_considerLiteralName() throws Exception {
        IEnumAttribute identiferAttribute = paymentMode.findIdentiferAttribute(ipsProject);
        int indexOfEnumAttribute = paymentMode.getIndexOfEnumAttribute(identiferAttribute, true);

        assertThat(indexOfEnumAttribute, is(1));
    }

    @Test
    public void testGetIndexOfEnumAttributeIEnumAttributeBoolean_ignoreLiteralName() throws Exception {
        IEnumAttribute identiferAttribute = paymentMode.findIdentiferAttribute(ipsProject);
        int indexOfEnumAttribute = paymentMode.getIndexOfEnumAttribute(identiferAttribute, false);

        assertThat(indexOfEnumAttribute, is(0));
    }

    @Test
    public void testGetIndexOfEnumLiteralNameAttribute() {
        assertThat(paymentMode.getIndexOfEnumLiteralNameAttribute(), is(0));
        paymentMode.moveEnumAttribute(paymentMode.getEnumLiteralNameAttribute(), false);
        assertThat(paymentMode.getIndexOfEnumLiteralNameAttribute(), is(1));
    }

    @Test
    public void testHasEnumLiteralNameAttribute() {
        genderEnumType.getEnumLiteralNameAttribute().delete();

        assertThat(genderEnumType.hasEnumLiteralNameAttribute(), is(false));
        assertThat(paymentMode.hasEnumLiteralNameAttribute(), is(true));
    }

    @Test
    public void testHasSuperEnumType() {
        assertThat(genderEnumType.hasSuperEnumType(), is(false));

        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        genderEnumType.setSuperEnumType(superEnumType.getQualifiedName());
        assertThat(genderEnumType.hasSuperEnumType(), is(true));
    }

    @Test
    public void testHasExistingSuperEnumType() {
        assertThat(genderEnumType.hasExistingSuperEnumType(ipsProject), is(false));

        IEnumType superEnumType = newEnumType(ipsProject, "SuperEnumType");
        genderEnumType.setSuperEnumType(superEnumType.getQualifiedName());
        assertThat(genderEnumType.hasExistingSuperEnumType(ipsProject), is(true));

        genderEnumType.setSuperEnumType("lila_laune_baer");
        assertThat(genderEnumType.hasExistingSuperEnumType(ipsProject), is(false));
    }

    @Test
    public void testFindAllSuperEnumTypes() {
        try {
            genderEnumType.findAllSuperEnumTypes(null);
            fail();
        } catch (NullPointerException e) {
        }

        assertThat(genderEnumType.findAllSuperEnumTypes(ipsProject).size(), is(0));

        IEnumType rootEnumType = newEnumType(ipsProject, "RootEnumType");
        IEnumType level1EnumType = newEnumType(ipsProject, "Level1EnumType");
        level1EnumType.setSuperEnumType(rootEnumType.getQualifiedName());
        genderEnumType.setSuperEnumType(level1EnumType.getQualifiedName());

        List<IEnumType> superEnumTypes = genderEnumType.findAllSuperEnumTypes(ipsProject);
        assertThat(superEnumTypes.size(), is(2));
        assertThat(superEnumTypes.get(0), is(level1EnumType));
        assertThat(superEnumTypes.get(1), is(rootEnumType));
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
        assertThat(superEnumTypes.size(), is(2));
        assertThat(superEnumTypes.get(0), is(level2EnumType));
        assertThat(superEnumTypes.get(1), is(level3EnumType));
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

        assertThat(rootEnumType.isSubEnumTypeOf(null, null), is(false));
        assertThat(rootEnumType.isSubEnumTypeOf(null, ipsProject), is(false));
        try {
            assertThat(level1EnumType.isSubEnumTypeOf(rootEnumType, null), is(false));
            fail();
        } catch (NullPointerException e) {
        }

        assertThat(rootEnumType.isSubEnumTypeOf(rootEnumType, ipsProject), is(false));
        assertThat(rootEnumType.isSubEnumTypeOf(level1EnumType, ipsProject), is(false));
        assertThat(rootEnumType.isSubEnumTypeOf(level2EnumType, ipsProject), is(false));
        assertThat(rootEnumType.isSubEnumTypeOf(level3EnumType, ipsProject), is(false));

        assertThat(level1EnumType.isSubEnumTypeOf(level1EnumType, ipsProject), is(false));
        assertThat(level1EnumType.isSubEnumTypeOf(rootEnumType, ipsProject), is(true));
        assertThat(level1EnumType.isSubEnumTypeOf(level2EnumType, ipsProject), is(false));
        assertThat(level1EnumType.isSubEnumTypeOf(level3EnumType, ipsProject), is(false));

        assertThat(level2EnumType.isSubEnumTypeOf(level2EnumType, ipsProject), is(false));
        assertThat(level2EnumType.isSubEnumTypeOf(rootEnumType, ipsProject), is(true));
        assertThat(level2EnumType.isSubEnumTypeOf(level1EnumType, ipsProject), is(true));
        assertThat(level2EnumType.isSubEnumTypeOf(level3EnumType, ipsProject), is(false));

        assertThat(level3EnumType.isSubEnumTypeOf(level3EnumType, ipsProject), is(false));
        assertThat(level3EnumType.isSubEnumTypeOf(rootEnumType, ipsProject), is(true));
        assertThat(level3EnumType.isSubEnumTypeOf(level1EnumType, ipsProject), is(true));
        assertThat(level3EnumType.isSubEnumTypeOf(level2EnumType, ipsProject), is(true));

        IIpsProject otherProject = newIpsProject("otherProject");
        IEnumType enumTypeInOtherProject = newEnumType(otherProject, "enumTypeInOtherProject");
        enumTypeInOtherProject.setSuperEnumType(level1EnumType.getQualifiedName());

        assertThat(enumTypeInOtherProject.isSubEnumTypeOf(rootEnumType, otherProject), is(false));
        assertThat(enumTypeInOtherProject.isSubEnumTypeOf(level1EnumType, otherProject), is(false));
        assertThat(enumTypeInOtherProject.isSubEnumTypeOf(level2EnumType, otherProject), is(false));
        assertThat(enumTypeInOtherProject.isSubEnumTypeOf(level3EnumType, otherProject), is(false));

        IIpsObjectPath path = otherProject.getIpsObjectPath();
        path.newIpsProjectRefEntry(ipsProject);
        otherProject.setIpsObjectPath(path);

        assertThat(enumTypeInOtherProject.isSubEnumTypeOf(rootEnumType, otherProject), is(true));
        assertThat(enumTypeInOtherProject.isSubEnumTypeOf(level1EnumType, otherProject), is(true));
        assertThat(enumTypeInOtherProject.isSubEnumTypeOf(level2EnumType, otherProject), is(false));
        assertThat(enumTypeInOtherProject.isSubEnumTypeOf(level3EnumType, otherProject), is(false));

        level2EnumType.setSuperEnumType(enumTypeInOtherProject.getQualifiedName());
        assertThat(level2EnumType.isSubEnumTypeOf(enumTypeInOtherProject, ipsProject), is(false));
        assertThat(level2EnumType.isSubEnumTypeOf(enumTypeInOtherProject, otherProject), is(true));

        rootEnumType.setSuperEnumType(level3EnumType.getQualifiedName());
        // false because one one super class is in wrong project
        assertThat(rootEnumType.isSubEnumTypeOf(rootEnumType, ipsProject), is(false));
        // true because of a hierarchy-cycle
        assertThat(rootEnumType.isSubEnumTypeOf(rootEnumType, otherProject), is(true));
        // false because of wrong project
        assertThat(rootEnumType.isSubEnumTypeOf(enumTypeInOtherProject, ipsProject), is(false));
        assertThat(rootEnumType.isSubEnumTypeOf(level1EnumType, ipsProject), is(false));
        assertThat(rootEnumType.isSubEnumTypeOf(enumTypeInOtherProject, otherProject), is(true));
        assertThat(rootEnumType.isSubEnumTypeOf(level1EnumType, otherProject), is(true));
        assertThat(rootEnumType.isSubEnumTypeOf(level2EnumType, otherProject), is(true));
        assertThat(rootEnumType.isSubEnumTypeOf(level3EnumType, otherProject), is(true));
    }

    @Test
    public void testIsSubEnumTypeOrSelf() {
        /*
         * The method isSubEnumTypeOrSelf only checks for self and calls isSubEnumTypeOf so only the
         * "self-case" have to be tested.
         */
        IEnumType rootEnumType = newEnumType(ipsProject, "RootEnumType");

        assertThat(rootEnumType.isSubEnumTypeOrSelf(null, null), is(false));
        assertThat(rootEnumType.isSubEnumTypeOrSelf(null, ipsProject), is(false));
        assertThat(rootEnumType.isSubEnumTypeOrSelf(rootEnumType, null), is(true));
        assertThat(rootEnumType.isSubEnumTypeOrSelf(rootEnumType, ipsProject), is(true));
    }

    @Test
    public void testGetSetEnumContentPackageFragment() {
        assertThat(genderEnumType.getEnumContentName(), is(ENUMCONTENTS_NAME));
        genderEnumType.setEnumContentName("bar");
        assertThat(genderEnumType.getEnumContentName(), is("bar"));
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
        assertThat(inheritEnumAttributeCandidates.size(), is(2));
        assertThat(inheritEnumAttributeCandidates.get(0), is(genderEnumAttributeId));
        assertThat(inheritEnumAttributeCandidates.get(1), is(genderEnumAttributeName));

        assertThat(genderEnumType.findInheritEnumAttributeCandidates(ipsProject).size(), is(0));
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
        assertThat(subEnumType.getEnumAttributesCountIncludeSupertypeCopies(false), is(2));
        IEnumAttribute inheritedName = subEnumType
                .getEnumAttributeIncludeSupertypeCopies((GENDER_ENUM_ATTRIBUTE_NAME_NAME));
        assertThat(inheritedName, is(notNullValue()));
        assertThat(inheritedName.isInherited(), is(true));

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
        assertThat(dependenciesSubEnumType.length, is(1));
        IDependency[] dependenciesSubSubEnumType = subSubEnumType.dependsOn();
        assertThat(dependenciesSubSubEnumType.length, is(1));

        List<IDependency> depencendiesListSubEnumType = Arrays.asList(dependenciesSubEnumType);
        IDependency superEnumTypeDependency = IpsObjectDependency.createSubtypeDependency(
                subEnumType.getQualifiedNameType(),
                new QualifiedNameType(genderEnumType.getQualifiedName(), IpsObjectType.ENUM_TYPE));
        assertThat(depencendiesListSubEnumType.contains(superEnumTypeDependency), is(true));

        List<IDependencyDetail> details = subEnumType.getDependencyDetails(dependenciesSubEnumType[0]);
        DependencyDetail detail = new DependencyDetail(subEnumType, IEnumType.PROPERTY_SUPERTYPE);
        assertThat(details.size(), is(1));
        assertThat(details.contains(detail), is(true));

        List<IDependency> depencendiesListSubSubEnumType = Arrays.asList(dependenciesSubSubEnumType);
        superEnumTypeDependency = IpsObjectDependency.createSubtypeDependency(subSubEnumType.getQualifiedNameType(),
                new QualifiedNameType(subEnumType.getQualifiedName(), IpsObjectType.ENUM_TYPE));
        assertThat(depencendiesListSubSubEnumType.contains(superEnumTypeDependency), is(true));

        details = subSubEnumType.getDependencyDetails(dependenciesSubSubEnumType[0]);
        detail = new DependencyDetail(subSubEnumType, IEnumType.PROPERTY_SUPERTYPE);
        assertThat(details.size(), is(1));
        assertThat(details.contains(detail), is(true));
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
        assertThat(resultList.size(), is(2));
        assertThat(resultList.contains(enum1.getIpsSrcFile()), is(true));
        assertThat(resultList.contains(enum2.getIpsSrcFile()), is(true));
        assertThat(resultList.contains(enum3.getIpsSrcFile()), is(false));

        EnumContent enumProj2 = newEnumContent(referencingProject, enumProj2QName);
        enumProj2.setEnumType(enumTypeQName);

        resultList = enumType.searchMetaObjectSrcFiles(true);
        assertThat(resultList.size(), is(3));
        assertThat(resultList.contains(enum1.getIpsSrcFile()), is(true));
        assertThat(resultList.contains(enum2.getIpsSrcFile()), is(true));
        assertThat(resultList.contains(enumProj2.getIpsSrcFile()), is(true));
        assertThat(resultList.contains(enum3.getIpsSrcFile()), is(false));

        EnumType enumTypeProj2 = newEnumType(independentProject, enumTypeProj2QName);

        resultList = enumTypeProj2.searchMetaObjectSrcFiles(true);
        assertThat(resultList.size(), is(0));

        EnumType superEnum = newEnumType(ipsProject, "superEnum");
        superEnum.setAbstract(true);
        enumType.setSuperEnumType(superEnum.getQualifiedName());

        resultList = enumTypeProj2.searchMetaObjectSrcFiles(false);
        assertThat(resultList.size(), is(0));

        resultList = superEnum.searchMetaObjectSrcFiles(true);
        assertThat(resultList.size(), is(3));
        assertThat(resultList.contains(enum1.getIpsSrcFile()), is(true));
        assertThat(resultList.contains(enum2.getIpsSrcFile()), is(true));
        assertThat(resultList.contains(enumProj2.getIpsSrcFile()), is(true));
        assertThat(resultList.contains(enum3.getIpsSrcFile()), is(false));
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
        assertThat(resultAttr, is(attr1));
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
        assertThat(resultAttr, is(attr2));
    }

    @Test
    public void testContainsEnumAttribute() {
        genderEnumType.setAbstract(true);
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());
        IEnumAttribute subAttribute = subEnumType.newEnumAttribute();
        subAttribute.setName("sub");
        subAttribute.setInherited(true);

        assertThat(genderEnumType.containsEnumAttribute(GENDER_ENUM_ATTRIBUTE_ID_NAME), is(true));
        assertThat(subEnumType.containsEnumAttribute("sub"), is(false));
        assertThat(subEnumType.containsEnumAttributeIncludeSupertypeCopies("sub"), is(true));
    }

    @Test
    public void testContainsEnumLiteralNameAttribute() {
        assertThat(genderEnumType.containsEnumLiteralNameAttribute(), is(true));
        genderEnumType.getEnumLiteralNameAttribute().delete();
        assertThat(genderEnumType.containsEnumLiteralNameAttribute(), is(false));
    }

    @Test
    public void testGetEnumLiteralNameAttribute() {
        assertThat(paymentMode.getEnumLiteralNameAttribute(), is(paymentMode.getEnumAttributes(true).get(0)));
    }

    @Test
    public void testGetEnumLiteralNameAttributesCount() {
        assertThat(paymentMode.getEnumLiteralNameAttributesCount(), is(1));
        paymentMode.newEnumLiteralNameAttribute();
        assertThat(paymentMode.getEnumLiteralNameAttributesCount(), is(2));
    }

    @Test
    public void testIsCapableOfContainingValues() {
        assertThat(genderEnumType.isCapableOfContainingValues(), is(true));
        genderEnumType.setAbstract(true);
        genderEnumType.setAbstract(true);
        genderEnumType.setExtensible(false);
        assertThat(genderEnumType.isCapableOfContainingValues(), is(false));
        genderEnumType.setAbstract(false);
        assertThat(genderEnumType.isCapableOfContainingValues(), is(true));

        genderEnumType.setAbstract(true);
        genderEnumType.setExtensible(true);
        assertThat(genderEnumType.isCapableOfContainingValues(), is(false));
        genderEnumType.setAbstract(false);
        assertThat(genderEnumType.isCapableOfContainingValues(), is(true));
    }

    @Test
    public void testGetDescriptionFromThisOrSuper() {
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(genderEnumType.getQualifiedName());

        genderEnumType.setDescriptionText(Locale.ENGLISH, "english description");
        assertThat(subEnumType.getDescriptionTextFromThisOrSuper(Locale.ENGLISH), is("english description"));
        subEnumType.setDescriptionText(Locale.ENGLISH, "overwritten description");
        assertThat(subEnumType.getDescriptionTextFromThisOrSuper(Locale.ENGLISH), is("overwritten description"));
    }

    @Test
    public void testFindAggregatedEnumValues_notExtensible() {
        List<IEnumValue> aggregated = paymentMode.findAggregatedEnumValues();

        assertThat(aggregated.size(), is(2));
        assertThat(aggregated, is(paymentMode.getEnumValues()));
    }

    @Test
    public void testFindAggregatedEnumValues_extensibleWithContent() {
        IEnumValue typeValue = genderEnumType.newEnumValue();
        typeValue.setEnumAttributeValue(0, ValueFactory.createStringValue("DIVERSE_LITERAL"));
        typeValue.setEnumAttributeValue(1, ValueFactory.createStringValue("d"));
        typeValue.setEnumAttributeValue(2, ValueFactory.createStringValue("diverse"));

        List<IEnumValue> aggregated = genderEnumType.findAggregatedEnumValues();
        assertThat(aggregated.size(), is(1));
        assertThat(aggregated, hasItem(typeValue));

        List<IEnumValue> aggregatedContent = genderEnumContent.findAggregatedEnumValues();
        assertThat(aggregatedContent.size(), is(3));
        assertThat(aggregatedContent, hasItem(typeValue));
    }

    @Test
    public void testFindAggregatedEnumValues_extensibleNoContent() {
        IEnumType extensibleType = newEnumType(ipsProject, "ExtensibleNoContent");
        extensibleType.setAbstract(false);
        extensibleType.setExtensible(true);
        extensibleType.setEnumContentName("enumcontents.NonExistent");

        extensibleType.newEnumLiteralNameAttribute();
        IEnumAttribute idAttr = extensibleType.newEnumAttribute();
        idAttr.setName("id");
        idAttr.setDatatype(Datatype.STRING.getQualifiedName());
        idAttr.setIdentifier(true);

        IEnumValue typeValue = extensibleType.newEnumValue();
        typeValue.setEnumAttributeValue(0, ValueFactory.createStringValue("LIT"));
        typeValue.setEnumAttributeValue(1, ValueFactory.createStringValue("val"));

        List<IEnumValue> aggregated = extensibleType.findAggregatedEnumValues();

        assertThat(aggregated.size(), is(1));
        assertThat(aggregated, hasItem(typeValue));
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
