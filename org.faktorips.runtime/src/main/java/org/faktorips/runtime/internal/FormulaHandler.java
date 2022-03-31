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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.runtime.formula.AbstractFormulaEvaluator;
import org.faktorips.runtime.formula.IFormulaEvaluator;
import org.faktorips.runtime.formula.IFormulaEvaluatorFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * 
 */
class FormulaHandler {

    private Object callerObject;

    private final IRuntimeRepository repository;

    private IFormulaEvaluator formulaEvaluator;

    private Map<String, String> availableFormulas = new LinkedHashMap<>();

    public FormulaHandler(Object callerObject, IRuntimeRepository repository) {
        this.callerObject = callerObject;
        this.repository = repository;
    }

    public IRuntimeRepository getRepository() {
        return repository;
    }

    public IFormulaEvaluator getFormulaEvaluator() {
        return formulaEvaluator;
    }

    /**
     * Initializes all formulas contained by Element. If formula evaluation is supported, the map
     * contains the compiled expression for every formula. *
     * <p>
     * IPSPV-199 : changed that <code>availableFormulas</code> is not overridden, because if the
     * method <code>initFromXML</code> is called twice, the product variant would have no formulas.
     * 
     * <p>
     * SW 29.02.2012: TODO ProductVariants call initFromXML() twice. As of yet no formulas can be
     * varied and thus the formula-evaluator should not be overridden if it already exists. This is
     * a rather dirty fix for the current problems. A clean solution would be to extend the
     * {@link IFormulaEvaluator} interface with an updateExpression() method, that will then be
     * called for each formula found in the XML. see FIPS-995
     */
    public void doInitFormulaFromXml(Element element) {
        availableFormulas.putAll(ProductComponentXmlUtil.getAvailableFormulars(element));

        if (getFormulaEvaluator() != null) {
            return;
        }
        if (getRepository() != null) {
            IFormulaEvaluatorFactory factory = getRepository().getFormulaEvaluatorFactory();
            boolean hasFormulas = availableFormulas.values()
                    .stream()
                    .anyMatch(IpsStringUtils::isNotBlank);
            if (factory != null && hasFormulas) {
                Map<String, String> expressions = getCompiledExpressionsFromFormulas(element);
                formulaEvaluator = factory.createFormulaEvaluator(callerObject, expressions);
            }
        }
    }

    /**
     * Returns a set containing the formulaSignatures and the compiled expressions of all available
     * formulas found in the indicated xml element.
     * 
     * @param element An xml element containing the data.
     * @throws NullPointerException if element is <code>null</code>.
     */
    protected Map<String, String> getCompiledExpressionsFromFormulas(Element element) {
        Map<String, String> expressions = new LinkedHashMap<>();
        NodeList formulas = element.getElementsByTagName(ProductComponentXmlUtil.XML_TAG_FORMULA);
        for (int i = 0; i < formulas.getLength(); i++) {
            Element aFormula = (Element)formulas.item(i);
            String name = aFormula.getAttribute(ProductComponentXmlUtil.XML_ATTRIBUTE_FORMULA_SIGNATURE);
            NodeList nodeList = aFormula.getElementsByTagName(AbstractFormulaEvaluator.COMPILED_EXPRESSION_XML_TAG);
            if (nodeList.getLength() == 1) {
                Element expression = (Element)nodeList.item(0);
                String formulaExpression = expression.getTextContent();
                expressions.put(name, formulaExpression);
            } else {
                throw new RuntimeException("Expression for Formula: " + name + " not found");
            }
        }
        return expressions;
    }

    public boolean isFormulaAvailable(String formularSignature) {
        String expression = availableFormulas.get(formularSignature);
        return IpsStringUtils.isNotBlank(expression);
    }

    /**
     * Adds the formulas with formulaSignature, expression and compiled expression to the given
     * Element.
     * 
     * @param element the element to add the formulas
     */
    public void writeFormulaToXml(Element element) {
        addFormulasToElement(element, formulaEvaluator, availableFormulas);
    }

    protected void addFormulasToElement(final Element element,
            final IFormulaEvaluator formulaEvaluator,
            final Map<String, String> availableFormulars) {
        if (availableFormulars != null) {
            for (Entry<String, String> expressionEntry : availableFormulars.entrySet()) {
                Element formula = element.getOwnerDocument().createElement(ProductComponentXmlUtil.XML_TAG_FORMULA);
                formula.setAttribute(ProductComponentXmlUtil.XML_ATTRIBUTE_FORMULA_SIGNATURE, expressionEntry.getKey());
                ValueToXmlHelper.addValueToElement(expressionEntry.getValue(), formula,
                        ProductComponentXmlUtil.XML_TAG_EXPRESSION);
                if (formulaEvaluator != null) {
                    String compiledExpression = formulaEvaluator.getNameToExpressionMap().get(expressionEntry.getKey());

                    ValueToXmlHelper.addCDataValueToElement(compiledExpression, formula,
                            AbstractFormulaEvaluator.COMPILED_EXPRESSION_XML_TAG);
                }
                element.appendChild(formula);
            }
        }
    }
}
