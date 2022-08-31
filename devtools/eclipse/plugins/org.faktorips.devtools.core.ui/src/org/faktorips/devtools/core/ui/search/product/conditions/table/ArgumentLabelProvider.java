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
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * This is the {@link CellLabelProvider} for the column of the arguments
 * 
 * @author dicker
 */
final class ArgumentLabelProvider extends CellLabelProvider {
    @Override
    public void update(ViewerCell cell) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)cell.getElement();

        cell.setText(createCellText(model));

    }

    private String createCellText(ProductSearchConditionPresentationModel model) {
        if (model.getConditionType() == null) {
            return IpsStringUtils.EMPTY;
        }
        if (model.getConditionType().hasValueSet()) {
            ValueDatatype datatype = getValueDatatype(model);

            if (datatype != null) {
                return IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(datatype, model.getArgument());
            }
        }

        if (model.getArgument() == null) {
            return IpsStringUtils.EMPTY;
        }
        return model.getArgument();
    }

    private ValueDatatype getValueDatatype(ProductSearchConditionPresentationModel model) {
        if (model.getSearchedElement() == null) {
            return null;
        }
        return model.getConditionType().getValueDatatype(model.getSearchedElement());
    }
}
