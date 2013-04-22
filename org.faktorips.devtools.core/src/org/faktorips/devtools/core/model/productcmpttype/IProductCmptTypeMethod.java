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

package org.faktorips.devtools.core.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.IMethod;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;

/**
 * Method signatures for product component types extend the "normal" method. The provide an
 * implementation type that defines how the method is implemented.
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptTypeMethod extends IMethod, IProductCmptProperty {

    public static final String PROPERTY_FORMULA_SIGNATURE_DEFINITION = "formulaSignatureDefinition"; //$NON-NLS-1$
    public static final String PROPERTY_OVERLOADS_FORMULA = "overloadsFormula"; //$NON-NLS-1$
    public static final String PROPERTY_FORMULA_NAME = "formulaName"; //$NON-NLS-1$
    public static final String PROPERTY_FORMULA_OPTIONAL = "formulaOptional"; //$NON-NLS-1$
    public static final String PROPERTY_FORMULA_OPTIONAL_SUPPORTED = "formulaOptionalSupported"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type of a formula signature definition is
     * void or a none value data type.
     */
    public static final String MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES = IMethod.MSGCODE_PREFIX
            + "DatatypeMustBeAValueDatatypeForFormulaSignature"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a method is marked as formula signature but the
     * formula name is missing.
     */
    public static final String MSGCODE_FORMULA_NAME_IS_EMPTY = IMethod.MSGCODE_PREFIX + "FormulaNameIsEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a method is marked as formula signature but the
     * formula name is missing.
     */
    public static final String MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT = IMethod.MSGCODE_PREFIX + "FormulaMustntBeAbstract"; //$NON-NLS-1$

    public static final String MSGCODE_NO_FORMULA_WITH_SAME_NAME_IN_TYPE_HIERARCHY = IMethod.MSGCODE_PREFIX
            + "NoFormulaWithSameNameInTypeHierarchy"; //$NON-NLS-1$

    public static final String MSGCODE_FORMULA_OPTIONAL_NOT_ALLOWED = IMethod.MSGCODE_PREFIX
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
     * Returns <code>true</code> if this is an optional formula, <code>false</code> if it is not. If
     * the formula is optional, the user does not need to enter a formula expression.
     * <p>
     * This method always returns false if {@link #isFormulaOptionalSupported()} returns false.
     * 
     * @return <code>true</code> if the formula is optional and optional formula is supported.
     */
    public boolean isFormulaOptional();

    /**
     * Sets if this method is an optional formula or not.
     * 
     * @param newValue <code>true</code> to set the formula to be optional, false to set mandatory.
     * 
     * @see #isFormulaOptional()
     */
    public void setFormulaOptional(boolean newValue);

    /**
     * Returns <code>true</code> if this {@link IProductCmptTypeMethod} is a formula signature and
     * hence supports the optional flag. If this method returns <code>false</code> the method
     * {@link #isFormulaOptional()} will always return <code>false</code>, too.
     * 
     * @return <code>true</code> if optional formula is supported.
     * 
     * @see #isFormulaOptional()
     */
    public boolean isFormulaOptionalSupported();

    /**
     * Returns the name of the formula signature. Note that this is not equal to the method name.
     * For example the formula name might be 'Premium' while the method name might be
     * 'calculatePremium'. The formula name is presented when editing product components.
     */
    public String getFormulaName();

    /**
     * Sets the name of the formula signature.
     * 
     * @see #getFormulaName() for more information.
     */
    public void setFormulaName(String newName);

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

    /**
     * Returns a default name for the method based on the formula name. E.g. if the formula name is
     * 'Premium' the method might return 'computePremium'.
     */
    public String getDefaultMethodName();

}
