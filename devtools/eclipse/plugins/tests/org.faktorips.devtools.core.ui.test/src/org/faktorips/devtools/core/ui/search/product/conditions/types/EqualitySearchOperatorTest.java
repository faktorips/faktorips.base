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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.faktorips.datatype.classtypes.IntegerDatatype;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class EqualitySearchOperatorTest {

    @Mock
    private IAttributeValue attributeValue;

    private SingleValueHolder singleValueHolder4 = new SingleValueHolder(attributeValue, "4");
    private SingleValueHolder singleValueHolder5 = new SingleValueHolder(attributeValue, "5");

    @Test
    public void testInteger() {
        EqualitySearchOperatorType equalityType = EqualitySearchOperatorType.EQUALITY;
        String vier = "4";

        AbstractStringSearchOperator<?> searchOperator = (AbstractStringSearchOperator<?>)equalityType
                .createSearchOperator(null, new IntegerDatatype(), vier);
        assertTrue(searchOperator.check(singleValueHolder4, null));
        assertFalse(searchOperator.check(singleValueHolder5, null));
        assertFalse(searchOperator.checkInternal(null));

        EqualitySearchOperatorType inEqualityType = EqualitySearchOperatorType.INEQUALITY;

        searchOperator = (AbstractStringSearchOperator<?>)inEqualityType.createSearchOperator(null,
                new IntegerDatatype(), vier);
        assertFalse(searchOperator.check(singleValueHolder4, null));
        assertTrue(searchOperator.check(singleValueHolder5, null));
        assertTrue(searchOperator.checkInternal(null));
    }

}
