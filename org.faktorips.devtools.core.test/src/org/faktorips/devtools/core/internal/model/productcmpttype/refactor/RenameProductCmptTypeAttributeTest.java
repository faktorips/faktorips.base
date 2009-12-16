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

package org.faktorips.devtools.core.internal.model.productcmpttype.refactor;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.AbstractIpsRefactoringTest;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;

public class RenameProductCmptTypeAttributeTest extends AbstractIpsRefactoringTest {

    private static final String PRODUCT_CMPT_TYPE_ATTRIBUTE_NAME = "productAttribute";

    private IProductCmptType productCmptType;

    private IProductCmptTypeAttribute productCmptTypeAttribute;

    private IProductCmpt productCmpt;

    private IProductCmptGeneration productCmptGeneration;

    private IAttributeValue attributeValue;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Create a product component type.
        productCmptType = newProductCmptType(ipsProject, "Product");
        productCmptType.setConfigurationForPolicyCmptType(false);
        productCmptType.setPolicyCmptType("");

        // Create a product component type attribute.
        productCmptTypeAttribute = productCmptType.newProductCmptTypeAttribute();
        productCmptTypeAttribute.setName(PRODUCT_CMPT_TYPE_ATTRIBUTE_NAME);
        productCmptTypeAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        productCmptTypeAttribute.setModifier(Modifier.PUBLISHED);

        // Create a product component based on the product component type.
        productCmpt = newProductCmpt(productCmptType, "ExampleProduct");
        productCmptGeneration = (IProductCmptGeneration)productCmpt.newGeneration();
        attributeValue = productCmptGeneration.newAttributeValue(productCmptTypeAttribute);
    }

    public void testRenameProductCmptTypeAttribute() throws CoreException {
        String newAttributeName = "test";
        runRenameRefactoring(productCmptTypeAttribute.getRenameRefactoring(), newAttributeName);

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
    public void testRenameProductCmptTypeAttributeSameAttributeNames() throws CoreException {
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

        String newAttributeName = "test";
        runRenameRefactoring(productCmptTypeAttribute.getRenameRefactoring(), newAttributeName);

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
    public void testRenameProductCmptTypeAttributeInheritance() throws CoreException {
        // Create a super product component type.
        IProductCmptType superProductCmptType = newProductCmptType(ipsProject, "SuperProductCmptType");
        superProductCmptType.setAbstract(true);
        superProductCmptType.setConfigurationForPolicyCmptType(false);
        superProductCmptType.setPolicyCmptType("");

        // Create an attribute in the super product component type.
        IProductCmptTypeAttribute superAttribute = superProductCmptType.newProductCmptTypeAttribute();
        superAttribute.setName("superAttribute");
        superAttribute.setDatatype(Datatype.INTEGER.getQualifiedName());
        superAttribute.setModifier(Modifier.PUBLISHED);

        productCmptType.setSupertype(superProductCmptType.getQualifiedName());

        IAttributeValue newAttributeValue = productCmptGeneration.newAttributeValue(superAttribute);

        String newAttributeName = "test";
        runRenameRefactoring(superAttribute.getRenameRefactoring(), newAttributeName);

    }

}
