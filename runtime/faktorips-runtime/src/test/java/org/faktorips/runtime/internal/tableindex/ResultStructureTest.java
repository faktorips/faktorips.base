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
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class ResultStructureTest {

    private ResultStructure<Integer> resultStructure;

    @Test
    public void testGetObject() throws Exception {
        resultStructure = new ResultStructure<>();

        SearchStructure<Integer> structure = resultStructure.get(1);

        assertSame(resultStructure, structure);
    }

    @Test
    public void testGetObject_Null() throws Exception {
        resultStructure = new ResultStructure<>();
        SearchStructure<Integer> structure = resultStructure.get(null);

        assertSame(resultStructure, structure);
    }

    @Test
    public void testGet() throws Exception {
        resultStructure = new ResultStructure<>(123);

        Set<Integer> set = resultStructure.get();

        assertThat(set, hasItem(123));
    }

    @Test
    public void testMerge() throws Exception {
        resultStructure = new ResultStructure<>(321);
        ResultStructure<Integer> resultStructure2 = new ResultStructure<>(123);

        resultStructure.merge(resultStructure2);

        assertThat(resultStructure.get(), hasItem(123));
        assertThat(resultStructure.get(), hasItem(321));
    }

    @Test
    public void testMerge_emptyResults() throws Exception {
        resultStructure = new ResultStructure<>();
        ResultStructure<Integer> resultStructure2 = new ResultStructure<>();

        resultStructure.merge(resultStructure2);

        assertTrue(resultStructure.get().isEmpty());
    }

    @Test
    public void testCopy_equalNotSame() {
        resultStructure = new ResultStructure<>(initResultSet());
        ResultStructure<Integer> copiedStructure = resultStructure.copy();

        assertEquals(copiedStructure, resultStructure);
        assertNotSame(copiedStructure, resultStructure);
    }

    @Test
    public void testCopy_deepCopy() {
        resultStructure = new ResultStructure<>(initResultSet());
        ResultStructure<Integer> copiedStructure = resultStructure.copy();
        HashSet<Integer> newSet = new HashSet<>();
        newSet.add(123);
        copiedStructure.merge(new ResultStructure<>(newSet));

        assertThat(resultStructure.get(), not(hasItem(123)));
    }

    private Set<Integer> initResultSet() {
        Set<Integer> resultSet1 = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            resultSet1.add(i);
        }
        return resultSet1;
    }
}
