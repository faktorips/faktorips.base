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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AbstractMapStructureTest {

    private final Map<String, ResultStructure<Integer>> map = new HashMap<>();

    private final Map<String, ResultStructure<Integer>> map2 = new HashMap<>();

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractMapStructure<String, ResultStructure<Integer>, Integer> abstractMapStructure;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractMapStructure<String, ResultStructure<Integer>, Integer> abstractMapStructure2;

    @Before
    public void setUpStructure() {
        when(abstractMapStructure.getMap()).thenReturn(map);
        when(abstractMapStructure2.getMap()).thenReturn(map2);
    }

    @Test
    public void testPut() throws Exception {
        abstractMapStructure.put("abc", new ResultStructure<>(123));

        assertEquals(123, abstractMapStructure.getUnique().intValue());
        assertEquals(1, abstractMapStructure.getMap().size());
        assertThat(abstractMapStructure.getMap().keySet(), hasItem("abc"));
        assertThat(abstractMapStructure.getMap().values(), hasItem(new ResultStructure<>(123)));
    }

    @Test
    public void testPut_multipleSameKey() throws Exception {
        abstractMapStructure.put("abc", new ResultStructure<>(123));
        abstractMapStructure.put("abc", new ResultStructure<>(321));
        ResultStructure<Integer> resultSet = createResultSet(123, 321);

        assertThat(abstractMapStructure.get(), hasItem(123));
        assertThat(abstractMapStructure.get(), hasItem(321));
        assertEquals(1, abstractMapStructure.getMap().size());
        assertThat(abstractMapStructure.getMap().keySet(), hasItem("abc"));
        assertThat(abstractMapStructure.getMap().values(), hasItem(resultSet));
    }

    @Test
    public void testPut_multipleDifferentKeys() throws Exception {
        abstractMapStructure.put("abc", new ResultStructure<>(123));
        abstractMapStructure.put("xyz", new ResultStructure<>(321));

        assertThat(abstractMapStructure.get(), hasItem(123));
        assertThat(abstractMapStructure.get(), hasItem(321));
        assertEquals(2, abstractMapStructure.getMap().size());
        assertThat(abstractMapStructure.getMap().keySet(), hasItem("abc"));
        assertThat(abstractMapStructure.getMap().keySet(), hasItem("xyz"));
        assertEquals(123, abstractMapStructure.getMap().get("abc").getUnique().intValue());
        assertEquals(321, abstractMapStructure.getMap().get("xyz").getUnique().intValue());
    }

    @Test
    public void testMerge_sameKey() throws Exception {
        map.put("abc", new ResultStructure<>(123));
        map2.put("abc", new ResultStructure<>(321));

        abstractMapStructure.merge(abstractMapStructure2);

        Map<String, ResultStructure<Integer>> resultMap = abstractMapStructure.getMap();
        assertEquals(1, map.size());
        assertEquals(1, map2.size());
        assertEquals(1, resultMap.size());
        assertThat(resultMap.keySet(), hasItem("abc"));
        assertThat(resultMap.values(), hasItem(createResultSet(123, 321)));
    }

    @Test
    public void testMerge_differentKeys() throws Exception {
        map.put("abc", new ResultStructure<>(123));
        map2.put("xyz", new ResultStructure<>(321));

        abstractMapStructure.merge(abstractMapStructure2);

        Map<String, ResultStructure<Integer>> resultMap = abstractMapStructure.getMap();
        assertEquals(2, resultMap.size());
        assertThat(resultMap.keySet(), hasItem("abc"));
        assertThat(resultMap.keySet(), hasItem("xyz"));
        assertEquals(createResultSet(123), resultMap.get("abc"));
        assertEquals(createResultSet(321), resultMap.get("xyz"));
    }

    /**
     * This test verifies that merge simply calls the put method for deep merge. This is important
     * because the put method may be overridden (like in {@link TwoColumnRangeStructure}) and may
     * implements own merging strategies.
     */
    @Test
    public void testMerge_callsPut() throws Exception {
        map.put("abc", new ResultStructure<>(123));
        ResultStructure<Integer> value2 = new ResultStructure<>(321);
        map2.put("xyz", value2);

        abstractMapStructure.merge(abstractMapStructure2);

        verify(abstractMapStructure).put("xyz", value2);
    }

    private ResultStructure<Integer> createResultSet(Integer... values) {
        ResultStructure<Integer> resultSet = new ResultStructure<>();
        for (Integer value : values) {
            ResultStructure<Integer> otherResult = new ResultStructure<>(value);
            resultSet.merge(otherResult);
        }
        return resultSet;
    }

    @Test
    public void testGet_multipleSameKey() throws Exception {
        abstractMapStructure.put("abc", new ResultStructure<>(123));
        abstractMapStructure.put("abc", new ResultStructure<>(321));

        assertThat(abstractMapStructure.get(), hasItem(123));
        assertThat(abstractMapStructure.get(), hasItem(321));
    }

    @Test
    public void testGet_multipleDifferentKeys() throws Exception {
        abstractMapStructure.put("abc", new ResultStructure<>(123));
        abstractMapStructure.put("xyz", new ResultStructure<>(321));

        assertThat(abstractMapStructure.get(), hasItem(123));
        assertThat(abstractMapStructure.get(), hasItem(321));
    }

}
