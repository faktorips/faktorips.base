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
