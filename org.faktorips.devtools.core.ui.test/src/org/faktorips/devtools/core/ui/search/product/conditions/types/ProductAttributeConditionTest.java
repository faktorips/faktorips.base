/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmpt;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;
import org.junit.Before;
import org.junit.Test;

public class ProductAttributeConditionTest extends AbstractIpsPluginTest {

    private final ProductAttributeConditionType condition = new ProductAttributeConditionType();
    private IIpsProject ipsProject;
    private ProductCmptType productCmptType;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();

        productCmptType = newProductCmptType(ipsProject, "aaa.ProductType");
    }

    @Test
    public void testGetSearchableElements() {

        IProductCmptTypeAttribute attributZahlweise = productCmptType.newProductCmptTypeAttribute("zahlweise");
        IProductCmptTypeAttribute attributeVersicherungssumme = productCmptType
                .newProductCmptTypeAttribute("versicherungssumme");

        List<? extends IIpsElement> searchableElements = condition.getSearchableElements(productCmptType);

        assertEquals(2, searchableElements.size());
        assertTrue(searchableElements.contains(attributZahlweise));
        assertTrue(searchableElements.contains(attributeVersicherungssumme));

    }

    @Test
    public void testGetValueDatatypeAndValueSet() {
        IProductCmptTypeAttribute attribut = productCmptType.newProductCmptTypeAttribute("zahlweise");

        IntegerDatatype integerDatatype = new IntegerDatatype();
        attribut.setDatatype(integerDatatype.getName());

        IValueSet valueSet = mock(IValueSet.class);
        when(valueSet.copy(any(IValueSetOwner.class), any(String.class))).thenReturn(valueSet);

        attribut.setValueSetCopy(valueSet);

        assertTrue(condition.hasValueSet());

        try {
            condition.getAllowedValues(attribut);
            fail();
        } catch (Exception e) {
            // nix
        }

        assertEquals(integerDatatype, condition.getValueDatatype(attribut));
        assertEquals(valueSet, condition.getValueSet(attribut));

    }

    @Test
    public void testGetSearchOperatorTypes() {
        IProductCmptTypeAttribute attributComparable = productCmptType.newProductCmptTypeAttribute("zahlweise");

        IntegerDatatype integerDatatype = new IntegerDatatype();
        attributComparable.setDatatype(integerDatatype.getName());

        List<EqualitySearchOperatorType> equalitySearchOperatorTypes = Arrays.asList(EqualitySearchOperatorType
                .values());
        List<ComparableSearchOperatorType> comparableSearchOperatorTypes = Arrays.asList(ComparableSearchOperatorType
                .values());

        List<ISearchOperatorType> searchOperatorTypes = condition.getSearchOperatorTypes(attributComparable);

        assertEquals(equalitySearchOperatorTypes.size() + comparableSearchOperatorTypes.size(),
                searchOperatorTypes.size());

        assertFalse(equalitySearchOperatorTypes.retainAll(searchOperatorTypes));
        assertFalse(comparableSearchOperatorTypes.retainAll(searchOperatorTypes));
    }

    @Test
    public void testOperandProvider() throws CoreException {
        String value = "monatlich";

        IProductCmptTypeAttribute attribut = productCmptType.newProductCmptTypeAttribute("zahlweise");

        ProductCmpt productCmpt = newProductCmpt(productCmptType, "ich.bin.ein.Baustein");
        ProductCmptGeneration generation = (ProductCmptGeneration)productCmpt.newGeneration();
        IAttributeValue attributeValue = generation.newAttributeValue(attribut);
        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, value));

        IOperandProvider operandProvider = condition.createOperandProvider(attribut);

        assertEquals(value, operandProvider.getSearchOperand(generation));
    }

    @Test
    public void testOperandProvider_staticAttribute() throws CoreException {
        String value = "monatlich";

        IProductCmptTypeAttribute attribut = productCmptType.newProductCmptTypeAttribute("zahlweise");
        attribut.setChangingOverTime(false);

        ProductCmpt productCmpt = newProductCmpt(productCmptType, "ich.bin.ein.Baustein");
        IAttributeValue attributeValue = (IAttributeValue)productCmpt.newPropertyValue(attribut);
        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, value));

        IOperandProvider operandProvider = condition.createOperandProvider(attribut);

        assertEquals(value, operandProvider.getSearchOperand(productCmpt));
    }

}
