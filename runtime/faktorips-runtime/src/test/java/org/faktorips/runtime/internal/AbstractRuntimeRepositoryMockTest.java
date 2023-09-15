/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.faktorips.runtime.IRuntimeRepositoryLookup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AbstractRuntimeRepositoryMockTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractRuntimeRepository repositoryA;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractRuntimeRepository repositoryB;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractRuntimeRepository repositoryC;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractRuntimeRepository repositoryD;

    @Test
    public void testGetEnumValuesDefinedInType() {
        AbstractRuntimeRepository abstractRuntimeRepository = mock(AbstractRuntimeRepository.class, CALLS_REAL_METHODS);

        List<ExtensibleEnum> enumValues = abstractRuntimeRepository.getEnumValuesDefinedInType(ExtensibleEnum.class);

        assertEquals(ExtensibleEnum.VALUES, enumValues);
    }

    @Test
    public void testGetEnumValuesDefinedInType_JavaEnum() {
        AbstractRuntimeRepository abstractRuntimeRepository = mock(AbstractRuntimeRepository.class, CALLS_REAL_METHODS);

        List<RealEnum> enumValues = abstractRuntimeRepository.getEnumValuesDefinedInType(RealEnum.class);

        assertEquals(List.of(RealEnum.values()), enumValues);
    }

    @Test
    public void testGetEnumValuesReferencedContent() throws Exception {
        ExtensibleEnum myEnum = mock(ExtensibleEnum.class);
        List<ExtensibleEnum> list = new ArrayList<>();
        list.add(myEnum);
        initRepositoryReferences(repositoryA, repositoryB, repositoryC, repositoryD);
        doReturn(list).when(repositoryD).getEnumValuesInternal(ExtensibleEnum.class);
        List<ExtensibleEnum> expected = new ArrayList<>(ExtensibleEnum.VALUES);
        expected.addAll(list);

        List<ExtensibleEnum> enumValues = repositoryA.getEnumValues(ExtensibleEnum.class);

        assertEquals(expected, enumValues);
    }

    @Test
    public void testGetEnumValuesNoContent() throws Exception {
        initRepositoryReferences(repositoryA, repositoryB, repositoryC, repositoryD);
        List<ExtensibleEnum> expected = new ArrayList<>(ExtensibleEnum.VALUES);

        List<ExtensibleEnum> enumValues = repositoryA.getEnumValues(ExtensibleEnum.class);

        assertEquals(expected, enumValues);
    }

    @Test
    public void testGetAllModelTypeImplementationClasses() throws Exception {
        initRepositoryReferences(repositoryA, repositoryB);

        Set<String> modelTypeImplementationClasses = repositoryA.getAllModelTypeImplementationClasses();

        assertNotNull(modelTypeImplementationClasses);
        verify(repositoryA).getAllModelTypeImplementationClasses(anySet());
        verify(repositoryB).getAllModelTypeImplementationClasses(anySet());
    }

    private void initRepositoryReferences(AbstractRuntimeRepository referencingRepository,
            AbstractRuntimeRepository... referencedRepositories) throws Exception {
        Field declaredField = AbstractRuntimeRepository.class.getDeclaredField("repositories");
        declaredField.setAccessible(true);
        declaredField.set(referencingRepository, new ArrayList<>());
        mockRepository(referencingRepository);

        for (AbstractRuntimeRepository referencedRepository : referencedRepositories) {
            declaredField.set(referencedRepository, new ArrayList<>());
            referencingRepository.addDirectlyReferencedRepository(referencedRepository);
            mockRepository(referencedRepository);
        }

    }

    private void mockRepository(AbstractRuntimeRepository repository) {
        doReturn(null).when(repository).getEnumValueLookupService(ExtensibleEnum.class);
        doReturn(null).when(repository).getEnumValuesInternal(ExtensibleEnum.class);
        doNothing().when(repository).getAllModelTypeImplementationClasses(anySet());
    }

    @Test
    public void testSetGetRuntimeRepositoryLookup() {
        IRuntimeRepositoryLookup repositoryLookupMock = mock(IRuntimeRepositoryLookup.class);
        repositoryA.setRuntimeRepositoryLookup(repositoryLookupMock);

        IRuntimeRepositoryLookup runtimeRepositoryLookup = repositoryA.getRuntimeRepositoryLookup();

        assertSame(repositoryLookupMock, runtimeRepositoryLookup);
    }

    public static class ExtensibleEnum {

        public static final ExtensibleEnum VALUE1 = new ExtensibleEnum();

        public static final ExtensibleEnum VALUE2 = new ExtensibleEnum();

        public static final List<ExtensibleEnum> VALUES = List.of(VALUE1, VALUE2);

    }

    public enum RealEnum {
        FOO,
        BAR
    }

}
