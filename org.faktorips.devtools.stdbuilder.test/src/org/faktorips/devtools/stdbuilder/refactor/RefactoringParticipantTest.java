/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.refactor;

import static org.faktorips.devtools.stdbuilder.refactor.RefactoringTestUtil.getGenerationConceptNameAbbreviation;
import static org.faktorips.devtools.stdbuilder.refactor.RefactoringTestUtil.getJavaType;
import static org.faktorips.devtools.stdbuilder.refactor.RefactoringTestUtil.getPublishedInterfaceName;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.geometry.Point;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.junit.Before;

/**
 * Provides basic functionality for the refactoring participant tests of the standard builder.
 * 
 * @author Alexander Weickmann
 */
public abstract class RefactoringParticipantTest extends AbstractStdBuilderTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        configureBuilderSetToGenerateJaxbSupport();
    }

    private void configureBuilderSetToGenerateJaxbSupport() throws CoreException {
        IIpsProjectProperties ipsProjectProperties = ipsProject.getProperties();
        IIpsArtefactBuilderSetConfigModel configModel = ipsProjectProperties.getBuilderSetConfig();
        configModel.setPropertyValue(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT, "true", null);
        ipsProjectProperties.setBuilderSetConfig(configModel);
        ipsProject.setProperties(ipsProjectProperties);
    }

    protected void checkJavaSourceFilesPolicyCmptType(String originalPackageName,
            String originalName,
            String targetPackageName,
            String newName) throws CoreException {

        assertFalse(getJavaType(originalPackageName, getPublishedInterfaceName(originalName, ipsProject), true, false,
                ipsProject).exists());
        assertFalse(getJavaType(originalPackageName, originalName, false, false, ipsProject).exists());

        assertTrue(getJavaType(targetPackageName, getPublishedInterfaceName(newName, ipsProject), true, false,
                ipsProject).exists());
        assertTrue(getJavaType(targetPackageName, newName, false, false, ipsProject).exists());
    }

    protected void checkJavaSourceFilesProductCmptType(String originalPackageName,
            String originalName,
            String targetPackageName,
            String newName) throws CoreException {

        assertFalse(getJavaType(originalPackageName, originalName, false, false, ipsProject).exists());
        assertFalse(getJavaType(originalPackageName, getPublishedInterfaceName(originalName, ipsProject), true, false,
                ipsProject).exists());
        assertFalse(getJavaType(originalPackageName, originalName + getGenerationConceptNameAbbreviation(ipsProject),
                false, false, ipsProject).exists());
        assertFalse(getJavaType(originalPackageName,
                getPublishedInterfaceName(originalName + getGenerationConceptNameAbbreviation(ipsProject), ipsProject),
                true, false, ipsProject).exists());

        assertTrue(getJavaType(targetPackageName, newName, false, false, ipsProject).exists());
        assertTrue(getJavaType(targetPackageName, getPublishedInterfaceName(newName, ipsProject), true, false,
                ipsProject).exists());
        assertTrue(getJavaType(targetPackageName, newName + getGenerationConceptNameAbbreviation(ipsProject), false,
                false, ipsProject).exists());
        assertTrue(getJavaType(targetPackageName,
                getPublishedInterfaceName(newName + getGenerationConceptNameAbbreviation(ipsProject), ipsProject),
                true, false, ipsProject).exists());
    }

    protected void checkJavaSourceFilesEnumType(String originalPackageName,
            String originalName,
            String targetPackageName,
            String newName) throws CoreException {

        assertFalse(getJavaType(originalPackageName, originalName, true, false, ipsProject).exists());
        assertFalse(getJavaType(originalPackageName, originalName + "XmlAdapter", false, true, ipsProject).exists());

        assertTrue(getJavaType(targetPackageName, newName, true, false, ipsProject).exists());
        assertTrue(getJavaType(targetPackageName, newName + "XmlAdapter", false, false, ipsProject).exists());
    }

    protected void checkJavaSourceFilesTableStructure(String originalPackageName,
            String originalName,
            String targetPackageName,
            String newName) throws CoreException {

        assertFalse(getJavaType(originalPackageName, originalName, false, false, ipsProject).exists());
        assertFalse(getJavaType(originalPackageName, originalName + "Row", false, false, ipsProject).exists());

        assertTrue(getJavaType(targetPackageName, newName, false, false, ipsProject).exists());
        assertTrue(getJavaType(targetPackageName, newName + "Row", false, false, ipsProject).exists());
    }

    protected void checkJavaSourceFilesTestCaseType(String originalPackageName,
            String originalName,
            String targetPackageName,
            String newName) throws CoreException {

        assertFalse(getJavaType(originalPackageName, originalName, false, false, ipsProject).exists());

        assertTrue(getJavaType(targetPackageName, newName, false, false, ipsProject).exists());
    }

    protected void checkJavaSourceFilesBusinessFunction(String originalPackageName,
            String originalName,
            String targetPackageName,
            String newName) throws CoreException {

        assertFalse(getJavaType(originalPackageName, originalName, true, false, ipsProject).exists());

        assertTrue(getJavaType(targetPackageName, newName, true, false, ipsProject).exists());
    }

    protected IEnumType createEnumType(String name,
            IEnumType superEnumType,
            String idAttributeName,
            String nameAttributeName) throws CoreException {

        IEnumType enumType = newEnumType(ipsProject, name);
        enumType.setAbstract(false);
        enumType.setExtensible(false);
        enumType.setSuperEnumType(superEnumType != null ? superEnumType.getQualifiedName() : "");

        IEnumAttribute idAttribute = enumType.newEnumAttribute();
        idAttribute.setName(idAttributeName);
        idAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        idAttribute.setIdentifier(true);
        idAttribute.setUnique(true);
        idAttribute.setInherited(superEnumType != null);

        IEnumAttribute nameAttribute = enumType.newEnumAttribute();
        nameAttribute.setName(nameAttributeName);
        nameAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        nameAttribute.setUnique(true);
        nameAttribute.setUsedAsNameInFaktorIpsUi(true);
        idAttribute.setInherited(superEnumType != null);

        return enumType;
    }

    protected IEnumType createEnumType(String name,
            IEnumType superEnumType,
            String idAttributeName,
            String nameAttributeName,
            String literalNameDefaultValueProviderAttribute,
            String idAttributeValue,
            String nameAttributeValue,
            String literalNameAttributeValue) throws CoreException {

        IEnumType enumType = createEnumType(name, superEnumType, idAttributeName, nameAttributeName);

        IEnumLiteralNameAttribute literalAttribute = enumType.newEnumLiteralNameAttribute();
        literalAttribute.setDefaultValueProviderAttribute(literalNameDefaultValueProviderAttribute);

        IEnumValue enumValue = enumType.newEnumValue();
        enumValue.setEnumAttributeValue(0, ValueFactory.createStringValue(idAttributeValue));
        enumValue.setEnumAttributeValue(1, ValueFactory.createStringValue(nameAttributeValue));
        enumValue.setEnumAttributeValue(2, ValueFactory.createStringValue(literalNameAttributeValue));

        return enumType;
    }

    protected ITableStructure createTableStructure(String name) throws CoreException {
        ITableStructure tableStructure = newTableStructure(ipsProject, name);
        tableStructure.setTableStructureType(TableStructureType.SINGLE_CONTENT);
        return tableStructure;
    }

    protected ITestCaseType createTestCaseType(String name) throws CoreException {
        return newTestCaseType(ipsProject, name);
    }

    protected IBusinessFunction createBusinessFunction(String name) throws CoreException {
        IBusinessFunction businessFunction = (IBusinessFunction)newIpsObject(ipsProject,
                BusinessFunctionIpsObjectType.getInstance(), name);
        businessFunction.newStart(new Point(0, 0));
        businessFunction.newEnd(new Point(10, 10));
        IControlFlow controlFlow = businessFunction.newControlFlow();
        controlFlow.setSource(businessFunction.getStart());
        controlFlow.setTarget(businessFunction.getEnd());
        return businessFunction;
    }

    protected void saveIpsSrcFile(IIpsObject ipsObject) throws CoreException {
        ipsObject.getIpsSrcFile().save(true, null);
    }

}
