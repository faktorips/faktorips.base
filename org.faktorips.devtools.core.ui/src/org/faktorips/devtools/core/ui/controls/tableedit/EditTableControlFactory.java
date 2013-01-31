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

import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.value.ValueType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controls.EditTableControl;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableUtil;

/**
 * Factory that creates UI similar to the old {@link EditTableControl}.
 * 
 * @since 3.7
 * @author Stefan Widmaier
 */
public class EditTableControlFactory {
    /**
     * Creates an editable table with its buttons that displays the contents of the given
     * {@link AbstractListTableModel}. The table only contains a single column. A
     * {@link DatatypeEditingSupport} is used to create cell-editors that allow for changing single
     * values in the table.
     * 
     * @param uiToolkit the toolkit to create {@link IpsCellEditor cell editors} with
     * @param parent the composite the table and button controls should be created in
     * @param attributeValue the attribute for which the table is created
     * @param valueDatatype the data type of the values
     * @param tableModel the model that contains the list of elements that can be edited using the
     *            table
     * @param elementModifier object that allows to access and modify the table model's elements
     * @param description the description of the table. Is displayed directly above the table.
     * @return the created {@link EditTableControlViewer}
     */
    public static EditTableControlViewer createListEditTable(UIToolkit uiToolkit,
            Composite parent,
            IAttributeValue attributeValue,
            ValueDatatype valueDatatype,
            MultiValueTableModel tableModel,
            IElementModifier elementModifier,
            String description) {
        EditTableControlViewer viewer = new EditTableControlViewer(parent);
        viewer.setTableDescription(description);

        TableViewerColumn errorColumn = new TableViewerColumn(viewer.getTableViewer(), SWT.LEFT);
        errorColumn.getColumn().setResizable(false);
        errorColumn.setLabelProvider(new ErrorCellLabelProvider(tableModel));
        ColumnViewerToolTipSupport.enableFor(viewer.getTableViewer(), ToolTip.NO_RECREATE);

        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault()
                .getValueDatatypeControlFactory(valueDatatype);
        TableViewerColumn valueColumn = new TableViewerColumn(viewer.getTableViewer(),
                ctrlFactory.getDefaultAlignment());
        valueColumn.getColumn().setResizable(false);

        IValueHolder<?> valueHolder = attributeValue.getValueHolder();

        DatatypeEditingSupport datatypeEditingSupport;
        if (valueHolder.getValueType() == ValueType.INTERNATIONAL_STRING) {
            TableUtil.increaseHeightOfTableRows(viewer.getTableViewer().getTable(), 2, 12);
            datatypeEditingSupport = new MultilingualDatatypeEditingSupport(uiToolkit, viewer.getTableViewer(),
                    attributeValue.getIpsProject(), valueDatatype, elementModifier, attributeValue);
        } else {
            datatypeEditingSupport = new DatatypeEditingSupport(uiToolkit, viewer.getTableViewer(),
                    attributeValue.getIpsProject(), valueDatatype, elementModifier);
        }
        datatypeEditingSupport.setTraversalStrategy(new EditTableTraversalStrategy(datatypeEditingSupport, 1,
                tableModel));
        valueColumn.setEditingSupport(datatypeEditingSupport);
        valueColumn.setLabelProvider(new DatatypeCellLabelProvider(datatypeEditingSupport));

        viewer.setContentProvider(new ListTableModelContentProvider());
        viewer.setTabelModel(tableModel);
        viewer.getTableViewer().getTable().setHeaderVisible(false);

        return viewer;
    }
}
