/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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
