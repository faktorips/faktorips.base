/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

import org.faktorips.runtime.FormulaExecutionException;
import org.faktorips.runtime.IProductComponentGeneration;

/**
 * An abstract implementation of {@link IFormulaEvaluator} holding the product component generation
 * and handles the exceptions thrown by the {@link #evaluateInternal(String, Object...)} method.
 * <p>
 * There is no method to set the compiled formula expression because the expressions have to be set
 * by the {@link IFormulaEvaluatorFactory} while creating the evaluator.
 * 
 * @author dirmeier
 */
public abstract class AbstractFormulaEvaluator implements IFormulaEvaluator {

    public static final String COMPILED_EXPRESSION_XML_TAG = "compiledExpression";

    private final IProductComponentGeneration productCmptGeneration;

    public AbstractFormulaEvaluator(IProductComponentGeneration gen) {
        if (gen == null) {
            throw new NullPointerException();
        }
        this.productCmptGeneration = gen;
    }

    /**
     * {@inheritDoc}
     */
    public IProductComponentGeneration getProductComponentGeneration() {
        return productCmptGeneration;
    }

    /**
     * Evaluates a formula that was added through the builder
     * 
     * @throws IllegalArgumentException if the formula signature is unknown
     */
    public Object evaluate(String formularName, Object... parameters) {
        try {
            return evaluateInternal(formularName, parameters);
        } catch (Exception e) {
            StringBuffer parameterValues = new StringBuffer();
            parameterValues.append("Parameters: ");
            for (Object param : parameters) {
                parameterValues.append("" + param);
                if (param != parameters[parameters.length - 1]) {
                    parameterValues.append(", ");
                }
            }
            throw new FormulaExecutionException(productCmptGeneration.toString(), formularName, parameterValues
                    .toString(), e);
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

}
