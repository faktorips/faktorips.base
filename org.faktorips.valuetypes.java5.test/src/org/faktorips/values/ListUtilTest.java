/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.values;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

public class ListUtilTest extends TestCase {

    private List<Object> objectList;

    @Override
    @Before
    public void setUp() {
        objectList = new ArrayList<Object>();
        objectList.add(new Integer(3));
        objectList.add(new Integer(5));
        objectList.add(new Integer(8));
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
        objectList.add(new Long(15));

        List<? extends Number> numberList = ListUtil.convert(objectList, Number.class);

        assertEquals(4, numberList.size());
        assertEquals(15, numberList.get(3).intValue());
    }

    @Test
    public void testConvert_classCastException() {
        objectList.add(new Long(15));
        try {
            ListUtil.convert(objectList, Integer.class);
            fail();
        } catch (ClassCastException e) {
            // expected
            // WTF? Why doesn't (expected=ClassCastException) work?
        }
    }

    private void assertListContent(List<? extends Number> integerList) {
        assertEquals(3, integerList.size());
        assertEquals(3, integerList.get(0).intValue());
        assertEquals(5, integerList.get(1).intValue());
        assertEquals(8, integerList.get(2).intValue());
    }
}
