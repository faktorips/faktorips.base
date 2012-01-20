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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Combo;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.table.ComboCellEditor;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;

/**
 * This is the {@link EditingSupport} for the columns of elements. It provides a {@link Combo} for
 * choosing the {@link IIpsElement} for the condition.
 * 
 * @author dicker
 */
final class ElementEditingSupport extends EnhancedCellTrackingEditingSupport {

    public ElementEditingSupport(TableViewer viewer) {
        super(viewer);
    }

    @Override
    protected IpsCellEditor getCellEditorInternal(Object element) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        UIToolkit toolkit = new UIToolkit(null);
        Combo combo = toolkit.createCombo(((TableViewer)getViewer()).getTable());

        List<? extends IIpsElement> searchableElements = model.getSearchableElements();
        String[] searchableElementsNames = new String[searchableElements.size()];
        for (int i = 0; i < searchableElementsNames.length; i++) {
            searchableElementsNames[i] = searchableElements.get(i).getName();
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

        return searchedElement == null ? StringUtils.EMPTY : searchedElement.getName();
    }

    @Override
    protected void setValue(Object element, Object value) {
        ProductSearchConditionPresentationModel model = (ProductSearchConditionPresentationModel)element;

        List<? extends IIpsElement> searchableElements = model.getSearchableElements();

        for (IIpsElement searchableElement : searchableElements) {
            if (searchableElement.getName().equals(value)) {
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