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
    public static final String PROPERTY_VALUE_PARAMETER = "testValueParameter"; //$NON-NLS-1$
    public static final String PROPERTY_VALUE = "value"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "TESTVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the corresponding test value parameter not exists.
     */
    public static final String MSGCODE_TEST_VALUE_PARAM_NOT_FOUND = MSGCODE_PREFIX + "TestValueParamNotFound"; //$NON-NLS-1$

    /**
     * Returns the test value.
     */
    public String getTestValueParameter();

    /**
     * Sets the test value.
     */
    public void setTestValueParameter(String valueParameter);

    /**
     * Returns the test value or <code>null</code> if the object does not exists.
     * 
     * @param ipsProject The IPS project which object path is used to search.
     * 
     * @throws IpsException if an error occurs while searching for the test value.
     */
    public ITestValueParameter findTestValueParameter(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns the value.
     */
    public String getValue();

    /**
     * Sets the value
     */
    public void setValue(String newValue);

    /**
     * Sets the default value of the test value. The default value of the data type will be used.
     * 
     * @throws IpsException if the test value parameter wasn't found
     */
    public void setDefaultValue() throws IpsException;

}
