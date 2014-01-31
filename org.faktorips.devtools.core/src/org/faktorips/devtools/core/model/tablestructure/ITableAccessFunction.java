/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.model.tablestructure;

import org.faktorips.devtools.core.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;

/**
 * The definition of a function to access a table.
 * 
 * @author firstName lastName
 */
public interface ITableAccessFunction extends IIpsObjectPart, IDescribedElement {

    /**
     * Returns the table structure this function gives access to.
     */
    public ITableStructure getTableStructure();

    /**
     * Returns the name of the column accessed by this function.
     */
    public String getAccessedColumn();

    /**
     * Sets the name of the column accessed by this function.
     */
    public void setAccessedColumn(String columnName);

    /**
     * Returns the column accessed by this function or <code>null</code> if the column can't be
     * found.
     */
    public IColumn findAccessedColumn();

    /**
     * Returns the function's return type.
     */
    public String getType();

    /**
     * Sets the function's return type.
     */
    public void setType(String newType);

    /**
     * Returns the function's arguments' types.
     */
    public String[] getArgTypes();

    /**
     * Sets the function's arguments' types.
     */
    public void setArgTypes(String[] argTypes);

}
