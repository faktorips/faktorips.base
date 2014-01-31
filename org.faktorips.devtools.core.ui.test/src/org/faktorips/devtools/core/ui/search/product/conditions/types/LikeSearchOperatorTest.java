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

import org.junit.Test;

public class LikeSearchOperatorTest {

    @Test
    public void testLike() {
        LikeSearchOperatorType likeType = LikeSearchOperatorType.LIKE;

        LikeSearchOperator searchOperator = (LikeSearchOperator)likeType.createSearchOperator(null, null, "*kas?o");

        assertTrue(searchOperator.check("VollKasko", null));
        assertTrue(searchOperator.check("kasko", null));
        assertFalse(searchOperator.check("VollKaskoLvb", null));
        assertFalse(searchOperator.check(null, null));
        assertFalse(searchOperator.check("VollKaskko", null));
        assertFalse(searchOperator.check("", null));

        LikeSearchOperatorType likeTypeNot = LikeSearchOperatorType.NOT_LIKE;

        LikeSearchOperator searchOperatorNot = (LikeSearchOperator)likeTypeNot.createSearchOperator(null, null,
                "*kas?o");

        assertFalse(searchOperatorNot.check("VollKasko", null));
        assertFalse(searchOperatorNot.check("kasko", null));
        assertTrue(searchOperatorNot.check("VollKaskoLvb", null));
        assertFalse(searchOperatorNot.check(null, null));
        assertTrue(searchOperatorNot.check("VollKaskko", null));
        assertTrue(searchOperatorNot.check("", null));
    }
}
