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
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.testcasetype.ITestAttribute;
import org.faktorips.devtools.model.type.IAttribute;

/**
 * Specification of a test attribute value.
 * 
 * @author Joerg Ortmann
 */
public interface ITestAttributeValue extends IIpsObjectPart {

    public static final String PROPERTY_ATTRIBUTE = "testAttribute"; //$NON-NLS-1$
    public static final String PROPERTY_VALUE = "value"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "TESTATTRIBUTEVALUE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the corresponding test attribute not exists.
     */
    public static final String MSGCODE_TESTATTRIBUTE_NOT_FOUND = MSGCODE_PREFIX + "TestAttributeNotFound"; //$NON-NLS-1$

    /**
     * Returns the attribute.
     */
    public String getTestAttribute();

    /**
     * Sets the given attribute.
     */
    public void setTestAttribute(String attribute);

    /**
     * Returns the test attribute or <code>null</code> if the test attribute does not exist.
     * 
     * @param ipsProject The IPS project which object path is used to search.
     * 
     * @throws IpsException if an error occurs while searching for the test attribute.
     */
    public ITestAttribute findTestAttribute(IIpsProject ipsProject) throws IpsException;

    /**
     * Search and returns the corresponding attribute.<br>
     * If the given test policy component is not product relevant then the attribute will be
     * searched using the policy component super-/subtype hierarchy. If the test policy component is
     * product relevant then the corresponding product component type will be used to start the
     * searching the supertype hierarchy.
     */
    public IAttribute findAttribute(IIpsProject ipsProject) throws IpsException;

    /**
     * Returns value of the attribute.
     */
    public String getValue();

    /**
     * Sets the value of the attribute.
     */
    public void setValue(String newValue);

    /**
     * Sets the default value of the test attribute value. If the test attribute defines an input
     * parameter then the default value of the model attribute will be used, otherwise the default
     * of the data type will be set.
     * 
     * @throws IpsException if the test value parameter wasn't found
     */
    public void setDefaultValue() throws IpsException;

    /**
     * Returns <code>true</code> if the test attribute value is an input attribute, otherwise
     * <code>false</code>.
     * 
     * @param ipsProject The IPS project which object path is used to search the attribute.
     */
    public boolean isInputAttribute(IIpsProject ipsProject);

    /**
     * Returns <code>true</code> if the test attribute value is an expected result attribute,
     * otherwise <code>false</code>.
     * 
     * @param ipsProject The IPS project which object path is used to search the attribute.
     */
    public boolean isExpectedResultAttribute(IIpsProject ipsProject);

    /**
     * Updates the default for the test attribute value. The default will be retrieved from the
     * product component or if no product component is available or the attribute isn't configured
     * by product then from the policy component. Don't update the value if not default is
     * specified.
     * 
     * @throws IpsException in case of an error.
     */
    public void updateDefaultTestAttributeValue() throws IpsException;

}
