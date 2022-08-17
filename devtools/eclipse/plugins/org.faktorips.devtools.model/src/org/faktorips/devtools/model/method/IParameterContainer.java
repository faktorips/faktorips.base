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

import java.util.List;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;

/**
 * A container containing parameters.
 * 
 * @author Jan Ortmann
 */
public interface IParameterContainer extends IIpsObjectPart {

    /**
     * Returns the method's parameters. Returns an empty array if the method doesn't have any
     * parameter.
     */
    IParameter[] getParameters();

    /**
     * Returns the method's parameter names. Returns an empty array if the method doesn't have any
     * parameter.
     */
    String[] getParameterNames();

    /**
     * Returns the method's parameter {@link Datatype datatypes}. Returns an empty array if the
     * method doesn't have any parameter.
     * 
     */
    List<Datatype> getParameterDatatypes();

    /**
     * Returns the number of parameters.
     */
    int getNumOfParameters();

    /**
     * Creates a new parameter.
     */
    IParameter newParameter();

    /**
     * Creates a new parameter.
     */
    IParameter newParameter(String datatype, String name);

    /**
     * Moves the parameters identified by the given indices up or down by one position. If one of
     * the indices is 0 (the first parameter), no parameter is moved up. If one of the indices is
     * the number of parameters - 1 (the last parameter) no parameter is moved down.
     * <p>
     * Returns the new indices of the moved parameters.
     * 
     * @param indices The indices identifying the parameters.
     * @param up <code>true</code>, to move the parameters up, <code>false</code> to move them down.
     * 
     * @throws NullPointerException If <code>indices</code> is <code>null</code>.
     * @throws IndexOutOfBoundsException If one of the indices does not identify a parameter.
     */
    int[] moveParameters(int[] indices, boolean up);

}
