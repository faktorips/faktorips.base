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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.DependencyDetail;
import org.faktorips.devtools.core.model.IDependency;
import org.faktorips.devtools.core.model.IDependencyDetail;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IpsObjectDependency;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumAttributeReference;
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.util.message.MessageList;
import org.junit.Test;
import org.w3c.dom.Element;

public class EnumContentTest extends AbstractIpsEnumPluginTest {

    @Test
    public void testGetSetEnumType() throws CoreException {
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
    public void testFindEnumType() throws CoreException {
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
    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = genderEnumContent.toXml(createXmlDocument(IEnumContent.XML_TAG));
        assertEquals(genderEnumType.getQualifiedName(), xmlElement.getAttribute(IEnumContent.PROPERTY_ENUM_TYPE));

        IEnumContent loadedEnumContent = newEnumContent(ipsProject, "LoadedEnumContent");
        loadedEnumContent.initFromXml(xmlElement);
        assertEquals(genderEnumType.getQualifiedName(), loadedEnumContent.getEnumType());
    }

    @Test
    public void testValidateThis() throws CoreException {
        assertTrue(genderEnumContent.isValid(ipsProject));
    }

    @Test
    public void testValidateEnumType() throws CoreException {
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
        genderEnumType.setContainingValues(true);
        validationMessageList = genderEnumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_VALUES_ARE_PART_OF_TYPE));
        genderEnumType.setContainingValues(false);

        // Test EnumType is abstract.
        ipsModel.clearValidationCache();
        genderEnumType.setAbstract(true);
        validationMessageList = genderEnumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_ENUM_TYPE_IS_ABSTRACT));
        genderEnumType.setAbstract(false);
    }

    @Test
    public void testValidateReferencedEnumAttributesCount() throws CoreException {
        genderEnumType.newEnumAttribute();
        MessageList validationMessageList = genderEnumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTES_COUNT_INVALID));
    }

    @Test
    public void testValidateReferencedEnumAttributeNames() throws CoreException {
        genderEnumAttributeId.setName("foo");
        MessageList validationMessageList = genderEnumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_NAMES_INVALID));
    }

    @Test
    public void testValidateReferencedEnumAttributeOrdering() throws CoreException {
        genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        MessageList validationMessageList = genderEnumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_ORDERING_INVALID));
    }

    @Test
    public void testValidateName() throws CoreException {
        IEnumContent enumContent = newEnumContent(ipsProject, "foo.Bar");
        enumContent.setEnumType(genderEnumType.getQualifiedName());
        MessageList validationMessageList = enumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_NAME_NOT_CORRECT));
    }

    @Test
    public void testDependsOn() throws CoreException {
        IDependency[] dependencies = genderEnumContent.dependsOn();
        assertEquals(1, dependencies.length);

        List<IDependency> depencendiesList = Arrays.asList(dependencies);
        IDependency enumTypeDependency = IpsObjectDependency.createReferenceDependency(genderEnumContent
                .getQualifiedNameType(), new QualifiedNameType(genderEnumType.getQualifiedName(),
                IpsObjectType.ENUM_TYPE));
        assertTrue(depencendiesList.contains(enumTypeDependency));

        List<IDependencyDetail> details = genderEnumContent.getDependencyDetails(dependencies[0]);
        DependencyDetail detail = new DependencyDetail(genderEnumContent, IEnumContent.PROPERTY_ENUM_TYPE);
        assertEquals(1, details.size());
        assertTrue(details.contains(detail));
    }

    @Test
    public void testGetEnumAttributeReferencesCount() throws CoreException {
        assertEquals(2, genderEnumContent.getEnumAttributeReferencesCount());

        genderEnumType.newEnumAttribute();
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());
        assertEquals(3, genderEnumContent.getEnumAttributeReferencesCount());
    }

    @Test
    public void testFindMetaClass() throws CoreException {
        IEnumType type = newEnumType(ipsProject, "EnumType");
        EnumContent enumContent = newEnumContent(type, "enumContent");

        IIpsSrcFile typeSrcFile = enumContent.findMetaClassSrcFile(ipsProject);
        assertEquals(type.getIpsSrcFile(), typeSrcFile);
    }

    @Test
    public void testContainsDifferenceToModel() throws CoreException {
        assertFalse(genderEnumContent.containsDifferenceToModel(ipsProject));
    }

    @Test
    public void testIsFixToModelRequired() throws CoreException {
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

        genderEnumType.setContainingValues(true);
        assertTrue(genderEnumContent.isFixToModelRequired());
        genderEnumType.setContainingValues(false);
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
        List<IEnumAttributeReference> references = genderEnumContent.getEnumAttributeReferences();
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
    public void testIsCapableOfContainingValues() throws CoreException {
        assertTrue(genderEnumContent.isCapableOfContainingValues());
        genderEnumContent.setEnumType("foo");
        assertFalse(genderEnumContent.isCapableOfContainingValues());
    }

}
