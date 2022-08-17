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

import java.util.List;

import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.model.IIpsElement;

/**
 * This is the {@link EditingSupport} for the columns of elements. It provides a {@link Combo} for
 * choosing the {@link IIpsElement} for the condition.
 * 
 * @author dicker
 */
final class ElementEditingSupport extends EnhancedCellTrackingEditingSupport {

    private ElementLabelProvider elementLabelProvider;

    public ElementEditingSupport(TableViewer viewer) {
        super(viewer);
        elementLabelProvider = new ElementLabelProvider();
    }

    @Override
    protected IpsCellEditor getCellEditorInternal(ProductSearchConditionPresentationModel element) {
        ProductSearchConditionPresentationModel model = element;

        UIToolkit toolkit = new UIToolkit(null);
        Combo combo = toolkit.createCombo(((TableViewer)getViewer()).getTable());

        List<? extends IIpsElement> searchableElements = model.getSearchableElements();
        String[] searchableElementsNames = new String[searchableElements.size()];
        for (int i = 0; i < searchableElementsNames.length; i++) {
            searchableElementsNames[i] = elementLabelProvider.getLabelOrName(searchableElements.get(i));
        }

        combo.setItems(searchableElementsNames);

        return new ComboCellEditor(combo);
    }

    @Override
    public boolean canEdit(Object element) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;
        return model.getConditionType() != null;
    }

    @Override
    protected Object getValue(Object element) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;
        IIpsElement searchedElement = model.getSearchedElement();
        return elementLabelProvider.getLabelOrName(searchedElement);
    }

    @Override
    protected void setValue(Object element, Object value) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        List<? extends IIpsElement> searchableElements = model.getSearchableElements();

        for (IIpsElement searchableElement : searchableElements) {
            if (elementLabelProvider.getLabelOrName(searchableElement).equals(value)) {
                model.setSearchedElement(searchableElement);
                getViewer().refresh();
                return;
            }
        }
    }

    @Override
    public int getColumnIndex() {
        return 1;
    }
}
