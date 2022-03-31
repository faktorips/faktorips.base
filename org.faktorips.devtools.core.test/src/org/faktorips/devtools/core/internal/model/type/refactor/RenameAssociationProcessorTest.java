/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.type.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.faktorips.abstracttest.core.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.junit.Before;
import org.junit.Test;

public class RenameAssociationProcessorTest extends AbstractIpsRefactoringTest {

    private static final String POLICY_ROLE_SINGULAR = "PolicyRoleSingular";

    private static final String POLICY_ROLE_PLURAL = "PolicyRolePlural";

    private static final String PRODUCT_ROLE_SINGULAR = "ProductRoleSingular";

    private static final String PRODUCT_ROLE_PLURAL = "ProductRolePlural";

    private IPolicyCmptTypeAssociation policyToOtherPolicyAssociation;

    private IProductCmptTypeAssociation productToOtherProductAssociation;

    private ITestPolicyCmptTypeParameter policyAssociationTestParameter;

    private IProductCmptLink productCmptLink;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        policyToOtherPolicyAssociation = policyCmptType.newPolicyCmptTypeAssociation();
        policyToOtherPolicyAssociation.setTarget(otherPolicyCmptType.getQualifiedName());
        policyToOtherPolicyAssociation.setTargetRoleSingular(POLICY_ROLE_SINGULAR);
        policyToOtherPolicyAssociation.setTargetRolePlural(POLICY_ROLE_PLURAL);
        policyToOtherPolicyAssociation.setInverseAssociation(otherPolicyToPolicyAssociation.getName());
        otherPolicyToPolicyAssociation.setInverseAssociation(policyToOtherPolicyAssociation.getName());

        productToOtherProductAssociation = productCmptType.newProductCmptTypeAssociation();
        productToOtherProductAssociation.setTarget(otherProductCmptType.getQualifiedName());
        productToOtherProductAssociation.setTargetRoleSingular(PRODUCT_ROLE_SINGULAR);
        productToOtherProductAssociation.setTargetRolePlural(PRODUCT_ROLE_PLURAL);

        policyAssociationTestParameter = testPolicyCmptTypeParameter.newTestPolicyCmptTypeParamChild();
        policyAssociationTestParameter.setAssociation(POLICY_ROLE_SINGULAR);
        policyAssociationTestParameter.setName(POLICY_ROLE_SINGULAR);
        policyAssociationTestParameter.setPolicyCmptType(policyCmptType.getQualifiedName());

