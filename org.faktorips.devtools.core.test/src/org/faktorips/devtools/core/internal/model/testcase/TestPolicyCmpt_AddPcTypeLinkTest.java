/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.testcase;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.core.model.testcase.ITestCase;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.core.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test cases for
 * {@link TestPolicyCmpt#addTestPcTypeLink(ITestPolicyCmptTypeParameter, String, String, String, boolean)}
 */
public class TestPolicyCmpt_AddPcTypeLinkTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws CoreException {
        ipsProject = newIpsProject();
    }

    /**
     * <strong>Scenario:</strong><br>
     * Three policy types 1 -&gt; 2 -&gt; 3 are linked together, cardinalities are all 1..1. Product
     * components are non-ambiguous. A first link is being added to the test case.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * In addition to the added link, one further link to the third policy component must be added
     * automatically. This is because the associations are not optional.
     */
    @Test
    public void testAddPcTypeLink_MinCardinalityOne() throws CoreException {
        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 1, 1);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(productType1, productType2, 1, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3 = createAssociation(policyType2, policyType3, 1, 1);
        IProductCmptTypeAssociation product2ToProduct3 = createAssociation(productType2, productType3, 1, 1);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter parameter1 = createTestParameter(testCaseType, policyType1, 1, 1);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(parameter1, policyType2, policy1ToPolicy2, 1, 1);
        createTestParameter(parameter2, policyType3, policy2ToPolicy3, 1, 1);

        // Create product components
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");
        IProductCmpt productCmpt3 = newProductCmpt(productType3, "Product3");

        // Create product links
        createProductCmptLink(productCmpt1, productCmpt2, product1ToProduct2);
        createProductCmptLink(productCmpt2, productCmpt3, product2ToProduct3);

        // Create test case
        ITestCase testCase = newTestCase(testCaseType, "MyTestCase");
        ITestPolicyCmpt testPolicyCmpt = testCase.newTestPolicyCmpt();
        testPolicyCmpt.setProductCmptAndNameAfterIfApplicable(productCmpt1.getQualifiedName());
        testPolicyCmpt.setPolicyCmptType(policyType1.getQualifiedName());
        testPolicyCmpt.setTestPolicyCmptTypeParameter(parameter1.getName());

        // Execute
        testPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2.getQualifiedName(), null, null, true);

        // Verify
        ITestPolicyCmpt child1 = testPolicyCmpt.getTestPolicyCmptLink(policyType2.getQualifiedName()).findTarget();
        ITestPolicyCmpt child2 = child1.getTestPolicyCmptLink(policyType3.getQualifiedName()).findTarget();
        assertSame(productCmpt3, child2.findProductCmpt(ipsProject));
    }

    /**
     * <strong>Scenario:</strong><br>
     * Three policy types 1 -&gt; 2 -&gt; 3 are linked together, cardinalities are all 0..1. Product
     * components are non-ambiguous. A first link is being added to the test case.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * No additional link must be added because the associations are all optional.
     */
    @Test
    public void testAddPcTypeLink_MinCardinalityZero() throws CoreException {
        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 0, 1);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(productType1, productType2, 0, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3 = createAssociation(policyType2, policyType3, 0, 1);
        IProductCmptTypeAssociation product2ToProduct3 = createAssociation(productType2, productType3, 0, 1);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter parameter1 = createTestParameter(testCaseType, policyType1, 0, 1);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(parameter1, policyType2, policy1ToPolicy2, 0, 1);
        createTestParameter(parameter2, policyType3, policy2ToPolicy3, 0, 1);

        // Create product components
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");
        IProductCmpt productCmpt3 = newProductCmpt(productType3, "Product3");

        // Create product links
        createProductCmptLink(productCmpt1, productCmpt2, product1ToProduct2);
        createProductCmptLink(productCmpt2, productCmpt3, product2ToProduct3);

        // Create test case
        ITestCase testCase = newTestCase(testCaseType, "MyTestCase");
        ITestPolicyCmpt testPolicyCmpt = testCase.newTestPolicyCmpt();
        testPolicyCmpt.setProductCmptAndNameAfterIfApplicable(productCmpt1.getQualifiedName());
        testPolicyCmpt.setPolicyCmptType(policyType1.getQualifiedName());
        testPolicyCmpt.setTestPolicyCmptTypeParameter(parameter1.getName());

        // Execute
        testPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2.getQualifiedName(), null, null, true);

        // Verify
        ITestPolicyCmptLink link1 = testPolicyCmpt.getTestPolicyCmptLink(policyType2.getQualifiedName());
        ITestPolicyCmptLink link2 = link1.findTarget().getTestPolicyCmptLink(policyType3.getQualifiedName());
        assertNull(link2);
    }

    /**
     * <strong>Scenario:</strong><br>
     * Three policy types 1 -&gt; 2 -&gt; 3 are linked together, cardinalities are all 2..3. Product
     * components are non-ambiguous. A first link is being added to the test case.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * In addition to the added link, two further link to the third policy component must be added
     * automatically. This is because the associations require at least two policy components.
     */
    @Test
    public void testAddPcTypeLink_MinCardinalityTwo() throws CoreException {
        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 2, 3);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(productType1, productType2, 2, 3);
        IPolicyCmptTypeAssociation policy2ToPolicy3 = createAssociation(policyType2, policyType3, 2, 3);
        IProductCmptTypeAssociation product2ToProduct3 = createAssociation(productType2, productType3, 2, 3);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter parameter1 = createTestParameter(testCaseType, policyType1, 2, 3);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(parameter1, policyType2, policy1ToPolicy2, 2, 3);
        ITestPolicyCmptTypeParameter parameter3 = createTestParameter(parameter2, policyType3, policy2ToPolicy3, 2, 3);

        // Create product components
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");
        IProductCmpt productCmpt3 = newProductCmpt(productType3, "Product3");

        // Create product links
        createProductCmptLink(productCmpt1, productCmpt2, product1ToProduct2);
        createProductCmptLink(productCmpt2, productCmpt3, product2ToProduct3);

        // Create test case
        ITestCase testCase = newTestCase(testCaseType, "MyTestCase");
        ITestPolicyCmpt testPolicyCmpt = testCase.newTestPolicyCmpt();
        testPolicyCmpt.setProductCmptAndNameAfterIfApplicable(productCmpt1.getQualifiedName());
        testPolicyCmpt.setPolicyCmptType(policyType1.getQualifiedName());
        testPolicyCmpt.setTestPolicyCmptTypeParameter(parameter1.getName());

        // Execute
        testPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2.getQualifiedName(), null, null, true);

        // Verify
        ITestPolicyCmptLink link1_1 = testPolicyCmpt.getTestPolicyCmptLinks(parameter2.getName())[0];
        assertSame(productCmpt2, link1_1.findTarget().findProductCmpt(ipsProject));

        ITestPolicyCmptLink link2_1 = link1_1.findTarget().getTestPolicyCmptLinks(parameter3.getName())[0];
        assertSame(productCmpt3, link2_1.findTarget().findProductCmpt(ipsProject));

        ITestPolicyCmptLink link2_2 = link1_1.findTarget().getTestPolicyCmptLinks(parameter3.getName())[1];
        assertSame(productCmpt3, link2_2.findTarget().findProductCmpt(ipsProject));
    }

    @Ignore
    @Test
    public void testAddPcTypeLink_MixedMinCardinalities() throws CoreException {
        // TODO
    }

    @Ignore
    @Test
    public void testAddPcTypeLink_MultipleAssociations() throws CoreException {
        // TODO
    }

    @Ignore
    @Test
    public void testAddPcTypeLink_AmbiguousProductComponents_OneLink_MinCardinalityOne() throws CoreException {
        // TODO
    }

    @Ignore
    @Test
    public void testAddPcTypeLink_AmbiguousProductComponents_OneLink_MinCardinalityZero() throws CoreException {
        // TODO
    }

    @Ignore
    @Test
    public void testAddPcTypeLink_AmbiguousProductComponents_OneLink_MinCardinalityTwo() throws CoreException {
        // TODO
    }

    @Ignore
    @Test
    public void testAddPcTypeLink_AmbiguousProductComponents_TwoLinks_MinCardinalityOne() throws CoreException {
        // TODO
    }

    @Ignore
    @Test
    public void testAddPcTypeLink_AmbiguousProductComponents_ThreeLinks_MixedCardinalities() throws CoreException {
        // TODO
    }

    private IPolicyCmptTypeAssociation createAssociation(IPolicyCmptType source,
            IPolicyCmptType target,
            int minCardinality,
            int maxCardinality) {

        IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)createAssociation((IType)source,
                (IType)target, minCardinality, maxCardinality);
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setConfigured(true);
        return association;
    }

    private IProductCmptTypeAssociation createAssociation(IProductCmptType source,
            IProductCmptType target,
            int minCardinality,
            int maxCardinality) {

        IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)createAssociation((IType)source,
                (IType)target, minCardinality, maxCardinality);
        association.setAssociationType(AssociationType.AGGREGATION);
        return association;
    }

    private IAssociation createAssociation(IType source, IType target, int minCardinality, int maxCardinality) {
        IAssociation association = source.newAssociation();
        association.setTarget(target.getQualifiedName());
        association.setTargetRoleSingular(target.getName());
        association.setTargetRolePlural(target.getName() + 's');
        association.setMinCardinality(minCardinality);
        association.setMinCardinality(maxCardinality);
        return association;
    }

    private ITestPolicyCmptTypeParameter createTestParameter(ITestCaseType testCaseType,
            IPolicyCmptType policyCmptType,
            int minCardinality,
            int maxCardinality) {

        ITestPolicyCmptTypeParameter parameter = testCaseType.newInputTestPolicyCmptTypeParameter();
        configureTestParameter(parameter, policyCmptType, minCardinality, maxCardinality);
        return parameter;
    }

    private ITestPolicyCmptTypeParameter createTestParameter(ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter,
            IPolicyCmptType policyCmptType,
            IAssociation association,
            int minCardinality,
            int maxCardinality) {

        ITestPolicyCmptTypeParameter parameter = testPolicyCmptTypeParameter.newTestPolicyCmptTypeParamChild();
        configureTestParameter(parameter, policyCmptType, minCardinality, maxCardinality);
        parameter.setAssociation(association.getName());
        return parameter;
    }

    private void configureTestParameter(ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter,
            IPolicyCmptType policyCmptType,
            int minCardinality,
            int maxCardinality) {

        testPolicyCmptTypeParameter.setName(policyCmptType.getName());
        testPolicyCmptTypeParameter.setPolicyCmptType(policyCmptType.getQualifiedName());
        testPolicyCmptTypeParameter.setRequiresProductCmpt(true);
        testPolicyCmptTypeParameter.setMinInstances(minCardinality);
        testPolicyCmptTypeParameter.setMaxInstances(maxCardinality);
    }

    private IProductCmptLink createProductCmptLink(IProductCmpt source,
            IProductCmpt target,
            IProductCmptTypeAssociation association) {

        IProductCmptLink link = source.getFirstGeneration().newLink(association);
        link.setTarget(target.getQualifiedName());
        link.setMinCardinality(1);
        link.setMaxCardinality(1);
        return link;
    }

}
