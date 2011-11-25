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

package org.faktorips.devtools.core.ui.search.product.conditions.types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.faktorips.devtools.core.ui.search.product.conditions.types.LikeSearchOperator;
import org.faktorips.devtools.core.ui.search.product.conditions.types.LikeSearchOperatorType;
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
