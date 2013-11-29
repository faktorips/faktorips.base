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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultStructureTest {

    private ResultStructure<Integer> resultStructure;

    @Test
    public void testGetObject() throws Exception {
        resultStructure = new ResultStructure<Integer>();

        SearchStructure<Integer> structure = resultStructure.get(1);

        assertSame(resultStructure, structure);
    }

    @Test
    public void testGetObject_Null() throws Exception {
        resultStructure = new ResultStructure<Integer>();
        SearchStructure<Integer> structure = resultStructure.get(null);

        assertSame(resultStructure, structure);
    }

    @Test
    public void testGet() throws Exception {
        resultStructure = new ResultStructure<Integer>(123);

        Set<Integer> set = resultStructure.get();

        assertThat(set, hasItem(123));
    }

    @Test
    public void testMerge() throws Exception {
        resultStructure = new ResultStructure<Integer>(321);
        ResultStructure<Integer> resultStructure2 = new ResultStructure<Integer>(123);

        resultStructure.merge(resultStructure2);

        assertThat(resultStructure.get(), hasItem(123));
        assertThat(resultStructure.get(), hasItem(321));
    }

    @Test
    public void testMerge_emptyResults() throws Exception {
        resultStructure = new ResultStructure<Integer>();
        ResultStructure<Integer> resultStructure2 = new ResultStructure<Integer>();

        resultStructure.merge(resultStructure2);

        assertTrue(resultStructure.get().isEmpty());
    }

}
