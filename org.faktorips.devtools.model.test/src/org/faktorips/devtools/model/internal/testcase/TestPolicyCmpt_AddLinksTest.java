/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.internal.testcase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.GregorianCalendar;
import java.util.UUID;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAssociation;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.faktorips.devtools.model.testcase.ITestCase;
import org.faktorips.devtools.model.testcase.ITestPolicyCmpt;
import org.faktorips.devtools.model.testcase.ITestPolicyCmptLink;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.type.AssociationType;
import org.faktorips.devtools.model.type.IAssociation;
import org.faktorips.devtools.model.type.IType;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for
 * {@link TestPolicyCmpt#addTestPcTypeLink(ITestPolicyCmptTypeParameter, String, String, String, boolean)}
 * and {@link ITestPolicyCmpt#addRequiredLinks(IIpsProject)}
 */
public class TestPolicyCmpt_AddLinksTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
    }

    /**
     * <strong>Scenario:</strong><br>
     * <ul>
     * <li>Three policy types are linked together as following:
     * <ul>
     * <li>1 &rarr; 2 &nbsp;(1..1)
     * <li>2 &rarr; 3 &nbsp;(1..1)
     * </ul>
     * <li>Product components are non-ambiguous
     * </ul>
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * One link to the third policy component must be added. This is because the associations are
     * not optional.
     */
    @Test
    public void testAddPcTypeLink_NonAmbiguousProductComponents_MinCardinalityOne() {
        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 1, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3 = createAssociation(policyType2, policyType3, 1, 1);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(productType1, productType2, 1, 1, true);
        IProductCmptTypeAssociation product2ToProduct3 = createAssociation(productType2, productType3, 1, 1, true);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter parameter1 = createTestParameter(testCaseType, policyType1, 1, 1);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(parameter1, policyType2, policy1ToPolicy2, 1, 1);
        ITestPolicyCmptTypeParameter parameter3 = createTestParameter(parameter2, policyType3, policy2ToPolicy3, 1, 1);

        // Create product components
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");
        IProductCmpt productCmpt3 = newProductCmpt(productType3, "Product3");

        // Create product links
        createProductCmptLink(productCmpt1, productCmpt2, product1ToProduct2, 1, 1);
        createProductCmptLink(productCmpt2, productCmpt3, product2ToProduct3, 1, 1);

        // Create test case
        ITestPolicyCmpt rootTestPolicyCmpt = createTestCase(testCaseType, policyType1, productCmpt1);

        // Execute
        rootTestPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2.getQualifiedName(), null, null, true);

        // Verify
        ITestPolicyCmpt child1 = rootTestPolicyCmpt.getTestPolicyCmptLinks(parameter2.getName())[0].findTarget();
        ITestPolicyCmpt child2 = child1.getTestPolicyCmptLinks(parameter3.getName())[0].findTarget();
        assertSame(productCmpt3, child2.findProductCmpt(ipsProject));
    }

    /**
     * <strong>Scenario:</strong><br>
     * <ul>
     * <li>Three policy types are linked together as following:
     * <ul>
     * <li>1 &rarr; 2 &nbsp;(0..1)
     * <li>2 &rarr; 3 &nbsp;(0..1)
     * </ul>
     * <li>Product components are non-ambiguous
     * </ul>
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * No link must be added because the associations are all optional.
     */
    @Test
    public void testAddPcTypeLink_NonAmbiguousProductComponents_MinCardinalityZero() {
        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 0, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3 = createAssociation(policyType2, policyType3, 0, 1);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(productType1, productType2, 0, 1, true);
        IProductCmptTypeAssociation product2ToProduct3 = createAssociation(productType2, productType3, 0, 1, true);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter parameter1 = createTestParameter(testCaseType, policyType1, 0, 1);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(parameter1, policyType2, policy1ToPolicy2, 0, 1);
        ITestPolicyCmptTypeParameter parameter3 = createTestParameter(parameter2, policyType3, policy2ToPolicy3, 0, 1);

        // Create product components
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");
        IProductCmpt productCmpt3 = newProductCmpt(productType3, "Product3");

        // Create product links
        createProductCmptLink(productCmpt1, productCmpt2, product1ToProduct2, 0, 1);
        createProductCmptLink(productCmpt2, productCmpt3, product2ToProduct3, 0, 1);

        // Create test case
        ITestPolicyCmpt rootTestPolicyCmpt = createTestCase(testCaseType, policyType1, productCmpt1);

        // Execute
        rootTestPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2.getQualifiedName(), null, null, true);

        // Verify
        ITestPolicyCmptLink link1 = rootTestPolicyCmpt.getTestPolicyCmptLinks(parameter2.getName())[0];
        assertEquals(0, link1.findTarget().getTestPolicyCmptLinks(parameter3.getName()).length);
    }

    /**
     * <strong>Scenario:</strong><br>
     * <ul>
     * <li>Three policy types are linked together as following:
     * <ul>
     * <li>1 &rarr; 2 &nbsp;(2..3)
     * <li>2 &rarr; 3 &nbsp;(2..3)
     * </ul>
     * <li>Product components are non-ambiguous
     * </ul>
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Two links to the third policy component must be added. This is because the associations
     * require at least two policy components.
     */
    @Test
    public void testAddPcTypeLink_NonAmbiguousProductComponents_MinCardinalityTwo() {
        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 2, 3);
        IPolicyCmptTypeAssociation policy2ToPolicy3 = createAssociation(policyType2, policyType3, 2, 3);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(productType1, productType2, 2, 3, true);
        IProductCmptTypeAssociation product2ToProduct3 = createAssociation(productType2, productType3, 2, 3, true);

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
        createProductCmptLink(productCmpt1, productCmpt2, product1ToProduct2, 1, 1);
        createProductCmptLink(productCmpt2, productCmpt3, product2ToProduct3, 1, 1);

        // Create test case
        ITestPolicyCmpt rootTestPolicyCmpt = createTestCase(testCaseType, policyType1, productCmpt1);

        // Execute
        rootTestPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2.getQualifiedName(), null, null, true);

        // Verify
        ITestPolicyCmptLink link1_1 = rootTestPolicyCmpt.getTestPolicyCmptLinks(parameter2.getName())[0];
        ITestPolicyCmptLink link2_1 = link1_1.findTarget().getTestPolicyCmptLinks(parameter3.getName())[0];
        ITestPolicyCmptLink link2_2 = link1_1.findTarget().getTestPolicyCmptLinks(parameter3.getName())[1];
        assertSame(productCmpt2, link1_1.findTarget().findProductCmpt(ipsProject));
        assertSame(productCmpt3, link2_1.findTarget().findProductCmpt(ipsProject));
        assertSame(productCmpt3, link2_2.findTarget().findProductCmpt(ipsProject));
    }

    /**
     * <strong>Scenario:</strong><br>
     * <ul>
     * <li>Four policy types are linked together as following:
     * <ul>
     * <li>1 &rarr; 2 &nbsp;(1..1)
     * <li>2 &rarr; 3 &nbsp;(3..3)
     * <li>3 &rarr; 4 &nbsp;(0..1)
     * </ul>
     * <li>Product components are non-ambiguous
     * </ul>
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Three links to the third policy component must be added. This is because the second
     * association requires at least three policy components. No link to the fourth policy component
     * must be added, because it is optional.
     */
    @Test
    public void testAddPcTypeLink_NonAmbiguousProductComponents_MixedMinCardinalities() {
        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IPolicyCmptType policyType4 = newPolicyAndProductCmptType(ipsProject, "PolicyType4", "ProductType4");
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);
        IProductCmptType productType4 = policyType4.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 1, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3 = createAssociation(policyType2, policyType3, 3, 3);
        IPolicyCmptTypeAssociation policy3ToPolicy4 = createAssociation(policyType3, policyType4, 0, 1);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(productType1, productType2, 1, 1, true);
        IProductCmptTypeAssociation product2ToProduct3 = createAssociation(productType2, productType3, 2, 3, true);
        IProductCmptTypeAssociation product3ToProduct4 = createAssociation(productType3, productType4, 0, 1, true);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter parameter1 = createTestParameter(testCaseType, policyType1, 1, 1);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(parameter1, policyType2, policy1ToPolicy2, 1, 1);
        ITestPolicyCmptTypeParameter parameter3 = createTestParameter(parameter2, policyType3, policy2ToPolicy3, 3, 3);
        ITestPolicyCmptTypeParameter parameter4 = createTestParameter(parameter3, policyType4, policy3ToPolicy4, 0, 1);

        // Create product components
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");
        IProductCmpt productCmpt3 = newProductCmpt(productType3, "Product3");
        IProductCmpt productCmpt4 = newProductCmpt(productType4, "Product4");

        // Create product links
        createProductCmptLink(productCmpt1, productCmpt2, product1ToProduct2, 1, 1);
        createProductCmptLink(productCmpt2, productCmpt3, product2ToProduct3, 1, 1);
        createProductCmptLink(productCmpt3, productCmpt4, product3ToProduct4, 0, 1);

        // Create test case
        ITestPolicyCmpt rootTestPolicyCmpt = createTestCase(testCaseType, policyType1, productCmpt1);

        // Execute
        rootTestPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2.getQualifiedName(), null, null, true);

        // Verify
        ITestPolicyCmptLink link1_1 = rootTestPolicyCmpt.getTestPolicyCmptLinks(parameter2.getName())[0];
        ITestPolicyCmptLink link2_1 = link1_1.findTarget().getTestPolicyCmptLinks(parameter3.getName())[0];
        ITestPolicyCmptLink link2_2 = link1_1.findTarget().getTestPolicyCmptLinks(parameter3.getName())[1];
        ITestPolicyCmptLink link2_3 = link1_1.findTarget().getTestPolicyCmptLinks(parameter3.getName())[2];
        assertEquals(0, link2_1.findTarget().getTestPolicyCmptLinks(parameter4.getName()).length);
        assertEquals(0, link2_2.findTarget().getTestPolicyCmptLinks(parameter4.getName()).length);
        assertEquals(0, link2_3.findTarget().getTestPolicyCmptLinks(parameter4.getName()).length);
    }

    /**
     * <strong>Scenario:</strong><br>
     * <ul>
     * <li>Multiple policy component types are linked together as following:
     * <ul>
     * <li>1 &rarr; 2 &nbsp;(1..1)
     * <li>2 &rarr; 3_1 &nbsp;(1..1)
     * <li>2 &rarr; 3_2 &nbsp;(1..1)
     * <li>2 &rarr; 3_3 &nbsp;(1..1)
     * </ul>
     * <li>Product components are non-ambiguous
     * </ul>
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Three links must be added originating from policy component 2 to each policy component on the
     * third level.
     */
    @Test
    public void testAddPcTypeLink_NonAmbiguousProductComponents_MultipleAssociations() {
        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IPolicyCmptType policyType3_1 = newPolicyAndProductCmptType(ipsProject, "PolicyType3_1", "ProductType3_1");
        IPolicyCmptType policyType3_2 = newPolicyAndProductCmptType(ipsProject, "PolicyType3_2", "ProductType3_2");
        IPolicyCmptType policyType3_3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3_3", "ProductType3_3");
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IProductCmptType productType3_1 = policyType3_1.findProductCmptType(ipsProject);
        IProductCmptType productType3_2 = policyType3_2.findProductCmptType(ipsProject);
        IProductCmptType productType3_3 = policyType3_3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 1, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3_1 = createAssociation(policyType2, policyType3_1, 1, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3_2 = createAssociation(policyType2, policyType3_2, 1, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3_3 = createAssociation(policyType2, policyType3_3, 1, 1);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(productType1, productType2, 1, 1, true);
        IProductCmptTypeAssociation product2ToProduct3_1 = createAssociation(productType2, productType3_1, 1, 1, true);
        IProductCmptTypeAssociation product2ToProduct3_2 = createAssociation(productType2, productType3_2, 1, 1, true);
        IProductCmptTypeAssociation product2ToProduct3_3 = createAssociation(productType2, productType3_3, 1, 1, true);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter parameter1 = createTestParameter(testCaseType, policyType1, 1, 1);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(parameter1, policyType2, policy1ToPolicy2, 1, 1);
        ITestPolicyCmptTypeParameter parameter3_1 = createTestParameter(parameter2, policyType3_1, policy2ToPolicy3_1,
                1, 1);
        ITestPolicyCmptTypeParameter parameter3_2 = createTestParameter(parameter2, policyType3_2, policy2ToPolicy3_2,
                1, 1);
        ITestPolicyCmptTypeParameter parameter3_3 = createTestParameter(parameter2, policyType3_3, policy2ToPolicy3_3,
                1, 1);

        // Create product components
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");
        IProductCmpt productCmpt3_1 = newProductCmpt(productType3_1, "Product3_1");
        IProductCmpt productCmpt3_2 = newProductCmpt(productType3_2, "Product3_2");
        IProductCmpt productCmpt3_3 = newProductCmpt(productType3_3, "Product3_3");

        // Create product links
        createProductCmptLink(productCmpt1, productCmpt2, product1ToProduct2, 1, 1);
        createProductCmptLink(productCmpt2, productCmpt3_1, product2ToProduct3_1, 1, 1);
        createProductCmptLink(productCmpt2, productCmpt3_2, product2ToProduct3_2, 1, 1);
        createProductCmptLink(productCmpt2, productCmpt3_3, product2ToProduct3_3, 1, 1);

        // Create test case
        ITestPolicyCmpt rootTestPolicyCmpt = createTestCase(testCaseType, policyType1, productCmpt1);

        // Execute
        rootTestPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2.getQualifiedName(), null, null, true);

        // Verify
        ITestPolicyCmpt child1 = rootTestPolicyCmpt.getTestPolicyCmptLink(policyType2.getQualifiedName()).findTarget();
        ITestPolicyCmpt child2_1 = child1.getTestPolicyCmptLinks(parameter3_1.getName())[0].findTarget();
        ITestPolicyCmpt child2_2 = child1.getTestPolicyCmptLinks(parameter3_2.getName())[0].findTarget();
        ITestPolicyCmpt child2_3 = child1.getTestPolicyCmptLinks(parameter3_3.getName())[0].findTarget();
        assertSame(productCmpt3_1, child2_1.findProductCmpt(ipsProject));
        assertSame(productCmpt3_2, child2_2.findProductCmpt(ipsProject));
        assertSame(productCmpt3_3, child2_3.findProductCmpt(ipsProject));
    }

    /**
     * <strong>Scenario:</strong><br>
     * <ul>
     * <li>Multiple policy component types are linked together as following:
     * <ul>
     * <li>1 &rarr; 2 &nbsp;(1..1)
     * <li>2 &rarr; 3 &nbsp;(1..1)
     * </ul>
     * <li>Product components 3_1 and 3_2 come into consideration for policy type 3. However, only
     * product component 3_2 is actually linked with product component 2.
     * </ul>
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * One link must be added originating from policy component 2. The correct product component 3_2
     * must be determined by analyzing the available product component links.
     */
    @Test
    public void testAddPcTypeLink_AmbiguousProductComponents_OneLink_MinCardinalityOne() {
        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 1, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3 = createAssociation(policyType2, policyType3, 1, 1);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(productType1, productType2, 1, 1, true);
        IProductCmptTypeAssociation product2ToProduct3 = createAssociation(productType2, productType3, 1, 1, true);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter parameter1 = createTestParameter(testCaseType, policyType1, 1, 1);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(parameter1, policyType2, policy1ToPolicy2, 1, 1);
        ITestPolicyCmptTypeParameter parameter3 = createTestParameter(parameter2, policyType3, policy2ToPolicy3, 1, 1);

        // Create product components
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");
        newProductCmpt(productType3, "Product3_1");
        IProductCmpt productCmpt3_2 = newProductCmpt(productType3, "Product3_2");

        // Create product links
        createProductCmptLink(productCmpt1, productCmpt2, product1ToProduct2, 1, 1);
        createProductCmptLink(productCmpt2, productCmpt3_2, product2ToProduct3, 1, 1);

        // Create test case
        ITestPolicyCmpt rootTestPolicyCmpt = createTestCase(testCaseType, policyType1, productCmpt1);

        // Execute
        rootTestPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2.getQualifiedName(), null, null, true);

        // Verify
        ITestPolicyCmpt child1 = rootTestPolicyCmpt.getTestPolicyCmptLinks(parameter2.getName())[0].findTarget();
        ITestPolicyCmpt child2 = child1.getTestPolicyCmptLinks(parameter3.getName())[0].findTarget();
        assertSame(productCmpt3_2, child2.findProductCmpt(ipsProject));
    }

    /**
     * <strong>Scenario:</strong><br>
     * <ul>
     * <li>Multiple policy component types are linked together as following:
     * <ul>
     * <li>1 &rarr; 2 &nbsp;(1..1)
     * <li>2 &rarr; 3 &nbsp;(1..1)
     * </ul>
     * <li>Product components 3_1 and 3_2 come into consideration for policy type 3. However, only
     * product component 3_1 is actually linked with product component 2. Furthermore, this link
     * defines a minimum cardinality of 0, so the link is optional.
     * </ul>
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * No link must be added originating from policy component 2. This is because the only available
     * link from product component 2 is optional.
     */
    @Test
    public void testAddPcTypeLink_AmbiguousProductComponents_OneLink_MinCardinalityZero() {
        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 1, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3 = createAssociation(policyType2, policyType3, 1, 1);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(productType1, productType2, 1, 1, true);
        IProductCmptTypeAssociation product2ToProduct3 = createAssociation(productType2, productType3, 1, 1, true);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter parameter1 = createTestParameter(testCaseType, policyType1, 1, 1);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(parameter1, policyType2, policy1ToPolicy2, 1, 1);
        createTestParameter(parameter2, policyType3, policy2ToPolicy3, 1, 1);

        // Create product components
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");
        IProductCmpt productCmpt3_1 = newProductCmpt(productType3, "Product3_1");
        newProductCmpt(productType3, "Product3_2");

        // Create product links
        createProductCmptLink(productCmpt1, productCmpt2, product1ToProduct2, 1, 1);
        createProductCmptLink(productCmpt2, productCmpt3_1, product2ToProduct3, 0, 1);

        // Create test case
        ITestPolicyCmpt rootTestPolicyCmpt = createTestCase(testCaseType, policyType1, productCmpt1);

        // Execute
        rootTestPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2.getQualifiedName(), null, null, true);

        // Verify
        ITestPolicyCmpt child1 = rootTestPolicyCmpt.getTestPolicyCmptLinks(parameter2.getName())[0].findTarget();
        assertNull(child1.getTestPolicyCmptLink(policyType3.getQualifiedName()));
    }

    /**
     * <strong>Scenario:</strong><br>
     * <ul>
     * <li>Multiple policy component types are linked together as following:
     * <ul>
     * <li>1 &rarr; 2 &nbsp;(1..1)
     * <li>2 &rarr; 3 &nbsp;(1..1)
     * </ul>
     * <li>Product components 3_1 and 3_2 come into consideration for policy type 3. However, only
     * product component 3_1 is actually linked with product component 2. Furthermore, this link
     * defines a minimum cardinality of 2.
     * </ul>
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Two links must be added originating from policy component 2. The correct product component is
     * 3_1 for both links.
     */
    @Test
    public void testAddPcTypeLink_AmbiguousProductComponents_OneLink_MinCardinalityTwo() {
        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 1, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3 = createAssociation(policyType2, policyType3, 1, 1);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(productType1, productType2, 1, 1, true);
        IProductCmptTypeAssociation product2ToProduct3 = createAssociation(productType2, productType3, 1, 1, true);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter parameter1 = createTestParameter(testCaseType, policyType1, 1, 1);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(parameter1, policyType2, policy1ToPolicy2, 1, 1);
        ITestPolicyCmptTypeParameter parameter3 = createTestParameter(parameter2, policyType3, policy2ToPolicy3, 1, 1);

        // Create product components
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");
        IProductCmpt productCmpt3_1 = newProductCmpt(productType3, "Product3_1");
        newProductCmpt(productType3, "Product3_2");

        // Create product links
        createProductCmptLink(productCmpt1, productCmpt2, product1ToProduct2, 1, 1);
        createProductCmptLink(productCmpt2, productCmpt3_1, product2ToProduct3, 2, 2);

        // Create test case
        ITestPolicyCmpt rootTestPolicyCmpt = createTestCase(testCaseType, policyType1, productCmpt1);

        // Execute
        rootTestPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2.getQualifiedName(), null, null, true);

        // Verify
        ITestPolicyCmpt child1 = rootTestPolicyCmpt.getTestPolicyCmptLinks(parameter2.getName())[0].findTarget();
        ITestPolicyCmpt child2_1 = child1.getTestPolicyCmptLinks(parameter3.getName())[0].findTarget();
        ITestPolicyCmpt child2_2 = child1.getTestPolicyCmptLinks(parameter3.getName())[1].findTarget();
        assertSame(productCmpt3_1, child2_1.findProductCmpt(ipsProject));
        assertSame(productCmpt3_1, child2_2.findProductCmpt(ipsProject));
    }

    /**
     * <strong>Scenario:</strong><br>
     * <ul>
     * <li>Multiple policy component types are linked together as following:
     * <ul>
     * <li>1 &rarr; 2 &nbsp;(1..1)
     * <li>2 &rarr; 3 &nbsp;(1..1)
     * </ul>
     * <li>Product components 3_1 and 3_2 come into consideration for policy type 3. Both product
     * components are linked with product component 2. Furthermore, each link defines a minimum
     * cardinality of 1 so both product components are mandatory.
     * </ul>
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Two links must be added originating from policy component 2, one with product component 3_1,
     * the other one with product component 3_2.
     */
    @Test
    public void testAddPcTypeLink_AmbiguousProductComponents_TwoLinks_MinCardinalityOne() {
        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 1, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3 = createAssociation(policyType2, policyType3, 1, 1);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(productType1, productType2, 1, 1, true);
        IProductCmptTypeAssociation product2ToProduct3 = createAssociation(productType2, productType3, 1, 1, true);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter parameter1 = createTestParameter(testCaseType, policyType1, 1, 1);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(parameter1, policyType2, policy1ToPolicy2, 1, 1);
        ITestPolicyCmptTypeParameter parameter3 = createTestParameter(parameter2, policyType3, policy2ToPolicy3, 1, 1);

        // Create product components
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");
        IProductCmpt productCmpt3_1 = newProductCmpt(productType3, "Product3_1");
        IProductCmpt productCmpt3_2 = newProductCmpt(productType3, "Product3_2");

        // Create product links
        createProductCmptLink(productCmpt1, productCmpt2, product1ToProduct2, 1, 1);
        createProductCmptLink(productCmpt2, productCmpt3_1, product2ToProduct3, 1, 1);
        createProductCmptLink(productCmpt2, productCmpt3_2, product2ToProduct3, 1, 1);

        // Create test case
        ITestPolicyCmpt rootTestPolicyCmpt = createTestCase(testCaseType, policyType1, productCmpt1);

        // Execute
        rootTestPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2.getQualifiedName(), null, null, true);

        // Verify
        ITestPolicyCmpt child1 = rootTestPolicyCmpt.getTestPolicyCmptLinks(parameter2.getName())[0].findTarget();
        ITestPolicyCmpt child2_1 = child1.getTestPolicyCmptLinks(parameter3.getName())[0].findTarget();
        ITestPolicyCmpt child2_2 = child1.getTestPolicyCmptLinks(parameter3.getName())[1].findTarget();
        assertSame(productCmpt3_1, child2_1.findProductCmpt(ipsProject));
        assertSame(productCmpt3_2, child2_2.findProductCmpt(ipsProject));
    }

    /**
     * <strong>Scenario:</strong><br>
     * <ul>
     * <li>Multiple policy component types are linked together as following:
     * <ul>
     * <li>1 &rarr; 2 &nbsp;(1..1)
     * <li>2 &rarr; 3 &nbsp;(1..1)
     * </ul>
     * <li>Following product components exist for policy type 3, and are linked with product
     * component 2:
     * <ul>
     * <li>3_1 &nbsp;(0..1)
     * <li>3_2 &nbsp;(2..2)
     * <li>3_3 &nbsp;(1..1)
     * </ul>
     * </ul>
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Three links must be added originating from policy component 2, two with product component 3_2
     * and one with product component 3_3.
     */
    @Test
    public void testAddPcTypeLink_AmbiguousProductComponents_ThreeLinks_MixedCardinalities() {
        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 1, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3 = createAssociation(policyType2, policyType3, 1, 1);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(productType1, productType2, 1, 1, true);
        IProductCmptTypeAssociation product2ToProduct3 = createAssociation(productType2, productType3, 1, 1, true);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter parameter1 = createTestParameter(testCaseType, policyType1, 1, 1);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(parameter1, policyType2, policy1ToPolicy2, 1, 1);
        ITestPolicyCmptTypeParameter parameter3 = createTestParameter(parameter2, policyType3, policy2ToPolicy3, 1, 1);

        // Create product components
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");
        IProductCmpt productCmpt3_1 = newProductCmpt(productType3, "Product3_1");
        IProductCmpt productCmpt3_2 = newProductCmpt(productType3, "Product3_2");
        IProductCmpt productCmpt3_3 = newProductCmpt(productType3, "Product3_3");

        // Create product links
        createProductCmptLink(productCmpt1, productCmpt2, product1ToProduct2, 1, 1);
        createProductCmptLink(productCmpt2, productCmpt3_1, product2ToProduct3, 0, 1);
        createProductCmptLink(productCmpt2, productCmpt3_2, product2ToProduct3, 2, 2);
        createProductCmptLink(productCmpt2, productCmpt3_3, product2ToProduct3, 1, 1);

        // Create test case
        ITestPolicyCmpt rootTestPolicyCmpt = createTestCase(testCaseType, policyType1, productCmpt1);

        // Execute
        rootTestPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2.getQualifiedName(), null, null, true);

        // Verify
        ITestPolicyCmpt child1 = rootTestPolicyCmpt.getTestPolicyCmptLinks(parameter2.getName())[0].findTarget();
        ITestPolicyCmpt child2_1 = child1.getTestPolicyCmptLinks(parameter3.getName())[0].findTarget();
        ITestPolicyCmpt child2_2 = child1.getTestPolicyCmptLinks(parameter3.getName())[1].findTarget();
        ITestPolicyCmpt child2_3 = child1.getTestPolicyCmptLinks(parameter3.getName())[2].findTarget();
        assertSame(productCmpt3_2, child2_1.findProductCmpt(ipsProject));
        assertSame(productCmpt3_2, child2_2.findProductCmpt(ipsProject));
        assertSame(productCmpt3_3, child2_3.findProductCmpt(ipsProject));
    }

    /**
     * <strong>Scenario:</strong><br>
     * <ul>
     * <li>Multiple policy component types are linked together as following:
     * <ul>
     * <li>1 &rarr; 2 &nbsp;(1..1)
     * <li>2 &rarr; 3 &nbsp;(0..1)
     * </ul>
     * <li>Product components are non-ambiguous. However, the cardinality from product component 2
     * to product component 3 is changed by the product component link to mandatory.
     * </ul>
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Because the minimum instances in the test parameter is 0, we evaluate the product component
     * link. The product component link sets the cardinality to mandatory, which is why a test
     * policy component link must be added from policy 2 to policy 3.
     */
    @Test
    public void testAddPcTypeLink_NonAmbiguousProductComponents_LinkOptionalInTestParameterButMandatoryInProductComponent()
            {

        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 1, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3 = createAssociation(policyType2, policyType3, 1, 1);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(productType1, productType2, 0, 1, true);
        IProductCmptTypeAssociation product2ToProduct3 = createAssociation(productType2, productType3, 0, 1, true);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter parameter1 = createTestParameter(testCaseType, policyType1, 1, 1);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(parameter1, policyType2, policy1ToPolicy2, 1, 1);
        ITestPolicyCmptTypeParameter parameter3 = createTestParameter(parameter2, policyType3, policy2ToPolicy3, 0, 1);

        // Create product components
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");
        IProductCmpt productCmpt3 = newProductCmpt(productType3, "Product3");

        // Create product links
        createProductCmptLink(productCmpt1, productCmpt2, product1ToProduct2, 1, 1);
        createProductCmptLink(productCmpt2, productCmpt3, product2ToProduct3, 1, 1);

        // Create test case
        ITestPolicyCmpt rootTestPolicyCmpt = createTestCase(testCaseType, policyType1, productCmpt1);

        // Execute
        rootTestPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2.getQualifiedName(), null, null, true);

        // Verify
        ITestPolicyCmpt child1 = rootTestPolicyCmpt.getTestPolicyCmptLinks(parameter2.getName())[0].findTarget();
        ITestPolicyCmpt child2 = child1.getTestPolicyCmptLinks(parameter3.getName())[0].findTarget();
        assertSame(productCmpt3, child2.findProductCmpt(ipsProject));
    }

    /**
     * <strong>Scenario:</strong><br>
     * <ul>
     * <li>Multiple policy component types are linked together as following:
     * <ul>
     * <li>1 &rarr; 2 &nbsp;(1..1)
     * <li>2 &rarr; 3 &nbsp;(1..1)
     * </ul>
     * <li>There is a test policy component for policy component type 1, but the product component
     * that is assigned to it does not exist
     * </ul>
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * When adding a test policy component link to the test policy component, a new test policy
     * component for policy component type 2 should be created. The link between the two test policy
     * components should be established. However, no test policy component link should be added from
     * test policy component 2 to policy component type 3, because no existing product component was
     * assigned to test policy component 1.
     */
    @Test
    public void testAddPcTypeLink_RecursiveAddNotPossibleIfNoProductCmptIsAssigned() {
        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 1, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3 = createAssociation(policyType2, policyType3, 1, 1);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter parameter1 = createTestParameter(testCaseType, policyType1, 1, 1);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(parameter1, policyType2, policy1ToPolicy2, 1, 1);
        createTestParameter(parameter2, policyType3, policy2ToPolicy3, 1, 1);

        // Create test case
        ITestCase testCase = newTestCase(testCaseType, "TestCase");
        ITestPolicyCmpt rootTestPolicyCmpt = ((TestCase)testCase).addRootTestPolicyCmpt(parameter1);
        rootTestPolicyCmpt.setPolicyCmptType(policyType1.getQualifiedName());
        rootTestPolicyCmpt.setProductCmpt("someNonExistentProductCmpt");

        // Execute
        ITestPolicyCmptLink newLink = rootTestPolicyCmpt.addTestPcTypeLink(parameter2, null,
                policyType2.getQualifiedName(), null, true);

        // Verify
        assertEquals(0, newLink.findTarget().getTestPolicyCmptLinks().length);
    }

    @Test
    public void testAddPcTypeLink_OnlyWholeContentChangedEvent() {
        // Create model types
        IPolicyCmptType rootPolicyType = newPolicyAndProductCmptType(ipsProject, "RootPolicyType", "RootProductType");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IProductCmptType rootProductType = rootPolicyType.findProductCmptType(ipsProject);
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(rootPolicyType, policyType2, 1, 1);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(rootProductType, productType2, 1, 1, true);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter rootParameter = createTestParameter(testCaseType, rootPolicyType, 1, 1);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(rootParameter, policyType2, policy1ToPolicy2, 1,
                1);

        // Create product components
        IProductCmpt rootProductCmpt = newProductCmpt(rootProductType, "RootProduct");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");

        // Create product links
        createProductCmptLink(rootProductCmpt, productCmpt2, product1ToProduct2, 1, 1);

        // Create test case
        ITestPolicyCmpt rootTestPolicyCmpt = createTestCase(testCaseType, rootPolicyType, rootProductCmpt);
        rootTestPolicyCmpt.setProductCmpt(rootProductCmpt.getQualifiedName());

        // Execute
        resetNumberContentChangeEvents();
        rootTestPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2.getQualifiedName(), null, null, true);

        // Verify
        assertSingleContentChangeEvent();
        assertWholeContentChangedEvent(rootTestPolicyCmpt.getIpsSrcFile());
    }

    /**
     * <strong>Scenario:</strong><br>
     * <ul>
     * <li>Policy types are linked together as following:
     * <ul>
     * <li>Root &rarr; 1 &nbsp;(1..1)
     * <li>Root &rarr; 2 &nbsp;(0..1)
     * <li>Root &rarr; 3 &nbsp;(2..2)
     * </ul>
     * <li>Product components can be non-ambiguously assigned to each association
     * </ul>
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * When adding the required links to the root policy , following links should be added:
     * <ul>
     * <li>1 link from root to policy 1
     * <li>2 links from root to policy 3
     * </ul>
     * <p>
     * There is no link to policy 2 because the corresponding association is optional.
     */
    @Test
    public void testAddRequiredLinks() {
        // Create model types
        IPolicyCmptType rootPolicyType = newPolicyAndProductCmptType(ipsProject, "RootPolicyType", "RootProductType");
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IProductCmptType rootProductType = rootPolicyType.findProductCmptType(ipsProject);
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation rootPolicyToPolicy1 = createAssociation(rootPolicyType, policyType1, 1, 1);
        IPolicyCmptTypeAssociation rootPolicyToPolicy2 = createAssociation(rootPolicyType, policyType2, 0, 1);
        IPolicyCmptTypeAssociation rootPolicyToPolicy3 = createAssociation(rootPolicyType, policyType3, 2, 2);
        IProductCmptTypeAssociation rootProductToProduct1 = createAssociation(rootProductType, productType1, 1, 1,
                true);
        IProductCmptTypeAssociation rootProductToProduct2 = createAssociation(rootProductType, productType2, 0, 1,
                true);
        IProductCmptTypeAssociation rootProductToProduct3 = createAssociation(rootProductType, productType3, 2, 2,
                true);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter rootParameter = createTestParameter(testCaseType, rootPolicyType, 1, 1);
        ITestPolicyCmptTypeParameter rootToPolicy1Parameter = createTestParameter(rootParameter, policyType1,
                rootPolicyToPolicy1, 1, 1);
        ITestPolicyCmptTypeParameter rootToPolicy2Parameter = createTestParameter(rootParameter, policyType2,
                rootPolicyToPolicy2, 0, 1);
        ITestPolicyCmptTypeParameter rootToPolicy3Parameter = createTestParameter(rootParameter, policyType3,
                rootPolicyToPolicy3, 2, 2);

        // Create product components
        IProductCmpt rootProductCmpt = newProductCmpt(rootProductType, "RootProduct");
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");
        IProductCmpt productCmpt3 = newProductCmpt(productType3, "Product3");

        // Create product links
        createProductCmptLink(rootProductCmpt, productCmpt1, rootProductToProduct1, 1, 1);
        createProductCmptLink(rootProductCmpt, productCmpt2, rootProductToProduct2, 1, 1);
        createProductCmptLink(rootProductCmpt, productCmpt3, rootProductToProduct3, 1, 1);

        // Create test case
        ITestCase testCase = newTestCase(testCaseType, "MyTestCase");
        ITestPolicyCmpt rootTestPolicyCmpt = ((TestCase)testCase).addRootTestPolicyCmpt(rootParameter);
        rootTestPolicyCmpt.setProductCmptAndNameAfterIfApplicable(rootProductCmpt.getQualifiedName());

        // Execute
        rootTestPolicyCmpt.addRequiredLinks(ipsProject);

        // Verify
        ITestPolicyCmpt child1 = rootTestPolicyCmpt.getTestPolicyCmptLinks(rootToPolicy1Parameter.getName())[0]
                .findTarget();
        ITestPolicyCmpt child2 = rootTestPolicyCmpt.getTestPolicyCmptLinks(rootToPolicy2Parameter.getName())[0]
                .findTarget();
        ITestPolicyCmpt child3 = rootTestPolicyCmpt.getTestPolicyCmptLinks(rootToPolicy3Parameter.getName())[0]
                .findTarget();
        assertSame(productCmpt1, child1.findProductCmpt(ipsProject));
        assertSame(productCmpt2, child2.findProductCmpt(ipsProject));
        assertSame(productCmpt3, child3.findProductCmpt(ipsProject));
    }

    /**
     * <strong>Scenario:</strong><br>
     * <ul>
     * <li>Policy types are linked together as following:
     * <ul>
     * <li>Root &rarr; 1 &nbsp;(1..1)
     * <li>Root &rarr; 2 &nbsp;(0..1)
     * <li>Root &rarr; 3 &nbsp;(2..2)
     * </ul>
     * <li>Product components can be non-ambiguously assigned to each association
     * </ul>
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * When adding the required links to the root policy , following links should be added:
     * <ul>
     * <li>1 link from root to policy 1
     * <li>2 links from root to policy 3
     * </ul>
     * <p>
     * There is no link to policy 2 because the corresponding association is optional.
     */
    @Test
    public void testAddRequiredLinks_staticLinks() {
        // Create model types
        IPolicyCmptType rootPolicyType = newPolicyAndProductCmptType(ipsProject, "RootPolicyType", "RootProductType");
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IProductCmptType rootProductType = rootPolicyType.findProductCmptType(ipsProject);
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation rootPolicyToPolicy1 = createAssociation(rootPolicyType, policyType1, 1, 1);
        IPolicyCmptTypeAssociation rootPolicyToPolicy2 = createAssociation(rootPolicyType, policyType2, 0, 1);
        IPolicyCmptTypeAssociation rootPolicyToPolicy3 = createAssociation(rootPolicyType, policyType3, 2, 2);
        IProductCmptTypeAssociation rootProductToProduct1 = createAssociation(rootProductType, productType1, 1, 1,
                false);
        IProductCmptTypeAssociation rootProductToProduct2 = createAssociation(rootProductType, productType2, 0, 1,
                false);
        IProductCmptTypeAssociation rootProductToProduct3 = createAssociation(rootProductType, productType3, 2, 2,
                false);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter rootParameter = createTestParameter(testCaseType, rootPolicyType, 1, 1);
        ITestPolicyCmptTypeParameter rootToPolicy1Parameter = createTestParameter(rootParameter, policyType1,
                rootPolicyToPolicy1, 1, 1);
        ITestPolicyCmptTypeParameter rootToPolicy2Parameter = createTestParameter(rootParameter, policyType2,
                rootPolicyToPolicy2, 0, 1);
        ITestPolicyCmptTypeParameter rootToPolicy3Parameter = createTestParameter(rootParameter, policyType3,
                rootPolicyToPolicy3, 2, 2);

        // Create product components
        IProductCmpt rootProductCmpt = newProductCmpt(rootProductType, "RootProduct");
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmpt productCmpt2 = newProductCmpt(productType2, "Product2");
        IProductCmpt productCmpt3 = newProductCmpt(productType3, "Product3");

        // Create product links
        createProductCmptLink(rootProductCmpt, productCmpt1, rootProductToProduct1, 1, 1);
        createProductCmptLink(rootProductCmpt, productCmpt2, rootProductToProduct2, 1, 1);
        createProductCmptLink(rootProductCmpt, productCmpt3, rootProductToProduct3, 1, 1);

        // Create test case
        ITestCase testCase = newTestCase(testCaseType, "MyTestCase");
        ITestPolicyCmpt rootTestPolicyCmpt = ((TestCase)testCase).addRootTestPolicyCmpt(rootParameter);
        rootTestPolicyCmpt.setProductCmptAndNameAfterIfApplicable(rootProductCmpt.getQualifiedName());

        // Execute
        rootTestPolicyCmpt.addRequiredLinks(ipsProject);

        // Verify
        ITestPolicyCmpt child1 = rootTestPolicyCmpt.getTestPolicyCmptLinks(rootToPolicy1Parameter.getName())[0]
                .findTarget();
        ITestPolicyCmpt child2 = rootTestPolicyCmpt.getTestPolicyCmptLinks(rootToPolicy2Parameter.getName())[0]
                .findTarget();
        ITestPolicyCmpt child3 = rootTestPolicyCmpt.getTestPolicyCmptLinks(rootToPolicy3Parameter.getName())[0]
                .findTarget();
        assertSame(productCmpt1, child1.findProductCmpt(ipsProject));
        assertSame(productCmpt2, child2.findProductCmpt(ipsProject));
        assertSame(productCmpt3, child3.findProductCmpt(ipsProject));
    }

    @Test(expected = IllegalStateException.class)
    public void testAddRequiredLinks_OnlyPossibleIfProductCmptAssigned() {
        // Create model types
        IPolicyCmptType rootPolicyType = newPolicyAndProductCmptType(ipsProject, "RootPolicyType", "RootProductType");

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter rootParameter = createTestParameter(testCaseType, rootPolicyType, 1, 1);

        // Create test case
        ITestCase testCase = newTestCase(testCaseType, "MyTestCase");
        ITestPolicyCmpt rootTestPolicyCmpt = ((TestCase)testCase).addRootTestPolicyCmpt(rootParameter);

        // Execute
        rootTestPolicyCmpt.addRequiredLinks(ipsProject);
    }

    @Test
    public void testAddRequiredLinks_OnlyWholeContentChangedEvent() {
        // Create model types
        IPolicyCmptType rootPolicyType = newPolicyAndProductCmptType(ipsProject, "RootPolicyType", "RootProductType");
        IProductCmptType rootProductCmptType = rootPolicyType.findProductCmptType(ipsProject);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter rootParameter = createTestParameter(testCaseType, rootPolicyType, 1, 1);

        // Create product component
        IProductCmpt rootProductCmpt = newProductCmpt(rootProductCmptType, "RootProduct");

        // Create test case
        ITestCase testCase = newTestCase(testCaseType, "MyTestCase");
        ITestPolicyCmpt rootTestPolicyCmpt = ((TestCase)testCase).addRootTestPolicyCmpt(rootParameter);
        rootTestPolicyCmpt.setProductCmpt(rootProductCmpt.getQualifiedName());

        // Execute
        resetNumberContentChangeEvents();
        rootTestPolicyCmpt.addRequiredLinks(ipsProject);

        // Verify
        assertSingleContentChangeEvent();
        assertWholeContentChangedEvent(testCase.getIpsSrcFile());
    }

    /**
     * <strong>Scenario:</strong><br>
     * <ul>
     * <li>There are following product components and generations:
     * <ul>
     * <li>Product Component 1 - Generation 1
     * <li>Product Component 1 - Generation 2
     * <li>Product Component 1 - Generation 3
     * <li>Product Component 2 - Generation 1
     * <li>Product Component 2 - Generation 2
     * <li>Product Component 2 - Generation 3
     * <li>Product Component 3 - Generation 1
     * </ul>
     * <li>Links are established between product component 1 / generation 1 to product component 2,
     * as well as product component 2 / generation 2 to product component 3.
     * </ul>
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Links do not need to exist in all generations for automatic link creation to work. It is
     * sufficient if the link is provided in one generation.
     */
    @Test
    public void testAddRequiredLinks_SearchAllGenerationsForLink() {
        // Create model types
        IPolicyCmptType policyType1 = newPolicyAndProductCmptType(ipsProject, "PolicyType1", "ProductType1");
        IPolicyCmptType policyType2 = newPolicyAndProductCmptType(ipsProject, "PolicyType2", "ProductType2");
        IPolicyCmptType policyType3 = newPolicyAndProductCmptType(ipsProject, "PolicyType3", "ProductType3");
        IProductCmptType productType1 = policyType1.findProductCmptType(ipsProject);
        IProductCmptType productType2 = policyType2.findProductCmptType(ipsProject);
        IProductCmptType productType3 = policyType3.findProductCmptType(ipsProject);

        // Create associations
        IPolicyCmptTypeAssociation policy1ToPolicy2 = createAssociation(policyType1, policyType2, 1, 1);
        IPolicyCmptTypeAssociation policy2ToPolicy3 = createAssociation(policyType2, policyType3, 1, 1);
        IProductCmptTypeAssociation product1ToProduct2 = createAssociation(productType1, productType2, 1, 1, true);
        IProductCmptTypeAssociation product2ToProduct3 = createAssociation(productType2, productType3, 1, 1, true);

        // Create test case type
        ITestCaseType testCaseType = newTestCaseType(ipsProject, "MyTestCaseType");
        ITestPolicyCmptTypeParameter parameter1 = createTestParameter(testCaseType, policyType1, 1, 1);
        ITestPolicyCmptTypeParameter parameter2 = createTestParameter(parameter1, policyType2, policy1ToPolicy2, 1, 1);
        ITestPolicyCmptTypeParameter parameter3 = createTestParameter(parameter2, policyType3, policy2ToPolicy3, 1, 1);

        // Create product components
        IProductCmpt productCmpt1 = newProductCmpt(productType1, "Product1");
        IProductCmptGeneration generation1_1 = (IProductCmptGeneration)productCmpt1
                .newGeneration(new GregorianCalendar(3000, 1, 1));
        productCmpt1.newGeneration(new GregorianCalendar(3000, 1, 2));
        productCmpt1.newGeneration(new GregorianCalendar(3000, 1, 2));

        IProductCmpt productCmpt2_1 = newProductCmpt(productType2, "Product2_1");
        productCmpt2_1.newGeneration(new GregorianCalendar(3000, 1, 1));
        IProductCmptGeneration generation2_2 = (IProductCmptGeneration)productCmpt2_1
                .newGeneration(new GregorianCalendar(3000, 1, 2));
        productCmpt2_1.newGeneration(new GregorianCalendar(3000, 1, 3));
        newProductCmpt(productType2, "Product2_2");

        IProductCmpt productCmpt3_1 = newProductCmpt(productType3, "Product3_1");
        newProductCmpt(productType3, "Product3_2");

        // Create product links
        createProductCmptLink(generation1_1, productCmpt2_1, product1ToProduct2, 1, 1);
        createProductCmptLink(generation2_2, productCmpt3_1, product2ToProduct3, 1, 1);

        // Create test case
        ITestPolicyCmpt rootTestPolicyCmpt = createTestCase(testCaseType, policyType1, productCmpt1);

        // Execute
        rootTestPolicyCmpt.addTestPcTypeLink(parameter2, productCmpt2_1.getQualifiedName(), null, null, true);

        // Verify
        ITestPolicyCmpt child1 = rootTestPolicyCmpt.getTestPolicyCmptLinks(parameter2.getName())[0].findTarget();
        ITestPolicyCmpt child2 = child1.getTestPolicyCmptLinks(parameter3.getName())[0].findTarget();
        assertSame(productCmpt3_1, child2.findProductCmpt(ipsProject));
    }

    private IPolicyCmptTypeAssociation createAssociation(IPolicyCmptType source,
            IPolicyCmptType target,
            int minCardinality,
            int maxCardinality) {

        IPolicyCmptTypeAssociation association = (IPolicyCmptTypeAssociation)createAssociation((IType)source,
                (IType)target, minCardinality, maxCardinality);
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setConfigurable(true);
        return association;
    }

    private IProductCmptTypeAssociation createAssociation(IProductCmptType source,
            IProductCmptType target,
            int minCardinality,
            int maxCardinality,
            boolean changingOverTime) {

        IProductCmptTypeAssociation association = (IProductCmptTypeAssociation)createAssociation(source, target,
                minCardinality, maxCardinality);
        association.setAssociationType(AssociationType.AGGREGATION);
        association.setChangingOverTime(changingOverTime);
        return association;
    }

    private IAssociation createAssociation(IType source, IType target, int minCardinality, int maxCardinality) {
        IAssociation association = source.newAssociation();
        association.setTarget(target.getQualifiedName());
        association.setTargetRoleSingular(target.getName() + '_' + UUID.randomUUID());
        association.setTargetRolePlural(target.getName() + 's' + '_' + UUID.randomUUID());
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
            IProductCmptTypeAssociation association,
            int minCardinality,
            int maxCardinality) {
        IProductCmptLink link;
        if (association.isChangingOverTime()) {
            link = source.getFirstGeneration().newLink(association);
        } else {
            link = source.newLink(association);
        }
        link.setTarget(target.getQualifiedName());
        link.setMinCardinality(minCardinality);
        link.setMaxCardinality(maxCardinality);
        return link;
    }

    private IProductCmptLink createProductCmptLink(IProductCmptGeneration source,
            IProductCmpt target,
            IProductCmptTypeAssociation association,
            int minCardinality,
            int maxCardinality) {
        IProductCmptLink link;
        if (association.isChangingOverTime()) {
            link = source.newLink(association);
        } else {
            link = source.getProductCmpt().newLink(association);
        }
        link.setTarget(target.getQualifiedName());
        link.setMinCardinality(minCardinality);
        link.setMaxCardinality(maxCardinality);
        return link;
    }

    /**
     * Creates a new {@link ITestCase} and returns the root {@link ITestPolicyCmpt}.
     */
    private ITestPolicyCmpt createTestCase(ITestCaseType testCaseType,
            IPolicyCmptType rootPolicyType,
            IProductCmpt rootProductCmpt) {

        ITestCase testCase = newTestCase(testCaseType, "MyTestCase");
        ITestPolicyCmpt testPolicyCmpt = testCase.newTestPolicyCmpt();
        testPolicyCmpt.setProductCmptAndNameAfterIfApplicable(rootProductCmpt.getQualifiedName());
        testPolicyCmpt.setPolicyCmptType(rootPolicyType.getQualifiedName());
        testPolicyCmpt.setTestPolicyCmptTypeParameter(testCaseType.getTestParameters()[0].getName());
        return testPolicyCmpt;
    }

}
