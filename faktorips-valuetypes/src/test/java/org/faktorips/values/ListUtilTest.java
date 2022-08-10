/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.values;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ListUtilTest {

    private List<Object> objectList;

    @Before
    public void setUp() {
        objectList = ListUtil.newList((Object)3, 5, 8);
    }

    @Test
    public void testConvert() {
        List<? extends Number> numberList = ListUtil.convert(objectList, Number.class);
        assertListContent(numberList);
    }

    @Test
    public void testConvertToFinalClass() {
        List<? extends Integer> integerList = ListUtil.convert(objectList, Integer.class);
        assertListContent(integerList);
    }

    @Test
    public void testConvert_differentElementTypes() {
        objectList.add(Long.valueOf(15));

        List<? extends Number> numberList = ListUtil.convert(objectList, Number.class);

        assertEquals(4, numberList.size());
        assertEquals(15, numberList.get(3).intValue());
    }

    @Test(expected = ClassCastException.class)
    public void testConvert_classCastException() {
        objectList.add(Long.valueOf(15));
        ListUtil.convert(objectList, Integer.class);
    }

    private void assertListContent(List<? extends Number> integerList) {
        assertEquals(3, integerList.size());
        assertEquals(3, integerList.get(0).intValue());
        assertEquals(5, integerList.get(1).intValue());
        assertEquals(8, integerList.get(2).intValue());
    }
}
