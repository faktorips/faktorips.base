/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime.formula;

import java.util.Map;

import org.faktorips.runtime.IProductComponentGeneration;

/**
 * An interface for formula evaluator factories to create a {@link IFormulaEvaluator}. It is part of
 * the creation of an evaluator to set the {@link IProductComponentGeneration} and a map of compiled
 * expressions the evaluator should handle.
 * 
 * @author dirmeier
 */
public interface IFormulaEvaluatorFactory {

    /**
     * This method creates a new formula evaluator. The evaluator getting the product component
     * generation in which context the formulas have to be evaluated and a list of compiled formula
     * expressions.
     * 
     * @param gen The {@link IProductComponentGeneration} in which context a formula have to be
     *            executed
     * @param nameToCompiledExpressionMap The map of compiled formula expressions with their name as
     *            a key
     * @return The new formula evaluator that is able to evaluate every formula the was in the list
     *         of compiled expressions
     */
    public IFormulaEvaluator createFormulaEvaluator(IProductComponentGeneration gen,
            Map<String, String> nameToCompiledExpressionMap);

}
