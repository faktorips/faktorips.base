/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.model.tablestructure;

import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.model.exception.CoreRuntimeException;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;
import org.faktorips.devtools.model.ipsproject.IIpsProject;

public interface IColumn extends IKeyItem, ILabeledElement {

    /**
     * Prefix for all message codes of this class.
     */
    public static final String MSGCODE_PREFIX = "ATTRIBUTE-"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the column's datatype is a primitive, (but
     * primitives aren't supported.)
     */
    public static final String MSGCODE_DATATYPE_IS_A_PRIMITTVE = MSGCODE_PREFIX + "DatatypeIsAPrimitive"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that the column's datatype is a primitive, (but
     * primitives aren't supported.)
     */
    public static final String MSGCODE_INVALID_NAME = MSGCODE_PREFIX + "InvalidName"; //$NON-NLS-1$

    public static final String PROPERTY_DATATYPE = "datatype"; //$NON-NLS-1$

    /**
     * Sets the column name.
     * 
     * @throws IllegalArgumentException if newName is <code>null</code>.
     */
    public void setName(String newName);

    /**
     * Sets the column's data type.
     * 
     * @throws IllegalArgumentException if newDatatype is <code>null</code>.
     */
    public void setDatatype(String newDatatype);

    /**
     * Returns the value data type of this column.
     */
    public ValueDatatype findValueDatatype(IIpsProject ipsProject) throws CoreRuntimeException;

}
