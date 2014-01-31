/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.method;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * A parameter (or argument) of a method.
 * 
 * @author Jan Ortmann
 */
public interface IParameter extends IIpsObjectPart {

    public static final String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    /**
     * Returns the parent container this parameter is part of.
     * 
     * @return The container this parameter is part of.
     */
    public IParameterContainer getParameterContainer();

    /**
     * Sets the name of the parameter.
     */
    public void setName(String newName);

    /**
     * Sets the parameter's value data type.
     */
    public void setDatatype(String type);

    /**
     * Returns the parameter's data type.
     */
    public String getDatatype();

    /**
     * Returns the parameter's data type or <code>null</code> if the data type can't be found.
     * 
     * @param ipsProject The IPS project which IPS object path is used to search.
     * 
     * @throws CoreException If an exception occurs while searching for the data type.
     */
    public Datatype findDatatype(IIpsProject ipsProject) throws CoreException;

}
