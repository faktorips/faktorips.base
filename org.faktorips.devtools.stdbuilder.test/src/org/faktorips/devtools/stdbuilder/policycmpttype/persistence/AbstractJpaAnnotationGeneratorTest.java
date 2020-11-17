/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
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

import org.faktorips.devtools.model.builder.IPersistenceProvider;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xmodel.AbstractGeneratorModelNode;
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
    private IIpsObjectPartContainer ipsPartContainer;

    @Mock
    private AbstractGeneratorModelNode modelNode;

    @Mock
    private IIpsProject project;

    @Mock
    private StandardBuilderSet builderSet;

    @Mock
    private IPersistenceProvider expectedPersistenceProvider;

    @Before
    public void setUp() {
        when(ipsPartContainer.getIpsProject()).thenReturn(project);
        when(project.getIpsArtefactBuilderSet()).thenReturn(builderSet);
        when(modelNode.getIpsObjectPartContainer()).thenReturn(ipsPartContainer);
    }

    @Test
    public void testgetPersistenceProvider() {
        when(builderSet.getPersistenceProvider()).thenReturn(expectedPersistenceProvider);
        IPersistenceProvider persistenceProvider = jpaAnnotationGenerator.getPersistenceProvider(ipsPartContainer
                .getIpsProject());

        assertEquals(expectedPersistenceProvider, persistenceProvider);
    }

    @Test
    public void testgetPersistentProvider_returnNull() {
        IPersistenceProvider persistenceProvider = jpaAnnotationGenerator.getPersistenceProvider(ipsPartContainer
                .getIpsProject());

        assertEquals(null, persistenceProvider);
    }

    @Test
    public void testIsGenerateAnnotationFor_TrueForInternal() {
        when(builderSet.getPersistenceProvider()).thenReturn(expectedPersistenceProvider);
        doReturn(true).when(jpaAnnotationGenerator).isGenerateAnnotationForInternal(ipsPartContainer);

        boolean result = jpaAnnotationGenerator.isGenerateAnnotationFor(modelNode);

        assertTrue(result);
    }

    @Test
    public void testIsGenerateAnnotationFor__FalseForInternal() {
        when(builderSet.getPersistenceProvider()).thenReturn(expectedPersistenceProvider);
        doReturn(false).when(jpaAnnotationGenerator).isGenerateAnnotationForInternal(ipsPartContainer);

        boolean result = jpaAnnotationGenerator.isGenerateAnnotationFor(modelNode);

        assertFalse(result);
    }

    @Test
    public void testIsGenerateAnnotationFor_NoPersistenceProvider() {
        doReturn(true).when(jpaAnnotationGenerator).isGenerateAnnotationForInternal(ipsPartContainer);

        boolean result = jpaAnnotationGenerator.isGenerateAnnotationFor(modelNode);

        assertFalse(result);
    }
}
