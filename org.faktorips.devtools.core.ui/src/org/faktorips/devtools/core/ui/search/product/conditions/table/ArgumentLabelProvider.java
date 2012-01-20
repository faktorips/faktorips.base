/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.search.product.conditions.table;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;

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
            return StringUtils.EMPTY;
        }
        if (model.getConditionType().hasValueSet()) {
            ValueDatatype datatype = getValueDatatype(model);

            if (datatype != null) {
                return IpsUIPlugin.getDefault().getDatatypeFormatter().formatValue(datatype, model.getArgument());
            }
        }

        if (model.getArgument() == null) {
            return StringUtils.EMPTY;
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