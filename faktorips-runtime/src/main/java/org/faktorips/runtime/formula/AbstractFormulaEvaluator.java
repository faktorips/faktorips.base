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

import java.util.LinkedHashMap;
import java.util.Map;

import org.faktorips.runtime.FormulaExecutionException;
import org.faktorips.runtime.util.StringBuilderJoiner;

/**
 * An abstract implementation of {@link IFormulaEvaluator} holding the product component generation
 * or product component and handles the exceptions thrown by the
 * {@link #evaluateInternal(String, Object...)} method.
 * <p>
 * There is no method to set the compiled formula expression because the expressions have to be set
 * by the {@link IFormulaEvaluatorFactory} while creating the evaluator.
 * 
 * @author dirmeier
 */
public abstract class AbstractFormulaEvaluator implements IFormulaEvaluator {

    public static final String COMPILED_EXPRESSION_XML_TAG = "compiledExpression";

    private final Object object;

    private final Map<String, String> nameToExpressionMap;

    public AbstractFormulaEvaluator(Object object, Map<String, String> nameToExpressionMap) {
        if (object == null) {
            throw new NullPointerException();
        }
        this.object = object;
        this.nameToExpressionMap = nameToExpressionMap;
    }

    @Override
    public Object getObject() {
        return object;
    }

    /**
     * Evaluates a formula that was added through the builder
     * 
     * @throws IllegalArgumentException if the formula signature is unknown
     */
    @Override
    public Object evaluate(String formularName, Object... parameters) {
        try {
            return evaluateInternal(formularName, parameters);
            // CSOFF: IllegalCatch
        } catch (Exception e) {
            // CSON: IllegalCatch
            StringBuilder parameterValues = new StringBuilder();
            parameterValues.append("Parameters: ");
            StringBuilderJoiner.join(parameterValues, parameters);
            throw new FormulaExecutionException(object.toString(), formularName, parameterValues.toString(), e);
        }

    }

    /**
     * This method evaluates the formula with the given name and the specified parameters. With the
     * name and parameters you have the complete formula method signature. The order of the
     * parameters is given by the formula signature.
     * 
     * @param formularName name of the formula to evaluate
     * @param parameters the parameters to evaluate the formula
     * @return the return value of the evaluated formula
     */
    protected abstract Object evaluateInternal(String formularName, Object... parameters) throws Exception;

    /**
     * Returns a defensive copy of the map of expressions/formulas held by this evaluator.
     * 
     * @return a map containing the expressions (with their names as keys) held by this formula
     *         evaluator
     */
    @Override
    public Map<String, String> getNameToExpressionMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.putAll(nameToExpressionMap);
        return map;
    }

}
