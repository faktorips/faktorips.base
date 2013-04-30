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

package org.faktorips.devtools.formulalibrary.ui.workbenchadapters;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.devtools.formulalibrary.internal.model.FormulaFunction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FormulaFunctionIpsObjectPartWorkbenchAdapterTest {

    @Test
    public void testGetLabel() throws Exception {
        FormulaFunction formulaFunction = mock(FormulaFunction.class);
        IFormulaMethod method = mock(IFormulaMethod.class);
        when(formulaFunction.getFormulaMethod()).thenReturn(method);
        when(method.getSignatureString()).thenReturn("computeFormula1(Boolean, Integer)");
        when(method.getDatatype()).thenReturn("String");

        FormulaFunctionIpsObjectPartWorkbenchAdapter adapter = new FormulaFunctionIpsObjectPartWorkbenchAdapter(null);

        assertEquals("computeFormula1(Boolean, Integer) : String", adapter.getLabel(formulaFunction));
    }
}
