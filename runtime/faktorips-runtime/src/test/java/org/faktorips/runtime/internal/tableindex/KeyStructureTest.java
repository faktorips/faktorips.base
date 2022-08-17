/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal.tableindex;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

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

    @Test
    public void testCopy_DeepCopyForMap() {
        KeyStructure<String, ResultStructure<Integer>, Integer> structure = KeyStructure.create();
        initKeyStructureMap(structure);
        KeyStructure<String, ResultStructure<Integer>, Integer> copyStructure = structure.copy();
        Map<String, ResultStructure<Integer>> structureMap = structure.getMap();
        Map<String, ResultStructure<Integer>> copiedMap = ((AbstractMapStructure<String, ResultStructure<Integer>, Integer>)copyStructure)
                .getMap();

        assertEquals(structureMap.get("ID_1"), copiedMap.get("ID_1"));
        assertNotSame(structureMap.get("ID_1"), copiedMap.get("ID_1"));
        assertEquals(structureMap.get("ID_2"), copiedMap.get("ID_2"));
        assertNotSame(structureMap.get("ID_2"), copiedMap.get("ID_2"));
    }

    @Test
    public void testCopy_CopyOfObject() {
        KeyStructure<String, ResultStructure<Integer>, Integer> structure = KeyStructure.create();
        initKeyStructureMap(structure);
        KeyStructure<String, ResultStructure<Integer>, Integer> copyStructure = structure.copy();

        assertEquals(((AbstractMapStructure<String, ResultStructure<Integer>, Integer>)copyStructure).getMap(),
                structure.getMap());
        assertNotSame(copyStructure, structure);
    }

    private void initKeyStructureMap(AbstractMapStructure<String, ResultStructure<Integer>, Integer> structure) {
        ResultStructure<Integer> first = new ResultStructure<>(1);
        ResultStructure<Integer> second = new ResultStructure<>(2);
        structure.put("ID_1", first);
        structure.put("ID_2", second);
    }

}
