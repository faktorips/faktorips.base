/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;

import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.faktorips.runtime.formula.IFormulaEvaluator;
import org.faktorips.runtime.formula.IFormulaEvaluatorFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Element;

@RunWith(MockitoJUnitRunner.class)
public class FormulaHandlerTest extends XmlAbstractTestCase {

    @Mock
    private Object callerObject;

    @Mock
    private IFormulaEvaluatorFactory factory;

    @Mock
    private InMemoryRuntimeRepository repository;

    @Mock
    private IFormulaEvaluator formulaEvaluator;

    private FormulaHandler formulaHandler;

    @Before
    public void setUp() throws Exception {
        formulaHandler = new FormulaHandler(callerObject, repository);
        when(repository.getFormulaEvaluatorFactory()).thenReturn(factory);
        when(factory.createFormulaEvaluator(eq(callerObject), anyMapOf(String.class, String.class))).thenReturn(
                formulaEvaluator);
    }

    @Test
    public void testDoInitFormulaFromXml() {
        Element element = getTestDocument().getDocumentElement();
        formulaHandler.doInitFormulaFromXml(element);

        assertEquals(formulaEvaluator, formulaHandler.getFormulaEvaluator());

    }

    @Test
    public void testDoInitFormulaFromXml_NoFormulas() {
        Element element = getTestDocument("FormulaHandlerTestWithoutFormulas.xml").getDocumentElement();
        formulaHandler.doInitFormulaFromXml(element);

        assertNull(formulaHandler.getFormulaEvaluator());

    }

    @Test
    public void testDoInitFormulaFromXml_EmptyFormulas() {
        Element element = getTestDocument("FormulaHandlerTestWithEmptyFormulas.xml").getDocumentElement();
        formulaHandler.doInitFormulaFromXml(element);

        assertNull(formulaHandler.getFormulaEvaluator());

    }

    @Test
    public void testGetCompiledExpressionsFromFormulas() {
        Element element = getTestDocument().getDocumentElement();
        Map<String, String> expressions = formulaHandler.getCompiledExpressionsFromFormulas(element);

        assertEquals(3, expressions.size());
        assertTrue(expressions.containsKey("testFormula"));
        assertFalse(IpsStringUtils.isEmpty(expressions.get("testFormula")));
        assertTrue(expressions.containsKey("emptyFormula"));
        assertFalse(IpsStringUtils.isEmpty(expressions.get("emptyFormula")));
        assertTrue(expressions.containsKey("whitespaceFormula"));
        assertFalse(IpsStringUtils.isEmpty(expressions.get("whitespaceFormula")));
    }

    @Test
    public void testIsFormulaAvailable() {
        Element element = getTestDocument().getDocumentElement();
        formulaHandler.doInitFormulaFromXml(element);

        assertTrue(formulaHandler.isFormulaAvailable("testFormula"));
        assertFalse(formulaHandler.isFormulaAvailable("emptyFormula"));
        assertFalse(formulaHandler.isFormulaAvailable("notExistingFormula"));
    }

    @Test
    public void testWriteFormulaToXml() throws Exception {
        Element element = getTestDocument().getDocumentElement();
        formulaHandler.writeFormulaToXml(element);
    }

    @Test
    public void testAddFormulasToElement() throws Exception {
        Element element = getTestDocument().getDocumentElement();
        Map<String, String> availableFormulars = new LinkedHashMap<>();
        Map<String, String> nameToExpressionMap = new LinkedHashMap<>();
        IFormulaEvaluator formulaEvaluator = mock(IFormulaEvaluator.class);
        when(formulaEvaluator.getNameToExpressionMap()).thenReturn(nameToExpressionMap);

        formulaHandler.addFormulasToElement(element, formulaEvaluator, availableFormulars);
        assertEquals(3, element.getElementsByTagName(ProductComponentXmlUtil.XML_TAG_FORMULA).getLength());

        availableFormulars.put("testFormula1", "NeueExpression1");
        availableFormulars.put("testFormula2", "NeueExpression2");
        nameToExpressionMap.put("testFormula1", "compiledExpression1");
        nameToExpressionMap.put("testFormula2", "compiledExpression2");

        availableFormulars.put("testFormula3", "NeueExpression3");

        formulaHandler.addFormulasToElement(element, formulaEvaluator, availableFormulars);
        assertEquals(6, element.getElementsByTagName(ProductComponentXmlUtil.XML_TAG_FORMULA).getLength());
    }
}
