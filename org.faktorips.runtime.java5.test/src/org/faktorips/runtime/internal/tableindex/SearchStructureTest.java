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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.faktorips.runtime.internal.tableindex.SearchStructure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SearchStructureTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private SearchStructure<?> structure;

    @Test
    public void testGetUnique_getOne() throws Exception {
        Object expectedObject = mock(Object.class);
        setUpResultSet(expectedObject);

        Object unique = structure.getUnique();

        assertEquals(expectedObject, unique);
    }

    @Test
    public void testGetUnique_getNone() throws Exception {
        setUpResultSet();

        Object unique = structure.getUnique();

        assertNull(unique);
    }

    @Test(expected = RuntimeException.class)
    public void testGetUnique_getNonUnique() throws Exception {
        setUpResultSet(mock(Object.class), mock(Object.class));

        structure.getUnique();
    }

    private void setUpResultSet(Object... expectedObject) {
        Set<Object> resultSet = new HashSet<Object>();
        resultSet.addAll(Arrays.asList(expectedObject));
        doReturn(resultSet).when(structure).get();
    }

}
