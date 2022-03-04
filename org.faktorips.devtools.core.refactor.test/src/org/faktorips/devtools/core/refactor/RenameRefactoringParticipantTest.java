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

import static org.faktorips.abstracttest.matcher.IpsElementNamesMatcher.containsInOrder;
import static org.faktorips.devtools.core.refactor.RefactoringTestUtil.getGenerationConceptNameAbbreviation;
import static org.faktorips.devtools.core.refactor.RefactoringTestUtil.getJavaType;
import static org.faktorips.devtools.core.refactor.RefactoringTestUtil.getPublishedInterfaceName;
import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.booleanParam;
import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.intParam;
import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.stringParam;
import static org.faktorips.devtools.stdbuilder.StdBuilderHelper.unresolvedParam;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.enums.IEnumLiteralNameAttributeValue;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment;
import org.faktorips.devtools.model.internal.ipsproject.IpsPackageFragment.DefinedOrderComparator;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.faktorips.util.StringUtil;
import org.junit.Test;

/**
 * Tests the various Faktor-IPS "Rename" refactorings with regard to the generated Java source code.
 * 
 * @author Alexander Weickmann
 */
public class RenameRefactoringParticipantTest extends RefactoringParticipantTest {

    @Test
    public void testRenamePolicyCmptTypeAttributeValueSetUnrestricted() throws CoreException {
        performTestRenamePolicyCmptTypeAttribute(ValueSetType.UNRESTRICTED);
    }

    @Test
    public void testRenamePolicyCmptTypeAttributeValueSetEnum() throws CoreException {
        performTestRenamePolicyCmptTypeAttribute(ValueSetType.ENUM);
    }

    @Test
    public void testRenamePolicyCmptTypeAttributeValueSetRange() throws CoreException {
        performTestRenamePolicyCmptTypeAttribute(ValueSetType.RANGE);
    }

    private void performTestRenamePolicyCmptTypeAttribute(ValueSetType valueSetType) throws CoreException {
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = createPolicyCmptTypeAttribute("policyAttribute", "Policy",
                "Product");
        policyCmptTypeAttribute.setValueSetType(valueSetType);

        IPolicyCmptType policyCmptType = policyCmptTypeAttribute.getPolicyCmptType();
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);
        saveIpsSrcFile(policyCmptType);
        saveIpsSrcFile(productCmptType);
        performFullBuild(ipsProject);

        performRenameRefactoring(policyCmptTypeAttribute, "test");

