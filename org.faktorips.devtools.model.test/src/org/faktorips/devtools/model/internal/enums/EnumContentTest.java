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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.faktorips.abstracttest.AbstractIpsEnumPluginTest;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.IPartReference;
import org.faktorips.devtools.model.dependency.IDependency;
import org.faktorips.devtools.model.dependency.IDependencyDetail;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.internal.dependency.DependencyDetail;
import org.faktorips.devtools.model.internal.dependency.IpsObjectDependency;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.QualifiedNameType;
import org.faktorips.runtime.MessageList;
import org.junit.Test;
import org.w3c.dom.Element;

public class EnumContentTest extends AbstractIpsEnumPluginTest {

    @Test
    public void testGetSetEnumType() {
        assertEquals(genderEnumType.getQualifiedName(), genderEnumContent.getEnumType());
        try {
            genderEnumContent.setEnumType(null);
            fail();
        } catch (NullPointerException e) {
        }

        genderEnumContent.setEnumType(paymentMode.getQualifiedName());
        assertEquals(paymentMode.getQualifiedName(), genderEnumContent.getEnumType());
        assertEquals(2, genderEnumContent.getEnumAttributeReferencesCount());
    }

    @Test
    public void testGetIpsObjectType() {
        assertEquals(IpsObjectType.ENUM_CONTENT, genderEnumContent.getIpsObjectType());
    }

    @Test
    public void testFindEnumType() {
        try {
            genderEnumContent.findEnumType(null);
            fail();
        } catch (NullPointerException e) {
        }

        assertEquals(genderEnumType, genderEnumContent.findEnumType(ipsProject));
        genderEnumContent.setEnumType("");
        assertNull(genderEnumContent.findEnumType(ipsProject));
    }

    @Test
    public void testXml() throws ParserConfigurationException {
        Element xmlElement = genderEnumContent.toXml(createXmlDocument(IEnumContent.XML_TAG));
        assertEquals(genderEnumType.getQualifiedName(), xmlElement.getAttribute(IEnumContent.PROPERTY_ENUM_TYPE));

        IEnumContent loadedEnumContent = newEnumContent(ipsProject, "LoadedEnumContent");
        loadedEnumContent.initFromXml(xmlElement);
        assertEquals(genderEnumType.getQualifiedName(), loadedEnumContent.getEnumType());
    }

    @Test
    public void testValidateThis() {
        assertTrue(genderEnumContent.isValid(ipsProject));
    }

