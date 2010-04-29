/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.runtime.pds;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An abstract implementation of {@link IFormulaEvaluator}. This implementation handles the
 * expression encapsulated in an {@link Element} and forwarding the expression as String. T
 * 
 * @author dirmeier
 */
public abstract class AbstractFormulaEvaluator implements IFormulaEvaluator {

    public AbstractFormulaEvaluator() {
        // nothing to do yet
    }

    public abstract void setVariable(String name, Object value);

    /**
     * Extracts the expression from the expressionElement and calls
     * {@link #parseFormula(String, String)} in subclass {@inheritDoc}
     */
    public void parseFormula(String name, Element expressionElement) {
        Node expression = expressionElement.getElementsByTagName("Expression").item(0); //$NON-NLS-1$
        String formulaExpression = expression.getTextContent();
        parseFormula(name, formulaExpression);
    }

    /**
     * Parse the formula
     * 
     * @param name the name of the formula
     * @param expression the formula expression
     */
    protected abstract void parseFormula(String name, String expression);

    /**
     * Evaluates an already pared formula.
     * 
     * @throws IllegalArgumentException if the formula signature is unknown
     */
    public abstract Object evaluate(String formularName, Object... parameters);

}
