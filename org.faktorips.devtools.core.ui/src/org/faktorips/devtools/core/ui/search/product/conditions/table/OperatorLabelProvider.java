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
import org.faktorips.devtools.core.ui.search.product.conditions.types.ISearchOperatorType;

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
            cell.setText(StringUtils.EMPTY);
        } else {
            cell.setText(operatorType.getLabel());
        }
    }
}