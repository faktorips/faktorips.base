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

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.faktorips.devtools.core.ui.search.product.conditions.types.ISearchOperatorType;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * This is the {@link CellLabelProvider} for the column of the operators
 * 
 * @author dicker
 */
final class OperatorLabelProvider extends CellLabelProvider {

    @Override
    public void update(ViewerCell cell) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)cell.getElement();
        ISearchOperatorType operatorType = model.getOperatorType();
        if (operatorType == null) {
            cell.setText(IpsStringUtils.EMPTY);
        } else {
            cell.setText(operatorType.getLabel());
        }
    }
}
