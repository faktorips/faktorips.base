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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.faktorips.devtools.model.IIpsElement;
import org.faktorips.devtools.model.IIpsModel;
import org.faktorips.devtools.model.ipsobject.ILabeledElement;

/**
 * This is the {@link CellLabelProvider} for the column of the elements
 * 
 * @author dicker
 */
final class ElementLabelProvider extends CellLabelProvider {
    @Override
    public void update(ViewerCell cell) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)cell.getElement();

        cell.setText(getLabelOrName(model.getSearchedElement()));
    }

    /**
     * Returns the label or name of the given {@link IIpsElement}. If no label is set, the name of
     * the {@link IIpsElement ipsElement} will be returned. If the {@link IIpsElement ipsElement} is
     * <code>null</code> an empty <code>String</code> is returned.
     * 
     * @param ipsElement the {@link IIpsElement} to get the label or name
     */
    public String getLabelOrName(IIpsElement ipsElement) {
        if (ipsElement == null) {
            return StringUtils.EMPTY;
        } else if (ipsElement instanceof ILabeledElement) {
            return IIpsModel.get().getMultiLanguageSupport().getLocalizedLabel((ILabeledElement)ipsElement);
        } else {
            return ipsElement.getName();
        }
    }
}
