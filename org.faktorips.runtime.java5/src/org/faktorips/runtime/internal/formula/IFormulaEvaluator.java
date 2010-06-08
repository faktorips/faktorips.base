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
 * The published interface for the formula evaluator. You could set variables for the formula
 * context and evaluate a formula.
 * 
 * @author dirmeier
 */
public interface IFormulaEvaluator {

    /**
     * Use this method to set additional variables that have to be visible inside your compiled
     * formula expression. For example the variable 'thiz' is set to get access to the class
     * context. Formula parameters are not set by this method but are specified directly when
     * calling {@link #evaluate(String, Object...)}.
     * 
     * @param name the name of the variable
     * @param value the value of the variable
     */
    void setVariable(String name, Object value);

    /**
     * Evaluates the formula with the given name and the specified parameters.
     * 
     * @param formularName The name of the formula to evaluate
     * @param parameters the parameters the formula neet to evaluate
     * @return the result of the evaluated formula
     */
    public Object evaluate(String formularName, Object... parameters);

}
