/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
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

import java.util.GregorianCalendar;

import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IConfiguredValueSet;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.junit.Before;
import org.junit.Test;

public class ValueSetSearchOperatorWithoutGenerationTest extends AbstractIpsPluginTest {

    private static final GregorianCalendar VALID_FROM = new GregorianCalendar(2013, 11, 1);
    private ValueDatatype valueDatatype;
    private IOperandProvider operandProvider;
    private IProductCmpt productCmpt;

    @Override
    @Before
    public void setUp() throws Exception {
        valueDatatype = new IntegerDatatype();

        IIpsProject ipsProject = newIpsProject("Project");
        PolicyCmptType policyCmptType = newPolicyAndProductCmptType(ipsProject, "PolicyType", "ProductType");
        IPolicyCmptTypeAttribute attribute = policyCmptType.newPolicyCmptTypeAttribute("attribute");
        attribute.setDatatype(valueDatatype.getName());
        attribute.setValueSetConfiguredByProduct(true);
        operandProvider = new PolicyAttributeConditionOperandProvider(attribute);

        productCmpt = newProductCmpt(ipsProject, "Product");
        productCmpt.setProductCmptType(policyCmptType.getProductCmptType());

        IConfiguredValueSet configuredValueSet = productCmpt.newPropertyValue(attribute, IConfiguredValueSet.class);
        IValueSet source = new RangeValueSet(configuredValueSet, "partIDXXXX", "2", "5", "1");
        configuredValueSet.setValueSet(source);

    }

    @Test
    public void testValueSetOperatorHit() {
        ValueSetSearchOperator operator = new ValueSetSearchOperator(valueDatatype, ValueSetSearchOperatorType.ALLOWED,
                operandProvider, "3");

        assertTrue(operator.check(productCmpt));
    }

    @Test
    public void testValueSetOperatorHitEnumValue() {
        ValueSetSearchOperator operator = new ValueSetSearchOperator(valueDatatype, ValueSetSearchOperatorType.ALLOWED,
                operandProvider, "3");

        assertTrue(operator.check(productCmpt));
    }

    @Test
    public void testValueSetOperatorMiss() {
        ValueSetSearchOperator operator = new ValueSetSearchOperator(valueDatatype, ValueSetSearchOperatorType.ALLOWED,
                operandProvider, "7");

        assertFalse(operator.check(productCmpt));
    }

    @Test
    public void testValueSetOperatorMissProductCmptGeneration() {
        ValueSetSearchOperator operator = new ValueSetSearchOperator(valueDatatype, ValueSetSearchOperatorType.ALLOWED,
                operandProvider, "7");
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration(VALID_FROM);

        assertFalse(operator.check(generation));
    }
}
