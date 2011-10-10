/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptCategoryTest extends AbstractIpsPluginTest {

    private IProductCmptType productCmptType;

    private IProductCmptProperty productCmptProperty;

    private IProductCmptCategory productCmptCategory;

    @Override
    @Before
    public void setUp() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        productCmptType = newProductCmptType(ipsProject, "ProductCmptType");
        productCmptCategory = productCmptType.newProductCmptCategory();

        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute();
        attribute.setName("bar");
        productCmptProperty = attribute;
    }

    @Test
    public void shouldReturnParentProductCmptType() {
        assertEquals(productCmptType, productCmptCategory.getProductCmptType());
    }

    @Test
    public void shouldAllowToSetName() {
        productCmptCategory.setName("foo");
        assertEquals("foo", productCmptCategory.getName());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldReturnUnmodifiableListWhenRequestingAssignedProductCmptProperties() {
        productCmptCategory.assignProductCmptProperty(productCmptProperty);

        productCmptCategory.getAssignedProductCmptProperties().remove(0);
    }

    @Test
    public void shouldAllowToAssignProductCmptProperty() {
        boolean added = productCmptCategory.assignProductCmptProperty(productCmptProperty);

        assertTrue(added);
        assertEquals(productCmptProperty, productCmptCategory.getAssignedProductCmptProperty("bar"));
    }

    @Test
    public void shouldReturnFalseWhenAssigningAlreadyAssignedProductCmptProperty() {
        productCmptCategory.assignProductCmptProperty(productCmptProperty);
        boolean added = productCmptCategory.assignProductCmptProperty(productCmptProperty);

        assertFalse(added);
    }

    @Test
    public void shouldReturnNullWhenTryingToRetrieveNotAssignedProperty() {
        assertNull(productCmptCategory.getAssignedProductCmptProperty("foobar"));
    }

    @Test
    public void shouldAllowToSetInheritedProperty() {
        productCmptCategory.setInherited(true);
        assertTrue(productCmptCategory.isInherited());
        productCmptCategory.setInherited(false);
        assertFalse(productCmptCategory.isInherited());
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForMethods() {
        productCmptCategory.setDefaultForMethods(true);
        assertTrue(productCmptCategory.isDefaultForMethods());
        productCmptCategory.setDefaultForMethods(false);
        assertFalse(productCmptCategory.isDefaultForMethods());
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForPolicyCmptTypeAttributes() {
        productCmptCategory.setDefaultForPolicyCmptTypeAttributes(true);
        assertTrue(productCmptCategory.isDefaultForPolicyCmptTypeAttributes());
        productCmptCategory.setDefaultForPolicyCmptTypeAttributes(false);
        assertFalse(productCmptCategory.isDefaultForPolicyCmptTypeAttributes());
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForProductCmptTypeAttributes() {
        productCmptCategory.setDefaultForProductCmptTypeAttributes(true);
        assertTrue(productCmptCategory.isDefaultForProductCmptTypeAttributes());
        productCmptCategory.setDefaultForProductCmptTypeAttributes(false);
        assertFalse(productCmptCategory.isDefaultForProductCmptTypeAttributes());
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForTableStructureUsages() {
        productCmptCategory.setDefaultForTableStructureUsages(true);
        assertTrue(productCmptCategory.isDefaultForTableStructureUsages());
        productCmptCategory.setDefaultForTableStructureUsages(false);
        assertFalse(productCmptCategory.isDefaultForTableStructureUsages());
    }

    @Test
    public void shouldAllowToBeMarkedAsDefaultForValidationRules() {
        productCmptCategory.setDefaultForValidationRules(true);
        assertTrue(productCmptCategory.isDefaultForValidationRules());
        productCmptCategory.setDefaultForValidationRules(false);
        assertFalse(productCmptCategory.isDefaultForValidationRules());
    }

}
