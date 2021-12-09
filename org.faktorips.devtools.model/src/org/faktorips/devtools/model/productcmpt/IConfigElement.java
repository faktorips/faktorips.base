/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.productcmpt;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;
import org.faktorips.devtools.model.pctype.IPolicyCmptTypeAttribute;

/**
 * A configuration element is based on an product component type's attribute.
 * <p>
 * For example a policy component could have a constant attribute interestRate. All product
 * components based on that policy component have a matching product attribute that stores the
 * concrete interest rate value.
 */
public interface IConfigElement extends IPropertyValue {

    public static final String PROPERTY_TYPE = "type"; //$NON-NLS-1$
    public static final String PROPERTY_POLICY_CMPT_TYPE_ATTRIBUTE = "policyCmptTypeAttribute"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "CONFIGELEMENT-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute the configuration element is based
     * can't be found.
     */
    public static final String MSGCODE_UNKNWON_ATTRIBUTE = MSGCODE_PREFIX + "UnknownAttribute"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the attribute's data type can't be found and so the
     * value can't be parsed.
     */
    public static final String MSGCODE_UNKNOWN_DATATYPE = MSGCODE_PREFIX + "UnknownDatatype"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the data type is invalid. (E.g. the definition of a
     * dynamic data type can be wrong.)
     */
    public static final String MSGCODE_INVALID_DATATYPE = MSGCODE_PREFIX + "InvalidDatatype"; //$NON-NLS-1$

    /**
     * Returns the name of the product component type's attribute this element is based on.
     */
    public String getPolicyCmptTypeAttribute();

    /**
     * Sets the name of the product component type's attribute this element is based on.
     */
    public void setPolicyCmptTypeAttribute(String policyCmptTypeAttribute);

    /**
     * Finds the corresponding attribute in the product component type this product component is an
     * instance of.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search.
     * 
     * @return the corresponding attribute or <code>null</code> if no such attribute exists.
     * 
     * @throws CoreRuntimeException if an exception occurs while searching for the attribute.
     */
    public IPolicyCmptTypeAttribute findPcTypeAttribute(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Returns the element's value data type, or <code>null</code> if it can't be found. The
     * configuration element's data type is the attribute's data type the element is based on.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search.
     * 
     */
    public ValueDatatype findValueDatatype(IIpsProject ipsProject);
}
