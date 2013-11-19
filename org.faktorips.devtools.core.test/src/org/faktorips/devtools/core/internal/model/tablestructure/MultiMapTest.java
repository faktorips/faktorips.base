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

package org.faktorips.devtools.core.internal.model.tablestructure;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.faktorips.devtools.core.internal.model.tablecontents.Row;
import org.faktorips.devtools.core.model.tablestructure.IIndex;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MultiMapTest {

    private MultiMap multimap;

    @Mock
    private IIndex indexKey1;

    @Mock
    private IIndex indexKey2;

    @Mock
    private Row row1;

    @Mock
    private Row row3;

    private Set<Row> resultSet;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        multimap = new MultiMap();
        resultSet = new HashSet<Row>();
    }

    @Test
    public void test_addinternal_newValueForNewKey() {
        multimap.addInternal(indexKey1, row1);
        Set<Row> setOfIndexKey1 = multimap.getSet(indexKey1);
        resultSet.add(row1);

        assertEquals(resultSet, setOfIndexKey1);
    }

    @Test
    public void test_addinternal_newValueForExistingKey() {
        multimap.addInternal(indexKey2, row1);
        multimap.addInternal(indexKey2, row3);
        Set<Row> setOfIndexKey2 = multimap.getSet(indexKey2);
        resultSet.add(row1);
        resultSet.add(row3);

        assertEquals(resultSet, setOfIndexKey2);
    }
}