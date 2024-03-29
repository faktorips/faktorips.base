/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.testcase;

import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.testcasetype.ITestValueParameter;

/**
 * Specification of a test value.
 * 
 * @author Joerg Ortmann
 */
public interface ITestValue extends ITestObject {

    /** Property names */
    String PROPERTY_VALUE_PARAMETER = "testValueParameter"; //$NON-NLS-1$
    String PROPERTY_VALUE = "value"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "TESTVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the corresponding test value parameter not exists.
     */
    String MSGCODE_TEST_VALUE_PARAM_NOT_FOUND = MSGCODE_PREFIX + "TestValueParamNotFound"; //$NON-NLS-1$

    /**
     * Returns the test value.
     */
    String getTestValueParameter();

    /**
     * Sets the test value.
     */
    void setTestValueParameter(String valueParameter);

    /**
     * Returns the test value or <code>null</code> if the object does not exist.
     * 
     * @param ipsProject The IPS project which object path is used to search.
     * 
     * @throws IpsException if an error occurs while searching for the test value.
     */
    ITestValueParameter findTestValueParameter(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the value.
     */
    String getValue();

    /**
     * Sets the value
     */
    void setValue(String newValue);

    /**
     * Sets the default value of the test value. The default value of the data type will be used.
     * 
     * @throws IpsException if the test value parameter wasn't found
     */
    void setDefaultValue() throws IpsException;

}
