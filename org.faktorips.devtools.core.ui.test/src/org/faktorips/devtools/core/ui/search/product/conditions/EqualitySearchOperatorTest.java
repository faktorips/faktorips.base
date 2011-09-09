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
