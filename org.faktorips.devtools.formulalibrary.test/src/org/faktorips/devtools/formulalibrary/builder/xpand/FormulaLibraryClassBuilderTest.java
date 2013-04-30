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

package org.faktorips.devtools.formulalibrary.builder.xpand;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.formulalibrary.builder.xpand.model.XFormulaLibraryClass;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FormulaLibraryClassBuilderTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private IIpsProject ipsProject;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private StandardBuilderSet builderSet;

    @Mock
    private GeneratorModelContext generatorModelContext;

    @Mock
    private IIpsObjectPartContainer ipsObjectPartContainer;

    @Mock
    private IIpsObject ipsObject;

    @Mock
    private IIpsSrcFile ipsSrcFile;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(ipsObjectPartContainer.getIpsObject()).thenReturn(ipsObject);
        when(ipsObjectPartContainer.getIpsSrcFile()).thenReturn(ipsSrcFile);
    }

    @Test
    public void testGetter() {
        FormulaLibraryClassBuilder formulaLibraryClassBuilder = new FormulaLibraryClassBuilder(builderSet,
                generatorModelContext, null);
        assertEquals("FormulaLibraryClassBuilder", formulaLibraryClassBuilder.getName());
        assertEquals("org::faktorips::devtools::formulalibrary::builder::xpand::template::FormulaLibrary::main",
                formulaLibraryClassBuilder.getTemplate());
        assertEquals(XFormulaLibraryClass.class, formulaLibraryClassBuilder.getGeneratorModelNodeClass());
        assertFalse(formulaLibraryClassBuilder.isBuildingPublishedSourceFile());
        assertFalse(formulaLibraryClassBuilder.generatesInterface());
        assertEquals(ipsObject, formulaLibraryClassBuilder.getSupportedIpsObject(ipsObjectPartContainer));
    }
}
