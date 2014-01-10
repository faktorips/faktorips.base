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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.junit.Test;

public class EqualitySearchOperatorTest {

    @Test
    public void testInteger() {
        EqualitySearchOperatorType equalityType = EqualitySearchOperatorType.EQUALITY;
        String fuenf = "5";
        String vier = "4";

        AbstractStringSearchOperator<?> searchOperator = (AbstractStringSearchOperator<?>)equalityType.createSearchOperator(null,
                new IntegerDatatype(), vier);
        assertTrue(searchOperator.check(vier, null));
        assertFalse(searchOperator.check(fuenf, null));
        assertFalse(searchOperator.checkInternal(null));

        EqualitySearchOperatorType inEqualityType = EqualitySearchOperatorType.INEQUALITY;

        searchOperator = (AbstractStringSearchOperator<?>)inEqualityType.createSearchOperator(null, new IntegerDatatype(),
                vier);
        assertFalse(searchOperator.check(vier, null));
        assertTrue(searchOperator.check(fuenf, null));
        assertTrue(searchOperator.checkInternal(null));
    }

}
