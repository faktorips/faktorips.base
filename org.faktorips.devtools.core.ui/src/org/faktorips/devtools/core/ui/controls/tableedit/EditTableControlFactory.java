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

import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controls.EditTableControl;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;

/**
 * Factory that creates UI similar to the old {@link EditTableControl}.
 * 
 * @since 3.7
 * @author Stefan Widmaier
 */
public class EditTableControlFactory {

    /**
     * Creates a editable table with its buttons that displays the contents of the given
     * {@link AbstractListTableModel}. The table only contains a single column. A
     * {@link DatatypeEditingSupport} is used to create cell-editors that allow for changing single
     * values in the table.
     * 
     * @param uiToolkit the toolkit to create {@link IpsCellEditor cell editors} with
     * @param parent the composite the table and button controls should be created in
     * @param ipsProject the IPS project the IPS object is contained in
     * @param valueDatatype the data type of the values
     * @param tableModel the model that contains the list of values that can be edited in the table
     * @param description the description of the table. Is displayed directly above the table.
     * @param columnText the header text of the single column
     * @return the created {@link EditTableControlViewer}
     */
    public static EditTableControlViewer createListEditTable(UIToolkit uiToolkit,
            Composite parent,
            IIpsProject ipsProject,
            ValueDatatype valueDatatype,
            AbstractListTableModel<?> tableModel,
            String description,
            String columnText) {
        EditTableControlViewer viewer = new EditTableControlViewer(parent);
        viewer.setTableDescription(description);

        TableViewerColumn tableViewerColumn = new TableViewerColumn(viewer.getTableViewer(), SWT.RIGHT);
        tableViewerColumn.getColumn().setText(columnText);
        DatatypeEditingSupport datatypeEditingSupport = new DatatypeEditingSupport(uiToolkit, viewer.getTableViewer(),
                ipsProject, valueDatatype);
        datatypeEditingSupport.setTraversalStrategy(new EditTableTraversalStrategy(datatypeEditingSupport, 0,
                tableModel));
        tableViewerColumn.setEditingSupport(datatypeEditingSupport);

        viewer.setContentProvider(new ListTableModelContentProvider());
        viewer.setLabelProvider(new EditingSupportLabelProvider(datatypeEditingSupport));
        viewer.setTabelModel(tableModel);
        return viewer;
    }
}
