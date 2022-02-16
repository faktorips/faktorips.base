/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;

public class ProductComponentAssociationConditionTest extends AbstractIpsPluginTest {

    private final ProductComponentAssociationConditionType condition = new ProductComponentAssociationConditionType();
    private IIpsProject ipsProject;
    private ProductCmptType productCmptType;
    private ProductCmptType linkedProductCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();

        productCmptType = newProductCmptType(ipsProject, "aaa.ProductType");
        linkedProductCmptType = newProductCmptType(ipsProject, "aaa.LinkedsProductType");
    }

    @Test
    public void testGetSearchableElements() {

        IProductCmptTypeAssociation link = productCmptType.newProductCmptTypeAssociation();
        IProductCmptTypeAssociation link2 = productCmptType.newProductCmptTypeAssociation();

        List<? extends IIpsElement> searchableElements = condition.getSearchableElements(productCmptType);

        assertEquals(2, searchableElements.size());
        assertTrue(searchableElements.contains(link));
        assertTrue(searchableElements.contains(link2));

    }

    @Test
    public void testGetValueDatatypeAndValueSet() {
        IProductCmptTypeAssociation link = productCmptType.newProductCmptTypeAssociation();
        String linkedProduct = "bbb.LinkedProduct";
        newProductCmpt(linkedProductCmptType, linkedProduct);
        String linkedProductTwo = "bbb.LinkedProductTwo";
        newProductCmpt(linkedProductCmptType, linkedProductTwo);

        Datatype datatype = new StringDatatype();

        link.setTarget(linkedProductCmptType.getQualifiedName());

        assertEquals(datatype, condition.getValueDatatype(link));

        assertFalse(condition.hasValueSet());

        try {
            condition.getValueSet(link);
            fail();
        } catch (Exception e) {
            // nix
        }

        Collection<?> allowedValues = condition.getAllowedValues(link);

        assertEquals(2, allowedValues.size());
        assertTrue(allowedValues.contains(linkedProduct));
        assertTrue(allowedValues.contains(linkedProductTwo));
    }

    @Test
    public void testGetSearchOperatorTypes() {
        IProductCmptTypeAssociation link = productCmptType.newProductCmptTypeAssociation();

        List<? extends ISearchOperatorType> searchOperatorTypes = condition.getSearchOperatorTypes(link);

        assertEquals(2, searchOperatorTypes.size());

        assertTrue(searchOperatorTypes.contains(ReferenceSearchOperatorType.REFERENCE));
        assertTrue(searchOperatorTypes.contains(ReferenceSearchOperatorType.NO_REFERENCE));
    }

    @Test
    public void testOperandProvider() {
        ProductCmpt productCmpt = newProductCmpt(productCmptType, "ich.bin.ein.Baustein");

        ProductCmptGeneration generation = (ProductCmptGeneration)productCmpt.newGeneration();

        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();

        IProductCmptLink link = generation.newLink(association);

        String linkedProduct = "bbb.LinkedProduct";
        newProductCmpt(linkedProductCmptType, linkedProduct);

        link.setTarget(linkedProduct);

        IProductCmptLink link2 = generation.newLink(association);

        String linkedProduct2 = "bbb.LinkedProductTwo";
        newProductCmpt(linkedProductCmptType, linkedProduct2);

        link2.setTarget(linkedProduct2);

        IOperandProvider operandProvider = condition.createOperandProvider(association);

        List<?> searchOperand = (List<?>)operandProvider.getSearchOperand(generation);

        assertEquals(2, searchOperand.size());
        assertTrue(searchOperand.contains(linkedProduct));
        assertTrue(searchOperand.contains(linkedProduct2));
    }

    @Test
    public void testOperandProvider_staticLinks() {
        ProductCmpt productCmpt = newProductCmpt(productCmptType, "ich.bin.ein.Baustein");

        IProductCmptTypeAssociation association = productCmptType.newProductCmptTypeAssociation();
        association.setChangingOverTime(false);

        IProductCmptLink link = productCmpt.newLink(association);

        String linkedProduct = "bbb.LinkedProduct";
        newProductCmpt(linkedProductCmptType, linkedProduct);

        link.setTarget(linkedProduct);

        IProductCmptLink link2 = productCmpt.newLink(association);

        String linkedProduct2 = "bbb.LinkedProductTwo";
        newProductCmpt(linkedProductCmptType, linkedProduct2);

        link2.setTarget(linkedProduct2);

        IOperandProvider operandProvider = condition.createOperandProvider(association);

        List<?> searchOperand = (List<?>)operandProvider.getSearchOperand(productCmpt);

        assertEquals(2, searchOperand.size());
        assertTrue(searchOperand.contains(linkedProduct));
        assertTrue(searchOperand.contains(linkedProduct2));
    }

}
