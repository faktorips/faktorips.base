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

package org.faktorips.devtools.formulalibrary.internal.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.devtools.formulalibrary.model.IFormulaFunction;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class FormulaFunctionExpressionTest {

    @Mock
    private IFormulaFunction formulaFunction;

    @Mock
    private IFormulaMethod formulaMethod;

    private FormulaFunctionExpression expression;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(formulaFunction.getFormulaMethod()).thenReturn(formulaMethod);
        expression = new FormulaFunctionExpression(formulaFunction, "12");
    }

    @Test
    public void testFindProductCmptType() throws Exception {
        assertNull(expression.findProductCmptType(null));
    }

    @Test
    public void testGetTableContentUsages() throws Exception {
        assertNotNull(expression.getTableContentUsages());
    }

    @Test
    public void testGetFormulaFunction() throws Exception {
        assertEquals(formulaFunction, expression.getFormulaFunction());
    }

    @Test
    public void testFindFormulaSignature() throws Exception {
        assertEquals(formulaMethod, expression.findFormulaSignature(null));
    }

    @Test
    public void testIsFormulaMandatory() throws Exception {
        assertTrue(expression.isFormulaMandatory());
    }

}
