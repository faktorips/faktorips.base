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

package org.faktorips.devtools.core.model.productcmpt;

import java.util.Arrays;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
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

    @Override
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
        IProductCmptTypeAttribute a1 = productType.newProductCmptTypeAttribute("a1");
        IProductCmptTypeAttribute a2 = productType.newProductCmptTypeAttribute("a2");
        IProductCmptTypeAttribute a3 = productSupertype.newProductCmptTypeAttribute("a3");

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
