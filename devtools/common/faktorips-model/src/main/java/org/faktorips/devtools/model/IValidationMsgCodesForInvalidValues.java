/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model;

/**
 * This interface defines global validation message codes that are used by the validation of
 * different IPS objects / parts.
 * 
 * @author Joerg Ortmann
 */
public interface IValidationMsgCodesForInvalidValues {

    /**
     * Validation message code to indicate that a value can't be validated because the required
     * value data type can't be found.
     */
    String MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_CANT_BE_FOUND = "CantCheckValueBecauseValueDatatypeCantBeFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a value can't be validated because the required
     * value data type is invalid.
     */
    String MSGCODE_CANT_CHECK_VALUE_BECAUSE_VALUEDATATYPE_IS_INVALID = "CantCheckValueBecauseValueDatatypeIsInvalid"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that a value is not an "instance" of the value data type,
     * it should be an instance of.
     */
    String MSGCODE_VALUE_IS_NOT_INSTANCE_OF_VALUEDATATYPE = "ValueIsNotInstanceOfValueDatatype"; //$NON-NLS-1$

}
