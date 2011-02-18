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

package org.faktorips.devtools.core.internal.model.type.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;
import org.faktorips.abstracttest.AbstractIpsRefactoringTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.core.model.testcasetype.ITestCaseType;
import org.faktorips.devtools.core.model.testcasetype.ITestPolicyCmptTypeParameter;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.devtools.core.refactor.IIpsRenameProcessor;
import org.junit.Test;

/**
 * 
 * 
 * @author Alexander Weickmann
 */
public class RenameAttributeProcessorTest extends AbstractIpsRefactoringTest {

    @Test
    public void testCheckInitialConditionsValid() throws CoreException {
        ProcessorBasedRefactoring renameRefactoring = policyCmptTypeAttribute.getRenameRefactoring();
        RefactoringStatus status = renameRefactoring.getProcessor().checkInitialConditions(new NullProgressMonitor());
        assertFalse(status.hasError());
    }

    @Test
    public void testCheckFinalConditionsValid() throws CoreException {
        ProcessorBasedRefactoring renameRefactoring = policyCmptTypeAttribute.getRenameRefactoring();
        IIpsRenameProcessor renameProcessor = (IIpsRenameProcessor)renameRefactoring.getProcessor();
        renameProcessor.setNewName("test");
        RefactoringStatus status = renameRefactoring.getProcessor().checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertFalse(status.hasError());
    }

    @Test
    public void testCheckFinalConditionsInvalidAttributeName() throws CoreException {
        // Create another policy component type attribute to test against.
        IAttribute attribute = policyCmptType.newAttribute();
        attribute.setName("otherAttribute");

        ProcessorBasedRefactoring renameRefactoring = policyCmptTypeAttribute.getRenameRefactoring();
        IIpsRenameProcessor renameProcessor = (IIpsRenameProcessor)renameRefactoring.getProcessor();
        renameProcessor.setNewName("otherAttribute");
        RefactoringStatus status = renameRefactoring.getProcessor().checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertTrue(status.hasFatalError());
    }

    @Test
    public void testRenamePolicyCmptTypeAttribute() throws CoreException {
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
        assertNull(productCmptGeneration.getConfigElement(POLICY_CMPT_TYPE_ATTRIBUTE_NAME));
        assertNotNull(productCmptGeneration.getConfigElement(newAttributeName));
        assertEquals(newAttributeName, productCmptGenerationConfigElement.getPolicyCmptTypeAttribute());
    }

