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

import java.util.List;

import org.faktorips.devtools.model.ipsproject.IIpsProject;

/**
 * The definition of a function to access a table.
 */
public interface ITableAccessFunction {

    /**
     * Returns the table structure this function gives access to.
     */
    ITableStructure getTableStructure();

    /**
     * Returns the name of the column accessed by this function.
     */
    String getAccessedColumnName();

    /**
     * Returns the column accessed by this function or <code>null</code> if the column can't be
     * found.
     */
    IColumn getAccessedColumn();

    /**
     * Returns the function's return type.
     */
    String getType();

    /**
     * Returns the function's arguments' types.
     */
    List<String> getArgTypes();

    /**
     * Returns the description of this function
     * 
     */
    String getDescription();

    IIpsProject getIpsProject();

}
