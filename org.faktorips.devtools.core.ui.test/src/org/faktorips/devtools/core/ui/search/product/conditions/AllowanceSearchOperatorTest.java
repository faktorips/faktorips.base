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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.devtools.core.internal.model.productcmpt.ProductCmptGeneration;
import org.faktorips.devtools.core.internal.model.valueset.RangeValueSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.junit.Test;

public class AllowanceSearchOperatorTest {

    @Test
    public void testInteger() throws CoreException {

        String argument = "10000";

        IProductCmptGeneration productCmptGeneration = mock(ProductCmptGeneration.class);

        IPolicyCmptTypeAttribute valueSetOwner = mock(IPolicyCmptTypeAttribute.class);
        when(valueSetOwner.findValueDatatype(any(IIpsProject.class))).thenReturn(new IntegerDatatype());

        String partId = "PART_ID";
        IValueSet fittingValueSet = new RangeValueSet(valueSetOwner, partId, "7000", "12000", "1000");
        IValueSet notFittingBoundsValueSet = new RangeValueSet(valueSetOwner, partId, "7000", "9000", "1000");
        IValueSet notFittingStepsValueSet = new RangeValueSet(valueSetOwner, partId, "8000", "14000", "1500");

        AllowanceSearchOperator operator = new AllowanceSearchOperator(new IntegerDatatype(),
                AllowanceSearchOperatorType.ALLOWED, mock(IOperandProvider.class), argument);

        assertTrue(operator.check(fittingValueSet, productCmptGeneration));
        assertFalse(operator.check(notFittingBoundsValueSet, productCmptGeneration));
        assertFalse(operator.check(notFittingStepsValueSet, productCmptGeneration));

        operator = new AllowanceSearchOperator(new IntegerDatatype(), AllowanceSearchOperatorType.NOT_ALLOWED,
                mock(IOperandProvider.class), argument);

        assertFalse(operator.check(fittingValueSet, productCmptGeneration));
        assertTrue(operator.check(notFittingBoundsValueSet, productCmptGeneration));
        assertTrue(operator.check(notFittingStepsValueSet, productCmptGeneration));
    }
}