    @Test
    public void testValidateEnumType() {
        IIpsModel ipsModel = getIpsModel();

        // Test EnumType missing.
        ipsModel.clearValidationCache();
        genderEnumContent.setEnumType("");
        MessageList validationMessageList = genderEnumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_MISSING));
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());

        // Test EnumType does not exist.
        ipsModel.clearValidationCache();
        genderEnumContent.setEnumType("FooBar");
        validationMessageList = genderEnumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_DOES_NOT_EXIST));
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());

        // Test values are part of type.
        ipsModel.clearValidationCache();
        genderEnumType.setExtensible(false);
        validationMessageList = genderEnumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(
                validationMessageList.getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_VALUES_ARE_PART_OF_TYPE));
        genderEnumType.setExtensible(true);

        // Test EnumType is abstract.
        ipsModel.clearValidationCache();
        genderEnumType.setAbstract(true);
        validationMessageList = genderEnumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_IS_ABSTRACT));
        genderEnumType.setAbstract(false);
    }

    @Test
    public void testValidateReferencedEnumAttributesCount() {
        genderEnumType.newEnumAttribute();
        MessageList validationMessageList = genderEnumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTES_COUNT_INVALID));
    }

    @Test
    public void testValidateReferencedEnumAttributeNames() {
        genderEnumAttributeId.setName("foo");
        MessageList validationMessageList = genderEnumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_NAMES_INVALID));
    }

    @Test
    public void testValidateReferencedEnumAttributeOrdering() {
        genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        MessageList validationMessageList = genderEnumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_ORDERING_INVALID));
    }

    @Test
    public void testValidateName() {
        IEnumContent enumContent = newEnumContent(ipsProject, "foo.Bar");
        enumContent.setEnumType(genderEnumType.getQualifiedName());
        MessageList validationMessageList = enumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_NAME_NOT_CORRECT));
    }

    @Test
    public void testDependsOn() {
        IDependency[] dependencies = genderEnumContent.dependsOn();
        assertEquals(1, dependencies.length);

        List<IDependency> depencendiesList = Arrays.asList(dependencies);
        IDependency enumTypeDependency = IpsObjectDependency.createInstanceOfDependency(genderEnumContent
                .getQualifiedNameType(),
                new QualifiedNameType(genderEnumType.getQualifiedName(),
                        IpsObjectType.ENUM_TYPE));
        assertTrue(depencendiesList.contains(enumTypeDependency));

        List<IDependencyDetail> details = genderEnumContent.getDependencyDetails(dependencies[0]);
        DependencyDetail detail = new DependencyDetail(genderEnumContent, IEnumContent.PROPERTY_ENUM_TYPE);
        assertEquals(1, details.size());
        assertTrue(details.contains(detail));
    }

    @Test
    public void testGetEnumAttributeReferencesCount() {
        assertEquals(2, genderEnumContent.getEnumAttributeReferencesCount());

        genderEnumType.newEnumAttribute();
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());
        assertEquals(3, genderEnumContent.getEnumAttributeReferencesCount());
    }

    @Test
    public void testFindMetaClass() {
        IEnumType type = newEnumType(ipsProject, "EnumType");
        EnumContent enumContent = newEnumContent(type, "enumContent");

        IIpsSrcFile typeSrcFile = enumContent.findMetaClassSrcFile(ipsProject);
        assertEquals(type.getIpsSrcFile(), typeSrcFile);
    }

    @Test
    public void testContainsDifferenceToModel() {
        assertFalse(genderEnumContent.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testIsFixToModelRequired() {
        assertFalse(genderEnumContent.isFixToModelRequired());
        genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        assertTrue(genderEnumContent.isFixToModelRequired());
        genderEnumType.moveEnumAttribute(genderEnumAttributeId, true);
        assertFalse(genderEnumContent.isFixToModelRequired());

        genderEnumAttributeId.setName("foo");
        assertTrue(genderEnumContent.isFixToModelRequired());
        genderEnumAttributeId.setName("Id");
        assertFalse(genderEnumContent.isFixToModelRequired());

        IEnumAttribute newAttribute = genderEnumType.newEnumAttribute();
        newAttribute.setName("new");
        assertTrue(genderEnumContent.isFixToModelRequired());
        newAttribute.delete();
        assertFalse(genderEnumContent.isFixToModelRequired());

        genderEnumType.setAbstract(true);
        assertTrue(genderEnumContent.isFixToModelRequired());
        genderEnumType.setAbstract(false);
        assertFalse(genderEnumContent.isFixToModelRequired());

        genderEnumType.setExtensible(false);
        assertTrue(genderEnumContent.isFixToModelRequired());
        genderEnumType.setExtensible(true);
        assertFalse(genderEnumContent.isFixToModelRequired());

        genderEnumContent.setEnumType("foo");
        assertTrue(genderEnumContent.isFixToModelRequired());
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());
        assertFalse(genderEnumContent.isFixToModelRequired());

        genderEnumContent.setEnumType("");
        assertTrue(genderEnumContent.isFixToModelRequired());
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());
        assertFalse(genderEnumContent.isFixToModelRequired());
    }

    @Test
    public void testGetEnumAttributeReferences() {
        List<IPartReference> references = genderEnumContent.getEnumAttributeReferences();
        assertEquals(2, references.size());
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, references.get(0).getName());
        assertEquals(GENDER_ENUM_ATTRIBUTE_NAME_NAME, references.get(1).getName());
    }

    @Test
    public void testGetEnumAttributeReference() {
        try {
            genderEnumContent.getEnumAttributeReference(null);
        } catch (NullPointerException e) {
        }

        assertNotNull(genderEnumContent.getEnumAttributeReference(GENDER_ENUM_ATTRIBUTE_ID_NAME));
        assertNotNull(genderEnumContent.getEnumAttributeReference(GENDER_ENUM_ATTRIBUTE_NAME_NAME));
        assertNull(genderEnumContent.getEnumAttributeReference("foobar"));
    }

    @Test
    public void testIsCapableOfContainingValues() {
        assertTrue(genderEnumContent.isCapableOfContainingValues());
        genderEnumContent.setEnumType("foo");
        assertFalse(genderEnumContent.isCapableOfContainingValues());
    }

    @Test
    public void testFindAggregatedEnumValue() {
        EnumContent enumContent = (EnumContent)genderEnumContent;
        assertEquals(2, enumContent.findAggregatedEnumValues().size());
        genderEnumType.newEnumValue();
        assertEquals(1, genderEnumType.getEnumValues().size());
        assertEquals(2, genderEnumContent.getEnumValues().size());
        assertEquals(3, enumContent.findAggregatedEnumValues().size());
    }
}