        PolicyCmptTypeAttributeExpectations expectations = new PolicyCmptTypeAttributeExpectations(
                policyCmptTypeAttribute, policyCmptType, productCmptType);
        expectations.check("policyAttribute", "test", intParam());
    }

    /**
     * Tests that the Java elements generated by the original attribute are renamed when renaming an
     * {@link IPolicyCmptTypeAttribute} that overwrites another attribute of the super type
     * hierarchy.
     */
    @Test
    public void testRenameOverwritingPolicyCmptTypeAttribute() throws CoreException {
        IPolicyCmptType superPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SuperPolicy", "SuperProduct");
        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        policyCmptType.setSupertype(superPolicyCmptType.getQualifiedName());

        IPolicyCmptTypeAttribute superPolicyCmptTypeAttribute = createPolicyCmptTypeAttribute("policyAttribute",
                superPolicyCmptType);
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = createPolicyCmptTypeAttribute(
                superPolicyCmptTypeAttribute.getName(), policyCmptType);
        policyCmptTypeAttribute.setOverwrite(true);

        saveIpsSrcFile(superPolicyCmptType);
        saveIpsSrcFile(superPolicyCmptType.findProductCmptType(ipsProject));
        saveIpsSrcFile(policyCmptType);
        saveIpsSrcFile(policyCmptType.findProductCmptType(ipsProject));
        performFullBuild(ipsProject);

        performRenameRefactoring(policyCmptTypeAttribute, "test");

        IProductCmptType superProductCmptType = superPolicyCmptType.findProductCmptType(ipsProject);
        PolicyCmptTypeAttributeExpectations expectations = new PolicyCmptTypeAttributeExpectations(
                policyCmptTypeAttribute, superPolicyCmptType, superProductCmptType);
        expectations.check("policyAttribute", "test", intParam());
    }

    /**
     * Tests that the Java elements generated by the original attribute are renamed when renaming an
     * {@link IPolicyCmptTypeAttribute} that overwrites another attribute of the super type
     * hierarchy which also overwrites an other attribute (transitive).
     */
    @Test
    public void testRenameOverwritingPolicyCmptTypeAttributeTransitive() throws CoreException {
        IPolicyCmptType superSuperPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SuperSuperPolicy",
                "SuperSuperProduct");
        IPolicyCmptType superPolicyCmptType = newPolicyAndProductCmptType(ipsProject, "SuperPolicy", "SuperProduct");
        superPolicyCmptType.setSupertype(superSuperPolicyCmptType.getQualifiedName());
        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        policyCmptType.setSupertype(superPolicyCmptType.getQualifiedName());

        IPolicyCmptTypeAttribute superSuperPolicyCmptTypeAttribute = createPolicyCmptTypeAttribute("policyAttribute",
                superSuperPolicyCmptType);
        IPolicyCmptTypeAttribute superPolicyCmptTypeAttribute = createPolicyCmptTypeAttribute(
                superSuperPolicyCmptTypeAttribute.getName(), superPolicyCmptType);
        superPolicyCmptTypeAttribute.setOverwrite(true);
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = createPolicyCmptTypeAttribute(
                superPolicyCmptTypeAttribute.getName(), policyCmptType);
        policyCmptTypeAttribute.setOverwrite(true);

        saveIpsSrcFile(superSuperPolicyCmptType);
        saveIpsSrcFile(superSuperPolicyCmptType.findProductCmptType(ipsProject));
        saveIpsSrcFile(superPolicyCmptType);
        saveIpsSrcFile(superPolicyCmptType.findProductCmptType(ipsProject));
        saveIpsSrcFile(policyCmptType);
        saveIpsSrcFile(policyCmptType.findProductCmptType(ipsProject));
        performFullBuild(ipsProject);

        performRenameRefactoring(policyCmptTypeAttribute, "test");

        IProductCmptType superSuperProductCmptType = superSuperPolicyCmptType.findProductCmptType(ipsProject);
        PolicyCmptTypeAttributeExpectations expectations = new PolicyCmptTypeAttributeExpectations(
                policyCmptTypeAttribute, superSuperPolicyCmptType, superSuperProductCmptType);
        expectations.check("policyAttribute", "test", intParam());
    }

    @Test
    public void testRenameProductCmptTypeAttribute() throws CoreException {
        IProductCmptTypeAttribute productCmptTypeAttribute = createProductCmptTypeAttribute("productAttribute",
                "Product", "Policy");

        IProductCmptType productCmptType = productCmptTypeAttribute.getProductCmptType();
        IPolicyCmptType policyCmptType = productCmptType.findPolicyCmptType(ipsProject);
        saveIpsSrcFile(policyCmptType);
        saveIpsSrcFile(productCmptType);
        performFullBuild(ipsProject);

        performRenameRefactoring(productCmptTypeAttribute, "test");

        ProductCmptTypeAttributeExpectations expectations = new ProductCmptTypeAttributeExpectations(
                productCmptTypeAttribute, productCmptType, policyCmptType);
        expectations.check("productAttribute", "test", stringParam());
    }

    @Test
    public void testRenameEnumAttributeAbstractJava5Enums() throws CoreException {
        IEnumType enumType = createEnumType("EnumType", null, "id", "name");
        enumType.setAbstract(true);

        saveIpsSrcFile(enumType);
        performFullBuild(ipsProject);

        performRenameRefactoring(enumType.getEnumAttribute("id"), "test");

        AbstractEnumAttributeExpectations expectations = new AbstractEnumAttributeExpectations(enumType);
        expectations.check("id", "test");
    }

    /**
     * Assures that Java elements referring to Java elements in the type hierarchy of an enum are
     * properly renamed.
     */
    @Test
    public void testRenameEnumAttributeHierarchy() throws CoreException {
        // Create the hierarchy
        IEnumType superEnumType = createEnumType("SuperEnumType", null, "id", "name");
        superEnumType.setAbstract(true);
        IEnumType midEnumType = createEnumType("MidEnumType", superEnumType, "id", "name");
        midEnumType.setAbstract(true);
        midEnumType.getEnumAttribute("name").setInherited(true);
        IEnumType subEnumType = createEnumType("SubEnumType", midEnumType, "id", "name");
        subEnumType.getEnumAttribute("name").setInherited(true);
        subEnumType.newEnumLiteralNameAttribute();

        saveIpsSrcFile(superEnumType);
        saveIpsSrcFile(midEnumType);
        saveIpsSrcFile(subEnumType);
        performFullBuild(ipsProject);

        performRenameRefactoring(superEnumType.getEnumAttribute("id"), "test");

        IType subJavaType = getJavaType("", "SubEnumType", true, false, ipsProject);
        assertFalse(subJavaType.getMethod("getValueById", new String[] { stringParam() }).exists());
        assertFalse(subJavaType.getMethod("isValueById", new String[] { stringParam() }).exists());
        assertTrue(subJavaType.getMethod("getValueByTest", new String[] { stringParam() }).exists());
        assertTrue(subJavaType.getMethod("isValueByTest", new String[] { stringParam() }).exists());
    }

    @Test
    public void testRenamePolicyCmptType() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);

        saveIpsSrcFile(policyCmptType);
        saveIpsSrcFile(productCmptType);
        performFullBuild(ipsProject);

        performRenameRefactoring(policyCmptType, "RenamedPolicy");

        checkJavaSourceFilesPolicyCmptType("", "Policy", "", "RenamedPolicy");
        PolicyCmptTypeExpectations expectations = new PolicyCmptTypeExpectations(productCmptType);
        expectations.check("Policy", "RenamedPolicy");
        assertThat(productCmptType.getPolicyCmptType(), is("RenamedPolicy"));
    }

    @Test
    public void testRenamePolicyCmptType_illegalName() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);

        saveIpsSrcFile(policyCmptType);
        saveIpsSrcFile(productCmptType);
        performFullBuild(ipsProject);

        performRenameRefactoring(policyCmptType, "?$. Foo");

        assertThat(productCmptType.getPolicyCmptType(), is("Policy"));
    }

    @Test
    public void testRenameProductCmptType() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);

        saveIpsSrcFile(policyCmptType);
        saveIpsSrcFile(productCmptType);
        performFullBuild(ipsProject);

        performRenameRefactoring(productCmptType, "RenamedProduct");

        checkJavaSourceFilesProductCmptType("", "Product", "", "RenamedProduct");
        ProductCmptTypeExpectations expectations = new ProductCmptTypeExpectations(policyCmptType);
        expectations.check("Product", "RenamedProduct");
    }

    @Test
    public void testRenameProductCmpt() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);
        ProductCmpt productCmpt = newProductCmpt(productCmptType, "Prod");
        ProductCmpt productCmpt2 = newProductCmpt(productCmptType, "Prod2");
        ((IpsPackageFragment)productCmpt.getIpsPackageFragment()).setChildOrderComparator(
                new DefinedOrderComparator(productCmpt.getIpsSrcFile(), productCmpt2.getIpsSrcFile()));

        saveIpsSrcFile(policyCmptType);
        saveIpsSrcFile(productCmptType);
        saveIpsSrcFile(productCmpt);
        performFullBuild(ipsProject);

        performRenameRefactoring(productCmpt, "RenamedProd");

        assertNull(ipsProject.findProductCmpt("Prod"));
        assertNotNull(ipsProject.findProductCmpt("RenamedProd"));
        IIpsElement[] elements = ((DefinedOrderComparator)productCmpt.getIpsPackageFragment().getChildOrderComparator())
                .getElements();
        assertThat(Arrays.asList(elements), containsInOrder("RenamedProd", "Prod2"));
    }

    @Test
    public void testRenamePackage() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        IProductCmptType productCmptType = policyCmptType.findProductCmptType(ipsProject);
        ProductCmpt productCmpt = newProductCmpt(productCmptType, "pack.Prod");
        ProductCmpt productCmpt2 = newProductCmpt(productCmptType, "pack.Prod2");
        IpsPackageFragment pack = (IpsPackageFragment)productCmpt.getIpsPackageFragment();
        pack.setChildOrderComparator(
                new DefinedOrderComparator(productCmpt.getIpsSrcFile(), productCmpt2.getIpsSrcFile()));

        IpsPackageFragment parentPack = (IpsPackageFragment)pack.getParentIpsPackageFragment();
        IIpsPackageFragment pack2 = parentPack.createSubPackage("pack2", true, null);
        parentPack.setChildOrderComparator(new DefinedOrderComparator(pack2, pack));

        saveIpsSrcFile(policyCmptType);
        saveIpsSrcFile(productCmptType);
        saveIpsSrcFile(productCmpt);
        saveIpsSrcFile(productCmpt2);
        performFullBuild(ipsProject);

        performRenameRefactoring(pack, "renamedPack");

        assertFalse(parentPack.getSubPackage("pack").exists());
        assertTrue(parentPack.getSubPackage("renamedPack").exists());
        IIpsElement[] elements = ((DefinedOrderComparator)parentPack.getChildOrderComparator()).getElements();
        assertThat(Arrays.asList(elements), containsInOrder("pack2", "renamedPack"));
        elements = ((DefinedOrderComparator)parentPack.getSubPackage("renamedPack").getChildOrderComparator())
                .getElements();
        assertThat(Arrays.asList(elements), containsInOrder("renamedPack.Prod", "renamedPack.Prod2"));
    }

    @Test
    public void testRenameEnumLiteralNameAttributeValue() throws CoreException {
        performTestRenameEnumLiteralNameAttributeValue();
    }

    private void performTestRenameEnumLiteralNameAttributeValue() throws CoreException {
        IEnumType enumType = createEnumType("EnumType", null, "id", "name", "name", "0", "foo", "FOO");
        IEnumLiteralNameAttributeValue enumLiteralNameAttributeValue = enumType.getEnumValues().get(0)
                .getEnumLiteralNameAttributeValue();

        saveIpsSrcFile(enumType);
        performFullBuild(ipsProject);

        performRenameRefactoring(enumLiteralNameAttributeValue, "bar");

        IType javaEnum = getJavaType("", "EnumType", true, false, ipsProject);
        assertFalse(javaEnum.getField("FOO").exists());
        assertTrue(javaEnum.getField("bar").exists());
    }

    @Test
    public void testRenameEnumType() throws CoreException {
        IEnumType enumType = createEnumType("EnumType", null, "id", "name");
        enumType.setExtensible(true);
        enumType.newEnumLiteralNameAttribute();
        enumType.setEnumContentName("EnumContent");

        saveIpsSrcFile(enumType);
        performFullBuild(ipsProject);

        performRenameRefactoring(enumType, "RenamedEnumType");

        checkJavaSourceFilesEnumType("", "EnumType", "", "RenamedEnumType");
    }

    @Test
    public void testRenameTableStructure() throws CoreException {
        ITableStructure tableStructure = createTableStructure("TableStructure");

        saveIpsSrcFile(tableStructure);
        performFullBuild(ipsProject);

        performRenameRefactoring(tableStructure, "RenamedTableStructure");

        checkJavaSourceFilesTableStructure("", "TableStructure", "", "RenamedTableStructure");
    }

    @Test
    public void testRenameTestCaseType() throws CoreException {
        ITestCaseType testCaseType = createTestCaseType("TestCaseType");

        saveIpsSrcFile(testCaseType);
        performFullBuild(ipsProject);

        performRenameRefactoring(testCaseType, "RenamedTestCaseType");

        checkJavaSourceFilesTestCaseType("", "TestCaseType", "", "RenamedTestCaseType");
    }

    @Test
    public void testRenameOnlyLetterCaseChanged() throws CoreException {
        IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "PolicyCmptType");

        saveIpsSrcFile(policyCmptType);
        performFullBuild(ipsProject);

        performRenameRefactoring(policyCmptType, "policyCmptType");

        assertTrue(getJavaType("", getPublishedInterfaceName("policyCmptType", ipsProject), true, false, ipsProject)
                .exists());
        assertTrue(getJavaType("", "policyCmptType", false, false, ipsProject).exists());
    }

    private IPolicyCmptTypeAttribute createPolicyCmptTypeAttribute(String name,
            String policyCmptTypeName,
            String productCmptTypeName) throws CoreException {

        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, policyCmptTypeName,
                productCmptTypeName);
        return createPolicyCmptTypeAttribute(name, policyCmptType);
    }

    private IPolicyCmptTypeAttribute createPolicyCmptTypeAttribute(String name, IPolicyCmptType policyCmptType) {
        IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttribute.setName(name);
        policyCmptTypeAttribute.setDatatype(Datatype.PRIMITIVE_INT.getQualifiedName());
        policyCmptTypeAttribute.setModifier(Modifier.PUBLISHED);
        policyCmptTypeAttribute.setAttributeType(AttributeType.CHANGEABLE);
        policyCmptTypeAttribute.setValueSetConfiguredByProduct(true);
        policyCmptTypeAttribute.setDefaultValue("0");
        policyCmptTypeAttribute.getValueSet().setContainsNull(false);
        return policyCmptTypeAttribute;
    }

    private IProductCmptTypeAttribute createProductCmptTypeAttribute(String name,
            String productCmptTypeName,
            String policyCmptTypeName) throws CoreException {

        IPolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, policyCmptTypeName,
                productCmptTypeName);
        IProductCmptTypeAttribute productCmptTypeAttribute = policyCmptType.findProductCmptType(ipsProject)
                .newProductCmptTypeAttribute();
        productCmptTypeAttribute.setName(name);
        productCmptTypeAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productCmptTypeAttribute.setModifier(Modifier.PUBLISHED);
        productCmptTypeAttribute.setChangingOverTime(true);
        return productCmptTypeAttribute;
    }

    private static class AbstractEnumAttributeExpectations {

        private final IIpsProject ipsProject;

        private final IType javaEnum;

        private AbstractEnumAttributeExpectations(IEnumType enumType) {
            ipsProject = enumType.getIpsProject();
            javaEnum = getJavaType("", enumType.getName(), true, false, ipsProject);
        }

        private void check(String oldName, String newName) {
            checkJava5Enums(oldName, newName);
        }

        private void checkJava5Enums(String oldName, String newName) {
            String oldNameCamelCase = StringUtil.toCamelCase(oldName, true);
            String newNameCamelCase = StringUtil.toCamelCase(newName, true);

            assertFalse(javaEnum.getMethod("get" + oldNameCamelCase, new String[0]).exists());

            assertTrue(javaEnum.getMethod("get" + newNameCamelCase, new String[0]).exists());
        }

    }

    private static class PolicyCmptTypeExpectations {

        private final IIpsProject ipsProject;

        private final IType productInterface;

        private final IType productClass;

        private PolicyCmptTypeExpectations(IProductCmptType productCmptType) {
            ipsProject = productCmptType.getIpsProject();
            productInterface = getJavaType("", getPublishedInterfaceName(productCmptType.getName(), ipsProject), true,
                    false, ipsProject);
            productClass = getJavaType("", productCmptType.getName(), false, false, ipsProject);
        }

        private void check(String oldName, String newName) {
            assertFalse(productClass.getMethod("create" + oldName, new String[0]).exists());
            assertFalse(productInterface.getMethod("create" + oldName, new String[0]).exists());

            assertTrue(productClass.getMethod("create" + newName, new String[0]).exists());
            assertTrue(productInterface.getMethod("create" + newName, new String[0]).exists());
        }

    }

    private static class ProductCmptTypeExpectations {

        private final IIpsProject ipsProject;

        private final IType policyClass;

        private ProductCmptTypeExpectations(IPolicyCmptType policyCmptType) {
            ipsProject = policyCmptType.getIpsProject();
            policyClass = getJavaType("", policyCmptType.getName(), false, false, ipsProject);
        }

        private void check(String oldName, String newName) {
            assertFalse(policyClass.getMethod("get" + oldName, new String[0]).exists());
            assertFalse(policyClass
                    .getMethod("get" + oldName + getGenerationConceptNameAbbreviation(ipsProject), new String[0])
                    .exists());
            assertFalse(policyClass.getMethod("set" + oldName,
                    new String[] { unresolvedParam(getPublishedInterfaceName(oldName, ipsProject)), booleanParam() })
                    .exists());

            if (!policyClass.getMethod("get" + newName, new String[0]).exists()) {
                System.err.println("arg");
            }
            assertTrue(policyClass.getMethod("get" + newName, new String[0]).exists());
            if (!policyClass
                    .getMethod("get" + newName + getGenerationConceptNameAbbreviation(ipsProject), new String[0])
                    .exists()) {
                System.err.println("arg2");
            }
            assertTrue(policyClass
                    .getMethod("get" + newName + getGenerationConceptNameAbbreviation(ipsProject), new String[0])
                    .exists());
            if (!policyClass.getMethod("set" + newName,
                    new String[] { unresolvedParam(getPublishedInterfaceName(newName, ipsProject)), booleanParam() })
                    .exists()) {
                System.err.println("arg3");
            }
            assertTrue(policyClass.getMethod("set" + newName,
                    new String[] { unresolvedParam(getPublishedInterfaceName(newName, ipsProject)), booleanParam() })
                    .exists());
        }

    }

}
