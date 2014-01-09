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

package org.faktorips.devtools.core.model.bf;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;

/**
 * A business function can have parameters that are represented by this interface. A parameter has a
 * data type which can be a value data type or a policy or product component type. Parameters can be
 * modified by the business function.
 * 
 * @author Peter Erzberger
 */
public interface IParameterBFE extends IBFElement {

    public final static String XML_TAG = "Parameter"; //$NON-NLS-1$
    public final static String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    public static final String MSGCODE_PREFIX = "PARAMETERBFE-"; //$NON-NLS-1$
    public static final String MSGCODE_NAME_DUBLICATE = MSGCODE_PREFIX + "nameDuplicate"; //$NON-NLS-1$
    public static final String MSGCODE_DATATYPE_NOT_SPECIFIED = MSGCODE_PREFIX + "datatypeNotSpecified"; //$NON-NLS-1$
    public static final String MSGCODE_DATATYPE_DOES_NOT_EXISIT = MSGCODE_PREFIX + "datatypeDoesNotExists"; //$NON-NLS-1$

    /**
     * Returns the parameters data type.
     */
    public String getDatatype();

    /**
     * Sets the parameters data type.
     */
    public void setDatatype(String datatype);

    /**
     * Returns the data type object or <code>null</code> if none is found.
     * 
     * @throws CoreException is throw if an exception occurs during the course of searching
     */
    public Datatype findDatatype() throws CoreException;

}
