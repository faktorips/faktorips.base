/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version 3
 * and if and when this source code belongs to the faktorips-runtime or faktorips-valuetype
 * component under the terms of the LGPL Lesser General Public License version 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and the
 * possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.method;

import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.ILabeledElement;

/**
 * Interface for methods that represents the signature of a formula
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

    public static final String PROPERTY_FORMULA_NAME = "formulaName"; //$NON-NLS-1$

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
