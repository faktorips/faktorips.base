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
import org.faktorips.devtools.core.model.IDependency;
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
import org.w3c.dom.Element;

public class EnumContentTest extends AbstractIpsEnumPluginTest {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

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

    public void testGetIpsObjectType() {
        assertEquals(IpsObjectType.ENUM_CONTENT, genderEnumContent.getIpsObjectType());
    }

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

    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = genderEnumContent.toXml(createXmlDocument(IEnumContent.XML_TAG));
        assertEquals(genderEnumType.getQualifiedName(), xmlElement.getAttribute(IEnumContent.PROPERTY_ENUM_TYPE));

        IEnumContent loadedEnumContent = newEnumContent(ipsProject, "LoadedEnumContent");
        loadedEnumContent.initFromXml(xmlElement);
        assertEquals(genderEnumType.getQualifiedName(), loadedEnumContent.getEnumType());
    }

    public void testValidateThis() throws CoreException {
        assertTrue(genderEnumContent.isValid());
    }

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

    public void testValidateReferencedEnumAttributesCount() throws CoreException {
        genderEnumType.newEnumAttribute();
        MessageList validationMessageList = genderEnumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTES_COUNT_INVALID));
    }

    public void testValidateReferencedEnumAttributeNames() throws CoreException {
        genderEnumAttributeId.setName("foo");
        MessageList validationMessageList = genderEnumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_NAMES_INVALID));
    }

    public void testValidateReferencedEnumAttributeOrdering() throws CoreException {
        genderEnumType.moveEnumAttribute(genderEnumAttributeId, false);
        MessageList validationMessageList = genderEnumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList
                .getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_REFERENCED_ENUM_ATTRIBUTE_ORDERING_INVALID));
    }

    public void testValidateName() throws CoreException {
        IEnumContent enumContent = newEnumContent(ipsProject, "foo.Bar");
        enumContent.setEnumType(genderEnumType.getQualifiedName());
        MessageList validationMessageList = enumContent.validate(ipsProject);
        assertOneValidationMessage(validationMessageList);
        assertNotNull(validationMessageList.getMessageByCode(IEnumContent.MSGCODE_ENUM_CONTENT_NAME_NOT_CORRECT));
    }

    public void testDependsOn() throws CoreException {
        IDependency[] dependencies = genderEnumContent.dependsOn();
        assertEquals(1, dependencies.length);

        List<IDependency> depencendiesList = Arrays.asList(dependencies);
        IDependency enumTypeDependency = IpsObjectDependency.createReferenceDependency(genderEnumContent
                .getQualifiedNameType(), new QualifiedNameType(genderEnumType.getQualifiedName(),
                IpsObjectType.ENUM_TYPE));
        assertTrue(depencendiesList.contains(enumTypeDependency));
    }

    public void testGetEnumAttributeReferencesCount() throws CoreException {
        assertEquals(2, genderEnumContent.getEnumAttributeReferencesCount());

        genderEnumType.newEnumAttribute();
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());
        assertEquals(3, genderEnumContent.getEnumAttributeReferencesCount());
    }

    public void testFindMetaClass() throws CoreException {
        IEnumType type = newEnumType(ipsProject, "EnumType");
        EnumContent enumContent = newEnumContent(type, "enumContent");

        IIpsSrcFile typeSrcFile = enumContent.findMetaClassSrcFile(ipsProject);
        assertEquals(type.getIpsSrcFile(), typeSrcFile);
    }

    public void testContainsDifferenceToModel() throws CoreException {
        assertFalse(genderEnumContent.containsDifferenceToModel(ipsProject));
    }

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

    public void testGetEnumAttributeReferences() {
        List<IEnumAttributeReference> references = genderEnumContent.getEnumAttributeReferences();
        assertEquals(2, references.size());
        assertEquals(GENDER_ENUM_ATTRIBUTE_ID_NAME, references.get(0).getName());
        assertEquals(GENDER_ENUM_ATTRIBUTE_NAME_NAME, references.get(1).getName());
    }

    public void testIsCapableOfContainingValues() throws CoreException {
        assertTrue(genderEnumContent.isCapableOfContainingValues());
        genderEnumContent.setEnumType("foo");
        assertFalse(genderEnumContent.isCapableOfContainingValues());
    }

}
