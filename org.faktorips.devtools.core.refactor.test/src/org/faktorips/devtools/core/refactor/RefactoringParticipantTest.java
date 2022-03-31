/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.refactor;

import static org.faktorips.devtools.core.refactor.RefactoringTestUtil.getGenerationConceptNameAbbreviation;
import static org.faktorips.devtools.core.refactor.RefactoringTestUtil.getJavaType;
import static org.faktorips.devtools.core.refactor.RefactoringTestUtil.getPublishedInterfaceName;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.IType;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.enums.IEnumValue;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.tablestructure.TableStructureType;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.value.ValueFactory;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
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

    private void configureBuilderSetToGenerateJaxbSupport() {
        IIpsProjectProperties ipsProjectProperties = ipsProject.getProperties();
        IIpsArtefactBuilderSetConfigModel configModel = ipsProjectProperties.getBuilderSetConfig();
        configModel.setPropertyValue(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT, "true", null);
        ipsProjectProperties.setBuilderSetConfig(configModel);
        ipsProject.setProperties(ipsProjectProperties);
    }

    protected void checkJavaSourceFilesPolicyCmptType(String originalPackageName,
            String originalName,
            String targetPackageName,
            String newName) {

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
            String newName) {

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
            String newName) {

        assertThat(getJavaType(originalPackageName, originalName, true, false, ipsProject), not(exists()));
        assertThat(getJavaType(originalPackageName, originalName + "XmlAdapter", false, true, ipsProject),
                not(exists()));

        assertThat(getJavaType(targetPackageName, newName, true, false, ipsProject), exists());
        assertThat(getJavaType(targetPackageName, newName + "XmlAdapter", false, false, ipsProject), exists());
    }

    private static Matcher<IType> exists() {
        return new TypeSafeMatcher<>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("exists");
            }

            @Override
            protected boolean matchesSafely(IType type) {
                return type.exists();
            }
        };
    }

    protected void checkJavaSourceFilesTableStructure(String originalPackageName,
            String originalName,
            String targetPackageName,
            String newName) {

        assertThat(getJavaType(originalPackageName, originalName, false, false, ipsProject), not(exists()));
        assertThat(getJavaType(originalPackageName, originalName + "Row", false, false, ipsProject), not(exists()));

        assertThat(getJavaType(targetPackageName, newName, false, false, ipsProject), exists());
        assertThat(getJavaType(targetPackageName, newName + "Row", false, false, ipsProject), exists());
    }

    protected void checkJavaSourceFilesTestCaseType(String originalPackageName,
            String originalName,
            String targetPackageName,
            String newName) {

        assertThat(getJavaType(originalPackageName, originalName, false, false, ipsProject), not(exists()));

        assertThat(getJavaType(targetPackageName, newName, false, false, ipsProject), exists());
    }

    protected IEnumType createEnumType(String name,
            IEnumType superEnumType,
            String idAttributeName,
            String nameAttributeName) {

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
            String literalNameAttributeValue) {

        IEnumType enumType = createEnumType(name, superEnumType, idAttributeName, nameAttributeName);

        IEnumLiteralNameAttribute literalAttribute = enumType.newEnumLiteralNameAttribute();
        literalAttribute.setDefaultValueProviderAttribute(literalNameDefaultValueProviderAttribute);

        IEnumValue enumValue = enumType.newEnumValue();
        enumValue.setEnumAttributeValue(0, ValueFactory.createStringValue(idAttributeValue));
        enumValue.setEnumAttributeValue(1, ValueFactory.createStringValue(nameAttributeValue));
        enumValue.setEnumAttributeValue(2, ValueFactory.createStringValue(literalNameAttributeValue));

        return enumType;
    }

    protected ITableStructure createTableStructure(String name) {
        ITableStructure tableStructure = newTableStructure(ipsProject, name);
        tableStructure.setTableStructureType(TableStructureType.SINGLE_CONTENT);
        return tableStructure;
    }

    protected ITestCaseType createTestCaseType(String name) {
        return newTestCaseType(ipsProject, name);
    }

    protected void saveIpsSrcFile(IIpsObject ipsObject) {
        ipsObject.getIpsSrcFile().save(null);
    }

}
