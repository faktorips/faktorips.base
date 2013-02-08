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

import org.eclipse.jface.viewers.TableViewer;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;

/**
 * A property binding to refresh {@link TableViewer table viewers} if the underlying model is
 * changed by cell editors in the viewer. The binding ensures that cell editing is not interrupted
 * by the refresh.
 * 
 * @author Bouillon
 */
public class RefreshTableViewerControlPropertyBinding extends ControlPropertyBinding {
    private final TableViewer tableViewer;

    public RefreshTableViewerControlPropertyBinding(TableViewer tableViewer, Object object, String propertyName,
            Class<?> expectedType) {
        super(tableViewer.getControl(), object, propertyName, expectedType);
        this.tableViewer = tableViewer;
    }

    /**
     * Refreshes the table viewer after the model has changed but only if no cell editor is
     * currently active. If a cell editor were active, the editing would be interrupted by the
     * refresh because the focus of the cell editor would be taken away.
     */
    @Override
    public void updateUiIfNotDisposed(String nameOfChangedProperty) {
        // if (!tableViewer.isCellEditorActive()) {
        tableViewer.refresh();
        // }
    }
}
