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

    public static final String PROPERTY_VALUEDATATYPE = "valueDatatype"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "TESTVALUEPARAMETER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the value data type not exists.
     */
    public static final String MSGCODE_VALUEDATATYPE_NOT_FOUND = MSGCODE_PREFIX + "ValueDatatypeNotFound"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there is an unsupported type.
     */
    public static final String MSGCODE_WRONG_TYPE = MSGCODE_PREFIX + "WrongType"; //$NON-NLS-1$

    /**
     * Returns the data type.
     */
    public String getValueDatatype();

    /**
     * Sets the data type.
     */
    public void setValueDatatype(String datatype);

    /**
     * Returns the data type or <code>null</code> if the object does not exists.
     * 
     * @param ipsProject The IPS project which object path is used to search.
     */
    public ValueDatatype findValueDatatype(IIpsProject ipsProject);

}
