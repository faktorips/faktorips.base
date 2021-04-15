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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SearchStructureTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private SearchStructure<Object> structure;

    @Test
    public void testGetUnique_getOne() throws Exception {
        Object expectedObject = mock(Object.class);
        setUpResultSet(expectedObject);

        Object unique = structure.getUnique();

        assertEquals(expectedObject, unique);
    }

    @Test
    public void testGetUniqueWithDefault_getOne() throws Exception {
        Object expectedObject = mock(Object.class);
        setUpResultSet(expectedObject);

        Object unique = structure.getUnique(null);

        assertEquals(expectedObject, unique);
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetUnique_getNone() throws Exception {
        setUpResultSet();

        structure.getUnique();
    }

    @Test(expected = AssertionError.class)
    public void testGetUnique_getNonUnique() throws Exception {
        setUpResultSet(mock(Object.class), mock(Object.class));

        structure.getUnique();
    }

    @Test(expected = AssertionError.class)
    public void testGetUniqueWithDefault_getNonUnique() throws Exception {
        setUpResultSet(mock(Object.class), mock(Object.class));

        structure.getUnique(null);
    }

    private void setUpResultSet(Object... expectedObject) {
        Set<Object> resultSet = new HashSet<>();
        resultSet.addAll(Arrays.asList(expectedObject));
        doReturn(resultSet).when(structure).get();
    }

    @Test
    public void testGetUniqueWithDefault_getNone() throws Exception {
        setUpResultSet();
        Object defaultValue = mock(Object.class);

        Object actual = structure.getUnique(defaultValue);

        assertSame(defaultValue, actual);
    }

}
