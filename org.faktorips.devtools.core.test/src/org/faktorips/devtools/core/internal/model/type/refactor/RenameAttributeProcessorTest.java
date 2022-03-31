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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.abstracttest.core.AbstractIpsRefactoringTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.refactor.IpsRefactoringProcessor;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.faktorips.devtools.model.ipsobject.Modifier;
import org.faktorips.devtools.model.pctype.AttributeType;
import org.faktorips.devtools.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IConfigElement;
import org.faktorips.devtools.model.productcmpt.IConfiguredDefault;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.model.type.IAttribute;
import org.faktorips.devtools.model.valueset.ValueSetType;
import org.junit.Test;

public class RenameAttributeProcessorTest extends AbstractIpsRefactoringTest {

    @Test
    public void testCheckInitialConditionsValid() {
        IpsRefactoringProcessor ipsRefactoringProcessor = new RenameAttributeProcessor(policyCmptTypeAttribute);
        RefactoringStatus status = ipsRefactoringProcessor.checkInitialConditions(new NullProgressMonitor());
        assertFalse(status.hasError());
    }

    @Test
    public void testCheckFinalConditionsValid() {
        IpsRenameProcessor ipsRenameProcessor = new RenameAttributeProcessor(policyCmptTypeAttribute);
        ipsRenameProcessor.setNewName("test");
        RefactoringStatus status = ipsRenameProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertFalse(status.hasError());
    }

    @Test
    public void testCheckFinalConditionsInvalidAttributeName() {
        // Create another policy component type attribute to test against.
        IAttribute attribute = policyCmptType.newAttribute();
        attribute.setName("otherAttribute");

        IpsRenameProcessor ipsRenameProcessor = new RenameAttributeProcessor(policyCmptTypeAttribute);
        ipsRenameProcessor.setNewName("otherAttribute");
        RefactoringStatus status = ipsRenameProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertTrue(status.hasError());
    }

    @Test
    public void testRenamePolicyCmptTypeAttribute() {
        String newAttributeName = "test";
        performRenameRefactoring(policyCmptTypeAttribute, newAttributeName);

        // Check for changed attribute name.
        assertNull(policyCmptType.getAttribute(POLICY_CMPT_TYPE_ATTRIBUTE_NAME));
        assertNotNull(policyCmptType.getAttribute(newAttributeName));
        assertTrue(policyCmptTypeAttribute.getName().equals(newAttributeName));

        // Check for test attribute update.
        assertEquals(1, testPolicyCmptTypeParameter.getTestAttributes(newAttributeName).length);
        assertTrue(testAttribute.getAttribute().equals(newAttributeName));

        // Check for product component configuration element update.
        assertNull(productCmptGeneration.getPropertyValue(POLICY_CMPT_TYPE_ATTRIBUTE_NAME, IConfiguredDefault.class));
        assertNotNull(productCmptGeneration.getPropertyValue(newAttributeName, IConfiguredDefault.class));
        assertEquals(newAttributeName, productCmptGenerationConfiguredDefault.getPolicyCmptTypeAttribute());
    }

    @Test
    public void testRenamePolicyCmptTypeAttributeWithValidationRule() {
        policyCmptTypeAttribute.createValueSetRule();
        policyCmptTypeAttribute.setValueSetType(ValueSetType.ENUM);
        String newAttributeName = "test";
        performRenameRefactoring(policyCmptTypeAttribute, newAttributeName);

        // Check for changed attribute name.
        assertNull(policyCmptType.getAttribute(POLICY_CMPT_TYPE_ATTRIBUTE_NAME));
        assertNotNull(policyCmptType.getAttribute(newAttributeName));
        assertTrue(policyCmptTypeAttribute.getName().equals(newAttributeName));

        // Check for validation rule update.
        assertNotNull(policyCmptTypeAttribute.findValueSetRule(ipsProject));
    }

