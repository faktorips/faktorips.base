/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 *
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 *
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.method;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.abstraction.exception.IpsException;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * A parameter (or argument) of a method.
 *
 * @author Jan Ortmann
 */
public interface IParameter extends IIpsObjectPart {

    String TAG_NAME = "Parameter"; //$NON-NLS-1$
    String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    /**
     * Returns the parent container this parameter is part of.
     *
     * @return The container this parameter is part of.
     */
    IParameterContainer getParameterContainer();

    /**
     * Sets the name of the parameter.
     */
    void setName(String newName);

    /**
     * Sets the parameter's value data type.
     */
    void setDatatype(String type);

    /**
     * Returns the parameter's data type.
     */
    String getDatatype();

    /**
     * Returns the parameter's data type or <code>null</code> if the data type can't be found.
     *
     * @param ipsProject The IPS project which IPS object path is used to search.
     *
     * @throws IpsException If an exception occurs while searching for the data type.
     */
    Datatype findDatatype(IIpsProject ipsProject) throws IpsException;

}
