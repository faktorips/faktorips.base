/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.bf;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * A decision business function element has one incoming and one or more outgoing control flows.
 * Which outgoing control flow is actually chosen at execution time depends on the condition value
 * that is calculated. The calculated condition value is compared to the values assigned to the
 * outgoing control flows. The one which value is equal to the calculated value is where the control
 * flow continues.
 * 
 * @author Peter Erzberger
 */
public interface IDecisionBFE extends IMethodCallBFE {

    public final static String XML_TAG = "Decision"; //$NON-NLS-1$
    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    public static final String MSGCODE_PREFIX = "DECISIONBFE-"; //$NON-NLS-1$
    public static final String MSGCODE_DATATYPE_NOT_SPECIFIED = MSGCODE_PREFIX + "datatypeNotSpecified"; //$NON-NLS-1$
    public static final String MSGCODE_DATATYPE_DOES_NOT_EXIST = MSGCODE_PREFIX + "datatypeDoesNotExist"; //$NON-NLS-1$
    public static final String MSGCODE_DATATYPE_ONLY_NONE_PRIM_VALUEDATATYPE = MSGCODE_PREFIX
            + "datatypeOnlyNonePrimValuedatatype"; //$NON-NLS-1$

    /**
     * Returns the data type of the condition value.
     */
    public String getDatatype();

    /**
     * Sets the data type of the condition value.
     */
    public void setDatatype(String datatype);

    /**
     * Returns the data type object for the specified data type. If none is found <code>null</code>
     * will be returned. Only value data types are allowed.
     * 
     * @throws CoreException is throw if an exception occurs during the course of searching
     */
    public ValueDatatype findDatatype(IIpsProject ipsProject) throws CoreException;

}
