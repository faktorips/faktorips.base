/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.testcasetype;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * Specification of a test value parameter.
 * 
 * @author Joerg Ortmann
 */
public interface ITestValueParameter extends ITestParameter {

    String PROPERTY_VALUEDATATYPE = "valueDatatype"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "TESTVALUEPARAMETER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value data type not exists.
     */
    String MSGCODE_VALUEDATATYPE_NOT_FOUND = MSGCODE_PREFIX + "ValueDatatypeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is an unsupported type.
     */
    String MSGCODE_WRONG_TYPE = MSGCODE_PREFIX + "WrongType"; //$NON-NLS-1$

    /**
     * Returns the data type.
     */
    String getValueDatatype();

    /**
     * Sets the data type.
     */
    void setValueDatatype(String datatype);

    /**
     * Returns the data type or <code>null</code> if the object does not exist.
     * 
     * @param ipsProject The IPS project which object path is used to search.
     */
    ValueDatatype findValueDatatype(IIpsProject ipsProject);

}
