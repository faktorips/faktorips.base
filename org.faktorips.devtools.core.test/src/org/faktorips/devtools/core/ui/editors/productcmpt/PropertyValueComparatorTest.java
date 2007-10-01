/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.editors.productcmpt;

import java.util.Arrays;

import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.product.IPropertyValue;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.product.IAttributeValue;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.product.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;

/**
 * 
 * @author Jan Ortmann
 */
public class PropertyValueComparatorTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
    private IPolicyCmptType policySupertype;
    private IPolicyCmptType policyType;
    private IProductCmptType productType;
    private IProductCmptType productSupertype;

    private IProductCmptGeneration generation;
    private PropertyValueComparator comparator;
    
    /**
     * {@inheritDoc}
     */
    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = super.newIpsProject();
        policySupertype = newPolicyAndProductCmptType(ipsProject, "SuperPolicy", "SuperProduct");
        policyType = newPolicyAndProductCmptType(ipsProject, "Policy", "Product");
        productType = policyType.findProductCmptType(ipsProject);
        productSupertype = policySupertype.findProductCmptType(ipsProject);
        
        policyType.setSupertype(policySupertype.getQualifiedName());
        productType.setSupertype(productSupertype.getQualifiedName());
        
        IProductCmpt product = newProductCmpt(policyType.findProductCmptType(ipsProject), "Product");
        generation = product.getProductCmptGeneration(0);
    }
    
    public void testConstructor_QName() {
        comparator = new PropertyValueComparator(productType.getQualifiedName(), ipsProject);
        assertEquals(productType, comparator.getProductCmptType());
    }
    
    public void testAttributeValueOrder() {
        IProductCmptTypeAttribute a1 = productType.newAttribute("a1");
        IProductCmptTypeAttribute a2 = productType.newAttribute("a2");
        IProductCmptTypeAttribute a3 = productSupertype.newAttribute("a3");
        
        IAttributeValue value2 = generation.newAttributeValue(a2, "value2");
        IAttributeValue value3 = generation.newAttributeValue(a3, "value3");
        IAttributeValue value1 = generation.newAttributeValue(a1, "value1");
        
        comparator = new PropertyValueComparator(productType, ipsProject);
        IPropertyValue[] values = generation.getAttributeValues();
        Arrays.sort(values, comparator);
        
        assertEquals(value3, values[0]); // values for supertype attributes first
        assertEquals(value1, values[1]);
        assertEquals(value2, values[2]);
    }

}
