/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.abstracttest.core.AbstractCoreIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.enums.IEnumAttribute;
import org.faktorips.devtools.model.enums.IEnumContent;
import org.faktorips.devtools.model.enums.IEnumType;
import org.faktorips.devtools.model.ipsobject.IIpsObject;
import org.faktorips.devtools.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.model.tablecontents.ITableContents;
import org.faktorips.devtools.model.tablestructure.ITableStructure;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.type.IMethod;
import org.junit.Before;

/**
 * @author Alexander Weickmann
 */
public abstract class AbstractMoveRenameIpsObjectTest extends AbstractCoreIpsPluginTest {

    protected IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
    }

    protected final void checkIpsSourceFile(String oldName,
            String newName,
            IIpsPackageFragment originalIpsPackageFragment,
            IIpsPackageFragment targetIpsPackageFragment,
            IpsObjectType ipsObjectType) {

        // The old file must no longer exist
        IIpsSrcFile oldIpsSrcFile = originalIpsPackageFragment.getIpsSrcFile(oldName, ipsObjectType);
        assertFalse(oldIpsSrcFile.exists());

        // Find the new file and IPS object
        IIpsSrcFile newIpsSrcFile = targetIpsPackageFragment.getIpsSrcFile(newName, ipsObjectType);
        assertTrue(newIpsSrcFile.exists());
        IIpsObject newIpsObject = newIpsSrcFile.getIpsObject();
        assertEquals(newName, newIpsObject.getName());
        assertEquals(targetIpsPackageFragment, newIpsObject.getIpsPackageFragment());
    }

    protected final void saveIpsSrcFile(IIpsObject ipsObject) {
        ipsObject.getIpsSrcFile().save(null);
    }

    protected final IEnumType createEnumType(String name,
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

    protected final class PolicyCmptTypeReferences {

        private final IPolicyCmptType policyCmptType;

        private final IPolicyCmptType otherPolicyCmptType;

        private final IProductCmptType productCmptType;

        private final IPolicyCmptTypeAttribute policyCmptTypeAttribute;

        private final IMethod policyCmptTypeMethod;

        private final IProductCmptTypeMethod productCmptTypeMethod;

        private final IPolicyCmptTypeAssociation policyCmptTypeAssociation;

        private final IPolicyCmptTypeAssociation policyToSelfAssociation;

        private final ITestCaseType testCaseType;

        private final ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter;

        private final ITestAttribute testAttribute;

        private final ITestPolicyCmptTypeParameter testParameterChild1;

        private final ITestPolicyCmptTypeParameter testParameterChild2;

        private final ITestPolicyCmptTypeParameter testParameterChild3;

        protected PolicyCmptTypeReferences(IPolicyCmptType policyCmptType, boolean createInverseAssociation) {

            this.policyCmptType = policyCmptType;
            productCmptType = createProductCmptType();
            otherPolicyCmptType = createOtherPolicyCmptType();
            policyCmptTypeAttribute = createPolicyCmptTypeAttribute();
            policyCmptTypeMethod = createPolicyCmptTypeMethod();
            productCmptTypeMethod = createProductCmptTypeMethod();
            policyCmptTypeAssociation = createPolicyCmptTypeAssociation(createInverseAssociation);
            policyToSelfAssociation = createPolicyToSelfAssociation();
            testCaseType = createTestCaseType();
            testPolicyCmptTypeParameter = createTestPolicyCmptTypeParameter();
            testAttribute = createTestAttribute();
            testParameterChild1 = createTestParameterChild1();
            testParameterChild2 = createTestParameterChild2();
            testParameterChild3 = createTestParameterChild3();
        }

        private IProductCmptType createProductCmptType() {
            IProductCmptType productCmptType = newProductCmptType(ipsProject, "Product");
            productCmptType.setConfigurationForPolicyCmptType(true);
            productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
            policyCmptType.setConfigurableByProductCmptType(true);
            policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
            return productCmptType;
        }

        private IPolicyCmptType createOtherPolicyCmptType() {
            return newPolicyCmptTypeWithoutProductCmptType(ipsProject, "OtherPolicy");
        }

        private IPolicyCmptTypeAttribute createPolicyCmptTypeAttribute() {
            IPolicyCmptTypeAttribute policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute();
            policyCmptTypeAttribute.setName("policyAttribute");
            policyCmptTypeAttribute.setDatatype(Datatype.MONEY.getQualifiedName());
            policyCmptTypeAttribute.setModifier(Modifier.PUBLISHED);
            policyCmptTypeAttribute.setAttributeType(AttributeType.CHANGEABLE);
            policyCmptTypeAttribute.setValueSetConfiguredByProduct(true);
            return policyCmptTypeAttribute;
        }

        private IMethod createPolicyCmptTypeMethod() {
            IMethod policyCmptTypeMethod = otherPolicyCmptType.newMethod();
            policyCmptTypeMethod.setName("policyMethod");
            policyCmptTypeMethod.setDatatype(Datatype.STRING.getQualifiedName());
            policyCmptTypeMethod.newParameter(Datatype.INTEGER.getQualifiedName(), "standardDatatype");
            policyCmptTypeMethod.newParameter(policyCmptType.getQualifiedName(), "policyDatatype");
            policyCmptTypeMethod.newParameter(productCmptType.getQualifiedName(), "productDatatype");
            return policyCmptTypeMethod;
        }

        private IProductCmptTypeMethod createProductCmptTypeMethod() {
            IProductCmptTypeMethod productCmptTypeMethod = productCmptType.newProductCmptTypeMethod();
            productCmptTypeMethod.setName("productMethod");
            productCmptTypeMethod.setDatatype(Datatype.STRING.getQualifiedName());
            productCmptTypeMethod.newParameter(Datatype.INTEGER.getQualifiedName(), "standardDatatype");
            productCmptTypeMethod.newParameter(productCmptType.getQualifiedName(), "productDatatype");
            productCmptTypeMethod.newParameter(policyCmptType.getQualifiedName(), "policyDatatype");
            return productCmptTypeMethod;
        }

        private IPolicyCmptTypeAssociation createPolicyCmptTypeAssociation(boolean createInverseAssociation) {
            IPolicyCmptTypeAssociation policyCmptTypeAssociation = otherPolicyCmptType.newPolicyCmptTypeAssociation();
            policyCmptTypeAssociation.setTarget(policyCmptType.getQualifiedName());
            policyCmptTypeAssociation.setTargetRoleSingular(policyCmptType.getName());

            if (createInverseAssociation) {
                IPolicyCmptTypeAssociation association = policyCmptType.newPolicyCmptTypeAssociation();
                association.setInverseAssociation(policyCmptTypeAssociation.getName());
                association.setTarget(otherPolicyCmptType.getQualifiedName());
                association.setTargetRoleSingular("foo");
                association.setTargetRolePlural("foobar");
                policyCmptTypeAssociation.setInverseAssociation(association.getName());
            }

            return policyCmptTypeAssociation;
        }

        private IPolicyCmptTypeAssociation createPolicyToSelfAssociation() {
            IPolicyCmptTypeAssociation policyToSelfAssociation = policyCmptType.newPolicyCmptTypeAssociation();
            policyToSelfAssociation.setTarget(policyCmptType.getQualifiedName());
            policyToSelfAssociation.setTargetRoleSingular("singular");
            policyToSelfAssociation.setTargetRolePlural("plural");
            return policyToSelfAssociation;
        }

        private ITestCaseType createTestCaseType() {
            return newTestCaseType(ipsProject, "TestCaseType");
        }

        private ITestPolicyCmptTypeParameter createTestPolicyCmptTypeParameter() {
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter = testCaseType
                    .newCombinedPolicyCmptTypeParameter();
            testPolicyCmptTypeParameter.setPolicyCmptType(policyCmptType.getQualifiedName());
            testPolicyCmptTypeParameter.setName("testParameter");
            return testPolicyCmptTypeParameter;
        }

        private ITestAttribute createTestAttribute() {
            ITestAttribute testAttribute = testPolicyCmptTypeParameter.newInputTestAttribute();
            testAttribute.setAttribute(policyCmptTypeAttribute);
            testAttribute.setName("testAttribute");
            testAttribute.setPolicyCmptType(policyCmptType.getQualifiedName());
            return testAttribute;
        }

        private ITestPolicyCmptTypeParameter createTestParameterChild1() {
            ITestPolicyCmptTypeParameter testParameterChild1 = testPolicyCmptTypeParameter
                    .newTestPolicyCmptTypeParamChild();
            testParameterChild1.setPolicyCmptType(policyCmptType.getQualifiedName());
            testParameterChild1.setName("child1");
            testParameterChild1.setAssociation(policyToSelfAssociation.getName());
            return testParameterChild1;
        }

        private ITestPolicyCmptTypeParameter createTestParameterChild2() {
            ITestPolicyCmptTypeParameter testParameterChild2 = testParameterChild1.newTestPolicyCmptTypeParamChild();
            testParameterChild2.setPolicyCmptType(policyCmptType.getQualifiedName());
            testParameterChild2.setName("child2");
            testParameterChild2.setAssociation(policyToSelfAssociation.getName());
            return testParameterChild2;
        }

        private ITestPolicyCmptTypeParameter createTestParameterChild3() {
            ITestPolicyCmptTypeParameter testParameterChild3 = testParameterChild2.newTestPolicyCmptTypeParamChild();
            testParameterChild3.setPolicyCmptType(policyCmptType.getQualifiedName());
            testParameterChild3.setName("child3");
            testParameterChild3.setAssociation(policyToSelfAssociation.getName());
            return testParameterChild2;
        }

        protected void saveIpsSrcFiles() {
            saveIpsSrcFile(policyCmptType);
            saveIpsSrcFile(productCmptType);
            saveIpsSrcFile(otherPolicyCmptType);
        }

        protected void check(String newQualifiedName) {
            // Check for product component type configuration update
            assertEquals(newQualifiedName, productCmptType.getPolicyCmptType());

            // Check for test parameter and test attribute update
            assertEquals(newQualifiedName, testPolicyCmptTypeParameter.getPolicyCmptType());
            assertEquals(newQualifiedName, testAttribute.getPolicyCmptType());
            assertEquals(newQualifiedName, testParameterChild1.getPolicyCmptType());
            assertEquals(newQualifiedName, testParameterChild2.getPolicyCmptType());
            assertEquals(newQualifiedName, testParameterChild3.getPolicyCmptType());

            // Check for method parameter update
            assertEquals(Datatype.INTEGER.getQualifiedName(), policyCmptTypeMethod.getParameters()[0].getDatatype());
            assertEquals(newQualifiedName, policyCmptTypeMethod.getParameters()[1].getDatatype());
            assertEquals(newQualifiedName, productCmptTypeMethod.getParameters()[2].getDatatype());

            // Check for association update
            assertEquals(newQualifiedName, policyCmptTypeAssociation.getTarget());
        }

    }

    protected final class SuperPolicyCmptTypeReferences {

        private final IPolicyCmptType superPolicyCmptType;

        private final IPolicyCmptType policyCmptType;

        private final ITestCaseType testCaseType;

        private final ITestAttribute superTestAttribute;

        protected SuperPolicyCmptTypeReferences(IPolicyCmptType superPolicyCmptType) {
            this.superPolicyCmptType = superPolicyCmptType;
            policyCmptType = createPolicyCmptType();
            testCaseType = createTestCaseType();
            superTestAttribute = createSuperTestAttribute();
        }

        private IPolicyCmptType createPolicyCmptType() {
            IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Policy");
            policyCmptType.setSupertype(superPolicyCmptType.getQualifiedName());
            return policyCmptType;
        }

        private ITestCaseType createTestCaseType() {
            return newTestCaseType(ipsProject, "TestCaseType");
        }

        private ITestAttribute createSuperTestAttribute() {
            ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter = testCaseType
                    .newCombinedPolicyCmptTypeParameter();
            testPolicyCmptTypeParameter.setPolicyCmptType(policyCmptType.getQualifiedName());
            testPolicyCmptTypeParameter.setName("testParameter");

            IPolicyCmptTypeAttribute superPolicyAttribute = superPolicyCmptType.newPolicyCmptTypeAttribute();
            superPolicyAttribute.setName("superPolicyAttribute");
            superPolicyAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
            superPolicyAttribute.setModifier(Modifier.PUBLISHED);
            superPolicyAttribute.setAttributeType(AttributeType.CHANGEABLE);

            ITestAttribute superTestAttribute = testPolicyCmptTypeParameter.newInputTestAttribute();
            superTestAttribute.setAttribute(superPolicyAttribute);
            superTestAttribute.setPolicyCmptType(superPolicyCmptType.getQualifiedName());
            superTestAttribute.setName("superPolicyAttribute");
            return superTestAttribute;
        }

        protected void saveIpsSrcFiles() {
            saveIpsSrcFile(superPolicyCmptType);
            saveIpsSrcFile(policyCmptType);
            saveIpsSrcFile(testCaseType);
        }

        protected void check(String newQualifiedName) {
            // Check for test attribute update
            assertEquals(newQualifiedName, superTestAttribute.getPolicyCmptType());

            // Check for subtype update
            assertEquals(newQualifiedName, policyCmptType.getSupertype());
        }

    }

    protected final class ProductCmptTypeReferences {

        private final IProductCmptType productCmptType;

        private final IPolicyCmptType policyCmptType;

        private final IProductCmptType otherProductCmptType;

        private final IProductCmpt productCmpt;

        private final IMethod policyCmptTypeMethod;

        private final IProductCmptTypeMethod productCmptTypeMethod;

        private final IProductCmptTypeAssociation productCmptTypeAssociation;

        protected ProductCmptTypeReferences(IProductCmptType productCmptType) {
            this.productCmptType = productCmptType;
            policyCmptType = createPolicyCmptType();
            otherProductCmptType = createOtherProductCmptType();
            productCmpt = createProductCmpt();
            policyCmptTypeMethod = createPolicyCmptTypeMethod();
            productCmptTypeMethod = createProductCmptTypeMethod();
            productCmptTypeAssociation = createProductCmptTypeAssociation();
        }

        private IPolicyCmptType createPolicyCmptType() {
            IPolicyCmptType policyCmptType = newPolicyCmptTypeWithoutProductCmptType(ipsProject, "Policy");
            policyCmptType.setConfigurableByProductCmptType(true);
            policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
            productCmptType.setConfigurationForPolicyCmptType(true);
            productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());
            return policyCmptType;
        }

        private IProductCmptType createOtherProductCmptType() {
            return newProductCmptType(ipsProject, "OtherProduct");
        }

        private IProductCmpt createProductCmpt() {
            return newProductCmpt(productCmptType, "ProductCmpt");
        }

        private IMethod createPolicyCmptTypeMethod() {
            IMethod policyCmptTypeMethod = policyCmptType.newMethod();
            policyCmptTypeMethod.setName("policyMethod");
            policyCmptTypeMethod.setDatatype(Datatype.STRING.getQualifiedName());
            policyCmptTypeMethod.newParameter(Datatype.INTEGER.getQualifiedName(), "standardDatatype");
            policyCmptTypeMethod.newParameter(policyCmptType.getQualifiedName(), "policyDatatype");
            policyCmptTypeMethod.newParameter(productCmptType.getQualifiedName(), "productDatatype");
            return policyCmptTypeMethod;
        }

        private IProductCmptTypeMethod createProductCmptTypeMethod() {
            IProductCmptTypeMethod productCmptTypeMethod = otherProductCmptType.newProductCmptTypeMethod();
            productCmptTypeMethod.setName("productMethod");
            productCmptTypeMethod.setDatatype(Datatype.STRING.getQualifiedName());
            productCmptTypeMethod.newParameter(Datatype.INTEGER.getQualifiedName(), "standardDatatype");
            productCmptTypeMethod.newParameter(productCmptType.getQualifiedName(), "productDatatype");
            productCmptTypeMethod.newParameter(policyCmptType.getQualifiedName(), "policyDatatype");
            return productCmptTypeMethod;
        }

        private IProductCmptTypeAssociation createProductCmptTypeAssociation() {
            IProductCmptTypeAssociation productCmptTypeAssociation = otherProductCmptType
                    .newProductCmptTypeAssociation();
            productCmptTypeAssociation.setTarget(productCmptType.getQualifiedName());
            productCmptTypeAssociation.setTargetRoleSingular(productCmptType.getName());
            return productCmptTypeAssociation;
        }

        protected void saveIpsSrcFiles() {
            saveIpsSrcFile(productCmptType);
            saveIpsSrcFile(policyCmptType);
            saveIpsSrcFile(otherProductCmptType);
        }

        protected void check(String newQualifiedName) {
            // Check for policy component type configuration update
            assertEquals(newQualifiedName, policyCmptType.getProductCmptType());

            // Check for product component reference update
            assertEquals(newQualifiedName, productCmpt.getProductCmptType());

            // Check for method parameter update
            assertEquals(Datatype.INTEGER.getQualifiedName(), policyCmptTypeMethod.getParameters()[0].getDatatype());
            assertEquals(newQualifiedName, productCmptTypeMethod.getParameters()[1].getDatatype());
            assertEquals(newQualifiedName, policyCmptTypeMethod.getParameters()[2].getDatatype());

            // Check for association update
            assertEquals(newQualifiedName, productCmptTypeAssociation.getTarget());
        }

    }

    protected final class SuperProductCmptTypeReferences {

        private final IProductCmptType superProductCmptType;

        private final IProductCmptType productCmptType;

        protected SuperProductCmptTypeReferences(IProductCmptType superProductCmptType) {
            this.superProductCmptType = superProductCmptType;
            productCmptType = createProductCmptType();
        }

        private IProductCmptType createProductCmptType() {
            IProductCmptType productCmptType = newProductCmptType(ipsProject, "Product");
            productCmptType.setSupertype(superProductCmptType.getQualifiedName());
            return productCmptType;
        }

        protected void saveIpsSrcFiles() {
            saveIpsSrcFile(superProductCmptType);
            saveIpsSrcFile(productCmptType);
        }

        protected void check(String newQualifiedName) {
            // Check for subtype update
            assertEquals(newQualifiedName, productCmptType.getSupertype());
        }

    }

    protected final class TestCaseTypeReferences {

        private final ITestCaseType testCaseType;

        private final ITestCase testCase;

        protected TestCaseTypeReferences(ITestCaseType testCaseType) {
            this.testCaseType = testCaseType;
            testCase = createTestCase();
        }

        private ITestCase createTestCase() {
            return newTestCase(testCaseType, "TestCase");
        }

        protected void saveIpsSrcFiles() {
            saveIpsSrcFile(testCaseType);
            saveIpsSrcFile(testCase);
        }

        protected void check(String newQualifiedName) {
            // Check for test case reference update
            assertEquals(newQualifiedName, testCase.getTestCaseType());
        }

    }

    protected final class EnumTypeReferences {

        private final IEnumType enumType;

        private final IEnumContent enumContent;

        protected EnumTypeReferences(IEnumType enumType) {
            this.enumType = enumType;
            enumContent = createEnumContent();
        }

        private IEnumContent createEnumContent() {
            return newEnumContent(enumType, "EnumContent");
        }

        protected void saveIpsSrcFiles() {
            saveIpsSrcFile(enumType);
            saveIpsSrcFile(enumContent);
        }

        protected void check(String newQualifiedName) {
            // Check for enumeration content reference update
            assertEquals(newQualifiedName, enumContent.getEnumType());
        }

    }

    protected final class TableStructureReferences {

        private final ITableStructure tableStructure;

        private final ITableContents tableContents;

        protected TableStructureReferences(ITableStructure tableStructure) {
            this.tableStructure = tableStructure;
            tableContents = createTableContents();
        }

        private ITableContents createTableContents() {
            return newTableContents(tableStructure, "TableContents");
        }

        protected void saveIpsSrcFiles() {
            saveIpsSrcFile(tableStructure);
            saveIpsSrcFile(tableContents);
        }

        protected void check(String newQualifiedName) {
            // Check for table contents reference update
            assertEquals(newQualifiedName, tableContents.getTableStructure());
        }

    }

    protected final class ProductCmptReferences {

        private final IProductCmptType productCmptType;

        private final IProductCmpt productCmpt;

        private final IProductCmptType otherProductCmptType;

        private final IProductCmptTypeAssociation productCmptTypeAssociation;

        private final IProductCmpt otherProductCmpt;

        protected ProductCmptReferences(IProductCmpt productCmpt, IProductCmptType productCmptType) {

            this.productCmptType = productCmptType;
            this.productCmpt = productCmpt;
            otherProductCmptType = createOtherProductCmptType();
            productCmptTypeAssociation = createProductCmptTypeAssociation();
            otherProductCmpt = createOtherProductCmpt();
        }

        private IProductCmptType createOtherProductCmptType() {
            return newProductCmptType(ipsProject, "OtherProduct");
        }

        private IProductCmptTypeAssociation createProductCmptTypeAssociation() {
            IProductCmptTypeAssociation productCmptTypeAssociation = otherProductCmptType
                    .newProductCmptTypeAssociation();
            productCmptTypeAssociation.setTarget(productCmptType.getQualifiedName());
            productCmptTypeAssociation.setTargetRoleSingular(productCmptType.getName());
            return productCmptTypeAssociation;
        }

        private IProductCmpt createOtherProductCmpt() {
            IProductCmpt otherProductCmpt = newProductCmpt(productCmptType, "OtherProductCmpt");
            IProductCmptGeneration productCmptGeneration = otherProductCmpt.getFirstGeneration();
            IProductCmptLink productCmptLink = productCmptGeneration.newLink(productCmptTypeAssociation);
            productCmptLink.setTarget(productCmpt.getQualifiedName());
            return otherProductCmpt;
        }

        protected void saveIpsSrcFiles() {
            saveIpsSrcFile(productCmptType);
            saveIpsSrcFile(otherProductCmptType);
            saveIpsSrcFile(productCmpt);
            saveIpsSrcFile(otherProductCmpt);
        }

        protected void check(String newQualifiedName) {
            // Check for update of referring product component generation
            IProductCmptGeneration generation = otherProductCmpt.getFirstGeneration();
            IProductCmptLink[] links = generation.getLinks();
            assertEquals(1, links.length);
            assertEquals(newQualifiedName, links[0].getTarget());
        }

    }

}
