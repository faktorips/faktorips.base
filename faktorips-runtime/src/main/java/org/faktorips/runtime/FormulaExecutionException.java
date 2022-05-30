/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.runtime;

import java.util.Arrays;

/**
 * Exception that indicates that the execution of a formula has changed.
 * 
 * @author Jan Ortmann
 */
public class FormulaExecutionException extends RuntimeException {

    private static final long serialVersionUID = -8311998096392381687L;

    public FormulaExecutionException(String productCmptGeneration, String formula, String params, Throwable cause) {
        super("Formula execution failed. ProductCmptGeneration: " + productCmptGeneration + ",  formula: " + formula
                + ", " + params, cause);
    }

    public FormulaExecutionException(IProductComponentGeneration productComponentGeneration, String formula,
            Object... parameters) {
        super("Invalid formula: Product Component Generation: " + productComponentGeneration + ",  formula: " + formula
                + ", Parameters: " + Arrays.toString(parameters));
    }

    public FormulaExecutionException(IProductComponent productComponent, String formula, Object... parameters) {
        super("Invalid formula: Product Component: " + productComponent + ",  formula: " + formula + ", Parameters: "
                + Arrays.toString(parameters));
    }

    public FormulaExecutionException(String formula, Object... parameters) {
        super("Invalid formula: " + formula + ", Parameters: " + Arrays.toString(parameters));
    }

}
