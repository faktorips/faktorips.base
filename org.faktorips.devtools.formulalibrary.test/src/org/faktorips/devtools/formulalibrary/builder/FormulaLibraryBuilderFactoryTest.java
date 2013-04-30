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

package org.faktorips.devtools.formulalibrary.builder;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.formulalibrary.builder.xpand.FormulaLibraryClassBuilder;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FormulaLibraryBuilderFactoryTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IIpsProject ipsProject;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StandardBuilderSet builderSet;

    @Mock
    private GeneratorModelContext generatorModelContext;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(builderSet.getGeneratorModelContext()).thenReturn(generatorModelContext);
        when(builderSet.getModelService()).thenReturn(null);
    }

    @Test
    public void testCreateBuilder() throws Exception {
        FormulaLibraryBuilderFactory factory = new FormulaLibraryBuilderFactory();
        IIpsArtefactBuilder createBuilder = factory.createBuilder(builderSet);
        assertNotNull(createBuilder);
        assertTrue(createBuilder instanceof FormulaLibraryClassBuilder);
    }

}
