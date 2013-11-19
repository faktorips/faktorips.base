/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MultiMapTest {

    private MultiMap<Object, Object> multimap;

    @Mock
    private Object indexKey1;

    @Mock
    private Object indexKey2;

    @Mock
    private Object row1;

    @Mock
    private Object row3;

    @Mock
    private Object row2;

    private Set<Object> resultSet;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        multimap = new MultiMap<Object, Object>();
        resultSet = new HashSet<Object>();
    }

    @Test
    public void test_get_ReturnEmptySet() {
        Set<Object> setOfIndexKey1 = multimap.get(indexKey1);
        assertEquals(resultSet, setOfIndexKey1);
    }

    @Test
    public void test_add_newValueForNewKey() {
        multimap.put(indexKey1, row1);
        Set<Object> setOfIndexKey1 = multimap.get(indexKey1);
        resultSet.add(row1);

        assertEquals(resultSet, setOfIndexKey1);
    }

    @Test
    public void test_add_newValueForExistingKey() {
        multimap.put(indexKey2, row1);
        multimap.put(indexKey2, row3);
        Set<Object> setOfIndexKey2 = multimap.get(indexKey2);
        resultSet.add(row1);
        resultSet.add(row3);

        assertEquals(resultSet, setOfIndexKey2);
    }

    @Test
    public void test_add_SameValuesForDifferentKeys() {
        multimap.put(indexKey2, row1);
        multimap.put(indexKey2, row3);
        multimap.put(indexKey1, row1);
        multimap.put(indexKey1, row3);
        Set<Object> setOfIndexKey2 = multimap.get(indexKey2);
        Set<Object> setOfIndexKey1 = multimap.get(indexKey1);

        assertEquals(setOfIndexKey2, setOfIndexKey1);
    }
}