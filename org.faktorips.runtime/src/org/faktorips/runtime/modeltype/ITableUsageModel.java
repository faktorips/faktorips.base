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

import java.util.Calendar;

import org.faktorips.runtime.IProductComponent;
import org.faktorips.runtime.IProductComponentGeneration;
import org.faktorips.runtime.ITable;
import org.faktorips.runtime.model.table.TableModel;

/**
 * This interface describes the model information for a table usage.
 */
public interface ITableUsageModel extends IModelElement {

    /**
     * Returns the table the given product component references for this table usage. If this table
     * usage is changing over time (resides in the generation) the date is used to retrieve the
     * correct generation. If the date is <code>null</code> the latest generation is used. If the
     * table usage is not changing over time the date will be ignored.
     * 
     * 
     * @param productComponent a product object corresponding to the {@link IProductModel} this
     *            association belongs to
     * @param effectiveDate The date that should be used to get the
     *            {@link IProductComponentGeneration} if this association is changing over time. May
     *            be <code>null</code> to get the latest generation.
     * 
     * @return The table instance hold by the product component and is identified by this table
     *         usage
     */
    public ITable getTable(IProductComponent productComponent, Calendar effectiveDate);

    /**
     * @return the model for the table structure referenced in this table usage.
     */
    public TableModel getTableModel();

}
