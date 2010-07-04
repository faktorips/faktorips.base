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

package org.faktorips.devtools.stdbuilder.refactor;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.faktorips.abstracttest.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfigModel;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * Provides basic functionality for the refactoring participant tests of the standard builder.
 * 
 * @author Alexander Weickmann
 */
/**
 * 
 * @author Alexander Weickmann
 */
/**
 * 
 * @author Alexander Weickmann
 */
public abstract class RefactoringParticipantTest extends AbstractIpsRefactoringTest {

    protected IFolder modelFolder;

    protected IFolder internalFolder;

    protected IType policyClass;

    protected IType policyInterface;

    protected IType productClass;

    protected IType productInterface;

    protected IType productGenClass;

    protected IType productGenInterface;

    protected IType enumTypeJavaType;

    protected IType enumTypeXmlAdapterClass;

    protected IType tableStructureClass;

    protected IType tableStructureRowClass;

    protected IType testCaseClass;

    protected IType businessFunctionClass;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setUpFolders();
        setUpPolicyAndProductTypes();
        setUpTableTypes();
        setUpTestTypes();
        setUpBusinessFunctionTypes();

        // Configure the builder set to generate JAXB support.
        IIpsProjectProperties ipsProjectProperties = ipsProject.getProperties();
        IIpsArtefactBuilderSetConfigModel configModel = ipsProjectProperties.getBuilderSetConfig();
        configModel.setPropertyValue(StandardBuilderSet.CONFIG_PROPERTY_GENERATE_JAXB_SUPPORT, "true", null); //$NON-NLS-1$
        ipsProjectProperties.setBuilderSetConfig(configModel);
        ipsProject.setProperties(ipsProjectProperties);

        /*
         * A full build needs to be done so that Java source is generated out of the Faktor-IPS
         * model. This Java source will be refactored by the tests.
         */
        performFullBuild();
    }

    private void setUpFolders() {
        modelFolder = ipsProject.getProject().getFolder(Path.fromOSString("src/org/faktorips/sample/model")); //$NON-NLS-1$
        internalFolder = modelFolder.getFolder("internal"); //$NON-NLS-1$
    }

    private void setUpPolicyAndProductTypes() throws CoreException {
        policyInterface = getJavaType(PACKAGE_NAME, "I" + POLICY_CMPT_TYPE_NAME, true, false); //$NON-NLS-1$
        policyClass = getJavaType(PACKAGE_NAME, POLICY_CMPT_TYPE_NAME, false, false);
        productInterface = getJavaType(PACKAGE_NAME, "I" + PRODUCT_CMPT_TYPE_NAME, true, false); //$NON-NLS-1$
        productClass = getJavaType(PACKAGE_NAME, PRODUCT_CMPT_TYPE_NAME, false, false);
        productGenInterface = getJavaType(PACKAGE_NAME, "I" + PRODUCT_CMPT_TYPE_NAME + "Gen", true, false); //$NON-NLS-1$ //$NON-NLS-2$
        productGenClass = getJavaType(PACKAGE_NAME, PRODUCT_CMPT_TYPE_NAME + "Gen", false, false); //$NON-NLS-1$
        enumTypeJavaType = getJavaType(PACKAGE_NAME, ENUM_TYPE_NAME, true, false);
        enumTypeXmlAdapterClass = getJavaType(PACKAGE_NAME, ENUM_TYPE_NAME + "XmlAdapter", false, true);
    }

    private void setUpTableTypes() throws CoreException {
        tableStructureClass = getJavaType(PACKAGE_NAME, TABLE_STRUCTURE_NAME, false, false);
        tableStructureRowClass = getJavaType(PACKAGE_NAME, TABLE_STRUCTURE_NAME + "Row", false, false);
    }

    private void setUpTestTypes() throws CoreException {
        testCaseClass = getJavaType(PACKAGE_NAME, TEST_CASE_TYPE_NAME, false, false);
    }

    private void setUpBusinessFunctionTypes() throws CoreException {
        businessFunctionClass = getJavaType(PACKAGE_NAME, BUSINESS_FUNCTION_NAME, true, false);
    }

    protected void checkJavaSourceFilesPolicyCmptType(String targetPackageName, String newName) throws CoreException {
        assertFalse(policyInterface.exists());
        assertFalse(policyClass.exists());

        policyInterface = getJavaType(targetPackageName, "I" + newName, true, false);
        policyClass = getJavaType(targetPackageName, newName, false, false);

        assertTrue(policyInterface.exists());
        assertTrue(policyClass.exists());
    }

    protected void checkJavaSourceFilesProductCmptType(String targetPackageName, String newName) throws CoreException {
        assertFalse(productClass.exists());
        assertFalse(productInterface.exists());
        assertFalse(productGenClass.exists());
        assertFalse(productGenInterface.exists());

        productClass = getJavaType(targetPackageName, newName, false, false);
        productInterface = getJavaType(targetPackageName, "I" + newName, true, false);
        productGenClass = getJavaType(targetPackageName, newName + "Gen", false, false);
        productGenInterface = getJavaType(targetPackageName, "I" + newName + "Gen", true, false);

        assertTrue(productClass.exists());
        assertTrue(productInterface.exists());
        assertTrue(productGenClass.exists());
        assertTrue(productGenInterface.exists());
    }

    protected void checkJavaSourceFilesEnumType(String targetPackageName, String newName) throws CoreException {
        assertFalse(enumTypeJavaType.exists());
        assertFalse(enumTypeXmlAdapterClass.exists());

        enumTypeJavaType = getJavaType(targetPackageName, newName, true, false);
        enumTypeXmlAdapterClass = getJavaType(targetPackageName, newName + "XmlAdapter", false, true);

        assertTrue(enumTypeJavaType.exists());
        assertTrue(enumTypeXmlAdapterClass.exists());
    }

    protected void checkJavaSourceFilesTableStructure(String targetPackageName, String newName) throws CoreException {
        assertFalse(tableStructureClass.exists());
        assertFalse(tableStructureRowClass.exists());

        tableStructureClass = getJavaType(targetPackageName, newName, false, false);
        tableStructureRowClass = getJavaType(targetPackageName, newName, false, false);

        assertTrue(tableStructureClass.exists());
        assertTrue(tableStructureRowClass.exists());
    }

    protected void checkJavaSourceFilesTestCaseType(String targetPackageName, String newName) throws CoreException {
        assertFalse(testCaseClass.exists());

        testCaseClass = getJavaType(targetPackageName, newName, false, false);

        assertTrue(testCaseClass.exists());
    }

    protected void checkJavaSourceFilesBusinessFunction(String targetPackageName, String newName) throws CoreException {
        assertFalse(businessFunctionClass.exists());

        businessFunctionClass = getJavaType(targetPackageName, newName, true, false);

        assertTrue(businessFunctionClass.exists());
    }

    /**
     * Returns the Java <tt>IType</tt> corresponding to the indicated package name, type name and
     * internal flag.
     * 
     * @param packageName The package where the <tt>IType</tt> is located.
     * @param typeName The name of the <tt>IType</tt>.
     * @param publishedSource Flag indicating whether a published interface or an implementation
     *            type is searched.
     * @param derivedSource Flag indicating whether the Java source file is a derived resource or
     *            not.
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

}
