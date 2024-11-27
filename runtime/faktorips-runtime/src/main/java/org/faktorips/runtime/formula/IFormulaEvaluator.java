/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.formula;

import java.util.Map;

/**
 * Evaluates the formulas of a product component or product component generation. This interface
 * only provides methods to get the configured product component generation and to evaluate already
 * configured formulas. The code of the formulas is set while creating the evaluator by an
 * {@link IFormulaEvaluatorFactory}.
 * 
 * 
 * @author dirmeier
 */
public interface IFormulaEvaluator {

    /**
     * Returns the product component generation or product component this is an evaluator for.
     */
    Object getObject();

    /**
     * Evaluates the formula with the given name and the specified parameters.
     * 
     * @param formulaName The name of the formula to evaluate
     * @param parameters the parameters the formula requires when being evaluated
     * @return the result of the evaluated formula
     */
    Object evaluate(String formulaName, Object... parameters);

    /**
     * Returns a defensive copy of the map of expressions/formulas held by this evaluator.
     * 
     * @return a map containing the expressions (with their names as keys) held by this formula
     *             evaluator
     */
    Map<String, String> getNameToExpressionMap();

}