    /**
     * Creates another attribute in the second <code>IPolicyCmptType</code> that that corresponds
     * exactly to the attribute of the already existing <code>IPolicyCmptType</code>.
     * <p>
     * This other <code>IPolicyCmptType</code> is configured by a new <code>IProductCmptType</code>.
     * Based on that <code>IProductCmptType</code> exists an <code>IProductCmpt</code>. The
     * refactoring of the original <code>IPolicyCmptTypeAttribute</code> may not cause modifications
     * to this new <code>IProductCmpt</code>'s <code>IConfigElement</code>s.
     * <p>
     * Also creates another <code>ITestCaseType</code> based on the new <code>IPolicyCmptType</code>
     * / <code>IPolicyCmptTypeAttribute</code>. The new <code>ITestCaseType</code> may not be
     * modified by the refactoring, too.
     */
    @Test
    public void testRenamePolicyCmptTypeAttributeSameNames() {
        otherPolicyCmptType.setConfigurableByProductCmptType(true);

        // Create an attribute corresponding to the attribute of the original policy component type.
        IPolicyCmptTypeAttribute otherAttribute = otherPolicyCmptType.newPolicyCmptTypeAttribute();
        otherAttribute.setName(POLICY_CMPT_TYPE_ATTRIBUTE_NAME);
        otherAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        otherAttribute.setModifier(Modifier.PUBLISHED);
        otherAttribute.setAttributeType(AttributeType.CHANGEABLE);
        otherAttribute.setValueSetConfiguredByProduct(true);

        // Create the other product component type.
        IProductCmptType otherProductCmptType = newProductCmptType(ipsProject, "OtherProduct");
        otherProductCmptType.setConfigurationForPolicyCmptType(true);
        otherProductCmptType.setPolicyCmptType(otherPolicyCmptType.getQualifiedName());
        otherPolicyCmptType.setProductCmptType(otherProductCmptType.getQualifiedName());

        // Create a product component on that new product component type.
        IProductCmpt otherProductCmpt = newProductCmpt(otherProductCmptType, "OtherExampleProduct");
        IProductCmptGeneration otherGeneration = (IProductCmptGeneration)otherProductCmpt.newGeneration();
        IConfigElement otherConfigElement = otherGeneration.newPropertyValue(otherAttribute, IConfiguredDefault.class);

        // Create another test case type based on the new policy component type.
        ITestCaseType otherTestCaseType = newTestCaseType(ipsProject, "OtherTestCaseType");
        ITestPolicyCmptTypeParameter otherPolicyParameter = otherTestCaseType.newCombinedPolicyCmptTypeParameter();
        otherPolicyParameter.setPolicyCmptType(otherPolicyCmptType.getQualifiedName());
        ITestAttribute otherTestAttribute = otherPolicyParameter.newInputTestAttribute();
        otherTestAttribute.setAttribute(otherAttribute);
        otherTestAttribute.setName("someOtherTestAttribute");
        otherTestAttribute.setDatatype(Datatype.STRING.getQualifiedName());

        // Run the refactoring.
        String newAttributeName = "test";
        performRenameRefactoring(policyCmptTypeAttribute, newAttributeName);

        // The new configuration element may not have been modified.
        assertEquals(POLICY_CMPT_TYPE_ATTRIBUTE_NAME, otherConfigElement.getName());
        assertNull(otherGeneration.getPropertyValue(newAttributeName, IConfiguredDefault.class));

        // The new test attribute may not have been modified.
        assertEquals(POLICY_CMPT_TYPE_ATTRIBUTE_NAME, otherTestAttribute.getAttribute());
        assertNull(otherPolicyParameter.getTestAttribute(newAttributeName));
    }

    /**
     * Test to rename an <code>IPolicyCmptTypeAttribute</code> from an <code>IPolicyCmptType</code>
     * that is a super type of another <code>IPolicyCmptType</code>.
     */
    @Test
    public void testRenamePolicyCmptTypeAttributeInheritance() {
        // Create an attribute in the super policy component type.
        IPolicyCmptTypeAttribute superAttribute = superPolicyCmptType.newPolicyCmptTypeAttribute();
        superAttribute.setName("superAttribute");
        superAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        superAttribute.setModifier(Modifier.PUBLISHED);
        superAttribute.setAttributeType(AttributeType.CHANGEABLE);
        superAttribute.setValueSetConfiguredByProduct(true);

        // Create a test attribute for this new attribute.
        ITestAttribute superTestAttribute = testPolicyCmptTypeParameter.newInputTestAttribute();
        superTestAttribute.setAttribute(superAttribute);
        superTestAttribute.setName("someSuperTestAttribute");
        superTestAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());

        // Create a configuration element for this new attribute.
        IConfigElement superConfigElement = productCmptGeneration.newPropertyValue(superAttribute,
                IConfiguredDefault.class);

        // Run the refactoring.
        String newAttributeName = "test";
        performRenameRefactoring(superAttribute, newAttributeName);

        // Check for test attribute update.
        assertEquals(1, testPolicyCmptTypeParameter.getTestAttributes(POLICY_CMPT_TYPE_ATTRIBUTE_NAME).length);
        assertEquals(1, testPolicyCmptTypeParameter.getTestAttributes(newAttributeName).length);
        assertTrue(superTestAttribute.getAttribute().equals(newAttributeName));

