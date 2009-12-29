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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.refactor.LocationDescriptor;
import org.faktorips.runtime.IValidationContext;

/**
 * Tests the various Faktor-IPS "Rename" refactorings with regard to the generated Java source code.
 * 
 * @author Alexander Weickmann
 */
public class RenameRefactoringParticipantTest extends RefactoringParticipantTest {

    public void testRenamePolicyCmptTypeAttribute() throws CoreException {
        performFullBuild();

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

        // Rename the attribute.
        performRenameRefactoring(policyCmptTypeAttribute, new LocationDescriptor(
                policyCmptType.getIpsPackageFragment(), "test"));

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
        performFullBuild();

        // Expect Java elements for published interface.
        assertTrue(productGenInterface.getMethod("getProductAttribute", new String[] {}).exists());

        // Expect Java elements for implementation.
        assertTrue(productGenClass.getField("productAttribute").exists());
        assertTrue(productGenClass.getMethod("getProductAttribute", new String[] {}).exists());
        assertTrue(productGenClass.getMethod("setProductAttribute", new String[] { "QString;" }).exists());
        assertTrue(policyClass.getMethod("getProductAttribute", new String[] {}).exists());

        // Rename the attribute.
        performRenameRefactoring(productCmptTypeAttribute, new LocationDescriptor(productCmptType
                .getIpsPackageFragment(), "test"));

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
        performFullBuild();

        performRenameRefactoring(policyCmptType, new LocationDescriptor(policyCmptType.getIpsPackageFragment(),
                "RenamedPolicy"));
        assertFalse(getJavaType(PACKAGE, POLICY_NAME, false).exists());
        assertFalse(getJavaType(PACKAGE, POLICY_NAME, true).exists());
        assertTrue(getJavaType(PACKAGE, "RenamedPolicy", false).exists());
        assertTrue(getJavaType(PACKAGE, "RenamedPolicy", true).exists());

        assertFalse(productClass.getMethod("createPolicy", new String[] {}).exists());
        assertFalse(productInterface.getMethod("createPolicy", new String[] {}).exists());
        assertTrue(productClass.getMethod("createRenamedPolicy", new String[] {}).exists());
        assertTrue(productInterface.getMethod("createRenamedPolicy", new String[] {}).exists());
    }

    public void testRenameProductCmptType() throws CoreException {
        performFullBuild();

        performRenameRefactoring(productCmptType, new LocationDescriptor(productCmptType.getIpsPackageFragment(),
                "RenamedProduct"));
        assertFalse(getJavaType(PACKAGE, PRODUCT_NAME, false).exists());
        assertFalse(getJavaType(PACKAGE, PRODUCT_NAME, true).exists());
        assertFalse(getJavaType(PACKAGE, PRODUCT_NAME + "Gen", false).exists());
        assertFalse(getJavaType(PACKAGE, PRODUCT_NAME + "Gen", true).exists());
        assertTrue(getJavaType(PACKAGE, "RenamedProduct", false).exists());
        assertTrue(getJavaType(PACKAGE, "RenamedProduct", true).exists());
        assertTrue(getJavaType(PACKAGE, "RenamedProductGen", false).exists());
        assertTrue(getJavaType(PACKAGE, "RenamedProductGen", true).exists());

        assertFalse(policyClass.getMethod("getProduct", new String[0]).exists());
        assertFalse(policyClass.getMethod("getProductGen", new String[0]).exists());
        assertFalse(policyClass.getMethod("setProduct", new String[] { "IProduct", "Z" }).exists());
        assertTrue(policyClass.getMethod("getRenamedProduct", new String[0]).exists());
        assertTrue(policyClass.getMethod("getRenamedProductGen", new String[0]).exists());
        assertTrue(policyClass.getMethod("setRenamedProduct", new String[] { "QIRenamedProduct;", "Z" }).exists());
    }

}
