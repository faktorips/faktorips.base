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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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

    @Test
    public void testGetUnique_getNone() throws Exception {
        assertThrows(NoSuchElementException.class, () -> {
            setUpResultSet();

            structure.getUnique();
        });
    }

    @Test
    public void testGetUnique_getNonUnique() throws Exception {
        assertThrows(AssertionError.class, () -> {
            setUpResultSet(mock(Object.class), mock(Object.class));

            structure.getUnique();
        });
    }

    @Test
    public void testGetUniqueWithDefault_getNonUnique() throws Exception {
        assertThrows(AssertionError.class, () -> {
            setUpResultSet(mock(Object.class), mock(Object.class));

            structure.getUnique(null);
        });
    }

    private void setUpResultSet(Object... expectedObject) {
        Set<Object> resultSet = new HashSet<>();
        resultSet.addAll(List.of(expectedObject));
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
