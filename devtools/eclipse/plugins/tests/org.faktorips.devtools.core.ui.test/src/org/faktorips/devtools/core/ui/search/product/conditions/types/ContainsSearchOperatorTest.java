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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.DecimalDatatype;
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.model.internal.productcmpt.MultiValueHolder;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.productcmpt.ISingleValueHolder;
import org.faktorips.devtools.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.model.productcmpttype.IProductCmptTypeAttribute;
import org.junit.Before;
import org.junit.Test;

public class ContainsSearchOperatorTest extends AbstractIpsPluginTest {

    private static final GregorianCalendar VALID_FROM = new GregorianCalendar(2013, 11, 1);
    private ValueDatatype valueDatatype;
    private IOperandProvider operandProvider;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IProductCmptTypeAttribute multiValueAttribute;
    private IAttributeValue attributeValue;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject ipsProject = newIpsProject("Project");
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        multiValueAttribute = productCmptType.newProductCmptTypeAttribute("attribute");
        multiValueAttribute.setMultiValueAttribute(true);
        valueDatatype = new StringDatatype();
        multiValueAttribute.setDatatype(valueDatatype.getName());
        operandProvider = new ProductAttributeConditionType().createOperandProvider(multiValueAttribute);

        productCmpt = newProductCmpt(ipsProject, "Product");
        productCmpt.setProductCmptType(productCmptType.getName());
        generation = (IProductCmptGeneration)productCmpt.newGeneration(VALID_FROM);

        attributeValue = generation.newAttributeValue(multiValueAttribute);
        createValues("testValue1", "testValue2");
    }

    @Test
    public void testCheck_Hit() {
        ContainsSearchOperator operator = new ContainsSearchOperator(valueDatatype,
                ContainsSearchOperatorType.CONTAINS, operandProvider, "testValue1");

        assertTrue(operator.check(generation));
    }

    @Test
    public void testCheck_Hit2() {
        ContainsSearchOperator operator = new ContainsSearchOperator(valueDatatype,
                ContainsSearchOperatorType.CONTAINS, operandProvider, "testValue2");

        assertTrue(operator.check(generation));
    }

    @Test
    public void testCheck_NoHit() {
        ContainsSearchOperator operator = new ContainsSearchOperator(valueDatatype,
                ContainsSearchOperatorType.CONTAINS, operandProvider, "notContainedValue");

        assertFalse(operator.check(generation));
    }

    @Test
    public void testCheck_ArgumentIsNull() {
        ContainsSearchOperator operator = new ContainsSearchOperator(valueDatatype,
                ContainsSearchOperatorType.CONTAINS, operandProvider, null);

        assertFalse(operator.check(generation));
    }

    @Test
    public void testCheck_NullValueIsContained() {
        String value = null;
        SingleValueHolder singleValueHolder1 = new SingleValueHolder(attributeValue, value);
        List<ISingleValueHolder> singleValueHolderList = new ArrayList<>();
        singleValueHolderList.add(singleValueHolder1);
        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue, singleValueHolderList);
        attributeValue.setValueHolder(multiValueHolder);

        ContainsSearchOperator operator = new ContainsSearchOperator(valueDatatype,
                ContainsSearchOperatorType.CONTAINS, operandProvider, null);

        assertTrue(operator.check(generation));
    }

    @Test
    public void testCheck_ValueDatatype() {
        valueDatatype = new DecimalDatatype();
        multiValueAttribute.setDatatype(valueDatatype.getName());
        operandProvider = new ProductAttributeConditionType().createOperandProvider(multiValueAttribute);
        createValues("1.0");

        ContainsSearchOperator operator = new ContainsSearchOperator(valueDatatype,
                ContainsSearchOperatorType.CONTAINS, operandProvider, "1");

        assertTrue(operator.check(generation));
    }

    /**
     * Creates values which are contained in the multiValueAttribute.
     */
    private void createValues(String... values) {
        List<ISingleValueHolder> singleValueHolderList = new ArrayList<>();
        for (String value : values) {
            SingleValueHolder singleValueHolder = new SingleValueHolder(attributeValue, value);
            singleValueHolderList.add(singleValueHolder);
        }
        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue, singleValueHolderList);
        attributeValue.setValueHolder(multiValueHolder);
    }
}
