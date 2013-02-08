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
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IValueHolder;
import org.faktorips.devtools.core.model.value.ValueType;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controls.tableedit.DatatypeCellLabelProvider;
import org.faktorips.devtools.core.ui.controls.tableedit.DatatypeEditingSupport;
import org.faktorips.devtools.core.ui.controls.tableedit.ErrorCellLabelProvider;
import org.faktorips.devtools.core.ui.controls.tableedit.FormattedCellEditingSupport;
import org.faktorips.devtools.core.ui.controls.tableedit.LocalizedStringEditingSupportForSingleValueViewItems;
import org.faktorips.devtools.core.ui.controls.tableedit.MultiValueTableControlViewer;
import org.faktorips.devtools.core.ui.controls.tableedit.MultiValueTableModelContentProvider;
import org.faktorips.devtools.core.ui.controls.tableedit.MultiValueTableTraversalStrategy;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel.SingleValueViewItem;
import org.faktorips.devtools.core.ui.editors.IpsPartEditDialog2;
import org.faktorips.devtools.core.ui.table.TableUtil;

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

    private TableViewerColumn setupTableColumns(MultiValueTableControlViewer viewer) {
        TableViewerColumn errorColumn = new TableViewerColumn(viewer.getTableViewer(), SWT.LEFT);
        errorColumn.getColumn().setResizable(false);
        errorColumn.setLabelProvider(new ErrorCellLabelProvider(tableModel));
        ColumnViewerToolTipSupport.enableFor(viewer.getTableViewer(), ToolTip.NO_RECREATE);

        ValueDatatypeControlFactory ctrlFactory = IpsUIPlugin.getDefault().getValueDatatypeControlFactory(datatype);
        TableViewerColumn valueColumn = new TableViewerColumn(viewer.getTableViewer(),
                ctrlFactory.getDefaultAlignment());
        valueColumn.getColumn().setResizable(false);

        return valueColumn;
    }

    private void createMultiValueTable(Composite parent) {
        String description = NLS.bind(Messages.MultiValueDialog_TableDescription, attributeValue.getAttribute());
        MultiValueTableControlViewer viewer = new MultiValueTableControlViewer(parent);
        viewer.setTableDescription(description);

        TableViewerColumn valueColumn = setupTableColumns(viewer);
        IValueHolder<?> valueHolder = attributeValue.getValueHolder();

        FormattedCellEditingSupport<?, ?> formattedCellEditingSupport;
        if (valueHolder.getValueType() == ValueType.INTERNATIONAL_STRING) {
            TableUtil.increaseHeightOfTableRows(viewer.getTableViewer().getTable(), 2, 12);
            formattedCellEditingSupport = new LocalizedStringEditingSupportForSingleValueViewItems(getToolkit(),
                    viewer.getTableViewer(), new InternationalStringMultiValueElementModifier());
        } else {
            formattedCellEditingSupport = new DatatypeEditingSupport<SingleValueViewItem>(getToolkit(),
                    viewer.getTableViewer(), attributeValue.getIpsProject(), datatype,
                    new StringMultiValueElementModifier());
        }
        formattedCellEditingSupport.setTraversalStrategy(new MultiValueTableTraversalStrategy(
                formattedCellEditingSupport, 1, tableModel));
        valueColumn.setEditingSupport(formattedCellEditingSupport);
        valueColumn.setLabelProvider(new DatatypeCellLabelProvider(formattedCellEditingSupport));

        viewer.setContentProvider(new MultiValueTableModelContentProvider());
        viewer.setTabelModel(tableModel);
        viewer.getTableViewer().getTable().setHeaderVisible(false);
    }
}
