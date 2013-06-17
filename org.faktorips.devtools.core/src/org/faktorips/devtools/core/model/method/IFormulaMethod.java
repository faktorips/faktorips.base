/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model.method;

import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;

/**
 * Interface to extend the {@link IBaseMethod}
 * 
 * @author frank
 */
public interface IFormulaMethod extends IBaseMethod, IDescribedElement, ILabeledElement {

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
    public static final String MSGCODE_FORMULA_NAME_IS_EMPTY = IBaseMethod.MSGCODE_PREFIX + "FormulaNameIsEmpty"; //$NON-NLS-1$

    /**
     * Comment for <code>PROPERTY_FORMULA_NAME</code>
     */
    public static final String PROPERTY_FORMULA_NAME = "formulaName"; //$NON-NLS-1$

    /**
     * Comment for <code>MSGCODE_INVALID_FORMULA_NAME</code>
     */
    public static final String MSGCODE_INVALID_FORMULA_NAME = IBaseMethod.MSGCODE_PREFIX + "InvalidFormulaName"; //$NON-NLS-1$

    public static final String MSGCODE_DUPLICATE_FUNCTION = MSGCODE_PREFIX + "duplicateFunction"; //$NON-NLS-1$

    public static final String MSGCODE_DUPLICATE_SIGNATURE = MSGCODE_PREFIX + "duplicateSignature"; //$NON-NLS-1$

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
     * Returns a default name for the method based on the formula name. E.g. if the formula name is
     * 'Premium' the method might return 'computePremium'.
     */
    public String getDefaultMethodName();

}
