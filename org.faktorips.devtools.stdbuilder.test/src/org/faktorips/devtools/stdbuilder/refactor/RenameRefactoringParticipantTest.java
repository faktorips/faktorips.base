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
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.faktorips.devtools.core.AbstractIpsRefactoringTest;
import org.faktorips.runtime.IValidationContext;

/**
 * Tests the various Faktor-IPS rename refactorings with regard to the generated Java source code.
 * 
 * @author Alexander Weickmann
 */
public class RenameRefactoringParticipantTest extends AbstractIpsRefactoringTest {

    private IFolder modelFolder;

    private IFolder internalFolder;

    private IType policyClass;

    private IType policyInterface;

    private IType productClass;

    private IType productInterface;

    private IType productGenClass;

    private IType productGenInterface;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Initialize folders and Java elements.
        modelFolder = ipsProject.getProject().getFolder(Path.fromOSString("src/org/faktorips/sample/model"));
        internalFolder = modelFolder.getFolder("internal");
        policyInterface = getJavaType(POLICY_NAME, false);
        policyClass = getJavaType(POLICY_NAME, true);
        productInterface = getJavaType(PRODUCT_NAME, false);
        productClass = getJavaType(PRODUCT_NAME, true);
        productGenInterface = getJavaType(PRODUCT_NAME + "Gen", false);
        productGenClass = getJavaType(PRODUCT_NAME + "Gen", true);
    }

    public void testRenamePolicyCmptTypeAttribute() throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

        // Expect Java elements for published interface.
        assertTrue(policyInterface.getField("PROPERTY_POLICYATTRIBUTE").exists());
        assertTrue(policyInterface.getMethod("getPolicyAttribute", new String[] {}).exists());
        assertTrue(policyInterface.getMethod("setPolicyAttribute", new String[] { "QString;" }).exists());
        assertTrue(productGenInterface.getMethod("getDefaultValuePolicyAttribute", new String[] {}).exists());
        assertTrue(productGenInterface.getMethod("getSetOfAllowedValuesForPolicyAttribute",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Expect Java elements for implementation.
        assertTrue(policyClass.getField("policyAttribute").exists());
        assertTrue(policyClass.getMethod("getPolicyAttribute", new String[] {}).exists());
        assertTrue(policyClass.getMethod("setPolicyAttribute", new String[] { "QString;" }).exists());
        assertTrue(productGenClass.getField("defaultValuePolicyAttribute").exists());
        assertTrue(productGenClass.getField("setOfAllowedValuesPolicyAttribute").exists());
        assertTrue(productGenClass.getMethod("getDefaultValuePolicyAttribute", new String[] {}).exists());
        assertTrue(productGenClass.getMethod("getSetOfAllowedValuesForPolicyAttribute",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Refactor the attribute.
        runRenameRefactoring(policyCmptTypeAttribute, "test");

        // The former Java elements must no longer exist.
        assertFalse(policyInterface.getField("PROPERTY_POLICYATTRIBUTE").exists());
        assertFalse(policyInterface.getMethod("getPolicyAttribute", new String[] {}).exists());
        assertFalse(policyInterface.getMethod("setPolicyAttribute", new String[] { "QString;" }).exists());
        assertFalse(productGenInterface.getMethod("getDefaultValuePolicyAttribute", new String[] {}).exists());
        assertFalse(productGenInterface.getMethod("getSetOfAllowedValuesForPolicyAttribute",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        assertFalse(policyClass.getField("policyAttribute").exists());
        assertFalse(policyClass.getMethod("getPolicyAttribute", new String[] {}).exists());
        assertFalse(policyClass.getMethod("setPolicyAttribute", new String[] { "QString;" }).exists());
        assertFalse(productGenClass.getField("defaultValuePolicyAttribute").exists());
        assertFalse(productGenClass.getField("setOfAllowedValuesPolicyAttribute").exists());
        assertFalse(productGenClass.getMethod("getDefaultValuePolicyAttribute", new String[] {}).exists());
        assertFalse(productGenClass.getMethod("getSetOfAllowedValuesForPolicyAttribute",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Expect new Java elements for published interface.
        assertTrue(policyInterface.getField("PROPERTY_TEST").exists());
        assertTrue(policyInterface.getMethod("getTest", new String[] {}).exists());
        assertTrue(policyInterface.getMethod("setTest", new String[] { "QString;" }).exists());
        assertTrue(productGenInterface.getMethod("getDefaultValueTest", new String[] {}).exists());
        assertTrue(productGenInterface.getMethod("getSetOfAllowedValuesForTest",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Expect new Java elements for implementation.
        assertTrue(policyClass.getField("test").exists());
        assertTrue(policyClass.getMethod("getTest", new String[] {}).exists());
        assertTrue(policyClass.getMethod("setTest", new String[] { "QString;" }).exists());
        assertTrue(productGenClass.getField("defaultValueTest").exists());
        assertTrue(productGenClass.getField("setOfAllowedValuesTest").exists());
        assertTrue(productGenClass.getMethod("getDefaultValueTest", new String[] {}).exists());
        assertTrue(productGenClass.getMethod("getSetOfAllowedValuesForTest",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());
    }

    public void testRenameProductCmptTypeAttribute() throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

        // Expect Java elements for published interface.
        assertTrue(productGenInterface.getMethod("getProductAttribute", new String[] {}).exists());

        // Expect Java elements for implementation.
        assertTrue(productGenClass.getField("productAttribute").exists());
        assertTrue(productGenClass.getMethod("getProductAttribute", new String[] {}).exists());
        assertTrue(productGenClass.getMethod("setProductAttribute", new String[] { "QString;" }).exists());
        assertTrue(policyClass.getMethod("getProductAttribute", new String[] {}).exists());

        // Refactor the attribute.
        runRenameRefactoring(productCmptTypeAttribute, "test");

        // The former Java elements must no longer exist.
        assertFalse(productGenInterface.getMethod("getProductAttribute", new String[] {}).exists());

        assertFalse(productGenClass.getField("productAttribute").exists());
        assertFalse(productGenClass.getMethod("getProductAttribute", new String[] {}).exists());
        assertFalse(productGenClass.getMethod("setProductAttribute", new String[] { "QString;" }).exists());
        assertFalse(policyClass.getMethod("getProductAttribute", new String[] {}).exists());

        // Expect new Java elements for published interface.
        assertTrue(productGenInterface.getMethod("getTest", new String[] {}).exists());

        // Expect new Java elements for implementation.
        assertTrue(productGenClass.getField("test").exists());
        assertTrue(productGenClass.getMethod("getTest", new String[] {}).exists());
        assertTrue(productGenClass.getMethod("setTest", new String[] { "QString;" }).exists());
        assertTrue(policyClass.getMethod("getTest", new String[] {}).exists());
    }

    public void testRenamePolicyCmptType() throws CoreException {
        ipsProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);

        assertTrue(productClass.getMethod("createPolicy", new String[] {}).exists());
        assertTrue(productInterface.getMethod("createPolicy", new String[] {}).exists());

        // Refactor the policy component type.
        runRenameRefactoring(policyCmptType, "RenamedPolicy");
        assertFalse(getJavaType("Policy", false).exists());
        assertFalse(getJavaType("Policy", true).exists());
        assertTrue(getJavaType("RenamedPolicy", false).exists());
        assertTrue(getJavaType("RenamedPolicy", true).exists());

        assertFalse(productClass.getMethod("createPolicy", new String[] {}).exists());
        assertFalse(productInterface.getMethod("createPolicy", new String[] {}).exists());
        assertTrue(productClass.getMethod("createRenamedPolicy", new String[] {}).exists());
        assertTrue(productInterface.getMethod("createRenamedPolicy", new String[] {}).exists());
    }

    public void testRenameProductCmptType() throws CoreException {
        // TODO AW: Implement test.
    }

    private IType getJavaType(String typeName, boolean internal) {
        IFolder folder = internal ? internalFolder : modelFolder;
        String interfaceSeparator = internal ? "" : "I";
        return ((ICompilationUnit)JavaCore.create(folder.getFile(interfaceSeparator + typeName + ".java")))
                .getType(interfaceSeparator + typeName);
    }

}
