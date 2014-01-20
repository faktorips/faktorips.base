/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.faktorips.runtime.IRuntimeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractRuntimeRepositoryTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractRuntimeRepository repositoryA;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractRuntimeRepository repositoryB;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractRuntimeRepository repositoryC;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractRuntimeRepository repositoryD;

    @Test
    public void testGetEnumValuesDefinedInType() throws Exception {
        AbstractRuntimeRepository abstractRuntimeRepository = mock(AbstractRuntimeRepository.class, CALLS_REAL_METHODS);

        List<EnumTestClass> enumValues = abstractRuntimeRepository.getEnumValuesDefinedInType(EnumTestClass.class);

        assertEquals(EnumTestClass.VALUES, enumValues);
    }

    @Test
    public void testGetEnumValuesReferencedContent() throws Exception {
        EnumTestClass myEnum = mock(EnumTestClass.class);
        List<EnumTestClass> list = new ArrayList<EnumTestClass>();
        list.add(myEnum);
        initRepositoryReferences(repositoryA, repositoryB, repositoryC, repositoryD);
        doReturn(list).when(repositoryD).getEnumValuesInternal(EnumTestClass.class);
        List<EnumTestClass> expected = new ArrayList<EnumTestClass>(EnumTestClass.VALUES);
        expected.addAll(list);

        List<EnumTestClass> enumValues = repositoryA.getEnumValues(EnumTestClass.class);

        assertEquals(expected, enumValues);
    }

    @Test
    public void testGetEnumValuesNoContent() throws Exception {
        initRepositoryReferences(repositoryA, repositoryB, repositoryC, repositoryD);
        List<EnumTestClass> expected = new ArrayList<EnumTestClass>(EnumTestClass.VALUES);

        List<EnumTestClass> enumValues = repositoryA.getEnumValues(EnumTestClass.class);

        assertEquals(expected, enumValues);
    }

    private void initRepositoryReferences(AbstractRuntimeRepository repositoryA,
            AbstractRuntimeRepository repositoryB,
            AbstractRuntimeRepository repositoryC,
            AbstractRuntimeRepository repositoryD) throws Exception {
        Field declaredField = AbstractRuntimeRepository.class.getDeclaredField("repositories");
        declaredField.setAccessible(true);
        declaredField.set(repositoryA, new ArrayList<IRuntimeRepository>());
        declaredField.set(repositoryB, new ArrayList<IRuntimeRepository>());
        declaredField.set(repositoryC, new ArrayList<IRuntimeRepository>());
        declaredField.set(repositoryD, new ArrayList<IRuntimeRepository>());

        repositoryA.addDirectlyReferencedRepository(repositoryB);
        repositoryA.addDirectlyReferencedRepository(repositoryC);
        repositoryC.addDirectlyReferencedRepository(repositoryD);

        mockRepositories();
    }

    private void mockRepositories() {
        doReturn(null).when(repositoryA).getEnumValueLookupService(EnumTestClass.class);
        doReturn(null).when(repositoryB).getEnumValueLookupService(EnumTestClass.class);
        doReturn(null).when(repositoryC).getEnumValueLookupService(EnumTestClass.class);
        doReturn(null).when(repositoryD).getEnumValueLookupService(EnumTestClass.class);

        doReturn(null).when(repositoryA).getEnumValuesInternal(EnumTestClass.class);
        doReturn(null).when(repositoryB).getEnumValuesInternal(EnumTestClass.class);
        doReturn(null).when(repositoryC).getEnumValuesInternal(EnumTestClass.class);
        doReturn(null).when(repositoryD).getEnumValuesInternal(EnumTestClass.class);
    }

    public static class EnumTestClass {

        public static final EnumTestClass VALUE1 = new EnumTestClass();

        public static final EnumTestClass VALUE2 = new EnumTestClass();

        public static final List<EnumTestClass> VALUES = Arrays.asList(VALUE1, VALUE2);

    }
}
