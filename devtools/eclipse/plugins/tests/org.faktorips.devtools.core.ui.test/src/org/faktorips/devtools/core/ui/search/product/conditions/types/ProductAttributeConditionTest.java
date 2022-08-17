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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmpt;
import org.faktorips.devtools.model.internal.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.internal.productcmpttype.ProductCmptType;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.faktorips.devtools.model.valueset.IValueSetOwner;
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
    public void testOperandProvider() {
        String value = "monatlich";

        IProductCmptTypeAttribute attribut = productCmptType.newProductCmptTypeAttribute("zahlweise");

        ProductCmpt productCmpt = newProductCmpt(productCmptType, "ich.bin.ein.Baustein");
        ProductCmptGeneration generation = (ProductCmptGeneration)productCmpt.newGeneration();
        IAttributeValue attributeValue = generation.newAttributeValue(attribut);
        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, value));

        IOperandProvider operandProvider = condition.createOperandProvider(attribut);

        assertEquals(value, ((SingleValueHolder)operandProvider.getSearchOperand(generation)).getValue().getContent());
    }

    @Test
    public void testOperandProvider_staticAttribute() {
        String value = "monatlich";

        IProductCmptTypeAttribute attribute = productCmptType.newProductCmptTypeAttribute("zahlweise");
        attribute.setChangingOverTime(false);

        ProductCmpt productCmpt = newProductCmpt(productCmptType, "ich.bin.ein.Baustein");
        IAttributeValue attributeValue = productCmpt.newPropertyValue(attribute, IAttributeValue.class);
        attributeValue.setValueHolder(new SingleValueHolder(attributeValue, value));

        IOperandProvider operandProvider = condition.createOperandProvider(attribute);

        assertEquals(value, ((SingleValueHolder)operandProvider.getSearchOperand(productCmpt)).getValue().getContent());
    }

    @Test
    public void testOperandProvider_MultiValueAttribute() {
        String value1 = "monatlich";
        String value2 = "taeglich";

        IProductCmptTypeAttribute attribut = productCmptType.newProductCmptTypeAttribute("zahlweise");
        attribut.setMultiValueAttribute(true);

        ProductCmpt productCmpt = newProductCmpt(productCmptType, "ich.bin.ein.Baustein");
        ProductCmptGeneration generation = (ProductCmptGeneration)productCmpt.newGeneration();
        IAttributeValue attributeValue = generation.newAttributeValue(attribut);
        SingleValueHolder singleValueHolder1 = new SingleValueHolder(attributeValue, value1);
        SingleValueHolder singleValueHolder2 = new SingleValueHolder(attributeValue, value2);
        List<ISingleValueHolder> singleValueHolderList = new ArrayList<>();
        singleValueHolderList.add(singleValueHolder1);
        singleValueHolderList.add(singleValueHolder2);
        attributeValue.setValueHolder(new MultiValueHolder(attributeValue, singleValueHolderList));

        IOperandProvider operandProvider = condition.createOperandProvider(attribut);

        List<ISingleValueHolder> valueList = ((MultiValueHolder)operandProvider.getSearchOperand(generation))
                .getValue();
        assertEquals(value1, valueList.get(0).getValue().getContent());
        assertEquals(value2, valueList.get(1).getValue().getContent());
    }

}
