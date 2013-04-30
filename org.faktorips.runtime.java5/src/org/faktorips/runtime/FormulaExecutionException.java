/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

    public FormulaExecutionException(IProductComponentGeneration productCmptGeneration, String formula,
            Object... parameters) {
        super("Invalid formula: ProductCmptGeneration: " + productCmptGeneration + ",  formula: " + formula
                + ", Parameters: " + Arrays.toString(parameters));
    }

    public FormulaExecutionException(String formula, Object... parameters) {
        super("Invalid formula: " + formula + ", Parameters: " + Arrays.toString(parameters));
    }

}
