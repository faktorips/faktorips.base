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
import org.faktorips.devtools.core.model.enums.IEnumContent;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
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
    }

    public void testGetIpsObjectType() {
        assertEquals(IpsObjectType.ENUM_CONTENT, genderEnumContent.getIpsObjectType());
    }

    public void testFindEnumType() throws CoreException {
        assertEquals(genderEnumType, genderEnumContent.findEnumType());
        genderEnumContent.setEnumType("");
        assertNull(genderEnumContent.findEnumType());
    }

    public void testXml() throws ParserConfigurationException, CoreException {
        Element xmlElement = genderEnumContent.toXml(createXmlDocument(IEnumContent.XML_TAG));
        assertEquals(genderEnumType.getQualifiedName(), xmlElement.getAttribute(IEnumContent.PROPERTY_ENUM_TYPE));

        IEnumContent loadedEnumContent = newEnumContent(ipsProject, "LoadedEnumValues");
        loadedEnumContent.initFromXml(xmlElement);
        assertEquals(genderEnumType.getQualifiedName(), loadedEnumContent.getEnumType());
    }

    public void testValidateThis() throws CoreException {
        assertTrue(genderEnumContent.isValid());
    }

    public void testValidateEnumType() throws CoreException {
        IIpsModel ipsModel = getIpsModel();

        // Test enum type missing
        ipsModel.clearValidationCache();
        genderEnumContent.setEnumType("");
        assertEquals(1, genderEnumContent.validate(ipsProject).getNoOfMessages());
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());

        // Test enum type does not exist
        ipsModel.clearValidationCache();
        genderEnumContent.setEnumType("FooBar");
        assertEquals(1, genderEnumContent.validate(ipsProject).getNoOfMessages());
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());

        // Test values are part of model
        ipsModel.clearValidationCache();
        genderEnumType.setValuesArePartOfModel(true);
        assertEquals(1, genderEnumContent.validate(ipsProject).getNoOfMessages());
        genderEnumType.setValuesArePartOfModel(false);

        // Test enum type is abstract
        ipsModel.clearValidationCache();
        genderEnumType.setAbstract(true);
        assertEquals(1, genderEnumContent.validate(ipsProject).getNoOfMessages());
        genderEnumType.setAbstract(false);
    }

    public void testValidateReferencedEnumAttributesCount() throws CoreException {
        genderEnumType.newEnumAttribute();
        assertEquals(1, genderEnumContent.validate(ipsProject).getNoOfMessages());
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

    public void testGetReferencedEnumAttributesCount() throws CoreException {
        assertEquals(2, genderEnumContent.getReferencedEnumAttributesCount());

        genderEnumType.newEnumAttribute();
        genderEnumContent.setEnumType(genderEnumType.getQualifiedName());
        assertEquals(3, genderEnumContent.getReferencedEnumAttributesCount());
    }

}
