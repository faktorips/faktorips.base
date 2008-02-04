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

package org.faktorips.devtools.core.model.productcmpttype;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.type.IMethod;

/**
 * Method signatures for product component types extend the "normal" method. The provide an implementation type
 * that defines how the method is implemented. 
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptTypeMethod extends IMethod, IProdDefProperty {

    public final static String PROPERTY_FORMULA_SIGNATURE_DEFINITION = "formulaSignatureDefinition"; //$NON-NLS-1$
    public final static String PROPERTY_OVERLOADED_FORMULA_SIGNATURE = "overloadedFormulaMethodSignature"; //$NON-NLS-1$
    public final static String PROPERTY_FORMULA_NAME = "formulaName"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the datatype of a formula signature definition is
     * void or a none valuedatatype.
     */
    public final static String MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES = MSGCODE_PREFIX + "DatatypeMustBeAValueDatatypeForFormulaSignature"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that a method is marked as formula signature but 
     * the formula name is missing.
     */
    public final static String MSGCODE_FORMULA_NAME_IS_EMPTY = MSGCODE_PREFIX + "FormulaNameIsEmpty"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a method is marked as formula signature but 
     * the formula name is missing.
     */
    public final static String MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT = MSGCODE_PREFIX + "FormulaMustntBeAbstract"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a the specified formula method signature doesn't exist in the supertype hierarchy.
     */
    public final static String MSGCODE_OVERLOADED_FORMULA_SIGNATURE_NOT_IN_SUPERTYPE_HIERARCHY = MSGCODE_PREFIX + "formulaSignatureNotInSupertypeHierarchy"; //$NON-NLS-1$

    /**
     * Returns the product component type this method belongs to.
     */
    public IProductCmptType getProductCmptType();
    
    /**
     * Returns <code>true</code> if this is formula signature definition, <code>false</code> if
     * it is not. 
     */
    public boolean isFormulaSignatureDefinition();

    /**
     * Sets if this method defines a formula signature or not.
     */
    public void setFormulaSignatureDefinition(boolean newValue);
    
    /**
     * Returns the name of the formula signature. Note that this is not equal to the method name.
     * For example the formula name might be 'Premium' while the method name might be 'calculatePremium'.
     * The formula name is presented when editing product components.
     */
    public String getFormulaName();
    
    /**
     * Sets the name of the formula signature. 
     * 
     * @see #getFormulaName() for more information.
     */
    public void setFormulaName(String newName);

    /**
     * Returns the formula method that is overloaded by this formula method. A formula method
     * overloads another formula method in the inheritence hierarchy if the formula names are
     * identical. Overloading of formula methods within the same class is not supported.
     */
    public String getOverloadedFormulaMethodSignature();

    /**
     * Returns true if this is a formula method that overloads a formula method within the supertype hierarchy.
     */
    public boolean overloadsFormulaInTypeHierarchy();
    
    /**
     * Returns the overloaded method of the supertype or <code>null</code> if none could be found.
     * 
     * @throws CoreException if an exception occurs during finding the method
     */
    public IProductCmptTypeMethod findOverloadedFormulaMethod() throws CoreException;
    
    /**
     * Sets the overloaed formula method. This method must be within the supertype hierarchy.
     * 
     * @see #getOverloadedFormulaMethodSignature()
     */
    public void setOverloadedFormulaMethodSignature(String overloadedFormulaMethod);

    /**
     * Returns a default name for the method based on the formula name. E.g. if the formula name is
     * 'Premium' the method  might return 'computePremium'.
     */
    public String getDefaultMethodName();
    
}
