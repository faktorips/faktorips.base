/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
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
import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.core.internal.model.productcmpt.MultiValueHolder;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.junit.Before;
import org.junit.Test;

public class ContainsSearchOperatorTest extends AbstractIpsPluginTest {

    private static final GregorianCalendar VALID_FROM = new GregorianCalendar(2013, 11, 1);
    private ValueDatatype valueDatatype;
    private IOperandProvider operandProvider;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IProductCmptTypeAttribute attribute;
    private IAttributeValue attributeValue;

    @Override
    @Before
    public void setUp() throws Exception {
        valueDatatype = new StringDatatype();
        IIpsProject ipsProject = newIpsProject("Project");
        IProductCmptType productCmptType = newProductCmptType(ipsProject, "ProductType");
        attribute = productCmptType.newProductCmptTypeAttribute("attribute");
        attribute.setDatatype(valueDatatype.getName());
        attribute.setMultiValueAttribute(true);
        operandProvider = new ProductAttributeConditionType().createOperandProvider(attribute);

        productCmpt = newProductCmpt(ipsProject, "Product");
        productCmpt.setProductCmptType(productCmptType.getName());
        generation = (IProductCmptGeneration)productCmpt.newGeneration(VALID_FROM);

        attributeValue = generation.newAttributeValue(attribute);
        String value1 = "testValue1";
        String value2 = "testValue2";
        SingleValueHolder singleValueHolder1 = new SingleValueHolder(attributeValue, value1);
        SingleValueHolder singleValueHolder2 = new SingleValueHolder(attributeValue, value2);
        List<SingleValueHolder> singleValueHolderList = new ArrayList<SingleValueHolder>();
        singleValueHolderList.add(singleValueHolder1);
        singleValueHolderList.add(singleValueHolder2);
        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue, singleValueHolderList);
        attributeValue.setValueHolder(multiValueHolder);
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
        List<SingleValueHolder> singleValueHolderList = new ArrayList<SingleValueHolder>();
        singleValueHolderList.add(singleValueHolder1);
        MultiValueHolder multiValueHolder = new MultiValueHolder(attributeValue, singleValueHolderList);
        attributeValue.setValueHolder(multiValueHolder);

        ContainsSearchOperator operator = new ContainsSearchOperator(valueDatatype,
                ContainsSearchOperatorType.CONTAINS, operandProvider, null);

        assertTrue(operator.check(generation));
    }
}
