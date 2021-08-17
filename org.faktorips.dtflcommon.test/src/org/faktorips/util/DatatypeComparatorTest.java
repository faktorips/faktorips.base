/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.util;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import org.faktorips.datatype.AbstractDatatype;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DatatypeComparatorTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractDatatype datatype1;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractDatatype datatype2;

    private final DatatypeComparator comparator = new DatatypeComparator();

    @Test
    public void testCompareTo_Same() throws Exception {
        doReturn("a").when(datatype1).getQualifiedName();
        doReturn("a").when(datatype2).getQualifiedName();

        assertTrue(comparator.compare(datatype1, datatype1) == 0);
        assertTrue(comparator.compare(datatype1, datatype2) == 0);
        assertTrue(comparator.compare(datatype2, datatype1) == 0);
        assertTrue(comparator.compare(null, null) == 0);
    }

    @Test
    public void testCompareTo() throws Exception {
        doReturn("a").when(datatype1).getQualifiedName();
        doReturn("b").when(datatype2).getQualifiedName();

        assertTrue(comparator.compare(datatype1, datatype2) < 0);
        assertTrue(comparator.compare(datatype2, datatype1) > 0);
    }

    @Test
    public void testCompareTo_Null() throws Exception {
        doReturn("a").when(datatype1).getQualifiedName();

        assertTrue(comparator.compare(null, datatype1) < 0);
        assertTrue(comparator.compare(datatype1, null) > 0);
    }

    @Test
    public void testCompareTo_NullQName() throws Exception {
        doReturn(null).when(datatype1).getQualifiedName();
        doReturn("a").when(datatype2).getQualifiedName();

        assertTrue(comparator.compare(datatype1, datatype2) < 0);
        assertTrue(comparator.compare(datatype2, datatype1) > 0);
    }

}
