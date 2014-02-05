/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.persistence.IPersistenceProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AbstractJpaAnnotationGeneratorTest {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AbstractJpaAnnotationGenerator jpaAnnotationGenerator;

    @Mock
    private IIpsElement ipsElement;

    @Mock
    private IIpsProject project;

    @Mock
    private StandardBuilderSet builderSet;

    @Mock
    private IPersistenceProvider expectedPersistenceProvider;

    @Before
    public void setUp() {
        when(ipsElement.getIpsProject()).thenReturn(project);
        when(project.getIpsArtefactBuilderSet()).thenReturn(builderSet);
    }

    @Test
    public void testgetPersistenceProvider() {
        when(builderSet.getPersistenceProviderImplementation()).thenReturn(expectedPersistenceProvider);
        IPersistenceProvider persistenceProvider = jpaAnnotationGenerator.getPersistenceProvider(ipsElement);

        assertEquals(expectedPersistenceProvider, persistenceProvider);
    }

    @Test
    public void testgetPersistentProvider_returnNull() {
        IPersistenceProvider persistenceProvider = jpaAnnotationGenerator.getPersistenceProvider(ipsElement);

        assertEquals(null, persistenceProvider);
    }

    @Test
    public void testIsGenerateAnnotationFor_TrueForInternal() {
        when(builderSet.getPersistenceProviderImplementation()).thenReturn(expectedPersistenceProvider);
        doReturn(true).when(jpaAnnotationGenerator).isGenerateAnnotationForInternal(ipsElement);

        boolean result = jpaAnnotationGenerator.isGenerateAnnotationFor(ipsElement);

        assertTrue(result);
    }

    @Test
    public void testIsGenerateAnnotationFor__FalseForInternal() {
        when(builderSet.getPersistenceProviderImplementation()).thenReturn(expectedPersistenceProvider);
        doReturn(false).when(jpaAnnotationGenerator).isGenerateAnnotationForInternal(ipsElement);

        boolean result = jpaAnnotationGenerator.isGenerateAnnotationFor(ipsElement);

        assertFalse(result);
    }

    @Test
    public void testIsGenerateAnnotationFor_NoPersistenceProvider() {
        doReturn(true).when(jpaAnnotationGenerator).isGenerateAnnotationForInternal(ipsElement);

        boolean result = jpaAnnotationGenerator.isGenerateAnnotationFor(ipsElement);

        assertFalse(result);
    }
}
