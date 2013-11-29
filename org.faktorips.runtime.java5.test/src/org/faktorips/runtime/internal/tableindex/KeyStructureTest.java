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

package org.faktorips.runtime.internal.tableindex;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class KeyStructureTest {

    private final KeyStructure<String, ResultStructure<Integer>, Integer> hashMapStructure = KeyStructure.create();

    private final KeyStructure<String, KeyStructure<String, ResultStructure<Integer>, Integer>, Integer> nestingMapStructure = KeyStructure
            .create();

    @Test
    public void testGet_noValue() throws Exception {
        SearchStructure<Integer> structure = hashMapStructure.get("abc");

        assertTrue(structure.get().isEmpty());
    }

    @Test
    public void testGet_null() throws Exception {
        SearchStructure<Integer> structure = hashMapStructure.get(null);

        assertTrue(structure.get().isEmpty());
    }

    @Test
    public void testGet_oneValue() throws Exception {
        hashMapStructure.put("abc", ResultStructure.createWith(123));

        SearchStructure<Integer> structure = hashMapStructure.get("abc");

        assertEquals(123, structure.getUnique().intValue());
    }

    @Test
    public void testGet_multipleValueSameKey() throws Exception {
        hashMapStructure.put("abc", ResultStructure.createWith(123));
        hashMapStructure.put("abc", ResultStructure.createWith(321));

        SearchStructure<Integer> structure = hashMapStructure.get("abc");

        assertEquals(2, hashMapStructure.get().size());
        assertThat(hashMapStructure.get(), hasItem(123));
        assertThat(hashMapStructure.get(), hasItem(321));
        assertEquals(2, structure.get().size());
        assertThat(structure.get(), hasItem(123));
        assertThat(structure.get(), hasItem(321));
    }

    @Test
    public void testGet_multipleValueSameValue() throws Exception {
        hashMapStructure.put("abc", ResultStructure.createWith(123));
        hashMapStructure.put("xyz", ResultStructure.createWith(123));

        SearchStructure<Integer> structure = hashMapStructure.get("abc");
        SearchStructure<Integer> structure2 = hashMapStructure.get("xyz");

        assertEquals(1, hashMapStructure.get().size());
        assertThat(hashMapStructure.get(), hasItem(123));
        assertEquals(1, structure.get().size());
        assertThat(structure.get(), hasItem(123));
        assertEquals(1, structure2.get().size());
        assertThat(structure2.get(), hasItem(123));
    }

    @Test
    public void testGet_multipleValueDifferentKeysValues() throws Exception {
        hashMapStructure.put("abc", ResultStructure.createWith(123));
        hashMapStructure.put("xyz", ResultStructure.createWith(321));

        SearchStructure<Integer> structure = hashMapStructure.get("abc");
        SearchStructure<Integer> structure2 = hashMapStructure.get("xyz");

        assertEquals(2, hashMapStructure.get().size());
        assertThat(hashMapStructure.get(), hasItem(123));
        assertThat(hashMapStructure.get(), hasItem(321));
        assertEquals(1, structure.get().size());
        assertThat(structure.get(), hasItem(123));
        assertEquals(1, structure2.get().size());
        assertThat(structure2.get(), hasItem(321));
    }

    @Test
    public void testPut_nestedStructure() throws Exception {
        KeyStructure<String, ResultStructure<Integer>, Integer> nestedMapStructure = KeyStructure.create();
        nestedMapStructure.put("abc", ResultStructure.createWith(123));
        KeyStructure<String, ResultStructure<Integer>, Integer> nestedMapStructure2 = KeyStructure.create();
        nestedMapStructure.put("xyz", ResultStructure.createWith(321));
        nestingMapStructure.put("aaa", nestedMapStructure);
        nestingMapStructure.put("aaa", nestedMapStructure2);

        SearchStructure<Integer> resultNestedStructure = nestingMapStructure.get("aaa");

        assertEquals(2, resultNestedStructure.get().size());
        assertThat(resultNestedStructure.get(), hasItem(123));
        assertThat(resultNestedStructure.get(), hasItem(321));
        assertEquals(123, resultNestedStructure.get("abc").getUnique().intValue());
        assertEquals(321, resultNestedStructure.get("xyz").getUnique().intValue());
    }

}
