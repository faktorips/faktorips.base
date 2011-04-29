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

import java.util.List;

import org.faktorips.runtime.IProductComponentGeneration;

/**
 * An interface for formula evaluator factories to create a {@link IFormulaEvaluator}. It is part of
 * the creation of an evaluator to set the {@link IProductComponentGeneration} and a list of
 * compiled expressions the evaluator should handle.
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
     * @param compiledExpressions The list of compiled formula expressions
     * @return The new formula evaluator that is able to evaluate every formula the was in the list
     *         of compiled expressions
     */
    public IFormulaEvaluator createFormulaEvaluator(IProductComponentGeneration gen, List<String> compiledExpressions);

}
