/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.model.table.TableModel;

/**
 * This interface describes the model information for a table usage.
 */
public interface ITableUsageModel extends IModelElement {

    /**
     * Calls the method to get the table described by this {@link ITableUsageModel} on the specified
     * product component.
     * 
     * @param productComponent The product component that holds the table instance
     * 
     * @return The table instance hold by the product component and is identified by this table
     *         usage
     */
    public ITable getTable(IProductComponent productComponent);

    /**
     * @return the model for the table structure referenced in this table usage.
     */
    public TableModel getTableModel();

}
