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

import org.faktorips.datatype.classtypes.StringDatatype;
import org.faktorips.devtools.model.internal.productcmpt.SingleValueHolder;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class LikeSearchOperatorTest {

    @Mock
    private IAttributeValue attributeValue;

    private SingleValueHolder singleValueHolder1 = new SingleValueHolder(attributeValue, "VollKasko");
    private SingleValueHolder singleValueHolder2 = new SingleValueHolder(attributeValue, "kasko");
    private SingleValueHolder singleValueHolder3 = new SingleValueHolder(attributeValue, "VollKaskoLvb");
    private SingleValueHolder singleValueHolder4 = new SingleValueHolder(attributeValue, "VollKaskko");
    private SingleValueHolder singleValueHolder5 = new SingleValueHolder(attributeValue, "");

    @Test
    public void testLike() {
        LikeSearchOperatorType likeType = LikeSearchOperatorType.LIKE;

        LikeSearchOperator searchOperator = (LikeSearchOperator)likeType.createSearchOperator(null,
                new StringDatatype(), "*kas?o");

        assertTrue(searchOperator.check(singleValueHolder1, null));
        assertTrue(searchOperator.check(singleValueHolder2, null));
        assertFalse(searchOperator.check(singleValueHolder3, null));
        assertFalse(searchOperator.check(null, null));
        assertFalse(searchOperator.check(singleValueHolder4, null));
        assertFalse(searchOperator.check(singleValueHolder5, null));

        LikeSearchOperatorType likeTypeNot = LikeSearchOperatorType.NOT_LIKE;

        LikeSearchOperator searchOperatorNot = (LikeSearchOperator)likeTypeNot.createSearchOperator(null,
                new StringDatatype(), "*kas?o");

        assertFalse(searchOperatorNot.check(singleValueHolder1, null));
        assertFalse(searchOperatorNot.check(singleValueHolder2, null));
        assertTrue(searchOperatorNot.check(singleValueHolder3, null));
        assertFalse(searchOperatorNot.check(null, null));
        assertTrue(searchOperatorNot.check(singleValueHolder4, null));
        assertTrue(searchOperatorNot.check(singleValueHolder5, null));
    }
}