        productCmptLink = productCmptGeneration.newLink(productToOtherProductAssociation);
    }

    @Test
    public void testValidateUserInputNewNameEmpty() {
        IpsRenameProcessor ipsRenameProcessor = new RenameAssociationProcessor(policyToOtherPolicyAssociation);
        ipsRenameProcessor.setNewName("");
        ipsRenameProcessor.setNewPluralName("somePluralName");
        RefactoringStatus status = ipsRenameProcessor.validateUserInput(new NullProgressMonitor());
        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputNeitherNameNorPluralNameChanged() {
        IpsRenameProcessor ipsRenameProcessor = new RenameAssociationProcessor(policyToOtherPolicyAssociation);
        ipsRenameProcessor.setNewName(POLICY_ROLE_SINGULAR);
        ipsRenameProcessor.setNewPluralName(POLICY_ROLE_PLURAL);
        RefactoringStatus status = ipsRenameProcessor.validateUserInput(new NullProgressMonitor());
        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputNoPluralNameForToManyAssociation() {
        IpsRenameProcessor ipsRenameProcessor = new RenameAssociationProcessor(policyToOtherPolicyAssociation);
        ipsRenameProcessor.setNewName("someNewName");
        ipsRenameProcessor.setNewPluralName("");
        RefactoringStatus status = ipsRenameProcessor.validateUserInput(new NullProgressMonitor());
        assertTrue(status.hasFatalError());
    }

    @Test
    public void testValidateUserInputValid() {
        IpsRenameProcessor ipsRenameProcessor = new RenameAssociationProcessor(policyToOtherPolicyAssociation);
        ipsRenameProcessor.setNewName("someNewName");
        ipsRenameProcessor.setNewPluralName("someNewPluralName");
        RefactoringStatus status = ipsRenameProcessor.validateUserInput(new NullProgressMonitor());
        assertTrue(status.isOK());
    }

    @Test
    public void testRenamePolicyCmptTypeAssociation() {
        String newAssociationName = "foo";
        String newPluralAssociationName = "bar";
        performRenameRefactoring(policyToOtherPolicyAssociation, newAssociationName, newPluralAssociationName);

        // Check for changed association roles
        assertNull(policyCmptType.getAssociation(POLICY_ROLE_SINGULAR));
        assertNull(policyCmptType.getAssociationByRoleNamePlural(POLICY_ROLE_PLURAL));
        assertNotNull(policyCmptType.getAssociation(newAssociationName));
        assertNotNull(policyCmptType.getAssociationByRoleNamePlural(newPluralAssociationName));
        assertTrue(policyToOtherPolicyAssociation.getTargetRoleSingular().equals(newAssociationName));
        assertTrue(policyToOtherPolicyAssociation.getTargetRolePlural().equals(newPluralAssociationName));

        // Check for inverse association update
        assertEquals(newAssociationName, otherPolicyToPolicyAssociation.getInverseAssociation());

        // Check for test parameter update
        assertNull(testPolicyCmptTypeParameter.getTestPolicyCmptTypeParamChild(POLICY_ROLE_SINGULAR));
        assertNotNull(testPolicyCmptTypeParameter.getTestPolicyCmptTypeParamChild(newAssociationName));
        assertEquals(newAssociationName, policyAssociationTestParameter.getName());
        assertEquals(newAssociationName, policyAssociationTestParameter.getAssociation());
    }

    @Test
    public void testRenamePolicyCmptTypeAssociationDerivedUnion() {
        IPolicyCmptType policyCmptType = newPolicyCmptType(ipsProject, "Policy");
        policyCmptType.setConfigurableByProductCmptType(false);
        policyCmptType.setAbstract(true);
        IPolicyCmptType subtype = newPolicyCmptType(ipsProject, "SubType");
        subtype.setConfigurableByProductCmptType(false);
        subtype.setSupertype(policyCmptType.getQualifiedName());
        IPolicyCmptType target = newPolicyCmptType(ipsProject, "Target");

        IPolicyCmptTypeAssociation derivedUnion = policyCmptType.newPolicyCmptTypeAssociation();
        derivedUnion.setTarget(target.getQualifiedName());
        derivedUnion.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        derivedUnion.setMinCardinality(1);
        derivedUnion.setMaxCardinality(IAssociation.CARDINALITY_MANY);
        derivedUnion.setDerivedUnion(true);
        derivedUnion.setTargetRoleSingular("foo");
        derivedUnion.setTargetRolePlural("foos");

        IPolicyCmptTypeAssociation subset = policyCmptType.newPolicyCmptTypeAssociation();
        subset.setTarget(target.getQualifiedName());
        subset.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        subset.setSubsettedDerivedUnion(derivedUnion.getName());
        subset.setMinCardinality(1);
        subset.setMaxCardinality(IAssociation.CARDINALITY_MANY);
        subset.setTargetRoleSingular("hexadecimal");
        subset.setTargetRolePlural("hexadecimals");

        IPolicyCmptTypeAssociation subtypeSubset = subtype.newPolicyCmptTypeAssociation();
        subtypeSubset.setTarget(target.getQualifiedName());
        subtypeSubset.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        subtypeSubset.setSubsettedDerivedUnion(derivedUnion.getName());
        subtypeSubset.setMinCardinality(1);
        subtypeSubset.setMaxCardinality(IAssociation.CARDINALITY_MANY);
        subtypeSubset.setTargetRoleSingular("bar");
        subtypeSubset.setTargetRolePlural("bars");

        String newName = "foobar";
        String newPluralName = "foobars";
        performRenameRefactoring(derivedUnion, newName, newPluralName);

        // Check for subsetting associations update
        assertEquals(newName, subset.getSubsettedDerivedUnion());
        assertEquals(newName, subtypeSubset.getSubsettedDerivedUnion());
    }

    @Test
    public void testRenameProductCmptTypeAssociation() {
        String newAssociationName = "foo";
        String newPluralAssociationName = "bar";
        performRenameRefactoring(productToOtherProductAssociation, newAssociationName, newPluralAssociationName);

        // Check for changed association roles
        assertNull(productCmptType.getAssociation(PRODUCT_ROLE_SINGULAR));
        assertNull(productCmptType.getAssociationByRoleNamePlural(PRODUCT_ROLE_PLURAL));
        assertNotNull(productCmptType.getAssociation(newAssociationName));
        assertNotNull(productCmptType.getAssociationByRoleNamePlural(newPluralAssociationName));
        assertTrue(productToOtherProductAssociation.getTargetRoleSingular().equals(newAssociationName));
        assertTrue(productToOtherProductAssociation.getTargetRolePlural().equals(newPluralAssociationName));

        // Check for product component link update
        assertEquals(0, productCmptGeneration.getLinks(PRODUCT_ROLE_SINGULAR).length);
        assertEquals(1, productCmptGeneration.getLinks(newAssociationName).length);
        assertEquals(newAssociationName, productCmptLink.getAssociation());
    }

    @Test
    public void testUpdateChangingLinks() {
        RenameAssociationProcessor renameProcessor = new RenameAssociationProcessor(productToOtherProductAssociation);
        renameProcessor.setNewName("DUMMY_NAME");
        IProductCmptLink staticLink = productCmpt.newLink(productToOtherProductAssociation);

        renameProcessor.updateProductCmptLinksFor(productCmpt);

        assertEquals("DUMMY_NAME", productCmptLink.getAssociation());
        assertEquals("DUMMY_NAME", staticLink.getAssociation());
    }
}