    @Test
    public void testRenamePolicyCmptTypeAttributeWithValidationRule() throws CoreException {
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
     * Creates another attribute in the second <tt>IPolicyCmptType</tt> that that corresponds
     * exactly to the attribute of the already existing <tt>IPolicyCmptType</tt>.
     * <p>
     * This other <tt>IPolicyCmptType</tt> is configured by a new <tt>IProductCmptType</tt>. Based
     * on that <tt>IProductCmptType</tt> exists an <tt>IProductCmpt</tt>. The refactoring of the
     * original <tt>IPolicyCmptTypeAttribute</tt> may not cause modifications to this new
     * <tt>IProductCmpt</tt>'s <tt>IConfigElement</tt>s.
     * <p>
     * Also creates another <tt>ITestCaseType</tt> based on the new <tt>IPolicyCmptType</tt> /
     * <tt>IPolicyCmptTypeAttribute</tt>. The new <tt>ITestCaseType</tt> may not be modified by the
     * refactoring, too.
     */
    @Test
    public void testRenamePolicyCmptTypeAttributeSameNames() throws CoreException {
        otherPolicyCmptType.setConfigurableByProductCmptType(true);

        // Create an attribute corresponding to the attribute of the original policy component type.
        IPolicyCmptTypeAttribute otherAttribute = otherPolicyCmptType.newPolicyCmptTypeAttribute();
        otherAttribute.setName(POLICY_CMPT_TYPE_ATTRIBUTE_NAME);
        otherAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        otherAttribute.setModifier(Modifier.PUBLISHED);
        otherAttribute.setAttributeType(AttributeType.CHANGEABLE);
        otherAttribute.setProductRelevant(true);

        // Create the other product component type.
        IProductCmptType otherProductCmptType = newProductCmptType(ipsProject, "OtherProduct");
        otherProductCmptType.setConfigurationForPolicyCmptType(true);
        otherProductCmptType.setPolicyCmptType(otherPolicyCmptType.getQualifiedName());
        otherPolicyCmptType.setProductCmptType(otherProductCmptType.getQualifiedName());

        // Create a product component on that new product component type.
        IProductCmpt otherProductCmpt = newProductCmpt(otherProductCmptType, "OtherExampleProduct");
        IProductCmptGeneration otherGeneration = (IProductCmptGeneration)otherProductCmpt.newGeneration();
        IConfigElement otherConfigElement = otherGeneration.newConfigElement(otherAttribute);

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
        assertNull(otherGeneration.getConfigElement(newAttributeName));

        // The new test attribute may not have been modified.
        assertEquals(POLICY_CMPT_TYPE_ATTRIBUTE_NAME, otherTestAttribute.getAttribute());
        assertNull(otherPolicyParameter.getTestAttribute(newAttributeName));
    }

    /**
     * Test to rename an <tt>IPolicyCmptTypeAttribute</tt> from an <tt>IPolicyCmptType</tt> that is
     * a super type of another <tt>IPolicyCmptType</tt>.
     */
    @Test
    public void testRenamePolicyCmptTypeAttributeInheritance() throws CoreException {
        // Create an attribute in the super policy component type.
        IPolicyCmptTypeAttribute superAttribute = superPolicyCmptType.newPolicyCmptTypeAttribute();
        superAttribute.setName("superAttribute");
        superAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        superAttribute.setModifier(Modifier.PUBLISHED);
        superAttribute.setAttributeType(AttributeType.CHANGEABLE);
        superAttribute.setProductRelevant(true);

        // Create a test attribute for this new attribute.
        ITestAttribute superTestAttribute = testPolicyCmptTypeParameter.newInputTestAttribute();
        superTestAttribute.setAttribute(superAttribute);
        superTestAttribute.setName("someSuperTestAttribute");
        superTestAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());

        // Create a configuration element for this new attribute.
        IConfigElement superConfigElement = productCmptGeneration.newConfigElement(superAttribute);

        // Run the refactoring.
        String newAttributeName = "test";
        performRenameRefactoring(superAttribute, newAttributeName);

        // Check for test attribute update.
        assertEquals(1, testPolicyCmptTypeParameter.getTestAttributes(POLICY_CMPT_TYPE_ATTRIBUTE_NAME).length);
        assertEquals(1, testPolicyCmptTypeParameter.getTestAttributes(newAttributeName).length);
        assertTrue(superTestAttribute.getAttribute().equals(newAttributeName));

        // Check for product component configuration element update.
        assertNotNull(productCmptGeneration.getConfigElement(POLICY_CMPT_TYPE_ATTRIBUTE_NAME));
        assertNull(productCmptGeneration.getConfigElement("superAttribute"));
        assertNotNull(productCmptGeneration.getConfigElement(newAttributeName));
        assertEquals(newAttributeName, superConfigElement.getPolicyCmptTypeAttribute());
    }

    @Test
    public void testRenameProductCmptTypeAttribute() throws CoreException {
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
     * Create yet another <tt>IProductCmpt</tt> based on another <tt>IProductCmptType</tt> that has
     * an attribute with the same name as the first <tt>IProductCmptType</tt> (this may no be
     * modified by the rename refactoring).
     */
    @Test
    public void testRenameProductCmptTypeAttributeSameNames() throws CoreException {
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
     * Test to rename an <tt>IProductCmptTypeAttribute</tt> from an <tt>IProductCmptType</tt> that
     * is a super type of another <tt>IProductCmptType</tt>.
     */
    @Test
    public void testRenameProductCmptTypeAttributeInheritance() throws CoreException {
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
    public void testRenameOverridingPolicyCmptTypeAttribute() throws CoreException {
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

}
