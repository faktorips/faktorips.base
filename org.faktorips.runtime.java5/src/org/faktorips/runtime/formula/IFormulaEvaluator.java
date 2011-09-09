/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.runtime.formula;

import java.util.Map;

import org.faktorips.runtime.IProductComponentGeneration;

/**
 * Evaluates the formulas of a product component generation. This interface only provides methods to
 * get the configured product component generation and to evaluate already configured formulas. The
 * code of the formulas is set while creating the evaluator by an {@link IFormulaEvaluatorFactory}.
 * 
 * 
 * @author dirmeier
 */
public interface IFormulaEvaluator {

    /**
     * Returns the product component generation this is an evaluator for.
     */
    public IProductComponentGeneration getProductComponentGeneration();

    /**
     * Evaluates the formula with the given name and the specified parameters.
     * 
     * @param formularName The name of the formula to evaluate
     * @param parameters the parameters the formula requires when being evaluated
     * @return the result of the evaluated formula
     */
    public Object evaluate(String formularName, Object... parameters);

    /**
     * Returns a defensive copy of the map of expressions/formulas held by this evaluator.
     * 
     * @return a map containing the expressions (with their names as keys) held by this formula
     *         evaluator
     */
    public Map<String, String> getNameToExpressionMap();

}
