/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.builder.java.annotations.policycmpt.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.model.builder.IPersistenceProvider;
import org.faktorips.devtools.model.builder.java.TestJavaBuilderSet;
import org.faktorips.devtools.model.builder.xmodel.AbstractGeneratorModelNode;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
    private TestJavaBuilderSet builderSet;

    @Mock
    private IPersistenceProvider expectedPersistenceProvider;

    @BeforeEach
    public void setUp() {
        when(ipsPartContainer.getIpsProject()).thenReturn(project);
        when(project.getIpsArtefactBuilderSet()).thenReturn(builderSet);
        lenient().when(modelNode.getIpsObjectPartContainer()).thenReturn(ipsPartContainer);
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
        boolean result = jpaAnnotationGenerator.isGenerateAnnotationFor(modelNode);

        assertFalse(result);
    }
}
