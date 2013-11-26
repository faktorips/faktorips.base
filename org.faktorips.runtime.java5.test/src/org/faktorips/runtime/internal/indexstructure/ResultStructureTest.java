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

package org.faktorips.runtime.internal.indexstructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResultStructureTest {

    @InjectMocks
    private ResultStructure<Integer> resultStructure;

    @Test
    public void testGetObject() throws Exception {
        Structure<Integer> structure = resultStructure.get(1);

        assertSame(resultStructure, structure);
    }

    @Test
    public void testGetObjectNull() throws Exception {
        Structure<Integer> structure = resultStructure.get(null);

        assertSame(resultStructure, structure);
    }

    @Test
    public void testGet() throws Exception {
        HashSet<Integer> result = new HashSet<Integer>();
        result.add(123);
        resultStructure = new ResultStructure<Integer>(result);

        Set<Integer> set = resultStructure.get();

        assertEquals(result, set);
    }

    @Test
    public void testMerge() throws Exception {
        HashSet<Integer> result = new HashSet<Integer>();
        result.add(321);
        resultStructure = new ResultStructure<Integer>(result);
        HashSet<Integer> result2 = new HashSet<Integer>();
        result2.add(123);
        ResultStructure<Integer> resultStructure2 = new ResultStructure<Integer>(result2);

        resultStructure.merge(resultStructure2);

        assertThat(resultStructure.get(), hasItem(123));
        assertThat(resultStructure.get(), hasItem(321));
    }

}
