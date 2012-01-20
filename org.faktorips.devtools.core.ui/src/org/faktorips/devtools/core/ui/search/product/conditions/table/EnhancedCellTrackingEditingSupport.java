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

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.faktorips.devtools.core.ui.table.CellTrackingEditingSupport;

/**
 * An {@link EditingSupport} for the table of search conditions.
 * 
 * @author dicker
 */
public abstract class EnhancedCellTrackingEditingSupport extends CellTrackingEditingSupport {

    public EnhancedCellTrackingEditingSupport(ColumnViewer viewer) {
        super(viewer);
    }

    /**
     * returns the index of the column of this EditingSupport
     */
    public abstract int getColumnIndex();

    /**
     * Enhance the visibility
     */
    @Override
    protected abstract boolean canEdit(Object element);
}
