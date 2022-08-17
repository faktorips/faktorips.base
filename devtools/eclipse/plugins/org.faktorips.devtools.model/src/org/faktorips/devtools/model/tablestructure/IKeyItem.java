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

import org.faktorips.devtools.model.ipsobject.IDescribedElement;
import org.faktorips.devtools.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.model.ipsobject.IVersionControlledElement;

/**
 * A key item is a part of an index. There are two kind of key items: columns and ranges.
 */
public interface IKeyItem extends IIpsObjectPart, IDescribedElement, IVersionControlledElement {

    /**
     * Returns the item's name.
     */
    @Override
    String getName();

    /**
     * Returns the name for a parameter in a table access function. For columns this is the name of
     * the column, for ranges this parameter can be specified.
     */
    String getAccessParameterName();

    /**
     * Returns the item's data type. For columns this is the column's data type and for ranges this
     * is the data type of the column if it's a one column range and the first column's data type if
     * it is a two column range.
     */
    String getDatatype();

    /**
     * Returns the columns this item comprises.
     */
    IColumn[] getColumns();

    /**
     * Returns <code>true</code> if this key item is a range, false if not.
     */
    boolean isRange();

}
