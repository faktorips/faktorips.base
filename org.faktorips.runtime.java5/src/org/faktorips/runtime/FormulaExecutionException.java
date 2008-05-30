/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.runtime;


/**
 * Exception that indicates that the execution of a formula has changed.
 *  
 * @author Jan Ortmann
 */
public class FormulaExecutionException extends RuntimeException {

    private static final long serialVersionUID = -8311998096392381687L;

    public FormulaExecutionException(String productCmptGeneration, String formula, String params, Throwable cause) {
        super("Formula execution failed. ProductCmptGeneration: " + productCmptGeneration 
                + ",  formula: " + formula + ", params: " + params, cause);
    }

}
