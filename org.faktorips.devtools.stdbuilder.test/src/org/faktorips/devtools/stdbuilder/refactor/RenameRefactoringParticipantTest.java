/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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
import org.eclipse.jdt.core.IType;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.ProjectConfigurationUtil;
import org.faktorips.runtime.IValidationContext;

/**
 * Tests the various Faktor-IPS "Rename" refactorings with regard to the generated Java source code.
 * 
 * @author Alexander Weickmann
 */
public class RenameRefactoringParticipantTest extends RefactoringParticipantTest {

    public void testRenamePolicyCmptTypeAttribute() throws CoreException {
        performRenameRefactoring(policyCmptTypeAttribute, "test");

        // The former Java elements must no longer exist.
        assertFalse(policyInterface.getField("PROPERTY_POLICYATTRIBUTE").exists());
        assertFalse(policyInterface.getMethod("getPolicyAttribute", new String[] {}).exists());
        assertFalse(policyInterface.getMethod("setPolicyAttribute", new String[] { "QMoney;" }).exists());
        assertFalse(productGenInterface.getMethod("getDefaultValuePolicyAttribute", new String[] {}).exists());
        assertFalse(productGenInterface.getMethod("getSetOfAllowedValuesForPolicyAttribute",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        assertFalse(policyClass.getField("policyAttribute").exists());
        assertFalse(policyClass.getMethod("getPolicyAttribute", new String[] {}).exists());
        assertFalse(policyClass.getMethod("setPolicyAttribute", new String[] { "QMoney;" }).exists());
        assertFalse(productGenClass.getField("defaultValuePolicyAttribute").exists());
        assertFalse(productGenClass.getField("setOfAllowedValuesPolicyAttribute").exists());
        assertFalse(productGenClass.getMethod("getDefaultValuePolicyAttribute", new String[] {}).exists());
        assertFalse(productGenClass.getMethod("getSetOfAllowedValuesForPolicyAttribute",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Expect new Java elements for published interface.
        assertTrue(policyInterface.getField("PROPERTY_TEST").exists());
        assertTrue(policyInterface.getMethod("getTest", new String[] {}).exists());
        assertTrue(policyInterface.getMethod("setTest", new String[] { "QMoney;" }).exists());
        assertTrue(productGenInterface.getMethod("getDefaultValueTest", new String[] {}).exists());
        assertTrue(productGenInterface.getMethod("getSetOfAllowedValuesForTest",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Expect new Java elements for implementation.
        assertTrue(policyClass.getField("test").exists());
        assertTrue(policyClass.getMethod("getTest", new String[] {}).exists());
        assertTrue(policyClass.getMethod("setTest", new String[] { "QMoney;" }).exists());
        assertTrue(productGenClass.getField("defaultValueTest").exists());
        assertTrue(productGenClass.getField("setOfAllowedValuesTest").exists());
        assertTrue(productGenClass.getMethod("getDefaultValueTest", new String[] {}).exists());
        assertTrue(productGenClass.getMethod("getSetOfAllowedValuesForTest",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());
    }

    public void testRenamePolicyCmptTypeAttributeValueSetEnum() throws CoreException {
        policyCmptTypeAttribute.setValueSetType(ValueSetType.ENUM);

        performFullBuild();

        performRenameRefactoring(policyCmptTypeAttribute, "test");

        // The former Java elements must no longer exist.
        assertFalse(productGenInterface.getMethod("getAllowedValuesForPolicyAttribute",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        assertFalse(productGenClass.getField("allowedValuesForPolicyAttribute").exists());
        assertFalse(productGenClass.getMethod("getAllowedValuesForPolicyAttribute",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Expect new Java elements for published interface.
        assertTrue(productGenInterface.getMethod("getAllowedValuesForTest",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Expect new Java elements for implementation.
        assertTrue(productGenClass.getField("allowedValuesForTest").exists());
        assertTrue(productGenClass.getMethod("getAllowedValuesForTest",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());
    }

    public void testRenamePolicyCmptTypeAttributeValueSetRange() throws CoreException {
        policyCmptTypeAttribute.setValueSetType(ValueSetType.RANGE);

        performFullBuild();

        performRenameRefactoring(policyCmptTypeAttribute, "test");

        // The former Java elements must no longer exist.
        assertFalse(productGenInterface.getMethod("getRangeForPolicyAttribute",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        assertFalse(productGenClass.getField("rangeForPolicyAttribute").exists());
        assertFalse(productGenClass.getMethod("getRangeForPolicyAttribute",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Expect new Java elements for published interface.
        assertTrue(productGenInterface.getMethod("getRangeForTest",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Expect new Java elements for implementation.
        assertTrue(productGenClass.getField("rangeForTest").exists());
        assertTrue(productGenClass.getMethod("getRangeForTest",
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());
    }

    public void testRenameProductCmptTypeAttribute() throws CoreException {
        performRenameRefactoring(productCmptTypeAttribute, "test");

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
        assertTrue(productGenClass.getMethod("getTest", new String[0]).exists());
        assertTrue(productGenClass.getMethod("setTest", new String[] { "QString;" }).exists());
        assertTrue(policyClass.getMethod("getTest", new String[0]).exists());
    }

    public void testRenameEnumAttributeAbstractJava5Enums() throws CoreException {
        ProjectConfigurationUtil.setUpUseJava5Enums(ipsProject, true);
        enumType.setAbstract(true);
        performFullBuild();
        IType enumJavaType = getJavaType("", ENUM_TYPE_NAME, true, false);

        performRenameRefactoring(enumAttribute, "test");

        // The former Java elements must no longer exist.
        assertFalse(enumJavaType.getMethod("getId", new String[0]).exists());

        // Expect the new Java elements.
        assertTrue(enumJavaType.getMethod("getTest", new String[0]).exists());
    }

    public void testRenameEnumAttributeAbstract() throws CoreException {
        ProjectConfigurationUtil.setUpUseJava5Enums(ipsProject, false);
        enumType.setAbstract(true);
        performFullBuild();
        IType enumJavaType = getJavaType("", ENUM_TYPE_NAME, true, false);

        performRenameRefactoring(enumAttribute, "test");

        // The former Java elements must no longer exist.
        assertFalse(enumJavaType.getField("id").exists());
        assertFalse(enumJavaType.getMethod("getId", new String[0]).exists());

        // Expect the new Java elements.
        assertTrue(enumJavaType.getField("test").exists());
        assertTrue(enumJavaType.getMethod("getTest", new String[0]).exists());
    }

    /** Assures that the referring Java elements in the subclass hierarchy are renamed. */
    public void testRenameEnumAttributeHierarchy() throws CoreException {
        ProjectConfigurationUtil.setUpUseJava5Enums(ipsProject, false);
        enumType.setAbstract(true);

        // Create the hierarchy.
        IEnumType midEnumType = newEnumType(ipsProject, "MidEnumType");
        midEnumType.setSuperEnumType(enumType.getQualifiedName());
        midEnumType.setAbstract(true);
        IEnumAttribute midAttribute = midEnumType.newEnumAttribute();
        midAttribute.setName("id");
        midAttribute.setInherited(true);
        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setSuperEnumType(midEnumType.getQualifiedName());
        subEnumType.setContainingValues(true);
        IEnumAttribute subAttribute = subEnumType.newEnumAttribute();
        subAttribute.setName("id");
        subAttribute.setInherited(true);
        subEnumType.newEnumLiteralNameAttribute();

        performFullBuild();
        performRenameRefactoring(enumAttribute, "test");

        IType subJavaType = getJavaType("", "SubEnumType", true, false);

        assertFalse(subJavaType.getMethod("getValueById",
                new String[] { "Q" + Datatype.STRING.getQualifiedName() + ";" }).exists());
        assertFalse(subJavaType.getMethod("isValueById",
                new String[] { "Q" + Datatype.STRING.getQualifiedName() + ";" }).exists());

        assertTrue(subJavaType.getMethod("getValueByTest",
                new String[] { "Q" + Datatype.STRING.getQualifiedName() + ";" }).exists());
        assertTrue(subJavaType.getMethod("isValueByTest",
                new String[] { "Q" + Datatype.STRING.getQualifiedName() + ";" }).exists());
    }

    public void testRenamePolicyCmptType() throws CoreException {
        performRenameRefactoring(policyCmptType, "RenamedPolicy");

        checkJavaSourceFilesPolicyCmptType(PACKAGE_NAME, "RenamedPolicy");

        assertFalse(productClass.getMethod("createPolicy", new String[] {}).exists());
        assertFalse(productInterface.getMethod("createPolicy", new String[] {}).exists());
        assertTrue(productClass.getMethod("createRenamedPolicy", new String[] {}).exists());
        assertTrue(productInterface.getMethod("createRenamedPolicy", new String[] {}).exists());
    }

    public void testRenameProductCmptType() throws CoreException {
        performRenameRefactoring(productCmptType, "RenamedProduct");

        checkJavaSourceFilesProductCmptType(PACKAGE_NAME, "RenamedProduct");

        assertFalse(policyClass.getMethod("getProduct", new String[0]).exists());
        assertFalse(policyClass.getMethod("getProductGen", new String[0]).exists());
        assertFalse(policyClass.getMethod("setProduct", new String[] { "IProduct", "Z" }).exists());
        assertTrue(policyClass.getMethod("getRenamedProduct", new String[0]).exists());
        assertTrue(policyClass.getMethod("getRenamedProductGen", new String[0]).exists());
        assertTrue(policyClass.getMethod("setRenamedProduct", new String[] { "QIRenamedProduct;", "Z" }).exists());
    }

    public void testRenameEnumLiteralNameAttributeValueJava5Enums() throws CoreException {
        ProjectConfigurationUtil.setUpUseJava5Enums(ipsProject, true);
        performTestRenameEnumLiteralNameAttributeValue();
    }

    public void testRenameEnumLiteralNameAttributeValue() throws CoreException {
        ProjectConfigurationUtil.setUpUseJava5Enums(ipsProject, false);
        performTestRenameEnumLiteralNameAttributeValue();
    }

    private void performTestRenameEnumLiteralNameAttributeValue() throws CoreException {
        performFullBuild();

        performRenameRefactoring(enumLiteralNameAttributeValue, "bar");

        IType javaType = getJavaType("", "ValuedEnumType", true, false);
        assertFalse(javaType.getField("FOO").exists());
        assertFalse(javaType.getField("bar").exists());
        assertTrue(javaType.getField("BAR").exists());
    }

    public void testRenameEnumType() throws CoreException {
        performRenameRefactoring(enumType, "RenamedEnumType");

        checkJavaSourceFilesEnumType("", "RenamedEnumType");
    }

    public void testRenameTableStructure() throws CoreException {
        performRenameRefactoring(tableStructure, "RenamedTableStructure");

        checkJavaSourceFilesTableStructure("", "RenamedTableStructure");
    }

    public void testRenameTestCaseType() throws CoreException {
        performRenameRefactoring(testCaseType, "RenamedTestCaseType");

        checkJavaSourceFilesTestCaseType("", "RenamedTestCaseType");
    }

    public void testRenameBusinessFunction() throws CoreException {
        performRenameRefactoring(businessFunction, "RenamedBusinessFunction");

        checkJavaSourceFilesBusinessFunction("", "RenamedBusinessFunction");
    }

    public void testRenameOnlyLetterCaseChanged() throws CoreException {
        performRenameRefactoring(policyCmptType, "policyCmptType");

        policyInterface = getJavaType(PACKAGE_NAME, "I" + POLICY_CMPT_TYPE_NAME, true, false);
        policyClass = getJavaType(PACKAGE_NAME, POLICY_CMPT_TYPE_NAME, false, false);

        assertTrue(policyInterface.exists());
        assertTrue(policyClass.exists());
    }

}
