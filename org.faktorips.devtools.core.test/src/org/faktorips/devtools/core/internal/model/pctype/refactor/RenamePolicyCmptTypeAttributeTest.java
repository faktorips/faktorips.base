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

package org.faktorips.devtools.core.internal.model.pctype.refactor;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;

public class RenamePolicyCmptTypeAttributeTest extends AbstractIpsRefactoringTest {

    private static final String POLICY_CMPT_TYPE_ATTRIBUTE_NAME = "policyAttribute";

    private IPolicyCmptType policyCmptType;

    private IPolicyCmptTypeAttribute policyCmptTypeAttribute;

    private IProductCmptType productCmptType;

    private ITestCaseType testCaseType;

    private ITestPolicyCmptTypeParameter testPolicyCmptTypeParameter;

    private ITestAttribute testAttribute;

    private IProductCmpt productCmpt;

    private IProductCmptGeneration productCmptGeneration;

    private IConfigElement productCmptGenerationConfigElement;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Create a policy component type and a product component type.
        policyCmptType = newPolicyCmptType(ipsProject, "Policy");
        productCmptType = newProductCmptType(ipsProject, "Product");
        policyCmptType.setConfigurableByProductCmptType(true);
        policyCmptType.setProductCmptType(productCmptType.getQualifiedName());
        productCmptType.setConfigurationForPolicyCmptType(true);
        productCmptType.setPolicyCmptType(policyCmptType.getQualifiedName());

        // Create a policy component type attribute.
        policyCmptTypeAttribute = policyCmptType.newPolicyCmptTypeAttribute();
        policyCmptTypeAttribute.setName(POLICY_CMPT_TYPE_ATTRIBUTE_NAME);
        policyCmptTypeAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        policyCmptTypeAttribute.setModifier(Modifier.PUBLISHED);
        policyCmptTypeAttribute.setAttributeType(AttributeType.CHANGEABLE);
        policyCmptTypeAttribute.setProductRelevant(true);

        // Create a test case type with a test attribute.
        testCaseType = newTestCaseType(ipsProject, "TestCaseType");
        testPolicyCmptTypeParameter = testCaseType.newCombinedPolicyCmptTypeParameter();
        testPolicyCmptTypeParameter.setPolicyCmptType(policyCmptType.getQualifiedName());
        testAttribute = testPolicyCmptTypeParameter.newInputTestAttribute();
        testAttribute.setAttribute(policyCmptTypeAttribute);
        testAttribute.setName("someTestAttribute");
        testAttribute.setDatatype(Datatype.STRING.getQualifiedName());

        // Create a product component based on the product component type.
        productCmpt = newProductCmpt(productCmptType, "ExampleProduct");
        productCmptGeneration = (IProductCmptGeneration)productCmpt.newGeneration();
        productCmptGenerationConfigElement = productCmptGeneration.newConfigElement(policyCmptTypeAttribute);
    }

    public void testRenamePolicyCmptTypeAttribute() throws CoreException {
        String newAttributeName = "test";
        runRenameRefactoring(policyCmptTypeAttribute.getRenameRefactoring(), newAttributeName);

        // Check for changed attribute name.
        assertNull(policyCmptType.getAttribute(POLICY_CMPT_TYPE_ATTRIBUTE_NAME));
        assertNotNull(policyCmptType.getAttribute(newAttributeName));
        assertTrue(policyCmptTypeAttribute.getName().equals(newAttributeName));

        // Check for test attribute update.
        assertEquals(1, testPolicyCmptTypeParameter.getTestAttributes(policyCmptTypeAttribute.getName()).length);
        assertTrue(testAttribute.getAttribute().equals(newAttributeName));

        // Check for product component configuration element update.
        assertNull(productCmptGeneration.getConfigElement(POLICY_CMPT_TYPE_ATTRIBUTE_NAME));
        assertNotNull(productCmptGeneration.getConfigElement(newAttributeName));
        assertEquals(newAttributeName, productCmptGenerationConfigElement.getPolicyCmptTypeAttribute());
    }

}
