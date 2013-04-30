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

package org.faktorips.devtools.formulalibrary.builder.xpand.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.devtools.formulalibrary.internal.model.FormulaFunction;
import org.faktorips.devtools.formulalibrary.internal.model.FormulaLibrary;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelCaches;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class XFormulaLibraryClassTest {

    @Mock
    private GeneratorModelContext modelContext;

    @Mock
    private ModelService modelService;

    @Mock
    private FormulaLibrary formulaLibrary;

    @Mock
    private XFormulaMethod xMethod1;

    @Mock
    private XFormulaMethod xMethod2;

    @Before
    public void initModelContext() {
        GeneratorModelCaches generatorModelCache = new GeneratorModelCaches();
        when(modelContext.getGeneratorModelCache()).thenReturn(generatorModelCache);
        when(modelContext.isGeneratePublishedInterfaces()).thenReturn(false);
    }

    @Test
    public void testGetMethods() {
        setUpMethods();

        XFormulaLibraryClass formulaLibraryClass = new XFormulaLibraryClass(formulaLibrary, modelContext, modelService);
        Set<XFormulaMethod> methods = formulaLibraryClass.getMethods();

        assertEquals(2, methods.size());
        assertThat(methods, hasItems(xMethod1, xMethod2));
    }

    @Test
    public void testImplementation() {
        XFormulaLibraryClass formulaLibraryClass = new XFormulaLibraryClass(formulaLibrary, modelContext, modelService);

        assertEquals(StringUtils.EMPTY, formulaLibraryClass.getBaseSuperclassName());
        assertEquals(0, formulaLibraryClass.getExtendedInterfaces().size());
        assertEquals(0, formulaLibraryClass.getExtendedOrImplementedInterfaces().size());
        assertEquals(0, formulaLibraryClass.getImplementedInterfaces().size());
    }

    private void setUpMethods() {
        FormulaFunction formulaFunction1 = mock(FormulaFunction.class);
        FormulaFunction formulaFunction2 = mock(FormulaFunction.class);

        IFormulaMethod method1 = mock(IFormulaMethod.class);
        IFormulaMethod method2 = mock(IFormulaMethod.class);

        List<IFormulaFunction> listFormulaFunctions = new ArrayList<IFormulaFunction>();
        listFormulaFunctions.add(formulaFunction1);
        listFormulaFunctions.add(formulaFunction2);
        when(formulaLibrary.getFormulaFunctions()).thenReturn(listFormulaFunctions);
        when(formulaFunction1.getFormulaMethod()).thenReturn(method1);
        when(formulaFunction2.getFormulaMethod()).thenReturn(method2);
        doReturn(xMethod1).when(modelService).getModelNode(method1, XFormulaMethod.class, modelContext);
        doReturn(xMethod2).when(modelService).getModelNode(method2, XFormulaMethod.class, modelContext);
    }
}
