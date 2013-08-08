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

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractRuntimeRepositoryTest {

    @Mock
    private Object enumValueA;

    @Mock
    private Object enumValueB;

    private List<? extends Object> enums;

    private AbstractRuntimeRepository abstractRuntimeRepository;

    @Before
    public void createAbstractRuntimeRepository() throws Exception {
        abstractRuntimeRepository = mock(AbstractRuntimeRepository.class, CALLS_REAL_METHODS);
    }

    @Before
    public void createEnumList() {
        ArrayList<Object> tmpList = new ArrayList<Object>();
        tmpList.add(enumValueA);
        tmpList.add(enumValueB);
        enums = new ArrayList<Object>(tmpList);
    }

    @Test
    public void testCompareEnumValues_null() throws Exception {
        assertTrue(abstractRuntimeRepository.compareEnumValues(null, enumValueB) > 0);
        assertTrue(abstractRuntimeRepository.compareEnumValues(enumValueA, null) < 0);
        assertTrue(abstractRuntimeRepository.compareEnumValues(null, null) == 0);
    }

    @Test
    public void testCompareEnumValues_equal() throws Exception {
        assertTrue(abstractRuntimeRepository.compareEnumValues(enumValueA, enumValueA) == 0);
    }

    @Test
    public void testCompareEnumValues_bothSameEnumType() throws Exception {
        doReturn(enums).when(abstractRuntimeRepository).getEnumValuesInternal(enumValueA.getClass());
        doReturn(enums).when(abstractRuntimeRepository).getEnumValuesInternal(enumValueB.getClass());
        doReturn(null).when(abstractRuntimeRepository).getEnumValueLookupService(enumValueA.getClass());
        doReturn(null).when(abstractRuntimeRepository).getEnumValueLookupService(enumValueB.getClass());

        assertTrue(abstractRuntimeRepository.compareEnumValues(enumValueA, enumValueB) < 0);
        assertTrue(abstractRuntimeRepository.compareEnumValues(enumValueB, enumValueA) > 0);
    }

    @Test(expected = ClassCastException.class)
    public void testCompareEnumValues_bothNotInEnumList() throws Exception {
        doReturn(new ArrayList<Object>()).when(abstractRuntimeRepository).getEnumValuesInternal(enumValueA.getClass());
        doReturn(null).when(abstractRuntimeRepository).getEnumValueLookupService(enumValueA.getClass());
        doReturn(null).when(abstractRuntimeRepository).getEnumValueLookupService(enumValueB.getClass());

        abstractRuntimeRepository.compareEnumValues(enumValueA, enumValueB);
    }

    @Test(expected = ClassCastException.class)
    public void testCompareEnumValues_firstNotInEnumList() throws Exception {
        enums.remove(enumValueA);
        doReturn(enums).when(abstractRuntimeRepository).getEnumValuesInternal(enumValueA.getClass());
        doReturn(null).when(abstractRuntimeRepository).getEnumValueLookupService(enumValueA.getClass());
        doReturn(null).when(abstractRuntimeRepository).getEnumValueLookupService(enumValueB.getClass());

        abstractRuntimeRepository.compareEnumValues(enumValueA, enumValueB);
    }

    @Test(expected = ClassCastException.class)
    public void testCompareEnumValues_secondNotInEnumList() throws Exception {
        enums.remove(enumValueB);
        doReturn(enums).when(abstractRuntimeRepository).getEnumValuesInternal(enumValueA.getClass());
        doReturn(null).when(abstractRuntimeRepository).getEnumValueLookupService(enumValueA.getClass());
        doReturn(null).when(abstractRuntimeRepository).getEnumValueLookupService(enumValueB.getClass());

        abstractRuntimeRepository.compareEnumValues(enumValueA, enumValueB);
    }

}
