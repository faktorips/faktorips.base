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

package org.faktorips.devtools.stdbuilder.refactor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.stdbuilder.ProjectConfigurationUtil;
import org.faktorips.runtime.IValidationContext;
import org.faktorips.util.StringUtil;
import org.junit.Test;

/**
 * Tests the various Faktor-IPS "Rename" refactorings with regard to the generated Java source code.
 * 
 * @author Alexander Weickmann
 */
public class RenameRefactoringParticipantTest extends RefactoringParticipantTest {

    @Test
    public void testRenamePolicyCmptTypeAttribute() throws CoreException {
        performFullBuild();

        final String newName = "test";
        performRenameRefactoring(policyCmptTypeAttribute, newName);

        expectationsForRenamePolicyCmptTypeAttribute(policyInterface, policyClass, productGenInterface,
                productGenClass, POLICY_CMPT_TYPE_ATTRIBUTE_NAME, newName, "QMoney;");
    }

    private void expectationsForRenamePolicyCmptTypeAttribute(IType javaInterface,
            IType javaClass,
            IType javaProductGenInterface,
            IType javaProductGenClass,
            String oldName,
            String newName,
            String dataTypeSignature) {

        String oldNameCamelCase = StringUtil.toCamelCase(oldName, true);
        String newNameCamelCase = StringUtil.toCamelCase(newName, true);

        // The former Java elements must no longer exist.
        assertFalse(javaInterface.getField("PROPERTY_" + oldName.toUpperCase()).exists());
        assertFalse(javaInterface.getMethod("get" + oldNameCamelCase, new String[] {}).exists());
        assertFalse(javaInterface.getMethod("set" + oldNameCamelCase, new String[] { dataTypeSignature }).exists());
        assertFalse(javaProductGenInterface.getMethod("getDefaultValue" + oldNameCamelCase, new String[] {}).exists());
        assertFalse(javaProductGenInterface.getMethod("getSetOfAllowedValuesFor" + oldNameCamelCase,
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        assertFalse(javaClass.getField(oldName).exists());
        assertFalse(javaClass.getMethod("get" + oldNameCamelCase, new String[] {}).exists());
        assertFalse(javaClass.getMethod("set" + oldNameCamelCase, new String[] { dataTypeSignature }).exists());
        assertFalse(javaProductGenClass.getField("defaultValue" + oldNameCamelCase).exists());
        assertFalse(javaProductGenClass.getField("setOfAllowedValues" + oldNameCamelCase).exists());
        assertFalse(javaProductGenClass.getMethod("getDefaultValue" + oldNameCamelCase, new String[] {}).exists());
        assertFalse(javaProductGenClass.getMethod("getSetOfAllowedValuesFor" + oldNameCamelCase,
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Expect new Java elements for published interface.
        assertTrue(javaInterface.getField("PROPERTY_" + newName.toUpperCase()).exists());
        assertTrue(javaInterface.getMethod("get" + newNameCamelCase, new String[] {}).exists());
        assertTrue(javaInterface.getMethod("set" + newNameCamelCase, new String[] { dataTypeSignature }).exists());
        assertTrue(javaProductGenInterface.getMethod("getDefaultValue" + newNameCamelCase, new String[] {}).exists());
        assertTrue(javaProductGenInterface.getMethod("getSetOfAllowedValuesFor" + newNameCamelCase,
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());

        // Expect new Java elements for implementation.
        assertTrue(javaClass.getField(newName).exists());
        assertTrue(javaClass.getMethod("get" + newNameCamelCase, new String[] {}).exists());
        assertTrue(javaClass.getMethod("set" + newNameCamelCase, new String[] { dataTypeSignature }).exists());
        assertTrue(javaProductGenClass.getField("defaultValue" + newNameCamelCase).exists());
        assertTrue(javaProductGenClass.getField("setOfAllowedValues" + newNameCamelCase).exists());
        assertTrue(javaProductGenClass.getMethod("getDefaultValue" + newNameCamelCase, new String[] {}).exists());
        assertTrue(javaProductGenClass.getMethod("getSetOfAllowedValuesFor" + newNameCamelCase,
                new String[] { "Q" + IValidationContext.class.getSimpleName() + ";" }).exists());
    }

    @Test
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

    @Test
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

    /**
     * Tests that when renaming an overwritten policy component type attribute the Java elements
     * generated by the original attribute are renamed.
     */
    @Test
    public void testRenameOverwrittenPolicyCmptTypeAttribute() throws CoreException {
        IPolicyCmptTypeAttribute superAttribute = superPolicyCmptType.newPolicyCmptTypeAttribute();
        superAttribute.setName(policyCmptTypeAttribute.getName());
        superAttribute.setDatatype(policyCmptTypeAttribute.getDatatype());
        superAttribute.setModifier(policyCmptTypeAttribute.getModifier());
        superAttribute.setAttributeType(policyCmptTypeAttribute.getAttributeType());
        superAttribute.setProductRelevant(policyCmptTypeAttribute.isProductRelevant());
        policyCmptTypeAttribute.setOverwrite(true);

        performFullBuild();

        final String newName = "test";
        performRenameRefactoring(policyCmptTypeAttribute, newName);

        expectationsForRenamePolicyCmptTypeAttribute(superPolicyInterface, superPolicyClass, superProductGenInterface,
                superProductGenClass, POLICY_CMPT_TYPE_ATTRIBUTE_NAME, newName, "QMoney;");
    }

    @Test
    public void testRenameProductCmptTypeAttribute() throws CoreException {
        performFullBuild();

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

    @Test
    public void testRenameEnumAttributeAbstractJava5Enums() throws CoreException {
        ProjectConfigurationUtil.setUpUseJava5Enums(ipsProject, true);
        enumType.setAbstract(true);

        performFullBuild();

        performRenameRefactoring(enumAttribute, "test");

        IType enumJavaType = getJavaType("", ENUM_TYPE_NAME, true, false);

        // The former Java elements must no longer exist.
        assertFalse(enumJavaType.getMethod("getId", new String[0]).exists());

        // Expect the new Java elements.
        assertTrue(enumJavaType.getMethod("getTest", new String[0]).exists());
    }

    @Test
    public void testRenameEnumAttributeAbstract() throws CoreException {
        ProjectConfigurationUtil.setUpUseJava5Enums(ipsProject, false);
        enumType.setAbstract(true);

        performFullBuild();

        performRenameRefactoring(enumAttribute, "test");

        IType enumJavaType = getJavaType("", ENUM_TYPE_NAME, true, false);

        // The former Java elements must no longer exist.
        assertFalse(enumJavaType.getField("id").exists());
        assertFalse(enumJavaType.getMethod("getId", new String[0]).exists());

        // Expect the new Java elements.
        assertTrue(enumJavaType.getField("test").exists());
        assertTrue(enumJavaType.getMethod("getTest", new String[0]).exists());
    }

    /** Assures that the referring Java elements in the subclass hierarchy are renamed. */
    @Test
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

    @Test
    public void testRenamePolicyCmptType() throws CoreException {
        performFullBuild();

        performRenameRefactoring(policyCmptType, "RenamedPolicy");

        checkJavaSourceFilesPolicyCmptType(PACKAGE_NAME, "RenamedPolicy");

        assertFalse(productClass.getMethod("createPolicy", new String[] {}).exists());
        assertFalse(productInterface.getMethod("createPolicy", new String[] {}).exists());
        assertTrue(productClass.getMethod("createRenamedPolicy", new String[] {}).exists());
        assertTrue(productInterface.getMethod("createRenamedPolicy", new String[] {}).exists());
    }

    @Test
    public void testRenameProductCmptType() throws CoreException {
        performFullBuild();

        performRenameRefactoring(productCmptType, "RenamedProduct");

        checkJavaSourceFilesProductCmptType(PACKAGE_NAME, "RenamedProduct");

        assertFalse(policyClass.getMethod("getProduct", new String[0]).exists());
        assertFalse(policyClass.getMethod("getProductGen", new String[0]).exists());
        assertFalse(policyClass.getMethod("setProduct", new String[] { "IProduct", "Z" }).exists());
        assertTrue(policyClass.getMethod("getRenamedProduct", new String[0]).exists());
        assertTrue(policyClass.getMethod("getRenamedProductGen", new String[0]).exists());
        assertTrue(policyClass.getMethod("setRenamedProduct", new String[] { "QIRenamedProduct;", "Z" }).exists());
    }

    @Test
    public void testRenameEnumLiteralNameAttributeValueJava5Enums() throws CoreException {
        ProjectConfigurationUtil.setUpUseJava5Enums(ipsProject, true);
        performTestRenameEnumLiteralNameAttributeValue();
    }

    @Test
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

    @Test
    public void testRenameEnumType() throws CoreException {
        performFullBuild();

        performRenameRefactoring(enumType, "RenamedEnumType");

        checkJavaSourceFilesEnumType("", "RenamedEnumType");
    }

    @Test
    public void testRenameTableStructure() throws CoreException {
        performFullBuild();

        performRenameRefactoring(tableStructure, "RenamedTableStructure");

        checkJavaSourceFilesTableStructure("", "RenamedTableStructure");
    }

    @Test
    public void testRenameTestCaseType() throws CoreException {
        performFullBuild();

        performRenameRefactoring(testCaseType, "RenamedTestCaseType");

        checkJavaSourceFilesTestCaseType("", "RenamedTestCaseType");
    }

    @Test
    public void testRenameBusinessFunction() throws CoreException {
        performFullBuild();

        performRenameRefactoring(businessFunction, "RenamedBusinessFunction");

        checkJavaSourceFilesBusinessFunction("", "RenamedBusinessFunction");
    }

    @Test
    public void testRenameOnlyLetterCaseChanged() throws CoreException {
        performFullBuild();

        performRenameRefactoring(policyCmptType, "policyCmptType");

        policyInterface = getJavaType(PACKAGE_NAME, "I" + POLICY_CMPT_TYPE_NAME, true, false);
        policyClass = getJavaType(PACKAGE_NAME, POLICY_CMPT_TYPE_NAME, false, false);

        assertTrue(policyInterface.exists());
        assertTrue(policyClass.exists());
    }

}