        // Check for product component configuration element update.
        assertNotNull(
                productCmptGeneration.getPropertyValue(POLICY_CMPT_TYPE_ATTRIBUTE_NAME, IConfiguredDefault.class));
        assertNull(productCmptGeneration.getPropertyValue("superAttribute", IConfiguredDefault.class));
        assertNotNull(productCmptGeneration.getPropertyValue(newAttributeName, IConfiguredDefault.class));
        assertEquals(newAttributeName, superConfigElement.getPolicyCmptTypeAttribute());
    }

    @Test
    public void testRenameProductCmptTypeAttribute() {
        String newAttributeName = "test";
        performRenameRefactoring(productCmptTypeAttribute, newAttributeName);

        // Check for changed attribute name.
        assertNull(productCmptType.getAttribute(PRODUCT_CMPT_TYPE_ATTRIBUTE_NAME));
        assertNotNull(productCmptType.getAttribute(newAttributeName));
        assertTrue(productCmptTypeAttribute.getName().equals(newAttributeName));

        // Check for product component attribute value update.
        assertNull(productCmptGeneration.getAttributeValue(PRODUCT_CMPT_TYPE_ATTRIBUTE_NAME));
        assertNotNull(productCmptGeneration.getAttributeValue(newAttributeName));
        assertEquals(newAttributeName, attributeValue.getAttribute());
    }

    /**
     * Create yet another <code>IProductCmpt</code> based on another <code>IProductCmptType</code>
     * that has an attribute with the same name as the first <code>IProductCmptType</code> (this may
     * no be modified by the rename refactoring).
     */
    @Test
    public void testRenameProductCmptTypeAttributeSameNames() {
        // Create other product component type.
        IProductCmptType otherProductCmptType = newProductCmptType(ipsProject, "OtherProductCmptType");
        IProductCmptTypeAttribute otherProductCmptTypeAttribute = otherProductCmptType.newProductCmptTypeAttribute();
        otherProductCmptTypeAttribute.setName(PRODUCT_CMPT_TYPE_ATTRIBUTE_NAME);
        otherProductCmptTypeAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        otherProductCmptTypeAttribute.setModifier(Modifier.PUBLISHED);

        // Create a product component based on the other product component type.
        IProductCmpt otherProductCmpt = newProductCmpt(otherProductCmptType, "OtherExampleProduct");
        IProductCmptGeneration otherProductCmptGeneration = (IProductCmptGeneration)otherProductCmpt.newGeneration();
        IAttributeValue otherAttributeValue = otherProductCmptGeneration
                .newAttributeValue(otherProductCmptTypeAttribute);

        // Run the refactoring.
        String newAttributeName = "test";
        performRenameRefactoring(productCmptTypeAttribute, newAttributeName);

        // Check that the other product component was not modified.
        assertNotNull(otherProductCmptGeneration.getAttributeValue(PRODUCT_CMPT_TYPE_ATTRIBUTE_NAME));
        assertNull(otherProductCmptGeneration.getAttributeValue(newAttributeName));
        assertEquals(PRODUCT_CMPT_TYPE_ATTRIBUTE_NAME, otherAttributeValue.getAttribute());

        // Check for product component attribute value update.
        assertNull(productCmptGeneration.getAttributeValue(PRODUCT_CMPT_TYPE_ATTRIBUTE_NAME));
        assertNotNull(productCmptGeneration.getAttributeValue(newAttributeName));
        assertEquals(newAttributeName, attributeValue.getAttribute());
    }

    /**
     * Test to rename an <code>IProductCmptTypeAttribute</code> from an
     * <code>IProductCmptType</code> that is a super type of another <code>IProductCmptType</code>.
     */
    @Test
    public void testRenameProductCmptTypeAttributeInheritance() {
        // Create an attribute in the super product component type.
        IProductCmptTypeAttribute superAttribute = superProductCmptType.newProductCmptTypeAttribute();
        superAttribute.setName("superAttribute");
        superAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        superAttribute.setModifier(Modifier.PUBLISHED);

        IAttributeValue newAttributeValue = productCmptGeneration.newAttributeValue(superAttribute);

        // Run the refactoring.
        String newAttributeName = "test";
        performRenameRefactoring(superAttribute, newAttributeName);

        // Check for product component attribute value update.
        assertNull(productCmptGeneration.getAttributeValue("superAttribute"));
        assertNotNull(productCmptGeneration.getAttributeValue(newAttributeName));
        assertEquals(newAttributeName, newAttributeValue.getAttribute());
    }

    /**
     * Test whether renaming a {@link IPolicyCmptTypeAttribute} that is overwriting an attribute of
     * the super type hierarchy also renames the super attribute.
     */
    @Test
    public void testRenameOverwritingPolicyCmptTypeAttribute() {
        // Create a type hierarchy of depth 3, always overwriting an attribute
        IPolicyCmptType deepPolicyCmptType = newPolicyCmptType(ipsProject, "DeepPolicyCmptType");
        deepPolicyCmptType.setSupertype(policyCmptType.getQualifiedName());
        IProductCmptType deepProductCmptType = newProductCmptType(ipsProject, "DeepProductCmptType");
        deepProductCmptType.setSupertype(productCmptType.getQualifiedName());
        deepProductCmptType.setConfigurationForPolicyCmptType(true);
        deepProductCmptType.setPolicyCmptType(deepPolicyCmptType.getQualifiedName());
        deepPolicyCmptType.setConfigurableByProductCmptType(true);
        deepPolicyCmptType.setProductCmptType(deepProductCmptType.getQualifiedName());

        String attributeName = "overwrittenAttribute";

        IPolicyCmptTypeAttribute superAttribute = superPolicyCmptType.newPolicyCmptTypeAttribute();
        superAttribute.setName(attributeName);
        superAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());

        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName(attributeName);
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setOverwrite(true);

        IPolicyCmptTypeAttribute deepAttribute = deepPolicyCmptType.newPolicyCmptTypeAttribute();
        deepAttribute.setName(attributeName);
        deepAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        deepAttribute.setOverwrite(true);

        // Run the refactoring
        String newAttributeName = "test";
        performRenameRefactoring(deepAttribute, newAttributeName);

        // Check that the names of all 3 attributes have changed
        assertEquals(newAttributeName, superAttribute.getName());
        assertEquals(newAttributeName, attribute.getName());
        assertEquals(newAttributeName, deepAttribute.getName());

        assertNull(superPolicyCmptType.getAttribute(attributeName));
        assertNull(policyCmptType.getAttribute(attributeName));
        assertNull(deepPolicyCmptType.getAttribute(attributeName));

        assertEquals(superAttribute, superPolicyCmptType.getAttribute(newAttributeName));
        assertEquals(attribute, policyCmptType.getAttribute(newAttributeName));
        assertEquals(deepAttribute, deepPolicyCmptType.getAttribute(newAttributeName));
    }

    /**
     * Test whether renaming a {@link IPolicyCmptTypeAttribute} that is overwritten by attributes of
     * the sub type hierarchy also renames the attributes in the sub type hierarchy.
     */
    @Test
    public void testRenameOverwrittenPolicyCmptTypeAttribute() {
        // Create a type hierarchy of depth 3, always overwriting an attribute
        IPolicyCmptType deepPolicyCmptType = newPolicyCmptType(ipsProject, "DeepPolicyCmptType");
        deepPolicyCmptType.setSupertype(policyCmptType.getQualifiedName());
        IProductCmptType deepProductCmptType = newProductCmptType(ipsProject, "DeepProductCmptType");
        deepProductCmptType.setSupertype(productCmptType.getQualifiedName());
        deepProductCmptType.setConfigurationForPolicyCmptType(true);
        deepProductCmptType.setPolicyCmptType(deepPolicyCmptType.getQualifiedName());
        deepPolicyCmptType.setConfigurableByProductCmptType(true);
        deepPolicyCmptType.setProductCmptType(deepProductCmptType.getQualifiedName());

        String attributeName = "overwrittenAttribute";

        IPolicyCmptTypeAttribute superAttribute = superPolicyCmptType.newPolicyCmptTypeAttribute();
        superAttribute.setName(attributeName);
        superAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());

        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute();
        attribute.setName(attributeName);
        attribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        attribute.setOverwrite(true);

        IPolicyCmptTypeAttribute deepAttribute = deepPolicyCmptType.newPolicyCmptTypeAttribute();
        deepAttribute.setName(attributeName);
        deepAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        deepAttribute.setOverwrite(true);

        // Run the refactoring
        String newAttributeName = "test";
        performRenameRefactoring(superAttribute, newAttributeName);

        // Check that the names of all 3 attributes have changed
        assertEquals(newAttributeName, superAttribute.getName());
        assertEquals(newAttributeName, attribute.getName());
        assertEquals(newAttributeName, deepAttribute.getName());

        assertNull(superPolicyCmptType.getAttribute(attributeName));
        assertNull(policyCmptType.getAttribute(attributeName));
        assertNull(deepPolicyCmptType.getAttribute(attributeName));

        assertEquals(superAttribute, superPolicyCmptType.getAttribute(newAttributeName));
        assertEquals(attribute, policyCmptType.getAttribute(newAttributeName));
        assertEquals(deepAttribute, deepPolicyCmptType.getAttribute(newAttributeName));
    }

}
