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

import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;

/**
 * Specification of a test parameter.
 * 
 * @author Joerg Ortmann
 */
public interface ITestParameter extends IIpsObjectPart, IDescribedElement {

    /** Property names */
    String PROPERTY_TEST_PARAMETER_TYPE = "testParameterType"; //$NON-NLS-1$
    String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    String MSGCODE_PREFIX = "TESTPARAMETER-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the test parameter name is duplicate.
     */
    String MSGCODE_DUPLICATE_NAME = MSGCODE_PREFIX + "DuplicateName"; //$NON-NLS-1$

    /**
     * Validation message code to indicate the correct name format.
     */
    String MSGCODE_INVALID_NAME = MSGCODE_PREFIX + "InvalidName"; //$NON-NLS-1$

    /**
     * Returns the parameters's name, e.g. 'effectiveDate'. {@inheritDoc}
     */
    @Override
    String getName();

    /**
     * Sets the parameters's name, e.g. 'effectiveDate'.
     */
    void setName(String newName);

    /**
     * Sets the data type.
     */
    void setDatatype(String datatype);

    /**
     * Returns the data type.
     */
    String getDatatype();

    /**
     * Returns <code>true</code> if the test parameter is an input parameter, otherwise
     * <code>false</code>. Note if the parameter is a child parameter then the type (input, expected
     * result, or combined) of the root test parameter will be checked. All children inherit the
     * type of the root parameter.
     */
    boolean isInputOrCombinedParameter();

    /**
     * Returns <code>true</code> if the parameter is an expected result parameter, otherwise
     * <code>false</code>. Note if the parameter is a child parameter then the type (input, expected
     * result, or combined) of the root test parameter will be checked. All children inherit the
     * type of the root parameter.
     */
    boolean isExpextedResultOrCombinedParameter();

    /**
     * Returns <code>true</code> if the test parameter is a combined parameter, otherwise
     * <code>false</code>. A combined test parameter is a parameter that specifies both input
     * objects and expected results objects. Note if the parameter is a child parameter then the
     * type (input, expected result, or combined) of the root test parameter will be checked. All
     * children inherit the type of the root parameter.
     */
    boolean isCombinedParameter();

    /**
     * Returns the type of the test parameter.
     */
    TestParameterType getTestParameterType();

    /**
     * Sets the parameter type of the test parameter.
     */
    void setTestParameterType(TestParameterType testParameterType);

    /**
     * Returns the root test parameter.
     */
    ITestParameter getRootParameter();

    /**
     * Returns <code>true</code> if the test parameter is a root parameter or <code>false</code> if
     * the parameter is a child of another object.
     */
    boolean isRoot();

}
