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

package org.faktorips.runtime.internal.formula;

/**
 * Interface for builders to build {@link IFormulaEvaluator}. The Builder implementation normally is
 * an internal class of the concrete formula evaluator and may take additional arguments forwarding
 * to the evaluator.
 * 
 * @author dirmeier
 */
public interface IFormulaEvaluatorBuilder {

    /**
     * Builds a new {@link IFormulaEvaluator}. Normally the constructor of the concrete formula
     * evaluator is called with the builder itself as single argument.
     * 
     * @return a new {@link IFormulaEvaluator}
     * @throws IllegalArgumentException if there are missing arguments
     */
    public IFormulaEvaluator build();

    /**
     * Setting the object context in which the formula should be executed
     */
    public IFormulaEvaluatorBuilder thiz(Object thiz);

    /**
     * Adding the sourcecode for a formula. This method should be called for every formula you want
     * to evaluate.
     * 
     */
    public IFormulaEvaluatorBuilder addFormula(String formulaString);

}
