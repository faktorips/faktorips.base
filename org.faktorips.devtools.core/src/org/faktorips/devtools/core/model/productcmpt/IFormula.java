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

package org.faktorips.devtools.core.model.productcmpt;

/**
 * A {@link IFormula} is a {@link IExpression} use by a {@link IPropertyValueContainer}. It provides
 * additional testing capabilities.
 * 
 * @author Jan Ortmann
 */
public interface IFormula extends IPropertyValue, IExpression {

    /**
     * Returns the generation this formula belongs to.
     */
    public IProductCmptGeneration getProductCmptGeneration();

    /**
     * Returns all formula test cases.
     */
    public IFormulaTestCase[] getFormulaTestCases();

    /**
     * Returns the formula test case identified by the given name. Return <code>null</code> if no
     * such element exists.
     */
    public IFormulaTestCase getFormulaTestCase(String name);

    /**
     * Creates and returns a new formula test case.
     */
    public IFormulaTestCase newFormulaTestCase();

    /**
     * Removes the given formula test case from the list of formula test cases.
     */
    public void removeFormulaTestCase(IFormulaTestCase formulaTest);

    /**
     * Moves the formula test case identified by the indexes up or down by one position. If one of
     * the indexes is 0 (the first element), nothing is moved up. If one of the indexes is the
     * number of elements - 1 (the last element) nothing moved down
     * 
     * @param indexes The indexes identifying the input value.
     * @param up <code>true</code>, to move up, <false> to move them down.
     * 
     * @return The new indexes of the moved input values.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify an input value.
     */
    public int[] moveFormulaTestCases(int[] indexes, boolean up);

}
