/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IBaseMethod;
import org.faktorips.devtools.core.model.method.IFormulaMethod;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;

/**
 * Method signatures for product component types extend the "normal" method. The provide an
 * implementation type that defines how the method is implemented.
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptTypeMethod extends IMethod, IFormulaMethod, IProductCmptProperty {

    public static final String PROPERTY_FORMULA_SIGNATURE_DEFINITION = "formulaSignatureDefinition"; //$NON-NLS-1$
    public static final String PROPERTY_OVERLOADS_FORMULA = "overloadsFormula"; //$NON-NLS-1$
    public static final String PROPERTY_FORMULA_MANDATORY = "formulaMandatory"; //$NON-NLS-1$
    public static final String PROPERTY_FORMULA_OPTIONAL_SUPPORTED = "formulaOptionalSupported"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of a formula signature definition is
     * void or a none value data type.
     */
    public static final String MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES = IBaseMethod.MSGCODE_PREFIX
            + "DatatypeMustBeAValueDatatypeForFormulaSignature"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a method is marked as formula signature but the
     * formula name is missing.
     */
    public static final String MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT = IBaseMethod.MSGCODE_PREFIX
            + "FormulaMustntBeAbstract"; //$NON-NLS-1$

    public static final String MSGCODE_NO_FORMULA_WITH_SAME_NAME_IN_TYPE_HIERARCHY = IBaseMethod.MSGCODE_PREFIX
            + "NoFormulaWithSameNameInTypeHierarchy"; //$NON-NLS-1$

    public static final String MSGCODE_FORMULA_MUSTBE_MANDATORY = IBaseMethod.MSGCODE_PREFIX
            + "NotOptionalIfNotOptionalInTypeHierarchy"; //$NON-NLS-1$

    /**
     * Returns the product component type this method belongs to.
     */
    public IProductCmptType getProductCmptType();

    /**
     * Returns <code>true</code> if this is a formula signature definition, <code>false</code> if it
     * is not.
     */
    public boolean isFormulaSignatureDefinition();

    /**
     * Sets if this method defines a formula signature or not.
     */
    public void setFormulaSignatureDefinition(boolean newValue);

    /**
     * Returns <code>true</code> if this is an mandatory formula, <code>false</code> if it is not.
     * If the formula is mandatory, the user needs to enter a formula expression.
     * <p>
     * This method always returns true if {@link #isFormulaOptionalSupported()} returns false.
     * 
     * @return <code>true</code> if the formula is mandatory or if optional formula is not
     *         supported.
     */
    public boolean isFormulaMandatory();

    /**
     * Sets if this method is an mandatory formula or not.
     * 
     * @param formulaMandatory <code>true</code> to set the formula to be mandatory, false to set
     *            optional.
     * 
     * @see #isFormulaMandatory()
     */
    public void setFormulaMandatory(boolean formulaMandatory);

    /**
     * Returns <code>true</code> if this {@link IProductCmptTypeMethod} is a formula signature and
     * hence supports optional formula, that means mandatory is allowed to be false. If this method
     * returns <code>false</code> the method {@link #isFormulaMandatory()} will always return
     * <code>true</code>.
     * 
     * @return <code>true</code> if optional formula is supported.
     * 
     * @see #isFormulaMandatory()
     */
    public boolean isFormulaOptionalSupported();

    /**
     * Returns true if this is a formula method that overloads a formula method within the supertype
     * hierarchy.
     */
    public boolean isOverloadsFormula();

    /**
     * Looks in the supertype hierarchy if a formula method can be found with the same formula name
     * than this one and returns the first that is found. If none can be found <code>null</code>
     * will be returned.
     * 
     * @throws CoreException if an exception occurs during finding the method
     */
    public IProductCmptTypeMethod findOverloadedFormulaMethod(IIpsProject ipsProject) throws CoreException;

    /**
     * Sets if this formula method overloads a formula method in the supertype hierarchy. If so, the
     * next formula method in the supertype hierarchy with the same formula name will be chosen to
     * be the method that will be overloaded.
     */
    public void setOverloadsFormula(boolean overloadsFormula);
}
