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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.devtools.model.internal.valueset.RangeValueSet;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.model.valueset.IValueSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AllowanceSearchOperatorTest {

    @Mock
    private IIpsProject ipsProject;

    @Mock
    private IIpsProjectProperties ipsProjectProperties;

    @Mock
    private IProductCmptGeneration productCmptGeneration;

    @Test
    public void testInteger() {

        String argument = "10000";

        when(productCmptGeneration.getIpsProject()).thenReturn(ipsProject);
        when(ipsProject.getReadOnlyProperties()).thenReturn(ipsProjectProperties);

        IPolicyCmptTypeAttribute valueSetOwner = mock(IPolicyCmptTypeAttribute.class);
        when(valueSetOwner.findValueDatatype(any(IIpsProject.class))).thenReturn(new IntegerDatatype());
        when(valueSetOwner.getIpsProject()).thenReturn(ipsProject);

        String partId = "PART_ID";
        IValueSet fittingValueSet = new RangeValueSet(valueSetOwner, partId, "7000", "12000", "1000");
        IValueSet notFittingBoundsValueSet = new RangeValueSet(valueSetOwner, partId, "7000", "9000", "1000");
        IValueSet notFittingStepsValueSet = new RangeValueSet(valueSetOwner, partId, "8000", "14000", "1500");

        ValueSetSearchOperator operator = new ValueSetSearchOperator(new IntegerDatatype(),
                ValueSetSearchOperatorType.ALLOWED, mock(IOperandProvider.class), argument);

        assertTrue(operator.check(fittingValueSet, productCmptGeneration));
        assertFalse(operator.check(notFittingBoundsValueSet, productCmptGeneration));
        assertFalse(operator.check(notFittingStepsValueSet, productCmptGeneration));

        operator = new ValueSetSearchOperator(new IntegerDatatype(), ValueSetSearchOperatorType.NOT_ALLOWED,
                mock(IOperandProvider.class), argument);

        assertFalse(operator.check(fittingValueSet, productCmptGeneration));
        assertTrue(operator.check(notFittingBoundsValueSet, productCmptGeneration));
        assertTrue(operator.check(notFittingStepsValueSet, productCmptGeneration));
    }
}
