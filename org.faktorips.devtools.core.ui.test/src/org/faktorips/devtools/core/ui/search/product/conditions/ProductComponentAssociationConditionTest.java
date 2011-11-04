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

package org.faktorips.devtools.core.ui.search.product.conditions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;
import org.junit.Before;
import org.junit.Test;

public class ProductComponentAssociationConditionTest extends AbstractIpsPluginTest {

    private final ProductComponentAssociationCondition condition = new ProductComponentAssociationCondition();
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
    public void testGetSearchableElements() throws CoreException {

        IProductCmptTypeAssociation link = productCmptType.newProductCmptTypeAssociation();
        IProductCmptTypeAssociation link2 = productCmptType.newProductCmptTypeAssociation();

        List<? extends IIpsElement> searchableElements = condition.getSearchableElements(productCmptType);

        assertEquals(2, searchableElements.size());
        assertTrue(searchableElements.contains(link));
        assertTrue(searchableElements.contains(link2));

    }

    @Test
    public void testGetValueDatatypeAndValueSet() throws CoreException {
        IProductCmptTypeAssociation link = productCmptType.newProductCmptTypeAssociation();
        String linkedProduct = "bbb.LinkedProduct";
        ProductCmpt productCmpt = newProductCmpt(linkedProductCmptType, linkedProduct);
        String linkedProductTwo = "bbb.LinkedProductTwo";
        ProductCmpt productCmpt2 = newProductCmpt(linkedProductCmptType, linkedProductTwo);

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
    public void testOperandProvider() throws CoreException {
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
}
