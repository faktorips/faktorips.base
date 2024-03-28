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
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.faktorips.runtime.InMemoryRuntimeRepository;
import org.faktorips.runtime.XmlAbstractTestCase;
import org.faktorips.runtime.formula.IFormulaEvaluator;
import org.faktorips.runtime.formula.IFormulaEvaluatorFactory;
import org.faktorips.values.InternationalString;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
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
        when(factory.createFormulaEvaluator(eq(callerObject), anyMap())).thenReturn(
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

    @Test
    public void testDescription_existingLanguage() {
        Element element = getTestDocument().getDocumentElement();
        formulaHandler.doInitFormulaFromXml(element);
        InternationalString description = formulaHandler.getDescription("testFormula");

        assertEquals("English description.", description.get(Locale.ENGLISH));
        assertEquals("Deutsche Beschreibung.", description.get(Locale.GERMAN));
        assertEquals("Je ne parle pas français.", description.get(Locale.FRENCH));
    }

    @Test
    public void testDescription_existingLanguage_fallback() {
        Element element = getTestDocument().getDocumentElement();
        formulaHandler.doInitFormulaFromXml(element);
        InternationalString description = formulaHandler.getDescription("testFormula");

        assertEquals("English description.", description.get(Locale.US));
        assertEquals("English description.", description.get(Locale.UK));
        assertEquals("English description.", description.get(Locale.CANADA));
        assertEquals("Deutsche Beschreibung.", description.get(Locale.GERMANY));
        assertEquals("Je ne parle pas français.", description.get(Locale.FRANCE));
        assertEquals("Je ne parle pas français.", description.get(Locale.CANADA_FRENCH));
    }

    @Test
    public void testDescription_nonExistingLanguage() {
        Element element = getTestDocument().getDocumentElement();
        formulaHandler.doInitFormulaFromXml(element);
        InternationalString description = formulaHandler.getDescription("testFormula");

        // English is currently considered the default language as it is defined as the first
        // language in the XML
        assertEquals("English description.", description.get(Locale.CHINESE));
    }

    @Test
    public void testWriteDescriptionToXml() {
        Element element = getTestDocument().getDocumentElement();
        formulaHandler.doInitFormulaFromXml(element);
        Document newDocument = newDocument();
        Element newElement = newDocument.createElement("f");
        newDocument.appendChild(newElement);
        formulaHandler.writeFormulaToXml(newElement);

        NodeList desriptionNodes = newElement.getElementsByTagName("Description");
        assertEquals(3, desriptionNodes.getLength());
        assertEquals("en", ((Element)desriptionNodes.item(0)).getAttribute("locale"));
        assertEquals("English description.", ((Element)desriptionNodes.item(0)).getTextContent());
        assertEquals("fr", ((Element)desriptionNodes.item(1)).getAttribute("locale"));
        assertEquals("Je ne parle pas français.", ((Element)desriptionNodes.item(1)).getTextContent());
        assertEquals("de", ((Element)desriptionNodes.item(2)).getAttribute("locale"));
        assertEquals("Deutsche Beschreibung.", ((Element)desriptionNodes.item(2)).getTextContent());
    }
}
