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
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * A type's method.
 */
public interface IBaseMethod extends IParameterContainer {

    public static final String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    public static final String PROPERTY_PARAMETERS = "parameters"; //$NON-NLS-1$

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "METHOD-"; //$NON-NLS-1$

    public static final String MSGCODE_MULTIPLE_USE_OF_SAME_PARAMETER_NAME = MSGCODE_PREFIX
            + "multipleUseOfSameParameterName"; //$NON-NLS-1$

    public static final String MSGCODE_NO_NAME = MSGCODE_PREFIX + "noName"; //$NON-NLS-1$

    public static final String MSGCODE_INVALID_METHODNAME = MSGCODE_PREFIX + "InvalidMethodname"; //$NON-NLS-1$

    /**
     * Sets the method's name.
     */
    public void setName(String newName);

    /**
     * Returns the name of the value data type this method returns.
     */
    public String getDatatype();

    /**
     * Sets name of the value data type this method returns.
     */
    public void setDatatype(String newDatatype);

    /**
     * Returns the method's (return) data type. Returns <code>null</code> if the data type can't be
     * found.
     * 
     * @param ipsProject The project which IPS object path is used to search. This is not
     *            necessarily the project this method belongs to.
     * 
     * @throws CoreRuntimeException If an error occurs while searching.
     * @throws NullPointerException If <code>ipsProject</code> is <code>null</code>.
     */
    public Datatype findDatatype(IIpsProject ipsProject) throws CoreRuntimeException;

    /**
     * Checks whether the signature of the <code>otherMethod</code> is the same as the signature of
     * this method. The signature is the same if the name of the method is equal, the number of
     * parameters is equal and the data types of the parameters are equal. Note that the return
     * values are not compared.
     * <p>
     * Returns <code>true</code> if the signature of the other method is the same as the signature
     * of this method.
     * 
     * @param otherMethod The other method which this method has to be compared with.
     */
    public boolean isSameSignature(IBaseMethod otherMethod);

    /**
     * Returns the method's signature in string format, e.g. <code>computePremium(base.Coverage,
     * base.Contract, Integer)</code>.
     */
    public String getSignatureString();

    /**
     * Returns the Java modifier. Determined from the IPS modifier and the abstract flag.
     * 
     * @see java.lang.reflect.Modifier
     */
    public int getJavaModifier();
}
