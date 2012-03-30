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

import java.util.List;

import org.faktorips.devtools.core.ui.table.CellTrackingEditingSupport;
import org.faktorips.devtools.core.ui.table.LinkedColumnsTraversalStrategy;

public class EditTableTraversalStrategy extends LinkedColumnsTraversalStrategy {

    private final int columnIndex;
    private final AbstractListTableModel<?> listTableModel;

    public EditTableTraversalStrategy(CellTrackingEditingSupport editingSupport, int columnIndex,
            AbstractListTableModel<?> listTableModel) {
        super(editingSupport);
        this.columnIndex = columnIndex;
        this.listTableModel = listTableModel;
    }

    @Override
    protected int getColumnIndex() {
        return columnIndex;
    }

    @Override
    protected boolean canEdit(Object currentViewItem) {
        return true;
    }

    /**
     * Returns the previous item if there is one. Returns <code>null</code> otherwise. If the
     * requested view item does not exist in the model the first element is returned or
     * <code>null</code> if the model contains no values. {@inheritDoc}
     */
    @Override
    protected Object getPreviousVisibleViewItem(Object currentViewItem) {
        List<?> list = listTableModel.getList();
        int currentIndex = indexOf(list, currentViewItem);
        if (currentIndex < 0) {
            return list.isEmpty() ? null : list.get(0);
        } else if (currentIndex == 0) {
            return null;
        } else {
            return list.get(currentIndex - 1);
        }
    }

    private int indexOf(List<?> list, Object currentViewItem) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == currentViewItem) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the next item if there is one. Returns <code>null</code> otherwise. If the requested
     * view item does not exist in the model the first element is returned or <code>null</code> if
     * the model contains no values. {@inheritDoc}
     */
    @Override
    protected Object getNextVisibleViewItem(Object currentViewItem) {
        List<?> list = listTableModel.getList();
        int currentIndex = indexOf(list, currentViewItem);
        if (currentIndex < 0) {
            return list.isEmpty() ? null : list.get(0);
        } else if (currentIndex == list.size() - 1) {
            return null;
        } else {
            return list.get(currentIndex + 1);
        }
    }

}
