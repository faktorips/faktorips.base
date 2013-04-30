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

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.devtools.core.model.method.IParameter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.w3c.dom.Element;

public class FormulaMethodTest extends AbstractIpsPluginTest {

    @Mock
    private FormulaFunction formulaFunction;

    private FormulaMethod formulaMethod;

    @Override
    @Before
    public void setUp() throws CoreException {
        MockitoAnnotations.initMocks(this);
        formulaMethod = new FormulaMethod(formulaFunction, "1");
    }

    @Test
    public void testInitFromXml() {
        Element element = getTestDocument().getDocumentElement();

        formulaMethod.initFromXml(element);

        assertEquals("1a", formulaMethod.getId());
        assertEquals("formula1", formulaMethod.getFormulaName());
        assertEquals("computeFormula1", formulaMethod.getName());
        assertEquals("computeFormula1", formulaMethod.getDefaultMethodName());
        assertEquals("Integer", formulaMethod.getDatatype());

        assertEquals("english", formulaMethod.getDescription(Locale.ENGLISH).getText());
        assertEquals("deutsch", formulaMethod.getDescription(Locale.GERMAN).getText());

        IParameter[] params = formulaMethod.getParameters();
        assertEquals(2, params.length);
        assertEquals("3a", params[0].getId());
        assertEquals("param1", params[0].getName());
        assertEquals("Boolean", params[0].getDatatype());
        assertEquals("3b", params[1].getId());
        assertEquals("param2", params[1].getName());
        assertEquals("Integer", params[1].getDatatype());

        Element element2 = formulaMethod.toXml(newDocument());

        IFormulaMethod copy = new FormulaMethod(formulaFunction, "2");
        copy.initFromXml(element2);

        assertEquals("Integer", copy.getDatatype());
        assertEquals("formula1", copy.getFormulaName());
        assertEquals("computeFormula1", copy.getName());
        assertEquals("computeFormula1", copy.getDefaultMethodName());

        IParameter[] params2 = copy.getParameters();
        assertEquals(2, params2.length);
        assertEquals("param1", params2[0].getName());
        assertEquals("Boolean", params2[0].getDatatype());
        assertEquals("param2", params2[1].getName());
        assertEquals("Integer", params2[1].getDatatype());

        assertEquals("deutsch", copy.getDescription(Locale.GERMAN).getText());
    }
}
