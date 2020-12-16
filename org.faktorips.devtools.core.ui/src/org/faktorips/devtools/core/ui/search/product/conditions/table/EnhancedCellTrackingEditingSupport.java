/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.table;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.faktorips.devtools.core.ui.table.CellTrackingEditingSupport;

/**
 * An {@link EditingSupport} for the table of search conditions.
 * 
 * @author dicker
 */
public abstract class EnhancedCellTrackingEditingSupport extends
        CellTrackingEditingSupport<ProductSearchConditionPresentationModel> {

    public EnhancedCellTrackingEditingSupport(ColumnViewer viewer) {
        super(viewer);
    }

    /**
     * returns the index of the column of this EditingSupport
     */
    public abstract int getColumnIndex();

    /**
     * Enhance the visibility
     */
    @Override
    protected abstract boolean canEdit(Object element);
}
