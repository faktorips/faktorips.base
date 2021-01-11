/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls.tableedit;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * Label provider for the table viewer of a {@link EditTableControlViewer}. Uses a
 * {@link DatatypeEditingSupport} to format each value depending on datatype and current locale.
 * 
 * @author Stefan Widmaier
 */
public class DatatypeCellLabelProvider extends CellLabelProvider {

    private final FormattedCellEditingSupport<Object, ?> editingSupport;

    public DatatypeCellLabelProvider(FormattedCellEditingSupport<?, ?> editingSupport) {
        @SuppressWarnings("unchecked")
        // the type does not matter and cell.getElement (in update(ViewerCell)) does only provide an
        // Object
        FormattedCellEditingSupport<Object, ?> castedEditingSupport = (FormattedCellEditingSupport<Object, ?>)editingSupport;
        this.editingSupport = castedEditingSupport;

    }

    public String getText(Object element) {
        return editingSupport.getFormattedValue(element);
    }

    @Override
    public void update(ViewerCell cell) {
        Object element = cell.getElement();
        cell.setText(getText(element));
    }
}
