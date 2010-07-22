/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.productcmpt;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.EnumDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.builder.ExtendedExprCompiler;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.fl.ExprCompiler;

/**
 * 
 * @author Jan Ortmann
 */
public interface IFormula extends IIpsObjectPart, IPropertyValue {

    public final static String PROPERTY_FORMULA_SIGNATURE_NAME = "formulaSignature"; //$NON-NLS-1$
    public final static String PROPERTY_EXPRESSION = "expression"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "CONFIGELEMENT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the formula signature's can't be found.
     */
    public final static String MSGCODE_SIGNATURE_CANT_BE_FOUND = MSGCODE_PREFIX + "SignatureCantBeFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the formula signature's data type can't be found and
     * so the formula's data type can't be checked against it.
     */
    public final static String MSGCODE_UNKNOWN_DATATYPE_FORMULA = MSGCODE_PREFIX + "UnknownDatatypeFormula"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the formula's data type is not compatible with the
     * one defined by the signature.
     */
    public final static String MSGCODE_WRONG_FORMULA_DATATYPE = MSGCODE_PREFIX + "WrongFormulaDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the expression is empty (and it should not.
     */
    public final static String MSGCODE_EXPRESSION_IS_EMPTY = MSGCODE_PREFIX + "ExpressionIsEmpty"; //$NON-NLS-1$

    /**
     * Returns the generation this formula belongs to.
     */
    @Override
    public IProductCmptGeneration getProductCmptGeneration();

    /**
     * For formulas this IIpsElement method returns the formula signature name.
     * 
     * @see #getFormulaSignature()
     */
    @Override
    public String getName();

    /**
     * Returns the name of the product component type method signature this formula is an
     * implementation of.
     */
    public String getFormulaSignature();

    /**
     * Sets the name of the product component type method signature this formula is an
     * implementation of.
     */
    public void setFormulaSignature(String newName);

    /**
     * Returns the method signature this formula implements. Returns <code>null</code> if the method
     * signature is not found.
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IProductCmptTypeMethod findFormulaSignature(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the product component type of the product component this formula belongs to.
     * 
     * @throws CoreException if an error occurs while searching
     */
    public IProductCmptType findProductCmptType(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the value data types the results of this formula expression are instances of. This
     * data type is equal to the formula signatures data type. If the formula signature's return
     * type is not a value data type (which is an error in the model and is reported via
     * validation), this method returns <code>null</code>.
     */
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the formula expression.
     */
    public String getExpression();

    /**
     * Sets the new formula expression.
     */
    public void setExpression(String newExpression);

    /**
     * Returns an expression compiler that can be used to compile the formula. or <code>null</code>
     * if the element does not contain a formula.
     */
    public ExprCompiler newExprCompiler(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns an expression compiler that can be used to compile the formula. or <code>null</code>
     * if the element does not contain a formula.
     * 
     * @param formulaTest if <code>true</code> the formula will be compiled for usage inside a
     *            formula test, in formula tests all type parameters will be replaced by their
     *            value, which is defined inside the formula test.
     */
    public ExtendedExprCompiler newExprCompiler(IIpsProject ipsProject, boolean formulaTest) throws CoreException;

    /**
     * Returns the enumeration data types that can be use in this formula. Allowed enumeration types
     * are those that are used as data type in one of the parameters or in a table used by the
     * product component generation this configuration element belongs to. If the data type of the
     * formula is itself an enumeration data type, this one is also returned.
     * <p>
     * Returns an empty array if this configuration element does not represent a formula.
     * 
     * @throws CoreException if an error occurs while searching the enum datatypes.
     */
    public EnumDatatype[] getEnumDatatypesAllowedInFormula() throws CoreException;

    /**
     * Returns all parameter identifiers which are used in the formula. Identifiers used in a
     * formula an be either identify a parameter or an enum value. This methods returns all
     * identifiers identifying parameters. Returns an empty string array if no identifier was found
     * in formula or the config element is no formula type or the policy component type attribute -
     * which specifies the formula interface - wasn't found.
     * 
     * @param ipsProject The project which ips object path is used to search.
     * 
     * @throws CoreException If an error occurs while searching the corresponding attribute.
     * @throws NullPointerException if ipsProject is <code>null</code>.
     */
    public String[] getParameterIdentifiersUsedInFormula(IIpsProject ipsProject) throws CoreException;

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
