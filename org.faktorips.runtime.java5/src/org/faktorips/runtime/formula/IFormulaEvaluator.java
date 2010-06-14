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

package org.faktorips.runtime.formula;

import org.faktorips.runtime.IProductComponentGeneration;

/**
 * Evaluates the formulas of a product component generation.
 * 
 * @author dirmeier
 */
public interface IFormulaEvaluator {

    /**
     * Returns the product component generation this is an evaluator for.
     */
    public IProductComponentGeneration getProductComponentGeneration();

    /**
     * Evaluates the formula with the given name and the specified parameters.
     * 
     * @param formularName The name of the formula to evaluate
     * @param parameters the parameters the formula neet to evaluate
     * @return the result of the evaluated formula
     */
    public Object evaluate(String formularName, Object... parameters);

}
