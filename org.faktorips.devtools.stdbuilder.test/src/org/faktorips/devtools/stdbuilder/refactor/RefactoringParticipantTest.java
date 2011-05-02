/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.stdbuilder.refactor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.internal.model.tablestructure.TableStructureType;
import org.faktorips.devtools.core.model.bf.BusinessFunctionIpsObjectType;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.enums.IEnumValue;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
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

        assertFalse(getJavaType(originalPackageName, "I" + originalName, true, false).exists());
        assertFalse(getJavaType(originalPackageName, originalName, false, false).exists());

        assertTrue(getJavaType(targetPackageName, "I" + newName, true, false).exists());
        assertTrue(getJavaType(targetPackageName, newName, false, false).exists());
    }

    protected void checkJavaSourceFilesProductCmptType(String originalPackageName,
            String originalName,
            String targetPackageName,
            String newName) throws CoreException {

        assertFalse(getJavaType(originalPackageName, originalName, false, false).exists());
        assertFalse(getJavaType(originalPackageName, "I" + originalName, true, false).exists());
        assertFalse(getJavaType(originalPackageName, originalName + "Gen", false, false).exists());
        assertFalse(getJavaType(originalPackageName, "I" + originalName + "Gen", true, false).exists());

        assertTrue(getJavaType(targetPackageName, newName, false, false).exists());
        assertTrue(getJavaType(targetPackageName, "I" + newName, true, false).exists());
        assertTrue(getJavaType(targetPackageName, newName + "Gen", false, false).exists());
        assertTrue(getJavaType(targetPackageName, "I" + newName + "Gen", true, false).exists());
    }

    protected void checkJavaSourceFilesEnumType(String originalPackageName,
            String originalName,
            String targetPackageName,
            String newName) throws CoreException {

        assertFalse(getJavaType(originalPackageName, originalName, true, false).exists());
        assertFalse(getJavaType(originalPackageName, originalName + "XmlAdapter", false, true).exists());

        assertTrue(getJavaType(targetPackageName, newName, true, false).exists());
        assertTrue(getJavaType(targetPackageName, newName + "XmlAdapter", false, true).exists());
    }

    protected void checkJavaSourceFilesTableStructure(String originalPackageName,
            String originalName,
            String targetPackageName,
            String newName) throws CoreException {

        assertFalse(getJavaType(originalPackageName, originalName, false, false).exists());
        assertFalse(getJavaType(originalPackageName, originalName + "Row", false, false).exists());

        assertTrue(getJavaType(targetPackageName, newName, false, false).exists());
        assertTrue(getJavaType(targetPackageName, newName + "Row", false, false).exists());
    }

    protected void checkJavaSourceFilesTestCaseType(String originalPackageName,
            String originalName,
            String targetPackageName,
            String newName) throws CoreException {

        assertFalse(getJavaType(originalPackageName, originalName, false, false).exists());

        assertTrue(getJavaType(targetPackageName, newName, false, false).exists());
    }

    protected void checkJavaSourceFilesBusinessFunction(String originalPackageName,
            String originalName,
            String targetPackageName,
            String newName) throws CoreException {

        assertFalse(getJavaType(originalPackageName, originalName, true, false).exists());

        assertTrue(getJavaType(targetPackageName, newName, true, false).exists());
    }

    /**
     * Returns the Java {@link IType} corresponding to the indicated package name, type name and
     * internal flag.
     * 
     * @param packageName The package where the {@link IType} is located
     * @param typeName The name of the {@link IType}
     * @param publishedSource Flag indicating whether a published interface or an implementation
     *            type is searched
     * @param derivedSource Flag indicating whether the Java source file is a derived resource or
     *            not
     */
    protected IType getJavaType(String packageName, String typeName, boolean publishedSource, boolean derivedSource)
            throws CoreException {

        IIpsSrcFolderEntry srcFolderEntry = ipsProject.getIpsObjectPath().getSourceFolderEntries()[0];
        IFolder javaSrcFolder = derivedSource ? srcFolderEntry.getOutputFolderForDerivedJavaFiles() : srcFolderEntry
                .getOutputFolderForMergableJavaFiles();
        IPackageFragmentRoot javaRoot = ipsProject.getJavaProject().getPackageFragmentRoot(javaSrcFolder);

        String basePackageName = derivedSource ? srcFolderEntry.getBasePackageNameForDerivedJavaClasses()
                : srcFolderEntry.getBasePackageNameForMergableJavaClasses();
        if (!(publishedSource)) {
            basePackageName += ".internal";
        }
        if (packageName.length() > 0) {
            packageName = "." + packageName;
        }
        IPackageFragment javaPackage = javaRoot.getPackageFragment(basePackageName + packageName);

        return javaPackage.getCompilationUnit(typeName + JavaSourceFileBuilder.JAVA_EXTENSION).getType(typeName);
    }

    protected IPolicyCmptTypeAttribute createPolicyCmptTypeAttribute(String name,
            String policyCmptTypeName,
            String productCmptTypeName) throws CoreException {

        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, policyCmptTypeName,
                productCmptTypeName);
        return createPolicyCmptTypeAttribute(name, policyCmptType);
    }

    protected IPolicyCmptTypeAttribute createPolicyCmptTypeAttribute(String name, IPolicyCmptType policyCmptType) {
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttribute.setName(name);
        policyCmptTypeAttribute.setDatatype(Datatype.MONEY.getQualifiedName());
        policyCmptTypeAttribute.setModifier(Modifier.PUBLISHED);
        policyCmptTypeAttribute.setAttributeType(AttributeType.CHANGEABLE);
        policyCmptTypeAttribute.setProductRelevant(true);
        return policyCmptTypeAttribute;
    }

    protected IProductCmptTypeAttribute createProductCmptTypeAttribute(String name,
            String productCmptTypeName,
            String policyCmptTypeName) throws CoreException {

        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, policyCmptTypeName,
                productCmptTypeName);
        IProductCmptTypeAttribute productCmptTypeAttribute = policyCmptType.findProductCmptType(ipsProject)
                .newProductCmptTypeAttribute();
        productCmptTypeAttribute.setName(name);
        productCmptTypeAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productCmptTypeAttribute.setModifier(Modifier.PUBLISHED);
        return productCmptTypeAttribute;
    }

    protected IEnumType createEnumType(String name,
            IEnumType superEnumType,
            String idAttributeName,
            String nameAttributeName) throws CoreException {

        IEnumType enumType = newEnumType(ipsProject, name);
        enumType.setAbstract(false);
        enumType.setContainingValues(true);
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
        enumValue.setEnumAttributeValue(0, idAttributeValue);
        enumValue.setEnumAttributeValue(1, nameAttributeValue);
        enumValue.setEnumAttributeValue(2, literalNameAttributeValue);

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
