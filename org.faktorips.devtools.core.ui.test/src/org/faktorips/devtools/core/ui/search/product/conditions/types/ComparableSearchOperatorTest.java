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

import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.devtools.core.model.productcmpt.IProductPartsContainer;
import org.junit.Test;

public class ComparableSearchOperatorTest {

    @Test
    public void testInteger() {
        ComparableSearchOperatorType searchOperatorType = ComparableSearchOperatorType.LESS;
        String fuenf = "5";
        String vier = "4";

        IOperandProvider operandProvider = new IOperandProvider() {

            @Override
            public String getSearchOperand(IProductPartsContainer productPartsContainer) {
                return null;
            }
        };

        ComparableSearchOperator searchOperator = new ComparableSearchOperator(new IntegerDatatype(),
                searchOperatorType, operandProvider, fuenf);
        assertTrue(searchOperator.check(vier, null));
        assertFalse(searchOperator.check(fuenf, null));

        searchOperatorType = ComparableSearchOperatorType.GREATER_OR_EQUALS;

        searchOperator = new ComparableSearchOperator(new IntegerDatatype(), searchOperatorType, operandProvider, vier);
        assertTrue(searchOperator.check(vier, null));
        assertTrue(searchOperator.check(fuenf, null));
    }
}
