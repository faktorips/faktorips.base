/*******************************************************************************
 * Copyright (c) Faktor Zehn GmbH - faktorzehn.org
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.dialogs;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controls.tableedit.DatatypeCellLabelProvider;
import org.faktorips.devtools.core.ui.controls.tableedit.DatatypeEditingSupport;
import org.faktorips.devtools.core.ui.controls.tableedit.EditTableControlViewer;
import org.faktorips.devtools.core.ui.controls.tableedit.EditTableTraversalStrategy;
import org.faktorips.devtools.core.ui.controls.tableedit.ErrorCellLabelProvider;
import org.faktorips.devtools.core.ui.controls.tableedit.FormattedCellEditingSupport;
import org.faktorips.devtools.core.ui.controls.tableedit.ListTableModelContentProvider;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel.SingleValueViewItem;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.model.productcmpt.IValueHolder;
import org.faktorips.devtools.model.value.ValueType;

public class MultiValueDialog extends IpsPartEditDialog2 {

    private final IAttributeValue attributeValue;
    private MultiValueTableModel tableModel;
    private ValueDatatype datatype;

    public MultiValueDialog(Shell parentShell, IAttributeValue attributeValue, ValueDatatype datatype) {
        super(attributeValue, parentShell, Messages.MultiValueDialog_TitleText);
        this.datatype = datatype;
        setShellStyle(getShellStyle() | SWT.RESIZE);
        Assert.isNotNull(attributeValue);
        this.attributeValue = attributeValue;
        tableModel = new MultiValueTableModel(attributeValue);
    }

    @Override
    protected void setDataChangeableThis(boolean changeable) {
        /*
         * Do not set data changeable (or unchangeable respectively). This dialog can never be
         * opened in browse mode, the Multi-Value button next to the attribute value's field is
         * disabled in that case.
         */
    }

    @Override
    protected Composite createWorkAreaThis(Composite parent) {
        createMultiValueTable(parent);
        ((GridData)parent.getLayoutData()).heightHint = 300;
        return parent;
    }

    private TableViewerColumn setupTableColumns(EditTableControlViewer<SingleValueViewItem> viewer) {
        TableViewerColumn errorColumn = new TableViewerColumn(viewer.getTableViewer(), SWT.LEFT);
        errorColumn.getColumn().setResizable(false);
        errorColumn.setLabelProvider(new ErrorCellLabelProvider<>(tableModel));
        ColumnViewerToolTipSupport.enableFor(viewer.getTableViewer(), ToolTip.NO_RECREATE);

        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
        TableViewerColumn valueColumn = new TableViewerColumn(viewer.getTableViewer(),
                ctrlFactory.getDefaultAlignment());
        valueColumn.getColumn().setResizable(false);

        return valueColumn;
    }

    private void createMultiValueTable(Composite parent) {
        String description = NLS.bind(Messages.MultiValueDialog_TableDescription, attributeValue.getAttribute());
        EditTableControlViewer<SingleValueViewItem> viewer = new EditTableControlViewer<>(parent);
        viewer.setTableDescription(description);

        TableViewerColumn valueColumn = setupTableColumns(viewer);
        IValueHolder<?> valueHolder = attributeValue.getValueHolder();

        FormattedCellEditingSupport<SingleValueViewItem, ?> formattedCellEditingSupport;
        if (valueHolder.getValueType() == ValueType.INTERNATIONAL_STRING) {
            formattedCellEditingSupport = new LocalizedStringEditingSupportForSingleValueViewItems(getToolkit(),
                    viewer.getTableViewer(), new InternationalStringMultiValueElementModifier());
        } else {
            formattedCellEditingSupport = new DatatypeEditingSupport<>(getToolkit(),
                    viewer.getTableViewer(), attributeValue.getIpsProject(), datatype,
                    new StringMultiValueElementModifier());
        }
        formattedCellEditingSupport.setTraversalStrategy(new EditTableTraversalStrategy<>(
                formattedCellEditingSupport, 1, tableModel));
        valueColumn.setEditingSupport(formattedCellEditingSupport);
        valueColumn.setLabelProvider(new DatatypeCellLabelProvider(formattedCellEditingSupport));

        viewer.setContentProvider(new ListTableModelContentProvider());
        viewer.setTabelModel(tableModel);
        viewer.getTableViewer().getTable().setHeaderVisible(false);
    }
}
