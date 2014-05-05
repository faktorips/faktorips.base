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

import java.util.List;

import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;

/**
 * The definition of a function to access a table.
 */
public interface ITableAccessFunction {

    /**
     * Returns the table structure this function gives access to.
     */
    public ITableStructure getTableStructure();

    /**
     * Returns the name of the column accessed by this function.
     */
    public String getAccessedColumnName();

    /**
     * Returns the column accessed by this function or <code>null</code> if the column can't be
     * found.
     */
    public IColumn getAccessedColumn();

    /**
     * Returns the function's return type.
     */
    public String getType();

    /**
     * Returns the function's arguments' types.
     */
    public List<String> getArgTypes();

    public Datatype[] findArgTypes();

    /**
     * Returns the description of this function
     * 
     */
    public String getDescription();

    public IIpsProject getIpsProject();

}
