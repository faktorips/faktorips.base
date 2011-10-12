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

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyExternalReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.junit.Before;
import org.junit.Test;

public class ProductCmptPropertyExternalReferenceTest extends AbstractIpsPluginTest {

    private IPolicyCmptType policyType;

    private IProductCmptType productType;

    private IProductCmptCategory category;

    private IPolicyCmptTypeAttribute attributeProperty;

    private IProductCmptPropertyExternalReference attributeReference;

    @Override
    @Before
    public void setUp() throws CoreException {
        IIpsProject ipsProject = newIpsProject();
        policyType = newPolicyAndProductCmptType(ipsProject, "PolicyCmptType", "ProductCmptType");
        productType = policyType.findProductCmptType(ipsProject);
        category = productType.newProductCmptCategory();
        attributeProperty = policyType.newPolicyCmptTypeAttribute();
        attributeProperty.setName("attribute");
        attributeProperty.setProductRelevant(true);
        attributeReference = category.newProductCmptPropertyReference(attributeProperty);
    }

    @Test
    public void shouldAllowToSetName() {
        attributeReference.setName("foo");
        assertEquals("foo", attributeReference.getName());
        attributeReference.setName("bar");
        assertEquals("bar", attributeReference.getName());
    }

    @Test
    public void shouldAllowToSetPropertyType() {
        attributeReference.setProductCmptPropertyType(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE);
        assertEquals(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE,
                attributeReference.getProductCmptPropertyType());
        attributeReference.setProductCmptPropertyType(ProductCmptPropertyType.VALIDATION_RULE);
        assertEquals(ProductCmptPropertyType.VALIDATION_RULE, attributeReference.getProductCmptPropertyType());
    }

    // @Test(expected = IllegalArgumentException.class)
    // public void shouldThrowIllegalArgumentExceptionWhenSettingPropertyTypeFormula() {
    // attributeReference.setProductCmptPropertyType(ProductCmptPropertyType.FORMULA);
    // }
    //
    // @Test(expected = IllegalArgumentException.class)
    // public void shouldThrowIllegalArgumentExceptionWhenSettingPropertyTypeValue() {
    // attributeReference.setProductCmptPropertyType(ProductCmptPropertyType.VALUE);
    // }
    //
    // @Test(expected = IllegalArgumentException.class)
    // public void shouldThrowIllegalArgumentExceptionWhenSettingPropertyTypeTableContentUsage() {
    // attributeReference.setProductCmptPropertyType(ProductCmptPropertyType.TABLE_CONTENT_USAGE);
    // }

}
