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

import org.faktorips.devtools.core.ui.table.CellTrackingEditingSupport;
import org.faktorips.devtools.core.ui.table.LinkedColumnsTraversalStrategy;
import org.faktorips.devtools.core.ui.table.TraversalStrategy;

/**
 * This class is the {@link TraversalStrategy} for the table of search conditions.
 * 
 * @author dicker
 */
final class ConditionsTableTraversalStrategy extends
        LinkedColumnsTraversalStrategy<ProductSearchConditionPresentationModel> {

    ConditionsTableTraversalStrategy(
            CellTrackingEditingSupport<ProductSearchConditionPresentationModel> editingSupport) {
        super(editingSupport);
        editingSupport.setTraversalStrategy(this);
    }

    @Override
    protected ProductSearchConditionPresentationModel getPreviousVisibleViewItem(
            ProductSearchConditionPresentationModel currentViewItem) {
        return null;
    }

    @Override
    protected ProductSearchConditionPresentationModel getNextVisibleViewItem(
            ProductSearchConditionPresentationModel currentViewItem) {
        return null;
    }

    @Override
    protected int getColumnIndex() {
        return getEnhancedCellTrackingEditingSupport().getColumnIndex();
    }

    @Override
    protected boolean canEdit(ProductSearchConditionPresentationModel currentViewItem) {
        return getEnhancedCellTrackingEditingSupport().canEdit(currentViewItem);
    }

    private EnhancedCellTrackingEditingSupport getEnhancedCellTrackingEditingSupport() {
        return ((EnhancedCellTrackingEditingSupport)getEditingSupport());
    }
}
